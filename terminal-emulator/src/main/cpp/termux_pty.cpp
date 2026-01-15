#include <jni.h>
#include <string>
#include <cstdlib>
#include <cstring>
#include <unistd.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/wait.h>
#include <termios.h>
#include <android/log.h>

#define LOG_TAG "TermuxPTY"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Helper function to open PTY
extern int create_pty(int* master_fd, int* slave_fd, char* slave_name, size_t slave_name_size);

// Helper function to set PTY window size
extern int set_pty_window_size(int fd, int rows, int cols);

extern "C" JNIEXPORT jint JNICALL
Java_com_termux_terminal_JNI_createSubprocess(
    JNIEnv* env,
    jclass /* clazz */,
    jstring cmd,
    jstring cwd,
    jobjectArray args,
    jobjectArray envVars,
    jintArray processIdOut)
{
    // Get command string
    const char* cmdStr = env->GetStringUTFChars(cmd, nullptr);
    if (!cmdStr) {
        LOGE("Failed to get command string");
        return -1;
    }

    // Get working directory
    const char* cwdStr = nullptr;
    if (cwd != nullptr) {
        cwdStr = env->GetStringUTFChars(cwd, nullptr);
    }

    // Create PTY
    int master_fd = -1;
    int slave_fd = -1;
    char slave_name[64];

    if (create_pty(&master_fd, &slave_fd, slave_name, sizeof(slave_name)) != 0) {
        LOGE("Failed to create PTY");
        env->ReleaseStringUTFChars(cmd, cmdStr);
        if (cwdStr) env->ReleaseStringUTFChars(cwd, cwdStr);
        return -1;
    }

    LOGD("Created PTY: master_fd=%d, slave=%s", master_fd, slave_name);

    // Fork process
    pid_t pid = fork();

    if (pid < 0) {
        // Fork failed
        LOGE("Fork failed");
        close(master_fd);
        close(slave_fd);
        env->ReleaseStringUTFChars(cmd, cmdStr);
        if (cwdStr) env->ReleaseStringUTFChars(cwd, cwdStr);
        return -1;
    }

    if (pid == 0) {
        // Child process
        close(master_fd);

        // Create new session and set controlling terminal
        setsid();
        if (ioctl(slave_fd, TIOCSCTTY, 0) != 0) {
            LOGE("Failed to set controlling terminal");
            exit(1);
        }

        // Redirect stdin/stdout/stderr
        dup2(slave_fd, STDIN_FILENO);
        dup2(slave_fd, STDOUT_FILENO);
        dup2(slave_fd, STDERR_FILENO);

        if (slave_fd > STDERR_FILENO) {
            close(slave_fd);
        }

        // Change directory if specified
        if (cwdStr && chdir(cwdStr) != 0) {
            LOGE("Failed to change directory to %s", cwdStr);
        }

        // Set environment variables
        if (envVars != nullptr) {
            jsize envCount = env->GetArrayLength(envVars);
            for (jsize i = 0; i < envCount; i++) {
                auto jEnvVar = (jstring)env->GetObjectArrayElement(envVars, i);
                if (jEnvVar) {
                    const char* envVar = env->GetStringUTFChars(jEnvVar, nullptr);
                    if (envVar) {
                        // Split on '=' and use putenv
                        char* envCopy = strdup(envVar);
                        putenv(envCopy);
                        env->ReleaseStringUTFChars(jEnvVar, envVar);
                    }
                    env->DeleteLocalRef(jEnvVar);
                }
            }
        }

        // Prepare argv array
        jsize argc = args ? env->GetArrayLength(args) : 0;
        char** argv = new char*[argc + 2];
        argv[0] = const_cast<char*>(cmdStr);

        for (jsize i = 0; i < argc; i++) {
            auto jArg = (jstring)env->GetObjectArrayElement(args, i);
            if (jArg) {
                const char* arg = env->GetStringUTFChars(jArg, nullptr);
                argv[i + 1] = const_cast<char*>(arg);
                env->DeleteLocalRef(jArg);
            } else {
                argv[i + 1] = nullptr;
            }
        }
        argv[argc + 1] = nullptr;

        // Execute command
        execvp(argv[0], argv);

        // If we get here, exec failed
        LOGE("execvp failed for command: %s", cmdStr);
        exit(127);
    }

    // Parent process
    close(slave_fd);

    LOGD("Forked process: pid=%d", pid);

    // Return PID through output parameter
    if (processIdOut) {
        jint pidValue = static_cast<jint>(pid);
        env->SetIntArrayRegion(processIdOut, 0, 1, &pidValue);
    }

    // Cleanup
    env->ReleaseStringUTFChars(cmd, cmdStr);
    if (cwdStr) {
        env->ReleaseStringUTFChars(cwd, cwdStr);
    }

    // Make master FD non-blocking
    int flags = fcntl(master_fd, F_GETFL, 0);
    if (flags != -1) {
        fcntl(master_fd, F_SETFL, flags | O_NONBLOCK);
    }

    return master_fd;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_termux_terminal_JNI_setPtyWindowSize(
    JNIEnv* /* env */,
    jclass /* clazz */,
    jint fd,
    jint rows,
    jint cols)
{
    return set_pty_window_size(fd, rows, cols);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_termux_terminal_JNI_waitFor(
    JNIEnv* /* env */,
    jclass /* clazz */,
    jint pid)
{
    int status = 0;
    pid_t result = waitpid(static_cast<pid_t>(pid), &status, 0);

    if (result == -1) {
        LOGE("waitpid failed for pid %d", pid);
        return -1;
    }

    if (WIFEXITED(status)) {
        int exitCode = WEXITSTATUS(status);
        LOGD("Process %d exited with code %d", pid, exitCode);
        return exitCode;
    }

    if (WIFSIGNALED(status)) {
        int signal = WTERMSIG(status);
        LOGD("Process %d killed by signal %d", pid, signal);
        return -signal;
    }

    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_com_termux_terminal_JNI_close(
    JNIEnv* /* env */,
    jclass /* clazz */,
    jint fd)
{
    if (fd >= 0) {
        close(fd);
        LOGD("Closed fd %d", fd);
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_termux_terminal_JNI_readFromPty(
    JNIEnv* env,
    jclass /* clazz */,
    jint fd,
    jbyteArray buffer,
    jint length)
{
    if (fd < 0 || !buffer) {
        return -1;
    }

    jbyte* bufPtr = env->GetByteArrayElements(buffer, nullptr);
    if (!bufPtr) {
        return -1;
    }

    ssize_t bytesRead = read(fd, bufPtr, static_cast<size_t>(length));

    env->ReleaseByteArrayElements(buffer, bufPtr, 0);

    return static_cast<jint>(bytesRead);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_termux_terminal_JNI_writeToPty(
    JNIEnv* env,
    jclass /* clazz */,
    jint fd,
    jbyteArray buffer,
    jint length)
{
    if (fd < 0 || !buffer) {
        return -1;
    }

    jbyte* bufPtr = env->GetByteArrayElements(buffer, nullptr);
    if (!bufPtr) {
        return -1;
    }

    ssize_t bytesWritten = write(fd, bufPtr, static_cast<size_t>(length));

    env->ReleaseByteArrayElements(buffer, bufPtr, JNI_ABORT);

    return static_cast<jint>(bytesWritten);
}

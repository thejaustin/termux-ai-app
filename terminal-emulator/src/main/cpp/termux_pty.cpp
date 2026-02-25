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

// Helper function to set PTY window size (rows, cols, cellWidth, cellHeight)
extern int set_pty_window_size(int fd, int rows, int cols, int cellWidth, int cellHeight);

extern "C" JNIEXPORT jint JNICALL
Java_com_termux_terminal_JNI_createSubprocess(
    JNIEnv* env,
    jclass /* clazz */,
    jstring cmd,
    jstring cwd,
    jobjectArray args,
    jobjectArray envVars,
    jintArray processId,
    jint rows,
    jint columns,
    jint cellWidth,
    jint cellHeight)
{
    // 1. Extract all data in parent BEFORE fork

    // Get command string
    const char* cmdStr = env->GetStringUTFChars(cmd, nullptr);
    if (!cmdStr) {
        LOGE("Failed to get command string");
        return -1;
    }
    char* cmdCopy = strdup(cmdStr);
    env->ReleaseStringUTFChars(cmd, cmdStr);

    // Get working directory
    char* cwdCopy = nullptr;
    if (cwd != nullptr) {
        const char* cwdStr = env->GetStringUTFChars(cwd, nullptr);
        if (cwdStr) {
            cwdCopy = strdup(cwdStr);
            env->ReleaseStringUTFChars(cwd, cwdStr);
        }
    }

    // Get arguments
    jsize argc = args ? env->GetArrayLength(args) : 0;
    char** argv = new char*[argc + 2];
    argv[0] = cmdCopy;
    for (jsize i = 0; i < argc; i++) {
        auto jArg = (jstring)env->GetObjectArrayElement(args, i);
        if (jArg) {
            const char* arg = env->GetStringUTFChars(jArg, nullptr);
            argv[i + 1] = strdup(arg);
            env->ReleaseStringUTFChars(jArg, arg);
            env->DeleteLocalRef(jArg);
        } else {
            argv[i + 1] = nullptr;
        }
    }
    argv[argc + 1] = nullptr;

    // Get environment variables
    jsize envCount = envVars ? env->GetArrayLength(envVars) : 0;
    char** envp = new char*[envCount + 1];
    for (jsize i = 0; i < envCount; i++) {
        auto jEnvVar = (jstring)env->GetObjectArrayElement(envVars, i);
        if (jEnvVar) {
            const char* envVar = env->GetStringUTFChars(jEnvVar, nullptr);
            envp[i] = strdup(envVar);
            env->ReleaseStringUTFChars(jEnvVar, envVar);
            env->DeleteLocalRef(jEnvVar);
        } else {
            envp[i] = nullptr;
        }
    }
    envp[envCount] = nullptr;

    // Create PTY
    int master_fd = -1;
    int slave_fd = -1;
    char slave_name[64];

    if (create_pty(&master_fd, &slave_fd, slave_name, sizeof(slave_name)) != 0) {
        LOGE("Failed to create PTY");
        free(cmdCopy);
        free(cwdCopy);
        for(int i=1; i<=argc; i++) free(argv[i]);
        delete[] argv;
        for(int i=0; i<envCount; i++) free(envp[i]);
        delete[] envp;
        return -1;
    }

    // Set initial window size before forking
    set_pty_window_size(master_fd, rows, columns, cellWidth, cellHeight);

    LOGD("Created PTY: master_fd=%d, slave=%s", master_fd, slave_name);

    // Fork process
    pid_t pid = fork();

    if (pid < 0) {
        // Fork failed
        LOGE("Fork failed");
        close(master_fd);
        close(slave_fd);
        free(cmdCopy);
        free(cwdCopy);
        for(int i=1; i<=argc; i++) free(argv[i]);
        delete[] argv;
        for(int i=0; i<envCount; i++) free(envp[i]);
        delete[] envp;
        return -1;
    }

    if (pid == 0) {
        // Child process - NO JNI CALLS ALLOWED HERE
        close(master_fd);

        // Create new session and set controlling terminal
        setsid();
        if (ioctl(slave_fd, TIOCSCTTY, 0) != 0) {
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
        if (cwdCopy && chdir(cwdCopy) != 0) {
            // Failed to change directory - continue anyway
        }

        // Set environment variables
        if (envp != nullptr) {
            for (int i = 0; envp[i] != nullptr; i++) {
                putenv(envp[i]);
            }
        }

        // Execute command
        execvp(argv[0], argv);

        // If we get here, exec failed
        exit(127);
    }

    // Parent process
    close(slave_fd);

    LOGD("Forked process: pid=%d", pid);

    // Return PID through output parameter
    if (processId) {
        jint pidValue = static_cast<jint>(pid);
        env->SetIntArrayRegion(processId, 0, 1, &pidValue);
    }

    // Cleanup parent's copies
    free(cmdCopy);
    free(cwdCopy);
    for(int i=1; i<=argc; i++) free(argv[i]);
    delete[] argv;
    for(int i=0; i<envCount; i++) free(envp[i]);
    delete[] envp;

    return master_fd;
}

extern "C" JNIEXPORT void JNICALL
Java_com_termux_terminal_JNI_setPtyWindowSize(
    JNIEnv* /* env */,
    jclass /* clazz */,
    jint fd,
    jint rows,
    jint cols,
    jint cellWidth,
    jint cellHeight)
{
    set_pty_window_size(fd, rows, cols, cellWidth, cellHeight);
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

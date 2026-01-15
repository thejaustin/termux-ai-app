package com.termux.terminal;

/**
 * JNI interface for native PTY operations.
 *
 * This class provides Java bindings to native C++ code for creating and managing
 * pseudo-terminals (PTY) on Android.
 */
public class JNI {
    private static final String TAG = "TermuxJNI";

    static {
        try {
            System.loadLibrary("termux-terminal");
        } catch (UnsatisfiedLinkError e) {
            android.util.Log.e(TAG, "Failed to load native library", e);
            throw e;
        }
    }

    /**
     * Create a subprocess with a pseudo-terminal.
     *
     * @param cmd Executable path or command
     * @param cwd Working directory (can be null for current directory)
     * @param args Command arguments (can be null)
     * @param envVars Environment variables in "KEY=VALUE" format (can be null)
     * @param processIdOut Output array to receive the process ID (length must be >= 1)
     * @return File descriptor for the PTY master, or -1 on failure
     */
    public static native int createSubprocess(
        String cmd,
        String cwd,
        String[] args,
        String[] envVars,
        int[] processIdOut
    );

    /**
     * Set the window size of a PTY.
     *
     * @param fd PTY master file descriptor
     * @param rows Number of rows
     * @param cols Number of columns
     * @return 0 on success, -1 on failure
     */
    public static native int setPtyWindowSize(int fd, int rows, int cols);

    /**
     * Wait for a process to exit and get its exit code.
     *
     * This is a blocking call.
     *
     * @param pid Process ID to wait for
     * @return Exit code if process exited normally, -signal if killed by signal, -1 on error
     */
    public static native int waitFor(int pid);

    /**
     * Close a file descriptor.
     *
     * @param fd File descriptor to close
     */
    public static native void close(int fd);

    /**
     * Read data from PTY.
     *
     * @param fd PTY master file descriptor
     * @param buffer Buffer to read into
     * @param length Maximum number of bytes to read
     * @return Number of bytes read, or -1 on error
     */
    public static native int readFromPty(int fd, byte[] buffer, int length);

    /**
     * Write data to PTY.
     *
     * @param fd PTY master file descriptor
     * @param buffer Buffer containing data to write
     * @param length Number of bytes to write
     * @return Number of bytes written, or -1 on error
     */
    public static native int writeToPty(int fd, byte[] buffer, int length);
}

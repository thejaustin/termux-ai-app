package com.termux.terminal;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * Real terminal session with PTY support.
 *
 * Manages a shell process with a pseudo-terminal, reads output,
 * processes it through the terminal emulator, and handles input.
 */
public class TerminalSession {
    private static final String TAG = "TerminalSession";
    private static final int MAX_READ_BYTES = 8192;

    private final String mExecutable;
    private final String mWorkingDirectory;
    private final String[] mArgs;
    private final String[] mEnvironment;
    private final TerminalSessionClient mClient;
    private final TerminalEmulator mEmulator;

    private int mPtyFd = -1;
    private int mProcessId = -1;
    private FileOutputStream mPtyOutputStream;
    private FileInputStream mPtyInputStream;
    private Thread mReaderThread;
    private boolean mIsRunning = false;

    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    /**
     * Create a new terminal session.
     *
     * @param executable Path to executable (e.g., "/system/bin/sh")
     * @param workingDir Working directory
     * @param args Command arguments
     * @param env Environment variables
     * @param client Callback interface for session events
     */
    public TerminalSession(String executable, String workingDir, String[] args, String[] env, TerminalSessionClient client) {
        this.mExecutable = executable;
        this.mWorkingDirectory = workingDir;
        this.mArgs = args != null ? args.clone() : new String[0];
        this.mEnvironment = env != null ? env.clone() : new String[0];
        this.mClient = client;
        this.mEmulator = new TerminalEmulator(80, 24, this);

        initializeSession();
    }

    private void initializeSession() {
        try {
            // Create subprocess with PTY
            int[] pid = new int[1];
            mPtyFd = JNI.createSubprocess(
                mExecutable,
                mWorkingDirectory,
                mArgs,
                mEnvironment,
                pid
            );

            if (mPtyFd < 0) {
                throw new IOException("Failed to create subprocess");
            }

            mProcessId = pid[0];
            mIsRunning = true;

            // Set initial window size
            JNI.setPtyWindowSize(mPtyFd, 24, 80);

            // Create streams
            FileDescriptor fd = createFileDescriptor(mPtyFd);
            mPtyOutputStream = new FileOutputStream(fd);
            mPtyInputStream = new FileInputStream(fd);

            // Start reader thread
            startReaderThread();

            Log.i(TAG, "Session started: PID=" + mProcessId + ", FD=" + mPtyFd);

        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize session", e);
            cleanup();
        }
    }

    private FileDescriptor createFileDescriptor(int fd) {
        try {
            FileDescriptor fileDescriptor = new FileDescriptor();
            Field descriptorField = FileDescriptor.class.getDeclaredField("descriptor");
            descriptorField.setAccessible(true);
            descriptorField.setInt(fileDescriptor, fd);
            return fileDescriptor;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create FileDescriptor", e);
        }
    }

    private void startReaderThread() {
        mReaderThread = new Thread(() -> {
            byte[] buffer = new byte[MAX_READ_BYTES];

            try {
                while (mIsRunning) {
                    int bytesRead = JNI.readFromPty(mPtyFd, buffer, MAX_READ_BYTES);

                    if (bytesRead < 0) {
                        // EOF or error - process terminated
                        break;
                    }

                    if (bytesRead > 0) {
                        // Process output through emulator
                        mEmulator.append(buffer, bytesRead);

                        // Notify client on main thread
                        mMainThreadHandler.post(() -> {
                            if (mClient != null) {
                                mClient.onTextChanged(this);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Reader thread error", e);
            } finally {
                handleProcessExit();
            }
        }, "TerminalSession-Reader-" + mProcessId);

        mReaderThread.start();
    }

    private void handleProcessExit() {
        mIsRunning = false;

        // Wait for process to cleanup
        if (mProcessId > 0) {
            int exitCode = JNI.waitFor(mProcessId);
            Log.i(TAG, "Process exited: PID=" + mProcessId + ", exitCode=" + exitCode);
        }

        cleanup();

        // Notify client on main thread
        mMainThreadHandler.post(() -> {
            if (mClient != null) {
                mClient.onSessionFinished(this);
            }
        });
    }

    /**
     * Write text to the terminal (user input).
     *
     * @param text Text to write
     */
    public void write(String text) {
        if (!mIsRunning || mPtyOutputStream == null || text == null) {
            return;
        }

        try {
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            JNI.writeToPty(mPtyFd, bytes, bytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Failed to write to PTY", e);
        }
    }

    /**
     * Update terminal window size.
     *
     * @param columns Number of columns
     * @param rows Number of rows
     */
    public void updateSize(int columns, int rows) {
        if (mPtyFd >= 0) {
            JNI.setPtyWindowSize(mPtyFd, rows, columns);
            mEmulator.resize(columns, rows);
        }
    }

    /**
     * Finish the session if running.
     */
    public void finishIfRunning() {
        if (mIsRunning) {
            mIsRunning = false;

            try {
                // Send SIGHUP to process
                if (mProcessId > 0) {
                    android.system.Os.kill(mProcessId, android.system.OsConstants.SIGHUP);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to kill process", e);
            }

            cleanup();
        }
    }

    private void cleanup() {
        try {
            if (mPtyOutputStream != null) {
                mPtyOutputStream.close();
                mPtyOutputStream = null;
            }
        } catch (IOException ignored) {}

        try {
            if (mPtyInputStream != null) {
                mPtyInputStream.close();
                mPtyInputStream = null;
            }
        } catch (IOException ignored) {}

        if (mPtyFd >= 0) {
            JNI.close(mPtyFd);
            mPtyFd = -1;
        }

        if (mReaderThread != null && mReaderThread.isAlive()) {
            mReaderThread.interrupt();
            mReaderThread = null;
        }
    }

    // Getters
    public TerminalEmulator getEmulator() {
        return mEmulator;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public String getExecutable() {
        return mExecutable;
    }

    public String getWorkingDirectory() {
        return mWorkingDirectory;
    }

    public String[] getArgs() {
        return mArgs.clone();
    }

    public String[] getEnvironment() {
        return mEnvironment.clone();
    }
}

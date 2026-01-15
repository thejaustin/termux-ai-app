#include <cstdlib>
#include <cstring>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <termios.h>
#include <android/log.h>

#define LOG_TAG "PTYHelper"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/**
 * Create a pseudo-terminal.
 *
 * @param master_fd Output parameter for master file descriptor
 * @param slave_fd Output parameter for slave file descriptor
 * @param slave_name Buffer to store slave device name
 * @param slave_name_size Size of slave_name buffer
 * @return 0 on success, -1 on failure
 */
int create_pty(int* master_fd, int* slave_fd, char* slave_name, size_t slave_name_size) {
    // Open PTY master
    *master_fd = open("/dev/ptmx", O_RDWR | O_NOCTTY);
    if (*master_fd < 0) {
        LOGE("Failed to open /dev/ptmx");
        return -1;
    }

    // Grant access to slave PTY
    if (grantpt(*master_fd) != 0) {
        LOGE("grantpt failed");
        close(*master_fd);
        return -1;
    }

    // Unlock slave PTY
    if (unlockpt(*master_fd) != 0) {
        LOGE("unlockpt failed");
        close(*master_fd);
        return -1;
    }

    // Get slave PTY name
    char* slave_path = ptsname(*master_fd);
    if (!slave_path) {
        LOGE("ptsname failed");
        close(*master_fd);
        return -1;
    }

    // Copy slave name to output buffer
    if (slave_name && slave_name_size > 0) {
        strncpy(slave_name, slave_path, slave_name_size - 1);
        slave_name[slave_name_size - 1] = '\0';
    }

    // Open slave PTY
    *slave_fd = open(slave_path, O_RDWR | O_NOCTTY);
    if (*slave_fd < 0) {
        LOGE("Failed to open slave PTY: %s", slave_path);
        close(*master_fd);
        return -1;
    }

    // Set up terminal attributes
    struct termios tios;
    if (tcgetattr(*slave_fd, &tios) == 0) {
        // Configure for raw mode
        tios.c_iflag &= ~(IGNBRK | BRKINT | PARMRK | ISTRIP | INLCR | IGNCR | ICRNL | IXON);
        tios.c_oflag &= ~OPOST;
        tios.c_lflag &= ~(ECHO | ECHONL | ICANON | ISIG | IEXTEN);
        tios.c_cflag &= ~(CSIZE | PARENB);
        tios.c_cflag |= CS8;

        // Enable UTF-8
        tios.c_iflag |= IUTF8;

        // Set to raw mode
        cfmakeraw(&tios);

        // Apply settings
        tcsetattr(*slave_fd, TCSANOW, &tios);
    } else {
        LOGE("tcgetattr failed");
    }

    LOGD("Created PTY: master=%d, slave=%d, path=%s", *master_fd, *slave_fd, slave_path);
    return 0;
}

/**
 * Set PTY window size.
 *
 * @param fd PTY file descriptor
 * @param rows Number of rows
 * @param cols Number of columns
 * @return 0 on success, -1 on failure
 */
int set_pty_window_size(int fd, int rows, int cols) {
    struct winsize ws;
    memset(&ws, 0, sizeof(ws));

    ws.ws_row = static_cast<unsigned short>(rows);
    ws.ws_col = static_cast<unsigned short>(cols);
    ws.ws_xpixel = 0;
    ws.ws_ypixel = 0;

    if (ioctl(fd, TIOCSWINSZ, &ws) != 0) {
        LOGE("ioctl(TIOCSWINSZ) failed for fd=%d, rows=%d, cols=%d", fd, rows, cols);
        return -1;
    }

    LOGD("Set PTY window size: fd=%d, rows=%d, cols=%d", fd, rows, cols);
    return 0;
}

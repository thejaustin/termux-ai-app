package com.termux.ai;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ClaudeCodeService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
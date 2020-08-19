package com.project.busbogi.ble.service;

import android.bluetooth.le.ScanCallback;

public interface ScanServiceCallback {
    boolean registCallback(ScanCallback callback);
    boolean unregistCallback(ScanCallback callback);
}

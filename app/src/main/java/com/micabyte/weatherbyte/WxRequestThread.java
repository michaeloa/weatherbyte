package com.micabyte.weatherbyte;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;

class WxRequestThread extends Thread {
    private static final String TAG = WxRequestThread.class.getName();
    private final Context context;
    private final ArrayList<Integer> widgets = new ArrayList<>();
    private PowerManager.WakeLock wakeLock;

    public WxRequestThread(Context context) {
        super();
        this.context = context;
    }

    public void add(int widget) {
        widgets.add(widget);
    }

    @Override
    public void start() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WeatherByte request wake lock");
        wakeLock.acquire();
        super.start();
    }

    @Override
    public void run() {
        Log.d(TAG, "WxRequestThread running");
        try {
            for (Integer id : widgets) {
                WxInfo info = Storage.restoreWeatherInfo(context, id);
                if (info == null)
                    continue;
                if (WxRequest.getWeather(info))
                    Storage.storeWeatherInfo(context, info, id);
            }
            widgets.clear();
        } finally {
            wakeLock.release();
        }
        Log.d(TAG, "WxRequestThread ending");
    }
}

package com.micabyte.weatherbyte;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WxConfigure extends Activity implements View.OnClickListener {
    private int widgetId= AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
        if(widgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            finish();
            return;
        }
        setContentView(R.layout.activity_configure);
        View button = findViewById(R.id.StationSave);
        if(button != null)
            button.setOnClickListener(this);
    }

    public void onClick(View view)
    {
        WxInfo weather = new WxInfo();
        Storage.storeWeatherInfo(this, weather, widgetId);
        WxRequestThread requestThread = new WxRequestThread(this);
        requestThread.add(widgetId);
        requestThread.start();
        /*
        if(WxRequest.getWeather(weather))
        {
            Storage.storeWeatherInfo(this, weather, widgetId);
            final RemoteViews views = WxWidgetProvider.getWeatherView(this, widgetId, weather);
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            appWidgetManager.updateAppWidget(widgetId,views);
        // Result */
        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK,result);
        //}
        finish();
    }

}

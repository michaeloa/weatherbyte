package com.micabyte.weatherbyte;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;

public class WxWidgetProvider extends AppWidgetProvider {
    private static final String TAG = WxWidgetProvider.class.getName();
    private static final String WIDGET_UPDATE_ACTION = "com.micabyte.weatherbyte.WIDGET_UPDATE_ACTION";
    private static final int    INTERVAL = 1000;
    private static final int    REQUEST_INTERVAL = 60*60*1000;

    public static int getMonthName(int month)
    {
        switch(month)
        {
            case Calendar.JANUARY:
                return R.string.january;
            case Calendar.FEBRUARY:
                return R.string.january;
            case Calendar.MARCH:
                return R.string.march;
            case Calendar.APRIL:
                return R.string.april;
            case Calendar.MAY:
                return R.string.may;
            case Calendar.JUNE:
                return R.string.june;

            case Calendar.DECEMBER:
                return R.string.december;
        }
        return 0;
    }

    public static int getDayOfWeek(int dayOfWeek)
    {
        switch(dayOfWeek)
        {
            case Calendar.MONDAY:
                return R.string.monday;
            case Calendar.TUESDAY:
                return R.string.tuesday;
            case Calendar.WEDNESDAY:
                return R.string.wednesday;
            case Calendar.THURSDAY:
                return R.string.thursday;
            case Calendar.FRIDAY:
                return R.string.friday;
            case Calendar.SATURDAY:
                return R.string.saturday;
            case Calendar.SUNDAY:
                return R.string.sunday;
        }
        return 0;
    }

    public static int getWeatherIcon(int icon)
    {
        final Calendar calendar = Calendar.getInstance();
        final int hours = calendar.get(Calendar.HOUR_OF_DAY);
        if(hours>6 && hours<19)
            return icon;
        switch(icon)
        {
            case R.drawable.ic_chance_of_rain:
                return R.drawable.ic_chance_of_rain_night;
            case R.drawable.ic_chance_of_snow:
                return R.drawable.ic_chance_of_snow_night;
            case R.drawable.ic_chance_of_storm:
                return R.drawable.ic_chance_of_storm_night;
            case R.drawable.ic_cloudy:
                return R.drawable.ic_cloudy;
            case R.drawable.ic_mostly_cloudy:
                return R.drawable.ic_mostly_cloudy_night;
            case R.drawable.ic_mostly_sunny:
                return R.drawable.ic_mostly_sunny_night;
            case R.drawable.ic_sunny:
                return R.drawable.ic_sunny_night;
        }
        return icon;
    }

    public static RemoteViews getWeatherView(Context context, int widgetId, WxInfo info)
    {
        if (info==null) {
            Log.d(TAG, "Null info");
            return null;
        }
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wxwidget);
        final StringBuilder text = new StringBuilder();
        if (info.icon != 0)
            views.setImageViewResource(R.id.weather_icon,getWeatherIcon(info.icon));
        text.append(info.temp).append("°");
        views.setTextViewText(R.id.location, info.location);
        views.setTextViewText(R.id.temp,text.toString());
        Calendar calendar = Calendar.getInstance();
        text.setLength(0);
        int time = calendar.get(Calendar.HOUR_OF_DAY);
        if(time<10)
            text.append('0');
        text.append(time).append(':');
        time = calendar.get(Calendar.MINUTE);
        if(time<10)
            text.append('0');
        text.append(time);
        views.setTextViewText(R.id.time, text.toString());
        views.setTextViewText(R.id.day_of_week, context.getString(getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK))));
        text.setLength(0);
        text.append(calendar.get(Calendar.DAY_OF_MONTH)).append(" ").append(context.getString(getMonthName(calendar.get(Calendar.MONTH))))
                .append(" ").append(calendar.get(Calendar.YEAR));
        views.setTextViewText(R.id.date,text.toString());
        final PackageManager pm = context.getPackageManager();
        final Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        String clockImpls[][] = {
                {"com.htc.android.worldclock",   "com.htc.android.worldclock.WorldClockTabControl" },
                {"com.android.deskclock",        "com.android.deskclock.AlarmClock"},
                {"com.google.android.deskclock", "com.android.deskclock.DeskClock"},
                {"com.motorola.blur.alarmclock", "com.motorola.blur.alarmclock.AlarmClock"}
        };
        boolean foundClockImpl = false;
        for(String[] clockImpl : clockImpls)
        {
            final String packageName = clockImpl[0];
            final String className = clockImpl[1];
            try
            {
                ComponentName cn = new ComponentName(packageName, className);
                pm.getActivityInfo(cn, PackageManager.GET_META_DATA);
                alarmClockIntent.setComponent(cn);
                foundClockImpl = true;
            }
            catch(PackageManager.NameNotFoundException e)
            {
                // nope
            }
        }
        if(foundClockImpl)
            views.setOnClickPendingIntent(R.id.time_container, PendingIntent.getActivity(context, 0, alarmClockIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        return views;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final String action = intent.getAction();
        if(WIDGET_UPDATE_ACTION.equals(action))
        {
            final AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int ids[] = manager.getAppWidgetIds(new ComponentName(context.getPackageName(), WxWidgetProvider.class.getName()));
            if(ids.length==0)
                return;
            updateWidget(context,manager,ids);
            setAlarm(context);
            return;
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
        for(int index=0, count=appWidgetIds.length; index<count; ++index)
            Storage.deleteWeatherInfo(context,appWidgetIds[index]);
    }

    @Override
    public void onDisabled(Context context)
    {
        super.onDisabled(context);
        final AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        final PendingIntent pendingIntent = getPendingIntent(context);
        am.cancel(pendingIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        setAlarm(context);
        updateWidget(context,appWidgetManager,appWidgetIds);
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        WxRequestThread requestThread = null;
        for(int index=0, count = appWidgetIds.length; index<count; index++)
        {
            final WxInfo info = Storage.restoreWeatherInfo(context, appWidgetIds[index]);
            if(info==null)
                continue;
            if(System.currentTimeMillis() - info.date>REQUEST_INTERVAL)
            {
                if(requestThread==null)
                    requestThread = new WxRequestThread(context);
                requestThread.add(appWidgetIds[index]);
            }
            final RemoteViews views = getWeatherView(context, appWidgetIds[index],info);
            if(views!=null)
                appWidgetManager.updateAppWidget(appWidgetIds[index],views);
        }
        if(requestThread!=null)
            requestThread.start();
    }

    private void setAlarm(Context context)
    {
        final AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        final PendingIntent pendingIntent = getPendingIntent(context);
        am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + INTERVAL,pendingIntent);
    }

    private PendingIntent getPendingIntent(Context context)
    {
        final Intent intent = new Intent(context, WxWidgetProvider.class);
        intent.setAction(WIDGET_UPDATE_ACTION);
        return PendingIntent.getBroadcast(context,0,intent,0);
    }}

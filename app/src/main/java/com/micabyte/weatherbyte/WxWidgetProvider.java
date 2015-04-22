package com.micabyte.weatherbyte;

import android.app.Activity;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by michaeloa on 4/22/15.
 */
public class WxWidgetProvider extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("WxWidget.UpdateService", "onUpdate()");
        // To prevent any ANR timeouts, we perform the update in a service
        context.startService(new Intent(context, UpdateService.class));
        /*
        final int N = appWidgetIds.length;
        // Loop over the widgets that belong to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wxwidget);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        */
    }


    public static class UpdateService extends Service {

        @Override
        public void onStart(Intent intent, int startId) {
            Log.d("WxWidget.UpdateService", "onStart()");
            // Build the widget update for today
            RemoteViews updateViews = buildUpdate(this);
            Log.d("WxWidget.UpdateService", "update built");
            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(this, WxWidgetProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
            Log.d("WxWidget.UpdateService", "widget updated");
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        public RemoteViews buildUpdate(Context context) {
            String base_uri = "https://data.met.no/v0/points?sources=KS18700";
            String stationId = MainActivity.loadTitlePref(getApplicationContext(), "stationId");
            Log.d("WxWidgetProvider", "StationId: " + stationId);
            String result = GET(base_uri);
            Log.d("WxWidgetProvider", "Result: " + result);
            /*
            // Pick out month names from resources
            Resources res = context.getResources();
            String[] monthNames = res.getStringArray(R.array.month_names);

            // Find current month and day
            Time today = new Time();
            today.setToNow();

            // Build the page title for today, such as "March 21"
            String pageName = res.getString(R.string.template_wotd_title,
                    monthNames[today.month], today.monthDay);
            String pageContent = null;

            try {
                // Try querying the Wiktionary API for today's word
                SimpleWikiHelper.prepareUserAgent(context);
                pageContent = SimpleWikiHelper.getPageContent(pageName, false);
            } catch (ApiException e) {
                Log.e("WxWidget", "Couldn't contact API", e);
            } catch (ParseException e) {
                Log.e("WxWidget", "Couldn't parse API response", e);
            }
            */

            RemoteViews views = null;
            // Build an update that holds the updated widget contents
            views = new RemoteViews(context.getPackageName(), R.layout.wxwidget);
            views.setTextViewText(R.id.WxValue, Integer.toString(1));
            return views;
        }

        public static String GET(String url){
            InputStream inputStream = null;
            String result = "";
            try {
                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // make GET request to the given URL
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();
                // convert inputstream to string
                if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";
            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }
            return result;
        }

        private static String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null)
                result += line;
            inputStream.close();
            return result;

        }

        public boolean isConnected(){
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
                return true;
            else
                return false;
        }


    }
}
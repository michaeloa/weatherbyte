package com.micabyte.weatherbyte;

import android.content.Context;
import android.os.Parcel;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

class Storage {
    private final static String TAG = Storage.class.getName();

    public static boolean storeWeatherInfo(Context context, WxInfo info, int widgetId)
    {
        final Parcel parcel = Parcel.obtain();
        final byte data[];
        final FileOutputStream output;
        StringBuilder path = new StringBuilder(context.getFilesDir().getAbsoluteFile().toString());
        path.append("/").append(String.valueOf(widgetId));
        Log.d("WEATHER", "Store file to: " + path.toString());
        try
        {
            output = new FileOutputStream(path.toString());
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
        try
        {
            parcel.writeValue(info);
            data = parcel.marshall();
            try
            {
                output.write(data);
                output.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
                return false;
            }
        }
        finally
        {
            parcel.recycle();
        }
        return true;
    }

    public static WxInfo restoreWeatherInfo(Context context, int widgetId)
    {
        final Parcel parcel = Parcel.obtain();
        final byte data[];
        final FileInputStream input;
        final File file = new File(context.getFilesDir().getAbsoluteFile().toString() + "/" + String.valueOf(widgetId));
        try
        {
            input = new FileInputStream(file.toString());
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
        Log.d(TAG, "Stored data found");
        data = new byte[(int) file.length()];
        try
        {
            try
            {
                if(input.read(data)!=data.length)
                    return null;
            }
            finally
            {
                input.close();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
        parcel.unmarshall(data,0,data.length);
        parcel.setDataPosition(0);
        final WxInfo info = (WxInfo)parcel.readValue(WxInfo.class.getClassLoader());
        parcel.recycle();
        return info;
    }

    public static boolean deleteWeatherInfo(Context context, int widgetId)
    {
        final File file = new File(context.getFilesDir().getAbsoluteFile().toString() + "/" + String.valueOf(widgetId));
        return !file.exists() || file.delete();
    }

}

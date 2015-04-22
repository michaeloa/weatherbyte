package com.micabyte.weatherbyte;

import android.content.res.Resources;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


class WxRequest {
    private final static String TAG = WxRequest.class.getName();
    private static int locN = 0;
    private final static String locations[] = { "KS18700", "KS18800" };
    // "KS18701","KS18703","KS18703",
    private final static int icons[] = { R.drawable.ic_mostly_sunny, R.drawable.ic_cloudy, R.drawable.ic_rain };
    private final static String uri = "https://data.met.no:443/v0/points?sources=";

    public static boolean getWeather(WxInfo weather)
    {
        URL url;
        InputStream in = null;
        try {
            locN ++;
            if (locN >= locations.length) locN = 0;
            String uri_full = uri + locations[locN];
            url = new URL(uri_full);
            URLConnection urlConnection;
            urlConnection = url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
            // Load Game File
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode rootNode = mapper.readTree(in);
                if (rootNode == null) {
                    Log.e(TAG, "Could not load data file");
                    return false;
                }
                Log.d(TAG, "Data Retrieved" + rootNode.toString());
                weather.temp = rootNode.get(0).path("value").asDouble();
                weather.location = rootNode.get(0).path("place").asText();
                weather.icon = icons[rootNode.get(0).path("quality").asInt()];
            } catch (Resources.NotFoundException | IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

}

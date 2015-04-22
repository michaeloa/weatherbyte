package com.micabyte.weatherbyte;

import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.GregorianCalendar;


public class WxRequest {
    private final static String TAG = WxRequest.class.getName();
    private static int locN = 0;
    private final static String locations[] = { "KS18700", "KS18800" };
    private final static String uri = "https://data.met.no:443/v0/points?sources=";

    public static boolean getWeather(WxInfo weather)
    {
        URL url;
        InputStream in = null;
        try {
            String uri_full = uri + locations[locN];
            locN ++;
            if (locN >= locations.length) locN = 0;
            url = new URL(uri_full);
            URLConnection urlConnection = null;
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
                weather.icon = R.drawable.ic_mostly_sunny;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
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

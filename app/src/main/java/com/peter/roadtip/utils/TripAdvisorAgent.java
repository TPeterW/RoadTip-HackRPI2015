package com.peter.roadtip.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.peter.roadtip.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Peter on 11/14/15.
 *
 */
public class TripAdvisorAgent {

    // simply singleton
    private static TripAdvisorAgent instance;

    private static String apiKey;
    private Context context;

    public static TripAdvisorAgent getInstance(Context context) {
        if (instance == null) {
            instance = new TripAdvisorAgent(context);
        }
        return instance;
    }

    private TripAdvisorAgent(Context context) {
        this.context = context;
        this.apiKey = context.getString(R.string.trip_advisor_key);
    }

    /**
     *
     * @param       request
     * @return      null if encounters error
     */
    public void getResponse(final String request) {
        new GetResponseTaskAsync().execute(request);
    }

    /**
     *
     * @param distance      radius from LatLng, 0 if no specification
     * @param name          name of property, null if no specification
     * @return request      string
     */
    public String createRequest(double latitude, double longitude, double distance, @Nullable String name) {
        // map is to find by LatLng, other options available
        String request = "http://api.tripadvisor.com/api/partner/2.0/map/";
        request += latitude + "," + longitude + "?key=" + apiKey;

        if (distance != 0) {
            name = null;                // we don't allow search both by distance and name
            request += "&distance=" + distance;
            //TODO: filtered map call
        }

        if (name != null) {
            request += "&q=" + name;
        }

        return request;
    }

    class GetResponseTaskAsync extends AsyncTask<String, Void, String> {

        ResponseListener listener = (ResponseListener) context;

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            try {
                URL url = new URL(urls[0]);
                URLConnection urlConnection = url.openConnection();

                urlConnection.setUseCaches(true);
                urlConnection.setRequestProperty("Content-length", "0");
                urlConnection.connect();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader isr = new InputStreamReader(in);

                int data = isr.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isr.read();
                    response += current;
                }

//                Log.i("GetResponse", "Here");
                Looper.prepare();
                listener.onReceiveResponse(response);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            JSONObject data = null;

            JSONOperator jsonOperator = JSONOperator.getInstance(context);
            String[] listName       = new String[0];
            double[] listLat        = new double[0];
            double[] listLng        = new double[0];
            double[] listDistance   = new double[0];
            int count = 0;

            try {
                data = new JSONObject(response);
                listName       = jsonOperator.getName(data);
                listLat        = jsonOperator.getLat(data);
                listLng        = jsonOperator.getLng(data);
                listDistance   = jsonOperator.getDistance(data);
                count          = jsonOperator.getName(data).length;
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, context.getString(R.string.problem_stream), Toast.LENGTH_SHORT).show();
            }
            Log.i("PostExecute", "drawMarkers Called");
            listener.drawMarkers(listName, listLat, listLng, listDistance);

            super.onPostExecute(response);
        }
    }

    public interface ResponseListener {
        void onReceiveResponse(String result);
        void drawMarkers(String[] listName, double[] listLat, double[] listLng, double[] listDistance);
    }
}

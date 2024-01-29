package com.example.supergps;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;

import android.util.Log;
import android.view.View;
import android.os.AsyncTask;

import org.json.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final static int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    final static String API_URL = "https://api.openweathermap.org/data/2.5/weather";
    final static String KEY = "ffc2846d4cdf263c6126bcdff348e461";


    double latitude;
    double longitude;
    int PERMISSION_ID = 44;
    JSONObject jsonObject;

    ProgressBar progressBar;
    TextView loadingText;
    Button reload;

    TextView city;
    TextView temp;
    TextView weather;
    TextView lonText;
    TextView latText;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingText = findViewById(R.id.loadingText);
        progressBar = findViewById(R.id.progressBar);
        reload =  findViewById(R.id.reload);
        reload.setVisibility(View.VISIBLE);

        city = findViewById(R.id.cityText);
        temp = findViewById(R.id.tempText);
        weather = findViewById(R.id.weatherText);
        lonText = findViewById(R.id.longitude);
        latText = findViewById(R.id.latitude);


        // "this" is MainActivity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }
        getGPS();
        // initial weather call
        new fetchWeatherTask().execute();

        // refresh new weather when pressed
        reload.setOnClickListener(view -> new fetchWeatherTask().execute());



    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    public void getGPS() {
        // "this" is MainActivity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = lm.getProviders(true);

            Location l = null;

            for (int i = providers.size() - 1; i >= 0; i--) {
                l = lm.getLastKnownLocation(providers.get(i));
                if (l != null) break;
            }

            if (l != null) {
                latitude =  l.getLatitude();
                longitude = l.getLongitude();
            }
        }

        // Keep asking the user for access to location
        else {
            requestPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    //Check if permission is granted
        if (requestCode == MY_PERMISSIONS_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getGPS();
            }

            // ask the user again for access to fine location
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            }
        }
    }

   @SuppressWarnings("deprecation")
   @SuppressLint("StaticFieldLeak")
   class fetchWeatherTask extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            loadingText.setVisibility(View.VISIBLE);
        }
        protected String doInBackground(Void... urls) {

            try {
                URL url = new URL(API_URL + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + KEY);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {

                    BufferedReader bufferedReader =
                            new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String line;
                    StringBuilder buffer = new StringBuilder();

                    while ((line = bufferedReader.readLine()) != null)
                        buffer.append(line).append("\n");

                    bufferedReader.close();

                    return buffer.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {

                Log.e("ERROR", e.getMessage(), e);

                return null;
            }
        }

        protected void onPostExecute(String response) {

            lonText.setText("Longitude:" + Double.toString(longitude));
            latText.setText("Latitude:"+ Double.toString(latitude));

            if (response == null) {

                loadingText.setText("Error getting data");
                progressBar.setVisibility(View.GONE);

                return;
            }

            try {
                jsonObject = new JSONObject(response);
            } catch (JSONException e) {

                Log.e("log_tag", "Error parsing data " + e);

                loadingText.setText(e.getMessage());
            }

            try {
                city.setText("Location: " + jsonObject.getString("name"));
                temp.setText((Integer.toString((int) (Double.parseDouble(jsonObject.getJSONObject("main").getString("temp")) - 273)) + "°C") + ", "
                        + jsonObject.getJSONArray("weather").getJSONObject(0).getString("main"));

            } catch (JSONException e) {

                Log.e("ERROR", e.getMessage());

                e.printStackTrace();
            }

            progressBar.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);

        }
    }
}
package com.icare.mal21.astertech;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.icare.mal21.astertech.Bareclasses.GpsLocation;
import com.icare.mal21.astertech.Bareclasses.HistoricPoint;
import com.icare.mal21.astertech.Bareclasses.Users;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ProgressDialog progressDialog;
    private String urlPost;
    private List<GpsLocation> gpsLocations;
    private SharedPreferences sharedPreferences;
    private TextView matricule,chauffeur,vitesse,adresse,kh;
    private List<HistoricPoint> historicPoints;
    String matriculeSP="";
    String idBoitier="";
    private String chauffeurSP;
    private String type;
    private String vitesseSP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        matricule = (TextView) findViewById(R.id.matricule);
        chauffeur = (TextView) findViewById(R.id.chauffeur);
        vitesse = (TextView) findViewById(R.id.vitesse);
        adresse = (TextView) findViewById(R.id.adresse);
        kh = (TextView) findViewById(R.id.kh);


        Intent myIntent = getIntent();
        matriculeSP = myIntent.getStringExtra("matricule");
        chauffeurSP = myIntent.getStringExtra("chauffeur");
        vitesseSP = myIntent.getStringExtra("vitesse");

        matricule.setText(matriculeSP);
        chauffeur.setText(chauffeurSP);
        vitesse.setText(vitesseSP);


        try {

            type = myIntent.getStringExtra("realTime");
            String matricule = matriculeSP.replaceAll(" ", "%20");
            urlPost = "http://" + Splashscreen.prefixe + ".ngrok.io/astertechgps/getInfoVehicleByMatriclue.php?matricule=" + matricule;
            Log.e("urlpost", urlPost);

            PostFetcher fetcher = new PostFetcher();
            fetcher.execute();

        } catch (Exception e) {


            matriculeSP = myIntent.getStringExtra("matriculeTrace");
            chauffeurSP = myIntent.getStringExtra("chauffeurTrace");

            matricule.setText(matriculeSP);
            chauffeur.setText(chauffeurSP);
            type = myIntent.getStringExtra("trace");
            idBoitier = myIntent.getStringExtra("idBoitier");
            urlPost = "http://" + Splashscreen.prefixe + ".ngrok.io/astertechgps/getHistoryByIdBoitier.php?Tgp_IdBoitierGps=" + idBoitier;
            Log.e("urlpost", urlPost);

            PostFetcherTrace postFetcherTrace = new PostFetcherTrace();
            postFetcherTrace.execute();

        }

        ImageView refresh = (ImageView) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });

    }

    private void updateDisplay() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                PostFetcher fetcher = new PostFetcher();
                fetcher.execute();
            }

        },0,30000);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(36.8188, 10.166);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Tunis"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private class PostFetcherTrace extends AsyncTask<Void, Void, String> {
        private static final String TAG = "PostFetcherTrace";
        //public static final String SERVER_URL = "http://kylewbanks.com/rest/posts.json";

        @Override
        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(MapsActivity.this);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Fetching data...");
                    progressDialog.show();
                    kh.setVisibility(View.INVISIBLE);
                    adresse.setVisibility(View.INVISIBLE);
                }
            });
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                //Create an HTTP client
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(urlPost);

                //Perform the request and check the status code
                HttpResponse response = client.execute(post);
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();

                    try {
                        //Read the server response and attempt to parse it as JSON
                        Reader reader = new InputStreamReader(content);

                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.setDateFormat("M/d/yy hh:mm a");
                        Gson gson = gsonBuilder.create();
                        List<HistoricPoint> historicPoints = new ArrayList<HistoricPoint>();
                        historicPoints = Arrays.asList(gson.fromJson(reader, HistoricPoint[].class));
                        content.close();

                        handlePostsListTrace(historicPoints);
                    } catch (Exception ex) {
                        Log.e(TAG, "Failed to parse JSON due to: " + ex);
                        failedLoadingPosts();
                    }
                } else {
                    Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
                    failedLoadingPosts();
                }
            } catch(Exception ex) {
                Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
                failedLoadingPosts();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
        }
    }

    private void handlePostsListTrace (final List<HistoricPoint> historicPoints) {
        this.historicPoints = historicPoints;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    float lat0 = historicPoints.get(0).Tgp_Latitude;
                    float longi0 = historicPoints.get(0).Tgp_Longitude;
                    LatLng location0 = new LatLng(lat0, longi0);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location0, 12.0f));

                } catch (Exception e){
                    noRecentInfomation();
                }

                PolylineOptions line= new PolylineOptions();

                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                for (int i=0;i<historicPoints.size();i++){
                    float lat = historicPoints.get(i).Tgp_Latitude;
                    float longi = historicPoints.get(i).Tgp_Longitude;
                    LatLng location = new LatLng(lat, longi);
                    line.add(location);

                    builder.include(location);

                   // mMap.addMarker(new MarkerOptions().position(location).title("" + historicPoint.Tgp_DateHeure.date));

                    if(i==0) {
                        mMap.addMarker(new MarkerOptions().position(location).title(""+historicPoints.get(i).Tgp_Vitesse+" k/h")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.depart)));
                    } else if (i==(historicPoints.size()-1)){
                        mMap.addMarker(new MarkerOptions().position(location).title(""+historicPoints.get(i).Tgp_Vitesse+" k/h")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrivee)));
                    } else if(historicPoints.get(i).Tgp_Direction.equals("E")) {
                        mMap.addMarker(new MarkerOptions().position(location).title(""+historicPoints.get(i).Tgp_Vitesse+" k/h")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.e)));
                    } else if(historicPoints.get(i).Tgp_Direction.equals("N")) {
                        mMap.addMarker(new MarkerOptions().position(location).title(""+historicPoints.get(i).Tgp_Vitesse+" k/h")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.n)));
                    } else if(historicPoints.get(i).Tgp_Direction.equals("W")) {
                        mMap.addMarker(new MarkerOptions().position(location).title(""+historicPoints.get(i).Tgp_Vitesse+" k/h")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.w)));
                    } else if(historicPoints.get(i).Tgp_Direction.equals("S")) {
                        mMap.addMarker(new MarkerOptions().position(location).title(""+historicPoints.get(i).Tgp_Vitesse+" k/h")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.s)));
                    } else if(historicPoints.get(i).Tgp_Direction.equals("NE")) {
                        mMap.addMarker(new MarkerOptions().position(location).title(""+historicPoints.get(i).Tgp_Vitesse+" k/h")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ne)));
                    } else if(historicPoints.get(i).Tgp_Direction.equals("NW")) {
                        mMap.addMarker(new MarkerOptions().position(location).title(""+historicPoints.get(i).Tgp_Vitesse+" k/h")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.nw)));
                    } else if(historicPoints.get(i).Tgp_Direction.equals("SE")) {
                        mMap.addMarker(new MarkerOptions().position(location).title(""+historicPoints.get(i).Tgp_Vitesse+" k/h")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.se)));
                    } else if(historicPoints.get(i).Tgp_Direction.equals("SW")) {
                        mMap.addMarker(new MarkerOptions().position(location).title(""+historicPoints.get(i).Tgp_Vitesse+" k/h")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.sw)));
                    }
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                }


                line.width(15).color(Color.BLACK);
                mMap.addPolyline(line);

                try {
                    LatLngBounds bounds = builder.build();
                    int padding = 0; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.animateCamera(cu);
                }
                catch (Exception e){
                    e.getStackTrace();
                }

                kh.setText("");

                matricule.setText(matriculeSP);
                chauffeur.setText(chauffeurSP);
                //adresse.setText(""+);
            }
        });
    }

    private class PostFetcher extends AsyncTask<Void, Void, String> {
        private static final String TAG = "PostFetcher";
        //public static final String SERVER_URL = "http://kylewbanks.com/rest/posts.json";

        @Override
        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(MapsActivity.this);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Fetching data...");
                    progressDialog.show();
                }
            });
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                //Create an HTTP client
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(urlPost);

                //Perform the request and check the status code
                HttpResponse response = client.execute(post);
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();

                    try {
                        //Read the server response and attempt to parse it as JSON
                        Reader reader = new InputStreamReader(content);

                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.setDateFormat("M/d/yy hh:mm a");
                        Gson gson = gsonBuilder.create();
                        List<GpsLocation> gpsLocations = new ArrayList<GpsLocation>();
                        gpsLocations = Arrays.asList(gson.fromJson(reader, GpsLocation[].class));
                        content.close();

                        handlePostsList(gpsLocations);
                    } catch (Exception ex) {
                        Log.e(TAG, "Failed to parse JSON due to: " + ex);
                        failedLoadingPosts();
                    }
                } else {
                    Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
                    failedLoadingPosts();
                }
            } catch(Exception ex) {
                Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
                failedLoadingPosts();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
        }
    }

    private void handlePostsList (final List<GpsLocation> gpsLocations) {
        this.gpsLocations = gpsLocations;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float lat = Float.parseFloat(gpsLocations.get(0).latitude);
                float longi = Float.parseFloat(gpsLocations.get(0).longitude);
                LatLng location = new LatLng(lat, longi);
                mMap.addMarker(new MarkerOptions().position(location).title(""));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 9.0f));

                matricule.setText(gpsLocations.get(0).matricule);
                chauffeur.setText(gpsLocations.get(0).chauffeur);
                vitesse.setText(gpsLocations.get(0).vitesse);

                Geocoder geocoder;
                List<Address> addresses = null;
                geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(lat, longi, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String address="";
                String city="";
                String state="";
                String area="";
                String country="";
                String knownName="";

                if (addresses.get(0).getAddressLine(0)!=null){
                    address = ", "+addresses.get(0).getAddressLine(0);
                }
                if (addresses.get(0).getLocality()!=null){
                    city = ", "+addresses.get(0).getLocality();
                }
                if (addresses.get(0).getAdminArea()!=null){
                    state = ", "+addresses.get(0).getAdminArea();
                }
                if (addresses.get(0).getSubAdminArea()!=null){
                    area = ", "+addresses.get(0).getSubAdminArea();
                }
                if (addresses.get(0).getCountryName()!=null){
                    country = addresses.get(0).getCountryName();
                }
                if (addresses.get(0).getThoroughfare()!=null){
                    knownName = ", "+addresses.get(0).getThoroughfare() ;
                }

                TextView adresse = (TextView) findViewById(R.id.adresse);
                adresse.setText(country+state+city+address+knownName);
            }
        });
    }

    private void failedLoadingPosts() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(MapsActivity.this, "Failed to load information.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void noRecentInfomation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(MapsActivity.this, "No recent information.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }
}

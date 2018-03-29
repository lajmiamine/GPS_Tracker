package com.icare.mal21.astertech;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.icare.mal21.astertech.Bareclasses.Vehicule;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * Created by mal21 on 23/08/2016.
 */
public class FragmentRealTime extends android.support.v4.app.Fragment {

    private View view;
    private SharedPreferences sharedPreferences;
    private RecyclerView recyclerView;
    private String urlPost;
    private ProgressDialog progressDialog;
    private List<Vehicule> vehicles;
    private List<Vehicule> vehiclesSearch=new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private int recyclerViewPaddingTop;
    private VehicleSiteAdapter recyclerViewAdapter;
    private EditText filterText;
    private ArrayList<String> genres = new ArrayList<>();
    private ArrayList<Integer> genresNbr = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_real_time, container, false);
        filterText = (EditText) view.findViewById(R.id.filterEditText);

        // Get shared preferences
        sharedPreferences = getActivity().getSharedPreferences("VALUES", Context.MODE_PRIVATE);

        getActivity().setTitle("Park in details");

        // Setup RecyclerView News
        recyclerViewDesign(view);


        HeaderDecoration headerDecoration = new HeaderDecoration(getContext(), recyclerView, R.layout.item_header, genres, genresNbr);
        int decorPriorityIndex = 0;
        recyclerView.addItemDecoration(headerDecoration,decorPriorityIndex);

        // Setup swipe to refresh
        swipeToRefresh(view);


        filterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                vehiclesSearch = new ArrayList<Vehicule>();

                for (int i=0;i<vehicles.size();i++){
                    if (vehicles.get(i).matricule.matches(".*"+s+".*")){
                        vehiclesSearch.add(vehicles.get(i));
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            recyclerViewAdapter = new VehicleSiteAdapter(getContext(), vehiclesSearch);
                            recyclerView.setAdapter(recyclerViewAdapter);
                        } catch (Exception e) {//}
                        }
                    }
                });
            }
        });

        return view;
    }

    private void recyclerViewDesign(View view) {

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewRealTime);

        // Divider
        recyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(android.R.drawable.divider_horizontal_bright)));

        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        sharedPreferences = getActivity().getSharedPreferences("VALUES", Context.MODE_PRIVATE);
        int Use_Id_Societe = sharedPreferences.getInt("Use_Id_Societe", 0);
        int Use_Id_Site = sharedPreferences.getInt("Use_Id_Site", 0);
        int Use_Id_GrVehicules = sharedPreferences.getInt("Use_Id_GrVehicules", 0);

        /*try {
            String status = sharedPreferences.getString("Status", "No Status");
            urlPost="http://"+Splashscreen.prefixe+".ngrok.io/astertechgps/getVehiclesByUser.php?" +
                    "idSociete="+Use_Id_Societe+"&Sit_Id="+Use_Id_Site+"&GVe_Id="+Use_Id_GrVehicules+"&status="+status;

        } catch (Exception e){
            urlPost="http://"+Splashscreen.prefixe+".ngrok.io/astertechgps/getVehiclesByUser.php?" +
                    "idSociete="+Use_Id_Societe+"&Sit_Id="+Use_Id_Site+"&GVe_Id="+Use_Id_GrVehicules;
        }*/

        urlPost="http://"+Splashscreen.prefixe+".ngrok.io/astertechgps/getVehiclesByUser.php?" +
                "idSociete="+Use_Id_Societe+"&Sit_Id="+Use_Id_Site+"&GVe_Id="+Use_Id_GrVehicules;
        Log.e("urlpost",urlPost);
        PostFetcher fetcher = new PostFetcher();
        fetcher.execute();

    }


    private class PostFetcher extends AsyncTask<Void, Void, String> {
        private static final String TAG = "PostFetcher";
        //public static final String SERVER_URL = "http://kylewbanks.com/rest/posts.json";

        @Override
        protected void onPreExecute() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(getActivity());
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
                        List<Vehicule> vehicles = new ArrayList<Vehicule>();
                        vehicles = Arrays.asList(gson.fromJson(reader, Vehicule[].class));
                        content.close();

                        handlePostsList(vehicles);
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

    private void handlePostsList(final List<Vehicule> vehicles) {
        this.vehicles = vehicles;
        Log.e("genresVehicles",String.valueOf(vehicles.size()));

        String genre="";
        for (int i=0;i<vehicles.size();i++){
            if (i==0){
                genre=vehicles.get(0).genre;
                genres.add(genre);
                Log.e("genre",genre);

                int k=0;
                for (int j=0;j<vehicles.size();j++){
                    if (vehicles.get(j).genre.equals(genre)){
                        k++;
                    }
                }
                genresNbr.add(k);
                Log.e("genresNbr",String.valueOf(genresNbr.get(0)));
            }else {
                if (!vehicles.get(i).genre.equals(genre)){
                    String tmp=vehicles.get(i).genre;
                    boolean f=false;
                    for (int e=0; e<genres.size();e++){
                        if (genres.get(e).equals(tmp)){
                            f=true;
                        }
                    }

                    if (f==false){
                        genre=vehicles.get(i).genre;
                        genres.add(genre);
                        Log.e("genre",genre);
                        int k=0;
                        for (int j=0;j<vehicles.size();j++){
                            if (vehicles.get(j).genre.equals(genre)){
                                k++;
                            }
                        }
                        Log.e("genresK",String.valueOf(k));
                        genresNbr.add(k);
                    }
                }
            }
        }



        getActivity().runOnUiThread(new Runnable() {
            //public SharedPreferences haredPreferences;

            @Override
            public void run() {

                sharedPreferences = getActivity().getSharedPreferences("VALUES", Context.MODE_PRIVATE);

                vehiclesSearch=vehicles;
                recyclerViewAdapter = new VehicleSiteAdapter(getContext(), vehiclesSearch);
                recyclerView.setAdapter(recyclerViewAdapter);

                swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
                swipeRefreshLayout.setRefreshing(false);

            }
        });
    }

    private void failedLoadingPosts() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Failed to load information.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void swipeToRefresh(View view) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        int start = recyclerViewPaddingTop - convertToPx(48), end = recyclerViewPaddingTop + convertToPx(16);
        swipeRefreshLayout.setProgressViewOffset(true, start, end);
        TypedValue typedValueColorPrimary = new TypedValue();
        TypedValue typedValueColorAccent = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.colorPrimary, typedValueColorPrimary, true);
        getActivity().getTheme().resolveAttribute(R.attr.colorAccent, typedValueColorAccent, true);
        final int colorPrimary = typedValueColorPrimary.data, colorAccent = typedValueColorAccent.data;
        swipeRefreshLayout.setColorSchemeColors(colorPrimary, colorAccent);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                vehicles=new ArrayList<>();
                PostFetcher fetcher = new PostFetcher();
                fetcher.execute();
            }
        });
    }

    public int convertToPx(int dp) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dp * scale + 0.5f);
    }
}

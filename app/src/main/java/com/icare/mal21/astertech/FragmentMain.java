package com.icare.mal21.astertech;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.icare.mal21.astertech.Bareclasses.VehiclesCard;
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
import java.util.List;

/**
 * Created by mal21 on 23/08/2016.
 */
public class FragmentMain extends android.support.v4.app.Fragment {

    private View view;
    private String urlPost;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    private List<VehiclesCard> vehiclesCards;
    private int card=0;
    TextView nbrMarche, nbrArret, nbrStop, nbrAlert, nbrAnomalie;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int recyclerViewPaddingTop;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_main, container, false);

        getActivity().setTitle("Overview");

        nbrMarche = (TextView) view.findViewById(R.id.nbrMarche);
        nbrArret = (TextView) view.findViewById(R.id.nbrArret);
        nbrStop = (TextView) view.findViewById(R.id.nbrStop);
        nbrAlert = (TextView) view.findViewById(R.id.nbrAlert);
        nbrAnomalie = (TextView) view.findViewById(R.id.nbrAnomalie);

        RelativeLayout clickableLayout = (RelativeLayout) view.findViewById(R.id.clickableLayout);
        clickableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentManager = getActivity().getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                FragmentAlerts fragmentAlerts = new FragmentAlerts();
                fragmentTransaction.replace(R.id.fragment, fragmentAlerts);
                fragmentTransaction.commit();
            }
        });

        sharedPreferences = getActivity().getSharedPreferences("VALUES", Context.MODE_PRIVATE);
        int Use_Id_Societe = sharedPreferences.getInt("Use_Id_Societe", 0);
        int Use_Id_Site = sharedPreferences.getInt("Use_Id_Site", 0);
        int Use_Id_GrVehicules = sharedPreferences.getInt("Use_Id_GrVehicules", 0);
        String Use_nom = sharedPreferences.getString("Use_nom","Use_nom");
        String Use_Prenom = sharedPreferences.getString("Use_Prenom","Use_Prenom");

        TextView user = (TextView) view.findViewById(R.id.user);
        user.setText(""+Use_nom+" "+Use_Prenom);

        urlPost="http://"+Splashscreen.prefixe+".ngrok.io/astertechgps/getVehiculesMarche.php?" +
                "idSociete="+Use_Id_Societe+"&Sit_Id="+Use_Id_Site+"&GVe_Id="+Use_Id_GrVehicules;


        PostFetcher fetcher = new PostFetcher();
        fetcher.execute();

        return view;
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
                    Log.e("urlpost",urlPost);

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
                            List<VehiclesCard> vehiclesCards = new ArrayList<VehiclesCard>();
                            vehiclesCards = Arrays.asList(gson.fromJson(reader, VehiclesCard[].class));
                            content.close();

                            handlePostsList(vehiclesCards);
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

    private void handlePostsList(final List<VehiclesCard> vehiclesCards) {
        this.vehiclesCards = vehiclesCards;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i=0;i<vehiclesCards.size();i++){
                    if (i==0){
                        nbrMarche.setText(String.valueOf(vehiclesCards.get(i).count)+"");
                    } else if (i==1){
                        nbrArret.setText(String.valueOf(vehiclesCards.get(i).count)+"");
                    } else if (i==2){
                        nbrStop.setText(String.valueOf(vehiclesCards.get(i).count)+"");
                    } else if (i==3) {
                        nbrAlert.setText(String.valueOf(vehiclesCards.get(i).count)+"");
                    } else {
                        nbrAnomalie.setText(String.valueOf(vehiclesCards.get(i).count)+"");
                    }
                }
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
}

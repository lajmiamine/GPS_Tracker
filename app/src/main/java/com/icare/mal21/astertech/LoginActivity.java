package com.icare.mal21.astertech;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.icare.mal21.astertech.Bareclasses.Users;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    String urlPost;
    int check;

    EditText _emailText;
    EditText _passwordText;
    Button _loginButton;
    TextView _signupLink;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    ProgressDialog progressDialog;
    private List<Users> users;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        _emailText = (EditText) findViewById(R.id.input_email);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);

        sharedPreferences = getSharedPreferences("VALUES", MODE_PRIVATE);


        if (sharedPreferences.getString("Use_Login","Use_Login").equals("Use_Login")) {

            ImageView logosplash3 = (ImageView) findViewById(R.id.logosplash3);
            logosplash3.setVisibility(View.INVISIBLE);

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {

                            Animation bottomUp = AnimationUtils.loadAnimation(getApplication().getApplicationContext(),
                                    R.anim.bottom_up);

                            ImageView logosplash = (ImageView) findViewById(R.id.logosplash);
                            logosplash.setVisibility(View.INVISIBLE);

                            ViewGroup hiddenPanel = (ViewGroup) findViewById(R.id.hidden_panel);
                            hiddenPanel.startAnimation(bottomUp);
                            hiddenPanel.setVisibility(View.VISIBLE);

                        }
                    }, 1000);
        } else {

            ImageView logosplash = (ImageView) findViewById(R.id.logosplash);
            logosplash.setVisibility(View.INVISIBLE);

            ImageView logosplash3 = (ImageView) findViewById(R.id.logosplash3);
            logosplash3.setVisibility(View.VISIBLE);

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {

                            Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                            LoginActivity.this.startActivity(myIntent);

                        }
                    }, 1000);

        }

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

    }

    public void login() {
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.equals("")&&password.equals("")){
            Toast.makeText(LoginActivity.this,"Please enter your username and your password",
                    Toast.LENGTH_SHORT).show();
        }else if(email.equals("")){
            Toast.makeText(LoginActivity.this,"Please enter your username",
                    Toast.LENGTH_SHORT).show();
        }else if(password.equals("")){
            Toast.makeText(LoginActivity.this,"Please enter your password",
                    Toast.LENGTH_SHORT).show();
        }else {

            //close keyboard
            InputMethodManager inputManager = (InputMethodManager) getApplicationContext(). getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            String encryptedPSW = encode(password);
            urlPost = "http://" + Splashscreen.prefixe + ".ngrok.io/astertechgps/getDataFromTable1.php?Use_Login=" + email + "&Use_Pwd=" + encryptedPSW;
            Log.e("urlpost", urlPost);

            if(!isNetworkAvailable(this)) {
                Toast.makeText(this,"No Internet connection",Toast.LENGTH_LONG).show();
            } else {
                PostFetcher fetcher = new PostFetcher();
                fetcher.execute();
            }
        }
    }

    private class PostFetcher extends AsyncTask<Void, Void, String> {
        private static final String TAG = "PostFetcher";
        //public static final String SERVER_URL = "http://kylewbanks.com/rest/posts.json";

        @Override
        protected void onPreExecute() {
            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Authenticating...");
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
                        List<Users> users = new ArrayList<Users>();
                        users = Arrays.asList(gson.fromJson(reader, Users[].class));
                        content.close();

                        handlePostsList(users);
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

    private void handlePostsList(List<Users> users) {
        this.users = users;

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                for(Users users : LoginActivity.this.users) {
                    Toast.makeText(LoginActivity.this, "Welcome "+users.Use_nom, Toast.LENGTH_SHORT).show();
                    sharedPreferences = getSharedPreferences("VALUES", Context.MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                    editor.putString("Use_nom", users.Use_nom);
                    editor.putString("Use_Prenom", users.Use_Prenom);
                    editor.putString("Use_Login", users.Use_Login);
                    editor.putString("Use_Mail", users.Use_Mail);
                    editor.putInt("Use_Id_Societe",users.Use_Id_Societe);
                    editor.putInt("Use_Id_Site",users.Use_Id_Site);
                    editor.putInt("Use_Id_GrVehicules",users.Use_GrVehicules);
                    editor.putString("Ugr_Nom",users.Ugr_Nom);
                    editor.commit();

                    getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                    );

                    Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(myIntent);
                }
            }
        });
    }

    private void failedLoadingPosts() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Failed to load information.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                //this.finish();
            }
        }
    }

    public String encode(String password) {
        byte[] uniqueKey = password.getBytes();
        byte[] hash = null;

        try {
            hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
        } catch (NoSuchAlgorithmException e) {
        }

        StringBuilder hashString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                hashString.append('0');
                hashString.append(hex.charAt(hex.length() - 1));
            } else {
                hashString.append(hex.substring(hex.length() - 2));
            }

        }
        return hashString.toString();
    }

    /**
     * This method check mobile is connected to network.
     * @param context
     * @return true if connected otherwise false.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conMan.getActiveNetworkInfo() != null && conMan.getActiveNetworkInfo().isConnected())
            return true;
        else
            return false;
    }

}

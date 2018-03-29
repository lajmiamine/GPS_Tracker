package com.icare.mal21.astertech;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 */
public class Splashscreen extends Activity {

    public static String prefixe="cc707421";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sharedPreferences = getSharedPreferences("VALUES", MODE_PRIVATE);


        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1500);

                    if (sharedPreferences.getString("Use_Login","Use_Login").equals("Use_Login")) {
                        Log.d("sharedPreferences error", "Can't get USERID");
                        Intent myIntent = new Intent(Splashscreen.this, LoginActivity.class);
                        Splashscreen.this.startActivity(myIntent);
                    }
                    else {
                        Log.e("Use_Login",sharedPreferences.getString("Use_Login","Use_Login"));
                        Intent myIntent = new Intent(Splashscreen.this, MainActivity.class);
                        Splashscreen.this.startActivity(myIntent);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

    }

}

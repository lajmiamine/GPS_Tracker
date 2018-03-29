package com.icare.mal21.astertech;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences sharedPreferences;
    private TextView user;
    private TextView email;
    private SharedPreferences.Editor editor;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private int backButtonCount=0;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        FragmentMain fragmentMain = new FragmentMain();
        fragmentTransaction.replace(R.id.fragment, fragmentMain);
        fragmentTransaction.commit();

    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if(backButtonCount >= 1)
            {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
                backButtonCount++;

                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                backButtonCount=0;
                            }
                        }, 1500);
            }
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_overview) {
            backButtonCount=0;
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            FragmentMain fragmentMain = new FragmentMain();
            fragmentTransaction.replace(R.id.fragment, fragmentMain);
            fragmentTransaction.commit();
            //
        } else if (id == R.id.nav_real_time) {
            backButtonCount=0;
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            FragmentRealTime fragmentRealTime = new FragmentRealTime();
            fragmentTransaction.replace(R.id.fragment, fragmentRealTime);
            fragmentTransaction.commit();
            //
        } else if (id == R.id.nav_alerts) {
            backButtonCount=0;
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            FragmentAlerts fragmentAlerts = new FragmentAlerts();
            fragmentTransaction.replace(R.id.fragment, fragmentAlerts);
            fragmentTransaction.commit();
            //
        } else if (id == R.id.nav_log_out) {
            backButtonCount=0;
            sharedPreferences = getSharedPreferences("VALUES", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            editor.putString("Use_nom", "Use_nom");
            editor.putString("Use_Prenom", "Use_Prenom");
            editor.putString("Use_Login", "Use_Login");
            editor.putString("Use_Mail", "Use_Mail");
            editor.putInt("Use_Id_Societe", 0);
            editor.putInt("Use_Id_Site",0);
            editor.putInt("Use_Id_GrVehicules",0);
            editor.commit();

            dialog = new Dialog(MainActivity.this);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.popup_logout);

            dialog.show();

            Button yesBtn = (Button) dialog.findViewById(R.id.yesBtn);
            Button noBtn = (Button) dialog.findViewById(R.id.noBtn);

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
                    MainActivity.this.startActivity(myIntent);
                }
            });

            noBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        backButtonCount=0;
    }
}

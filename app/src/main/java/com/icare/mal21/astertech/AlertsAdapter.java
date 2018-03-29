package com.icare.mal21.astertech;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.icare.mal21.astertech.Bareclasses.Alert;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.ViewHolder>{

    private List<Alert> alerts;
    Context context;

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Toast.makeText(context,"Clicked",Toast.LENGTH_SHORT).show();
        }
    };


    // Adapter's Constructor
    public AlertsAdapter(Context context, List<Alert> alerts) {
        this.alerts = alerts;
        this.context = context;
    }

    // Create new views. This is invoked by the layout manager.
    @Override
    public AlertsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view by inflating the row item xml.
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        v.setOnClickListener(onClickListener);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);

        // Set strings to the views
        final TextView matriculeAlert = (TextView) holder.view.findViewById(R.id.matriculeAlert);
        final TextView messageAlert = (TextView) holder.view.findViewById(R.id.messageAlert);
        final TextView dateAlert = (TextView) holder.view.findViewById(R.id.dateAlert);
        final TextView alertValue = (TextView) holder.view.findViewById(R.id.alertValue);
        final ImageView imageViewImage = (ImageView) holder.view.findViewById(R.id.imageViewImage);
        matriculeAlert.setText(alerts.get(position).matricule_vehicule+"");
        messageAlert.setText(alerts.get(position).Ano_Message+"");
        try {
            //DateFormat f = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.S");
            //Date d = f.parse(alerts.get(position).Ano_DateHeure.date);
            //DateFormat time = new SimpleDateFormat("hh:mm:ss");
            //String timeAlert = time.format(d);
            String[] timeAlertParts = alerts.get(position).Ano_DateHeure.date.split(" ");
            String[] timeAlertDateWithMilli = timeAlertParts[1].split("\\.");
            String timeAlert = timeAlertDateWithMilli[0];
            dateAlert.setText(""+timeAlert);
        } catch (Exception e) {
            e.printStackTrace();
        }

        switch (alerts.get(position).Ano_Type){
            case "V": imageViewImage.setImageResource(R.drawable.vitesse);
                alertValue.setVisibility(View.VISIBLE);
                alertValue.setText(""+alerts.get(position).valeur);

                break;
            case "VG": imageViewImage.setImageResource(R.drawable.virage);
                break;
            case "T": imageViewImage.setImageResource(R.drawable.temperature);
                alertValue.setVisibility(View.VISIBLE);
                alertValue.setText(""+alerts.get(position).valeur);
                break;
            case "F": imageViewImage.setImageResource(R.drawable.freinage);
                break;
            case "A": imageViewImage.setImageResource(R.drawable.accesleration);
                break;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return alerts.size();
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;

        public ViewHolder(View v) {
            super(v);
            view = v;
        }
    }
}

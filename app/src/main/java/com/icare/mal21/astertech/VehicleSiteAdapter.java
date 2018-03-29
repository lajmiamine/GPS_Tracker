package com.icare.mal21.astertech;

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
import android.Manifest;

import com.icare.mal21.astertech.Bareclasses.Vehicule;

import java.util.List;

public class VehicleSiteAdapter extends RecyclerView.Adapter<VehicleSiteAdapter.ViewHolder>{

    private List<Vehicule> vehicles;
    Context context;
    Dialog dialog;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Toast.makeText(context,"Clicked",Toast.LENGTH_SHORT).show();
        }
    };


    // Adapter's Constructor
    public VehicleSiteAdapter(Context context, List<Vehicule> vehicles) {
        this.vehicles = vehicles;
        this.context = context;
    }

    // Create new views. This is invoked by the layout manager.
    @Override
    public VehicleSiteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view by inflating the row item xml.
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle, parent, false);
        v.setOnClickListener(onClickListener);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);

        // Set strings to the views
        final TextView matricule = (TextView) holder.view.findViewById(R.id.Veh_Id);
        final TextView chauffeur = (TextView) holder.view.findViewById(R.id.Veh_Matricule);
        final TextView vitesse = (TextView) holder.view.findViewById(R.id.vitesse);
        final TextView temperature = (TextView) holder.view.findViewById(R.id.temperature);
        final TextView kh = (TextView) holder.view.findViewById(R.id.kh);
        final TextView celsius = (TextView) holder.view.findViewById(R.id.celsius);
        final ImageView imageViewImage = (ImageView) holder.view.findViewById(R.id.imageViewImage);
        chauffeur.setText(vehicles.get(position).chauffeur+"");
        matricule.setText(vehicles.get(position).matricule+"");

        //if (vehicles.get(position).vitesse!=0){
            vitesse.setText(vehicles.get(position).vitesse+"");
        //}else{
            //vitesse.setVisibility(View.INVISIBLE);
            //kh.setVisibility(View.INVISIBLE);
        //}

        if (vehicles.get(position).optionTemperature!=0){
            if (vehicles.get(position).temperature<vehicles.get(position).Occ_TemperatureMax && vehicles.get(position).temperature>vehicles.get(position).Occ_TemperatureMin)
            {
                temperature.setText(vehicles.get(position).temperature+"");
            }else if (vehicles.get(position).temperature>40){
                temperature.setText("Error");
                temperature.setTextColor(Color.RED);
                celsius.setVisibility(View.INVISIBLE);
            }else{
                temperature.setText(vehicles.get(position).temperature+"");
                temperature.setTextColor(Color.RED);
                celsius.setTextColor(Color.RED);
            }
        }else{
            temperature.setVisibility(View.INVISIBLE);
            celsius.setVisibility(View.INVISIBLE);
        }
        try {
            switch (vehicles.get(position).genre){
                case "VOITURE": imageViewImage.setImageResource(R.drawable.carblue);
                    break;
                case "S-Remorque FRIGO": imageViewImage.setImageResource(R.drawable.camionbleu);
                    break;
                case "CITERNE": imageViewImage.setImageResource(R.drawable.citernebleu);
                    break;
                default: imageViewImage.setImageResource(R.drawable.busbleu);
            }
        } catch (Exception e){
            imageViewImage.setVisibility(View.INVISIBLE);
        }



        LinearLayout item_post_item = (LinearLayout) holder.view.findViewById(R.id.item_post_item);
        item_post_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(context);

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.popup);

                try{

                    Button stopCarBtn = (Button) dialog.findViewById(R.id.stopCarBtn);

                    sharedPreferences = context.getSharedPreferences("VALUES", Context.MODE_PRIVATE);
                    String Ugr_Nom = sharedPreferences.getString("Ugr_Nom","Ugr_Nom");
                    Log.e("Ugr_Nom",Ugr_Nom);

                    if (Ugr_Nom.equals("admin")){
                        stopCarBtn.setVisibility(View.VISIBLE);
                        stopCarBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions((Activity) context,new String[]{Manifest.permission.SEND_SMS},1);
                                String phoneNo = "55122282";
                                String msg = "sms core test";
                                try {

                                    String SENT = "sent";
                                    String DELIVERED = "delivered";

                                    Intent sentIntent = new Intent(SENT);
                                    /*Create Pending Intents*/
                                    PendingIntent sentPI = PendingIntent.getBroadcast(
                                            context, 0, sentIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT);

                                    Intent deliveryIntent = new Intent(DELIVERED);

                                    PendingIntent deliverPI = PendingIntent.getBroadcast(
                                            context, 0, deliveryIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT);
                                    /* Register for SMS send action */
                                    context.registerReceiver(new BroadcastReceiver() {

                                        @Override
                                        public void onReceive(Context context, Intent intent) {
                                            String result = "";

                                            switch (getResultCode()) {

                                                case Activity.RESULT_OK:
                                                    result = "Transmission successful";
                                                    break;
                                                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                    result = "Transmission failed";
                                                    break;
                                                case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                    result = "Radio off";
                                                    break;
                                                case SmsManager.RESULT_ERROR_NULL_PDU:
                                                    result = "No PDU defined";
                                                    break;
                                                case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                    result = "No service";
                                                    break;
                                            }

                                            Toast.makeText(context, result,
                                                    Toast.LENGTH_LONG).show();
                                        }

                                    }, new IntentFilter(SENT));
                                    /* Register for Delivery event */
                                    context.registerReceiver(new BroadcastReceiver() {

                                        @Override
                                        public void onReceive(Context context, Intent intent) {
                                            Toast.makeText(context, "Deliverd",
                                                    Toast.LENGTH_LONG).show();
                                        }

                                    }, new IntentFilter(DELIVERED));

                                    /*Send SMS*/
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(phoneNo, null, msg, sentPI,
                                            deliverPI);
                                } catch (Exception ex) {
                                    Toast.makeText(context,
                                            ex.getMessage().toString(), Toast.LENGTH_LONG)
                                            .show();
                                    ex.printStackTrace();
                                }
                            }
                        });

                    }



                    Button realTimeBtn = (Button) dialog.findViewById(R.id.realTimeBtn);
                    Button historyBtn = (Button) dialog.findViewById(R.id.historyBtn);
                    realTimeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent myIntent = new Intent(context, MapsActivity.class);
                            myIntent.putExtra("matricule",vehicles.get(position).matricule);
                            myIntent.putExtra("chauffeur",vehicles.get(position).chauffeur);
                            myIntent.putExtra("vitesse",vehicles.get(position).vitesse);

                            myIntent.putExtra("realTime","true");
                            context.startActivity(myIntent);
                            dialog.dismiss();
                        }
                    });
                    historyBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent myIntent = new Intent(context, MapsActivity.class);
                            myIntent.putExtra("idBoitier",vehicles.get(position).idBoitier);

                            myIntent.putExtra("matriculeTrace",vehicles.get(position).matricule);
                            myIntent.putExtra("chauffeurTrace",vehicles.get(position).chauffeur);

                            myIntent.putExtra("vitesse",vehicles.get(position).vitesse);

                            myIntent.putExtra("trace","true");
                            context.startActivity(myIntent);
                            dialog.dismiss();
                        }
                    });


                } catch (Exception e){
                }

                dialog.show();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return vehicles.size();
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

package com.ayzconsultores.diegoshouse.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.ayzconsultores.diegoshouse.R;

public class LoadingDialog {

    private Activity activity;
    private AlertDialog alertDialog;

    LoadingDialog(Activity myactivity){
        activity = myactivity;
    }

    void showLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading, null));
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
    }

    void ocultarDialog(){
        alertDialog.dismiss();
    }

}

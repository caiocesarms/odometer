package com.example.caioblade.odometer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private OdometerService odometer;
    private boolean bound = false;

    private final int PERMISSION_REQUEST_CODE = 698;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            OdometerService.OdometerBinder odometerBinder = (OdometerService.OdometerBinder) binder;
            odometer = odometerBinder.getOdometer();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        displayDistance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, OdometerService.PERMISSION_STRING) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{OdometerService.PERMISSION_STRING}, PERMISSION_REQUEST_CODE);
        } else {
            Intent intent = new Intent(this, OdometerService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, OdometerService.class);
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
                } else {
                    //Code to run if permission was denied
                    System.out.println("ERRO NA REQUISIÇÃO DE PERMISSÃO");
                }
            }
        }
    }

    private void displayDistance() {
        final TextView distanceView = (TextView) findViewById(R.id.distance);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                double distance = 0.0;
                if (bound && odometer != null) {
                    distance = odometer.getDistance();
                }
                String distanceStr = String.format(Locale.getDefault(), "%f meters", distance);
                distanceView.setText(distanceStr);
                handler.postDelayed(this, 1000);
            }
        });
    }
}

package com.breakevenpoint.tracker.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.breakevenpoint.tracker.R;
import com.breakevenpoint.tracker.billing.IabBroadcastReceiver;

public class DonateActivity extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        initView();

    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_donate);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public void receivedBroadcast() {

    }
}

package com.sm.supere.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.sm.supere.R;
import com.sm.supere.ui.activity.MqttActivity;
import com.sm.supere.ui.activity.ProviderActivity;
import com.sm.supere.ui.activity.SurfaceActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onMQClick(View view) {
        startActivity(new Intent(this, MqttActivity.class));
    }

    public void onProviderClick(View view) {
        startActivity(new Intent(this, ProviderActivity.class));
    }

    public void onSurfaceClick(View view) {
        startActivity(new Intent(this, SurfaceActivity.class));
    }
}
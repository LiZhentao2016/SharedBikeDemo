package com.devil.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.devil.mapdemo.function.GetMyLocationActivity;
import com.devil.mapdemo.function.MyBikeActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.mainMyLocation).setOnClickListener(this);
        findViewById(R.id.mainMyBike).setOnClickListener(this);
    }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mainMyLocation:
                    startActivity(new Intent(this, GetMyLocationActivity.class));
                    break;
                case R.id.mainMyBike:
                    startActivity(new Intent(this, MyBikeActivity.class));
                    break;
            }
        }

}

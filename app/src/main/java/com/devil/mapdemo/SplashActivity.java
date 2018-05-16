package com.devil.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Administrator on 2018/3/21/021.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
        try{
            Thread.currentThread().sleep(2000);
        }catch(InterruptedException ie){
            ie.printStackTrace();
        } */
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
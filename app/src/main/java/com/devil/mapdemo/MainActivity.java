package com.devil.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.devil.mapdemo.function.MyBikeActivity;
import com.devil.mapdemo.history.MainHistoryActivity;
import com.devil.mapdemo.message.MainMessageActivity;
import com.devil.mapdemo.userinfo.UserMainActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //findViewById(R.id.mainMyLocation).setOnClickListener(this);
        findViewById(R.id.mainMyBike).setOnClickListener(this);
        findViewById(R.id.mainUser).setOnClickListener(this);
        findViewById(R.id.mainMessage).setOnClickListener(this);
        findViewById(R.id.mainHistory).setOnClickListener(this);
    }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //case R.id.mainMyLocation:
                //    startActivity(new Intent(this, GetMyLocationActivity.class));
                //    break;
                case R.id.mainMyBike:
                    startActivity(new Intent(this, MyBikeActivity.class));
                    break;
                case R.id.mainUser:
                    startActivity(new Intent(this, UserMainActivity.class));
                    break;
                case R.id.mainMessage:
                    startActivity(new Intent(this, MainMessageActivity.class));
                    break;
                case  R.id.mainHistory:
                    startActivity(new Intent(this, MainHistoryActivity.class));
                    break;
            }
        }

}

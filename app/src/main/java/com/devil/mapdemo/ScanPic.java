package com.devil.mapdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.shenyuanqing.zxingsimplify.zxing.Activity.CaptureActivity;

public class ScanPic extends AppCompatActivity {
    private String stateTitile;
    private Context mContext;
    private Activity mActivity;
    private static final int REQUEST_SCAN = 0;

    TextView textScan;
    Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_pic);
        mContext = this;
        mActivity = this;

        textScan=findViewById(R.id.textScan);
        textScan.setText("自行车编码：空");
        scanButton=findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textScan.getText()=="自行车编码：空")
                {
                    Toast.makeText(mContext, "自行车编码：空！ 请扫码！", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(mContext, "请稍等，正在开锁中！！！", Toast.LENGTH_LONG).show();
                    Toast.makeText(mContext, "已成开锁！！！", Toast.LENGTH_LONG).show();
                    textScan.setText("自行车编码：空");
                }
            }
        });
        init();
    }

    private void init() {
        findViewById(R.id.ll_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRuntimeRight();
            }
        });
    }

    /**
     * 获得运行时权限
     */
    private void getRuntimeRight() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            jumpScanPage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    jumpScanPage();
                } else {
                    Toast.makeText(mContext, "拒绝", Toast.LENGTH_LONG).show();
                }
            default:
                break;
        }
    }

    /**
     * 跳转到扫码页
     */
    private void jumpScanPage() {
        startActivityForResult(new Intent(this, CaptureActivity.class),REQUEST_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SCAN && resultCode == RESULT_OK){
            stateTitile=data.getStringExtra("barCode");
            textScan.setText("自行车编号： "+stateTitile);
            Toast.makeText(mContext,"自行车编号： "+stateTitile,Toast.LENGTH_LONG).show();
        }
    }
}


/**


 */
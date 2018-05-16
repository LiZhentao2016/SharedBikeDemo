package com.devil.mapdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.shenyuanqing.zxingsimplify.zxing.Activity.CaptureActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ScanPic extends AppCompatActivity {
    private String stateTitile;
    private Context mContext;
    private Activity mActivity;
    private static final int REQUEST_SCAN = 0;

    static final String db_name="testDB";
    static final String tb_name="user";
    SQLiteDatabase db;
    String user_name=null;

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

        //确保数据库和表的存在
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("SELECT * FROM " + tb_name, null);
        c.moveToFirst();
        user_name = c.getString(0);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textScan.getText()=="自行车编码：空")
                {
                    Toast.makeText(mContext, "自行车编码：空！ 请扫码！", Toast.LENGTH_LONG).show();
                }
                else
                {
                    textScan.setText("自行车编码：空");

                    Log.v("stateCode", "上传开锁的车编号");
                    //把自行车编码发给服务器
                    sendRequestConnection();
                }
            }
        });
        init();
    }

    //网络传输部分
    private void sendRequestConnection(){
        String stateCode=null;

        new Thread(new Runnable() {
            private String HOST="67.209.186.100";//"67.209.186.100";//IP地址需要根据现场环境进行修改
            private int PORT=10000;
            private int stateCode;
            private String message;
            private PrintWriter printWriter;
            private BufferedReader in;

            @Override
            public void run()
            {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(HOST, PORT), 10000);//设置连接请求超时时间10 s
                    socket.setSoTimeout(10000);//设置读操作超时时间10 s
                    //Socket socket = new Socket(HOST, PORT);
                    //socket.setSoTimeout(10000);

                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));//输入流

                    printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);

                    Log.v("stateCode", "发送开始");
                    //向服务器请求验证登录信息，login_validate 为请求标志
                    printWriter.println("bike_num"+" "+user_name+" "+stateTitile);
                    Log.v("stateCode", "发送结束");

                    Log.v("stateCode", "接收开始");
                    message=in.readLine();
                    if(message==null){
                        //网络意外断开
                        stateCode=0;
                    }else {
                        message = message.trim();
                        Log.v("stateCode", message);
                        stateCode=Integer.valueOf(message);
                    }

                    Log.v("stateCode", "接收结束");


                    //返回状态码，0为失败， 1为成功
                    if(stateCode==0){
                        Log.v("stateCode", "失败");
                        showError();
                    }else{
                        Log.v("stateCode", "成功");

                        showSuccess();
                    }
                } catch (IOException e) {
                    showTimeOut();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showSuccess()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, "请稍等，正在开锁中！！！", Toast.LENGTH_LONG).show();
                Toast.makeText(mContext, "已成功开锁！！！", Toast.LENGTH_LONG).show();

                //把自行车编码返回上一个activity
                Intent intent=new Intent();
                intent.putExtra("data_return", stateTitile);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void showError()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ScanPic.this, "错误，该编号自行车不存在或已被开启或存在欠费或已有订单未完成！！！", Toast.LENGTH_LONG).show();

            }
        });
    }

    private void showTimeOut()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ScanPic.this, "网络连接错误！！！", Toast.LENGTH_LONG).show();
            }
        });
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

            try {
                Integer.parseInt(stateTitile);
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(ScanPic.this, "错误，自行车编号非标准！！！", Toast.LENGTH_LONG).show();
                return;
            }

            textScan.setText("自行车编号： "+stateTitile);
            Toast.makeText(mContext,"自行车编号： "+stateTitile,Toast.LENGTH_LONG).show();
        }
    }
}

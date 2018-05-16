package com.devil.mapdemo.history;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.devil.mapdemo.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.NumberFormat;

public class MainOrderActivity extends AppCompatActivity {
    TextView text_view_1;
    TextView text_view_2;
    TextView text_view_3;
    TextView text_view_4;
    TextView text_view_5;

    Button button;

    Long hour, minute;
    double money;

    Long deta;

    String []message=new String[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_order);

        text_view_1 = findViewById(R.id.history_text1);
        text_view_2 = findViewById(R.id.history_text2);
        text_view_3 = findViewById(R.id.history_text3);
        text_view_4 = findViewById(R.id.history_text4);
        text_view_5 = findViewById(R.id.history_text5);

        button=findViewById(R.id.return_bike_button);

        Intent intent=getIntent();
        String data=intent.getStringExtra("extra_data_time");//用户名   订单号    用时时间
        Log.v("extra_data_time", data);//打印接收的数据

        message=data.split("\\s+");

        text_view_1.setText(message[0]);
        text_view_2.setText(message[1]+"#");

        try {
            deta=Long.parseLong(message[2]);

            if (deta<0)
            {
                text_view_3.setText("空");
                text_view_4.setText("空");
                text_view_5.setText("订单未完成！！！");
            }//没有完成订单

            if (deta==0)
            {
                text_view_3.setText("1分钟");
                text_view_4.setText("0.1元");
                text_view_5.setText("订单已完成！！！");
                button.setVisibility(View.GONE);
            }//下单后直接买单

            if (deta>0)
            {
                deta=deta/1000/60;//总共分钟
                hour=deta/60;     //小时
                minute=deta%60;   //分钟

                text_view_3.setText(hour+"小时"+minute+"分钟");

                if(minute<=10)
                {money=hour+minute*0.1;}
                else{money=hour+1;}

                NumberFormat nbf=NumberFormat.getInstance();
                nbf.setMinimumFractionDigits(2);
                String c = nbf.format(money);

                text_view_4.setText(c+"元");
                text_view_5.setText("订单已完成！！！");

                button.setVisibility(View.GONE);
            }//下单后直接买单

        }catch (Exception e){e.printStackTrace();}

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //还车操作
                sendRequestConnection();//发送用户名

            }
        });

    }


    //网络传输部分
    private void sendRequestConnection(){

        new Thread(new Runnable() {
            private String HOST="67.209.186.100";//"67.209.186.100";//IP地址需要根据现场环境进行修改
            private int PORT=10000;
            private int stateCode;
            private PrintWriter printWriter;
            private BufferedReader in;
            private String message0;

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
                    //向服务器请求  结账， order_finished 为请求标志    订单号
                    printWriter.println("order_finished"+" "+message[1]);
                    Log.v("stateCode", "发送结束");


                    Log.v("stateCode", "接收开始");
                    try {
                        message0=in.readLine().trim();
                        stateCode=Integer.parseInt(message0);

                        //返回状态码，0为失败， 1为成功
                        if(stateCode==0){
                            Log.v("stateCode", "失败");
                            showError();
                        }else{
                            Log.v("stateCode", "成功");

                            showSuccess();////////////////////////
                            Log.v("stateCode", message0);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    Log.v("stateCode", "接收结束");
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
                //该用户可以结账，  该订单为未完成订单
                Toast.makeText(MainOrderActivity.this, "结账成功！！！", Toast.LENGTH_LONG).show();

                Intent intent2=new Intent();
                intent2.putExtra("data_return", "1");//0表示不触发    1表示触发操作
                //   1  重新加载
                setResult(RESULT_OK, intent2);
                finish();
            }
        });
    }

    private void showError()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //该用户不可以结账，  该订单为已经完成订单
                Toast.makeText(MainOrderActivity.this, "结账失败！！！", Toast.LENGTH_LONG).show();

            }
        });
    }

    private void showTimeOut()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainOrderActivity.this, "网络连接错误！！！", Toast.LENGTH_LONG).show();
            }
        });
    }

}

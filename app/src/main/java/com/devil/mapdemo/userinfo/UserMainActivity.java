package com.devil.mapdemo.userinfo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

class User_Info{
    String user_name;
    String user_telephone;
    String user_email;
    String user_money;
    String hours;
    String minutes;
    User_Info(String user_name, String user_telephone, String user_email,
              String user_money, String user_hours, String user_minutes){
        this.user_name=user_name;
        this.user_telephone=user_telephone;
        this.user_email=user_email;
        this.user_money=user_money;
        this.hours=user_hours;
        this.minutes=user_minutes;
    }
}

public class UserMainActivity extends AppCompatActivity {
    static final String db_name="testDB";
    static final String tb_name="user";
    String  []receive_message=new String [5];
    String user_name;
    SQLiteDatabase db;
    User_Info user_info;

    TextView user_name_string;
    TextView user_telephon_string;
    TextView user_email_string;
    TextView user_money_string;
    TextView user_hours_string;
    TextView user_minutes_string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        user_name_string=(TextView)findViewById(R.id.user_name);
        user_telephon_string=(TextView)findViewById(R.id.user_phone);
        user_email_string=(TextView)findViewById(R.id.user_email);
        user_money_string=(TextView)findViewById(R.id.user_money);
        user_hours_string=(TextView)findViewById(R.id.user_hours);
        user_minutes_string=(TextView)findViewById(R.id.user_minutes);

        user_name_string.setText("");
        user_telephon_string.setText("");
        user_email_string.setText("");
        user_money_string.setText("");
        user_hours_string.setText("");
        user_minutes_string.setText("");

        findViewById(R.id.user_button_1).setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        Intent intent = new Intent(UserMainActivity.this, MakeMoneyActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
        });

        findViewById(R.id.user_button_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserMainActivity.this, UserInfoActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //确保数据库和表的存在
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("SELECT * FROM " + tb_name, null);
        c.moveToFirst();
        user_name = c.getString(0);

        //上传用户名，并下载用户的具体信息
        sendRequestConnection(user_name);
    }



    //网络传输部分
    private void sendRequestConnection(final String user_name){

        new Thread(new Runnable() {
            private String HOST="67.209.186.100";//"67.209.186.100";//IP地址需要根据现场环境进行修改
            private int PORT=10000;
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
                    //向服务器发送用户名，user_name 为请求标志
                    printWriter.println("user_name"+" "+user_name);
                    Log.v("stateCode", "发送结束");

                    Log.v("stateCode", "接收开始");
                    message=null;

                        message=in.readLine();

                        if(message!=null)
                        {
                            message=message.trim();
                            Log.v("message", message+"!");
                            receive_message=message.split("\\s+");
                            Log.v("message", receive_message[0]+":"+receive_message[1]+":"+receive_message[2]+receive_message[3]+":"+receive_message[4]);
                            user_info=new User_Info(
                                    user_name,
                                    receive_message[0],
                                    receive_message[1],
                                    receive_message[2],
                                    receive_message[3],
                                    receive_message[4]
                            );
                            Log.v("stateCode", "接收结束");
                            showSuccess();
                        }
                        else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(UserMainActivity.this, "网络中断！！！", Toast.LENGTH_LONG).show();
                                }
                            });
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
                //Toast.makeText(LoginActivity.this, "成功登入！！！", Toast.LENGTH_LONG).show();
                //Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                //startActivity(intent);
                //finish();
                //////////////////待添加功能
                user_name_string.setText(user_info.user_name);
                user_telephon_string.setText(user_info.user_telephone);
                user_email_string.setText(user_info.user_email);
                user_money_string.setText(user_info.user_money);
                user_hours_string.setText(user_info.hours);
                user_minutes_string.setText(user_info.minutes);
            }
        });
    }


    private void showTimeOut()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(UserMainActivity.this, "网络连接错误！！！", Toast.LENGTH_LONG).show();
            }
        });
    }

}//类结束
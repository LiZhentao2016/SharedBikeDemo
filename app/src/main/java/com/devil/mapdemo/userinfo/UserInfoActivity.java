package com.devil.mapdemo.userinfo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

public class UserInfoActivity extends AppCompatActivity {
    EditText user_info_phone;
    EditText user_info_email;
    TextView user_name_view;
    String phone_string;
    String email_string;

    static final String db_name="testDB";
    static final String tb_name="user";
    SQLiteDatabase db;
    String user_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        user_name_view=(TextView)findViewById(R.id.user_name);
        user_info_phone= (EditText) findViewById(R.id.user_info_phone);
        user_info_email= (EditText) findViewById(R.id.user_info_email);
        user_name_view.setText("");
        user_info_phone.setText("");
        user_info_email.setText("");


        //确保数据库和表的存在
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("SELECT * FROM " + tb_name, null);
        c.moveToFirst();
        user_name = c.getString(0);

        user_name_view.setText(user_name);

        findViewById(R.id.user_infor_button_1).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        user_info_phone.setText("");
                        user_info_email.setText("");
                        Toast.makeText(UserInfoActivity.this, "输入已取消！！！", Toast.LENGTH_LONG).show();

                    }
                }
        );

        findViewById(R.id.user_info_button_2).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        phone_string=user_info_phone.getText().toString().trim();
                        email_string=user_info_email.getText().toString().trim();

                        if( (phone_string.length()>20) )
                        {
                            Toast.makeText(UserInfoActivity.this, "输入字符不允许超出20个 ！！！", Toast.LENGTH_LONG).show();
                            user_info_phone.setText("");
                            return;
                        }

                        if( (phone_string.length()==0) )
                        {
                            Toast.makeText(UserInfoActivity.this, "输入字符不允许为空！！！", Toast.LENGTH_LONG).show();
                            user_info_phone.setText("");
                            return;
                        }

                        if( (email_string.length()>20) )
                        {
                            Toast.makeText(UserInfoActivity.this, "输入字符不允许超出20个 ！！！", Toast.LENGTH_LONG).show();
                            //user_info_email.setText("");
                            return;
                        }

                        if( (email_string.length()==0) )
                        {
                            Toast.makeText(UserInfoActivity.this, "输入字符不允许为空 ！！！", Toast.LENGTH_LONG).show();
                            //user_info_email.setText("");
                            return;
                        }

                        //上传修改后的消息
                        sendRequestConnection(user_name, phone_string, email_string);
                    }
                }
        );
    }


    //网络传输部分
    private void sendRequestConnection(final String user_name, final String phone_string, final String email_string){

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
                    //向服务器发送 修改后的信息，user_info_changed 为请求标志
                    printWriter.println("user_info_changed"+" "+user_name+" "+phone_string+" "+email_string);
                    Log.v("stateCode", "发送结束");

                    Log.v("stateCode", "接收开始");
                    Log.v("stateCode", "接收结束");
                    showSuccess();
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
                Toast.makeText(UserInfoActivity.this, "信息修改成功！！！", Toast.LENGTH_LONG).show();
                user_info_phone.setText("");
                user_info_email.setText("");

            }
        });
    }


    private void showTimeOut()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(UserInfoActivity.this, "网络连接错误！！！", Toast.LENGTH_LONG).show();
            }
        });
    }


}

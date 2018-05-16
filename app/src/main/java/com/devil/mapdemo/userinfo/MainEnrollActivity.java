package com.devil.mapdemo.userinfo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class MainEnrollActivity extends AppCompatActivity {
    EditText enroll_name;
    EditText enroll_phone;
    EditText enroll_email;
    EditText enroll_password;
    EditText enroll_password_sure;

    String enroll_name_string;
    String enroll_phone_string;
    String enroll_email_string;
    String enroll_password_string;
    String enroll_password_sure_string;

    Button enroll_button_1;
    Button enroll_button_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_enroll);

        enroll_name=findViewById(R.id.enroll_name);
        enroll_phone=findViewById(R.id.enroll_phone);
        enroll_email=findViewById(R.id.enroll_email);
        enroll_password=findViewById(R.id.enroll_password);
        enroll_password_sure=findViewById(R.id.enroll_password_sure);

        enroll_button_1=findViewById(R.id.enroll_button_1);
        enroll_button_2=findViewById(R.id.enroll_button_2);

        enroll_button_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enroll_name.setText("");
                enroll_phone.setText("");
                enroll_email.setText("");
                enroll_password.setText("");
                enroll_password_sure.setText("");
            }
        });

        enroll_button_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enroll_name_string =enroll_name.getText().toString();
                enroll_phone_string =enroll_phone.getText().toString();
                enroll_email_string = enroll_email.getText().toString();
                enroll_password_string = enroll_password.getText().toString();
                enroll_password_sure_string = enroll_password_sure.getText().toString();

                if(enroll_name_string==null||enroll_name_string.equals("")||
                        enroll_phone_string==null||enroll_phone_string.equals("")||
                        enroll_email_string==null||enroll_email_string.equals("")||
                        enroll_password_string==null||enroll_password_string.equals("")||
                        enroll_password_sure_string==null||enroll_password_sure_string.equals(""))
                {
                    Toast.makeText(MainEnrollActivity.this, "输入内容不能为空！！！", Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    enroll_name_string = enroll_name_string.trim();
                    enroll_phone_string =enroll_phone_string.trim();
                    enroll_email_string = enroll_email_string.trim();
                    enroll_password_string = enroll_password_string.trim();
                    enroll_password_sure_string = enroll_password_sure_string.trim();
                }

                if(enroll_name_string==null||enroll_name_string.equals("")||
                        enroll_phone_string==null||enroll_phone_string.equals("")||
                        enroll_email_string==null||enroll_email_string.equals("")||
                        enroll_password_string==null||enroll_password_string.equals("")||
                        enroll_password_sure_string==null||enroll_password_sure_string.equals(""))
                {
                    Toast.makeText(MainEnrollActivity.this, "输入内容不能为空！！！", Toast.LENGTH_LONG).show();
                    return;
                }

                if(enroll_name_string.contains(" ")||
                        enroll_phone_string.contains(" ")||
                        enroll_email_string.contains(" ")||
                        enroll_password_string.contains(" ")||
                        enroll_password_sure_string.contains(" "))
                {
                    Toast.makeText(MainEnrollActivity.this, "输入内容不能含空格！！！", Toast.LENGTH_LONG).show();
                    return;
                }

                if (enroll_password_string.equals(enroll_password_sure_string))
                {
                    sendRequestConnection();
                }else{
                    Toast.makeText(MainEnrollActivity.this, "确认密码不正确！！！", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

    }

    //网络传输部分
    private void sendRequestConnection(){

        new Thread(new Runnable() {
            private String HOST="67.209.186.100";//"67.209.186.100";//IP地址需要根据现场环境进行修改
            private int PORT=10000;
            private int stateCode;
            private String message=null;
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
                    //向服务器请求  注册信息验证，enroll_validate 为请求标志
                    printWriter.println("enroll_validate"+" "+enroll_name_string+" "+
                    enroll_phone_string+" "+enroll_email_string+" "+enroll_password_string);
                    Log.v("stateCode", "发送结束");

                    Log.v("stateCode", "接收开始");
                    message=in.readLine();

                    if(message==null)
                    {
                        stateCode=0;
                        Log.v("stateCode", "网络连接中断");
                    }
                    else{
                        message=message.trim();
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
                Toast.makeText(MainEnrollActivity.this, "成功注册！！！", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void showError()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainEnrollActivity.this, "用户名已存在！！！", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showTimeOut()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainEnrollActivity.this, "网络连接错误！！！", Toast.LENGTH_LONG).show();
            }
        });
    }

}

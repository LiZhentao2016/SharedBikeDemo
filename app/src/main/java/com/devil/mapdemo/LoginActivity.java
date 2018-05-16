package com.devil.mapdemo;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.devil.mapdemo.userinfo.MainEnrollActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {

    EditText loginEditTextView1;
    EditText loginEditTextView2;
    Button loginButton1;
    Button loginButton2;

    static final String db_name="testDB";
    static final String tb_name="user";
    SQLiteDatabase db;

    String c_string_0=null;
    String c_string_1=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginEditTextView1 = (EditText) findViewById(R.id.loginEditText1);
        loginEditTextView2 = (EditText) findViewById(R.id.loginEditText2);
        loginButton1 = (Button) findViewById(R.id.loginButton1);
        loginButton2 = (Button) findViewById(R.id.loginButton2);


        //确保数据库和表的存在
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);

        String createTable = "CREATE TABLE IF NOT EXISTS " + tb_name +
                "(userId VARCHAR(20)," +
                "userPassword VARCHAR(20))";
        db.execSQL(createTable);

        Cursor c = db.rawQuery("SELECT * FROM " + tb_name, null);

        if (c.getCount() == 0) {
            //表里没有数据, 不进行任何操作
        } else {
            c.moveToFirst();
            c_string_0 = c.getString(0);
            c_string_1 = c.getString(1);
            Log.v("login", c_string_0+" "+c_string_1);
            loginEditTextView1.setText(c_string_0);
            loginEditTextView2.setText(c_string_1);
        }

        findViewById(R.id.enroll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainEnrollActivity.class);
                startActivity(intent);
            }
        });

        //取消按键的Click事件
        loginButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEditTextView1.setText("");
                loginEditTextView2.setText("");
                Toast.makeText(LoginActivity.this, "输入内容已取消！！！", Toast.LENGTH_LONG).show();

            }
        });

        //确认事件的Click事件
        loginButton2.setOnClickListener(new View.OnClickListener() {
            private String eDitText1;
            private String eDitText2;


            @Override
            public void onClick(View v) {
                c_string_0 = loginEditTextView1.getText().toString().trim();
                c_string_1 = loginEditTextView2.getText().toString().trim();

                if ((c_string_0.isEmpty()) || (c_string_1.isEmpty())) {
                    Toast.makeText(LoginActivity.this, "用户名和密码不能为空！！！", Toast.LENGTH_LONG).show();
                }else if ((c_string_0.length()>20)||c_string_1.length()>20)
                {
                    Toast.makeText(LoginActivity.this, "用户名和密码输入长度不能超过20字符！！！", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(LoginActivity.this, "进行网络验证中！！！", Toast.LENGTH_LONG).show();
                    //网络请求
                    sendRequestConnection();
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
                    //向服务器请求验证登录信息，login_validate 为请求标志
                    printWriter.println("login_validate"+" "+c_string_0+" "+c_string_1);
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

                        //清空数据库表内容
                        db.execSQL("delete from "+tb_name);
                        //将数据写入数据库
                        addData(c_string_0, c_string_1);
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
                Toast.makeText(LoginActivity.this, "成功登入！！！", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showError()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, "用户名或密码错误！！！", Toast.LENGTH_LONG).show();
                loginEditTextView1.setText("");
                loginEditTextView2.setText("");
            }
        });
    }

    private void showTimeOut()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, "网络连接错误！！！", Toast.LENGTH_LONG).show();
            }
        });
    }


    //addData 已搞定
    private void addData(String userId, String userPassword){
        ContentValues cv=new ContentValues(2);
        cv.put("userId", userId);
        cv.put("userPassword", userPassword);
        db.insert(tb_name, null, cv);
    }

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


}
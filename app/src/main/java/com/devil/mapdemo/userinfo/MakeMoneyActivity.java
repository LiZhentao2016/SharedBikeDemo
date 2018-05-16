package com.devil.mapdemo.userinfo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

public class MakeMoneyActivity extends AppCompatActivity {
    EditText money_input;
    String user_name;
    String money_input_string;

    static final String db_name="testDB";
    static final String tb_name="user";
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_money);

        money_input=(EditText)findViewById(R.id.money_add);

        //取出用户的用户名
        //确保数据库和表的存在
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);

        Cursor c = db.rawQuery("SELECT * FROM " + tb_name, null);

        c.moveToFirst();
        user_name = c.getString(0);


        findViewById(R.id.money_button_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                money_input.setText("");
                Toast.makeText(MakeMoneyActivity.this, "输入取消！！！", Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.money_button_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                money_input_string = money_input.getText().toString().trim();
                double money_double;
                if(money_input_string.isEmpty())
                {
                    Toast.makeText(MakeMoneyActivity.this, "充值失败，输入的金额不能为空！！！", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    money_double=Double.parseDouble(money_input_string);
                    if(money_double>100)
                    {
                        Toast.makeText(MakeMoneyActivity.this, "充值失败，输入的金额不能大于 100 元！！！", Toast.LENGTH_LONG).show();
                        money_input.setText("");
                        return;
                    }
                } catch (Exception e) {
                    Toast.makeText(MakeMoneyActivity.this, "请输入标准格式的金额！！！", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    return;
                }

                //充值金额上传
                sendRequestConnection(user_name, money_input_string);
            }
        });
    }

    //网络传输部分
    private void sendRequestConnection(final String user_name, final String money_input_string){

        new Thread(new Runnable() {
            private String HOST="67.209.186.100";//"67.209.186.100";//IP地址需要根据现场环境进行修改
            private int PORT=10000;
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

                    Log.v("stateCodeXXX", "发送开始");
                    //发送充值金额，money_add 为请求标志
                    printWriter.println("money_add"+" "+user_name+" "+money_input_string);
                    Log.v("stateCodeXXX", "发送结束");

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
                Toast.makeText(MakeMoneyActivity.this, "充值成功！！！", Toast.LENGTH_LONG).show();
                money_input.setText("");
            }
        });
    }

    private void showTimeOut()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MakeMoneyActivity.this, "网络连接错误！！！", Toast.LENGTH_LONG).show();
            }
        });
    }


}

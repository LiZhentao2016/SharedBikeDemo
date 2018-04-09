package com.devil.mapdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {

    EditText loginEditTextView1;
    EditText loginEditTextView2;
    Button loginButton1;
    Button loginButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginEditTextView1=(EditText) findViewById(R.id.loginEditText1);
        loginEditTextView2=(EditText) findViewById(R.id.loginEditText2);
        loginButton1=(Button)findViewById(R.id.loginButton1);
        loginButton2=(Button)findViewById(R.id.loginButton2);

        loginButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEditTextView1.setText("");
                loginEditTextView2.setText("");
            }
        });

        loginButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //sendRequestWithHttpURLConnection();
    }





    //网络传输部分
    private void sendRequestWithHttpURLConnection(){
        new Thread(new Runnable() {
            private String HOST="192.168.32.35";//IP地址需要根据现场环境进行修改
            private int PORT=9999;
            private PrintWriter printWriter;
            private BufferedReader in;
            private String message=null;
            @Override
            public void run()
            {
                try {
                    Socket socket = new Socket(HOST, PORT);
                    socket.setSoTimeout(60000);

                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));//输入流

                    printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);



                    printWriter.println("Hello!");
                    Log.v("hello0", "12345");

                    while(true){
                        Log.v("hello00", "123456");
                        message=in.readLine();
                        Log.v("hello000", message+"123456");

                       /* if((message=in.readLine())!=null){
                            Log.v("hello000", message);
                            showResponse(message);
                        }else{break;} */
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void showResponse(final String response)
    {
        Log.v("hello1", "12345");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //loginTextView.setText(response);
            }
        });
    }
}

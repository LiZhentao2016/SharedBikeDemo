package com.devil.mapdemo.history;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.devil.mapdemo.R;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainHistoryActivity extends AppCompatActivity {

    static final String db_name="testDB";
    static final String tb_name="user";
    SQLiteDatabase db;
    private String c_string=null;

    private PullToRefreshListView refresh_lv;
    private List<Music2> list;
    private com.devil.mapdemo.history.MainHistoryActivity.DataAdapter adapter;
    String  []receive_message=new String [2];
    String message=null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode){
            case 123:
                if(resultCode==RESULT_OK){String returnedData=data.getStringExtra("data_return");
                    Log.v("extra_data_time", returnedData);//接收到的数据
                    int state;
                    state = Integer.parseInt(returnedData);
                    if(state==0)//不触发任何操作
                    {}
                    if (state==1)//触发操作，刷新
                    {
                        list.clear();
                        count=0;
                        loadData();
                    }

                }
                break;
            default:
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_history);

        //确保数据库和表的存在
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("SELECT * FROM " + tb_name, null);
        c.moveToFirst();
        c_string = c.getString(0);


        list = new ArrayList<>();

        refresh_lv = (PullToRefreshListView) findViewById(R.id.main_pull_refresh_lv2);





        //设置可上拉刷新和下拉刷新
        refresh_lv.setMode(PullToRefreshBase.Mode.BOTH);

        //设置刷新时显示的文本
        ILoadingLayout startLayout = refresh_lv.getLoadingLayoutProxy(true,false);
        startLayout.setPullLabel("正在下拉刷新...");
        startLayout.setRefreshingLabel("正在玩命加载中...");
        startLayout.setReleaseLabel("放开以刷新");


        ILoadingLayout endLayout = refresh_lv.getLoadingLayoutProxy(false,true);
        endLayout.setPullLabel("正在上拉刷新...");
        endLayout.setRefreshingLabel("正在玩命加载中...");
        endLayout.setReleaseLabel("放开以刷新");


        refresh_lv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                new com.devil.mapdemo.history.MainHistoryActivity.LoadDataAsyncTask(com.devil.mapdemo.history.MainHistoryActivity.this).execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                new com.devil.mapdemo.history.MainHistoryActivity.LoadDataAsyncTask(com.devil.mapdemo.history.MainHistoryActivity.this).execute();
            }
        });

        adapter = new com.devil.mapdemo.history.MainHistoryActivity.DataAdapter(com.devil.mapdemo.history.MainHistoryActivity.this,list);



        loadData();

    }

    private int count = 0;
    private void loadData(){

        if(count<10){
            Log.v("count", ""+count);
            sendRequestConnection(count);

            count=count+1;
        }

    }


    //网络传输部分
    private void sendRequestConnection(final int num){

        new Thread(new Runnable() {
            private String HOST="67.209.186.100";//"67.209.186.100";//IP地址需要根据现场环境进行修改
            private int PORT=10000;
            private int stateCode;
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
                    //向服务器请求消息推送，history_push_request 为请求标志
                    printWriter.println("history_push_request"+" "+num+" "+c_string);
                    Log.v("stateCode", "发送结束");


                    Log.v("stateCode", "接收开始");
                    try {
                        message=in.readLine().trim();
                        stateCode=Integer.parseInt(message);

                        //返回状态码，0为失败， 1为成功
                        if(stateCode==0){
                            Log.v("stateCode", "失败");
                            showError();
                        }else{
                            Log.v("stateCode", "成功");


                            for (int i=0;i<stateCode;i++)
                            {
                                message=in.readLine().trim();  //订单号
                                receive_message[0]=in.readLine().trim();//开始时间   结束时间
                                receive_message[1]=in.readLine().trim();//开始时间   结束时间
                                list.add(new Music2(message,
                                        receive_message[0],receive_message[1]));
                            }

                            showSuccess();////////////////////////
                            Log.v("stateCode", message);
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
                refresh_lv.setAdapter(adapter);
                Toast.makeText(com.devil.mapdemo.history.MainHistoryActivity.this, "消息刷新成功！！！", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showError()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(com.devil.mapdemo.history.MainHistoryActivity.this, "已经没有消息可以推送！！！", Toast.LENGTH_LONG).show();

            }
        });
    }

    private void showTimeOut()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(com.devil.mapdemo.history.MainHistoryActivity.this, "网络连接错误！！！", Toast.LENGTH_LONG).show();
            }
        });
    }


    /**
     * 异步下载任务
     *////////////////////////////////////////////////////////
    private static class LoadDataAsyncTask extends AsyncTask<Void,Void,String> {

        private com.devil.mapdemo.history.MainHistoryActivity mainActivity;

        public LoadDataAsyncTask(com.devil.mapdemo.history.MainHistoryActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
                mainActivity.loadData();
                return "seccess";
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 完成时的方法
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("seccess")){
                mainActivity.adapter.notifyDataSetChanged();
                mainActivity.refresh_lv.onRefreshComplete();//刷新完成
            }
        }
    }



    /**
     * 自定义适配器
     */
    private  class DataAdapter extends BaseAdapter {

        private Context context;
        private List<Music2> list;
        private Date date1;
        private Date date2;
        private Long deta=0L;

        public DataAdapter(Context context, List<Music2> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            if (list != null){
                return list.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            com.devil.mapdemo.history.MainHistoryActivity.DataAdapter.ViewHolder vh;
            final Music2 music = (Music2) getItem(position);

            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item2,parent,false);
                vh = new com.devil.mapdemo.history.MainHistoryActivity.DataAdapter.ViewHolder();
                vh.tv_title = (TextView) convertView.findViewById(R.id.order);
                vh.tv_time1 = (TextView) convertView.findViewById(R.id.time1);
                vh.tv_time2 = (TextView) convertView.findViewById(R.id.time2);


                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm");

                        Log.v("dateXXX", music.getTime1()+"12345");
                        Log.v("dateXXX", music.getTime2()+"12345");

                        try {
                            date1 = df.parse(music.getTime1());
                            date2 = df.parse(music.getTime2());

                            Log.v("dateXXX", date1+"123");
                            Log.v("dateXXX", date2+"123");

                            deta=date2.getTime()-date1.getTime();//将时间差发送给下个Activity
                        }catch (Exception e){e.printStackTrace();}
                        Log.v("dateXXX", "123"+deta);

                        if(deta==null)
                            deta=0L;

                        Intent intent=new Intent(com.devil.mapdemo.history.MainHistoryActivity.this, MainOrderActivity.class);
                        intent.putExtra("extra_data_time",c_string+" "+music.getTitle()+" "+deta);//毫秒
                        startActivityForResult(intent, 123);

                    }
                });

                convertView.setTag(vh);
            }else{
                vh = (com.devil.mapdemo.history.MainHistoryActivity.DataAdapter.ViewHolder) convertView.getTag();
            }
            vh.tv_title.setText(music.getTitle()+"#");
            vh.tv_time1.setText(music.getTime1());

            Log.v("1990", music.getTime2());

            if (music.getTime2().equals("1990-01-01  01:01"))
            {
                vh.tv_time2.setText("空");
            }else{
                vh.tv_time2.setText(music.getTime2());
            }
            return convertView;
        }

        class ViewHolder{
            TextView tv_title;
            TextView tv_time1;
            TextView tv_time2;
        }
    }

}


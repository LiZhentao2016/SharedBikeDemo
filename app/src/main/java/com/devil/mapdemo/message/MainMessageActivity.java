package com.devil.mapdemo.message;

import android.content.Context;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.List;

public class MainMessageActivity extends AppCompatActivity {

    private PullToRefreshListView refresh_lv;
    private List<Music> list;
    private MainMessageActivity.DataAdapter adapter;
    String  []receive_message=new String [2];
    String message=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_message);

        list = new ArrayList<>();

        refresh_lv = (PullToRefreshListView) findViewById(R.id.main_pull_refresh_lv);



       /* refresh_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music music=list.get(position);
                Toast.makeText(MainMessageActivity.this, "wxw123！！！", Toast.LENGTH_LONG).show();

            }
        }); */

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
                new MainMessageActivity.LoadDataAsyncTask(MainMessageActivity.this).execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                new MainMessageActivity.LoadDataAsyncTask(MainMessageActivity.this).execute();
            }
        });

        adapter = new DataAdapter(MainMessageActivity.this,list);



        loadData();

    }

    private int count = 0;
    private void loadData(){

        if(count<10){

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
                    //向服务器请求消息推送，message_push_request 为请求标志
                    printWriter.println("message_push_request"+" "+num);
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
                                message=in.readLine().trim();  //主题
                                receive_message=in.readLine().trim().split("\\s+");//发布时间  链接网址
                                list.add(new Music(message,
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
                Toast.makeText(MainMessageActivity.this, "消息刷新成功！！！", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showError()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainMessageActivity.this, "已经没有消息可以推送！！！", Toast.LENGTH_LONG).show();

            }
        });
    }

    private void showTimeOut()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainMessageActivity.this, "网络连接错误！！！", Toast.LENGTH_LONG).show();
            }
        });
    }


    /**
     * 异步下载任务
     *////////////////////////////////////////////////////////
    private static class LoadDataAsyncTask extends AsyncTask<Void,Void,String>{

        private MainMessageActivity mainActivity;

        public LoadDataAsyncTask(MainMessageActivity mainActivity) {
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
    private  class DataAdapter extends BaseAdapter{

        private Context context;
        private List<Music> list;

        public DataAdapter(Context context, List<Music> list) {
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
            ViewHolder vh;
            final Music music = (Music) getItem(position);

            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
                vh = new ViewHolder();
                vh.tv_title = (TextView) convertView.findViewById(R.id.item_title);
                vh.tv_singer = (TextView) convertView.findViewById(R.id.item_singer);

               convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v("web", "123");
                        Intent intent=new Intent(MainMessageActivity.this, MainWebActivity.class);
                        intent.putExtra("extra_data", music.getPublice_url());
                        startActivity(intent);
                    }
                });

                convertView.findViewById(R.id.list_item_message).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v("web", "123");
                        Intent intent=new Intent(MainMessageActivity.this, MainWebActivity.class);
                        intent.putExtra("extra_data", music.getPublice_url());
                        startActivity(intent);
                    }
                });


                /*((PullToRefreshListView)findViewById(R.id.main_pull_refresh_lv)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Music music=list.get(position);
                        Intent intent=new Intent(MainMessageActivity.this, MainWebActivity.class);
                        intent.putExtra("extra_data", music.getPublice_url());
                        startActivity(intent);

                    }
                }); */



                convertView.setTag(vh);
            }else{
                vh = (ViewHolder) convertView.getTag();
            }
            vh.tv_title.setText(music.getTitle());
            vh.tv_singer.setText(music.getSinger());
            return convertView;
        }

        class ViewHolder{
            TextView tv_title;
            TextView tv_singer;
        }
    }

}


package com.devil.mapdemo.function;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.devil.mapdemo.R;
import com.devil.mapdemo.ScanPic;
import com.devil.mapdemo.bean.BikeBean;
import com.devil.mapdemo.bikeutils.MapUtils;


public class MyBikeActivity extends AppCompatActivity implements View.OnClickListener {
    private MapUtils mapInstance=new MapUtils(39.083946, 121.813489);

    private MapView mapView;
    private AMap aMap;//地图控制器对象
    private UiSettings mUiSettings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bike);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        init();
    }

    /**
     * * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            mUiSettings = aMap.getUiSettings();
        }
        //设置logo位置
        mUiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_CENTER);
        mUiSettings.setZoomControlsEnabled(false);
        findViewById(R.id.ivInitLocation).setOnClickListener(this);
        findViewById(R.id.flRefresh).setOnClickListener(this);
        findViewById(R.id.tvScan).setOnClickListener(this);
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                BikeBean bikeBean = (BikeBean) marker.getObject();
                Toast.makeText(MyBikeActivity.this, "车编号： " + bikeBean.getId(), Toast.LENGTH_LONG).show();
                return true;
            }
        });
        setMakersOnMap();

    }

    public void setMakersOnMap() {
        aMap.clear();
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapInstance.getMyLocation(), 19));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mapInstance.getMyLocation());
        markerOptions.title("我的位置");
        markerOptions.visible(true);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_location));
        markerOptions.icon(bitmapDescriptor);
        markerOptions.draggable(false);
        aMap.addMarker(markerOptions).setObject(new BikeBean("我的位置", mapInstance.getMyLocation().latitude, mapInstance.getMyLocation().longitude));

        for (int i = 0; i < mapInstance.getNearByBike().size(); i++) {
            MarkerOptions markerOptions2 = new MarkerOptions();
            markerOptions2.visible(true);
            markerOptions2.title("i==" + i);
            markerOptions2.position(mapInstance.getNearByBike().get(i));
            if (i == 0) {
                markerOptions2.title("离我最近");
            }
            BitmapDescriptor bitmapDescriptor2 = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.station_big_icon));
            markerOptions2.icon(bitmapDescriptor2);

            markerOptions2.draggable(true);
            Marker marker = aMap.addMarker(markerOptions2);

            marker.setObject(new BikeBean("" + i, mapInstance.getNearByBike().get(i).latitude, mapInstance.getNearByBike().get(i).longitude));
            if (i == 0) {
                marker.showInfoWindow();
            }
        }
    }


    /**
     * 重新绘制加载地图
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 暂停地图的绘制
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }


    /**
     * 保存地图当前的状态方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 销毁地图
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivInitLocation:
                if (scal % 2 == 0) {
                    changeCamera(CameraUpdateFactory.zoomOut(), null);
                } else {
                    changeCamera(CameraUpdateFactory.zoomIn(), null);
                }
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapInstance.getMyLocation(), 19));
                break;
            case R.id.flRefresh:
                setMakersOnMap();
                break;
            case R.id.tvScan:
                Toast.makeText(getApplicationContext(),"点击", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(this, ScanPic.class);
                startActivity(intent);
                break;
        }
    }

    private int scal = 0;

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update, AMap.CancelableCallback callback) {
        scal++;
        aMap.animateCamera(update, 5, callback);
    }
}

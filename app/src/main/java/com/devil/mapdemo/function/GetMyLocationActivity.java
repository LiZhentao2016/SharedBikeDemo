package com.devil.mapdemo.function;

import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.devil.mapdemo.R;

import static com.amap.api.location.AMapLocationClientOption.AMapLocationMode.Hight_Accuracy;


public class GetMyLocationActivity extends AppCompatActivity implements LocationSource {
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    private double lat;
    private double lon;
    private MapView mapView;
    private AMap aMap;//地图控制器对象
    private UiSettings mUiSettings;

    OnLocationChangedListener mListener;

    boolean isFirstLoc = true;
    boolean isFirstLoc2 = true;

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        if( isFirstLoc2 ) {
            mListener = onLocationChangedListener;
            isFirstLoc2=false;}
        //Toast.makeText(getApplicationContext(), "定位到我的位置", Toast.LENGTH_SHORT).show();
        aMap.moveCamera(CameraUpdateFactory.zoomTo(18));//更新地图缩放程度

        //判断是否为android6.0系统版本，如果是，需要动态添加权限
        if (Build.VERSION.SDK_INT>=23){
            showContacts();
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
    }

    //6.0定位需要的权限申请
    public void showContacts(){
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION")
                != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(),"没有权限,请手动开启定位权限", Toast.LENGTH_SHORT).show();
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(this,new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 100);
        }
    }


    //Android6.0申请权限的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                    //
                } else {
                    // 没有获取到权限，做特殊处理
                    Toast.makeText(getApplicationContext(), "获取位置权限失败，请手动开启", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_my_location);

        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);//设置其为定位完成后的回调函数
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        init();
    }

    /**
     * * 初始化AMap类对象 aMap 地图控制器
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();//地图控制器对象
            aMap.setLocationSource(this);//设置了定位按键的监听,这里要实现LocationSource接口
            mUiSettings = aMap.getUiSettings();
        }
        //设置logo位置
        mUiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_CENTER);//高德地图标志的摆放位置
        mUiSettings.setZoomControlsEnabled(true);//地图缩放控件是否可见
        mUiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_BUTTOM);//地图缩放控件的摆放位置
        //aMap  为地图控制器对象
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//地图的定位标志是否可见
        aMap.setMyLocationEnabled(true);//地图上的定位标记marker是否显示，同时也使地图定位标志点击是否有效果
        setUpMap();
    }


    /**
     * 配置定位参数
     */
    private void setUpMap() {
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();

        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(Hight_Accuracy);

        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);

        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);

        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);

        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);//2秒一次定位

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);

        //启动定位
        mLocationClient.startLocation();
    }

    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    Log.v("getLocationType", ""+amapLocation.getLocationType() ) ;
                    lat = amapLocation.getLatitude();
                    lon = amapLocation.getLongitude();

                    Log.v("getAccuracy", ""+amapLocation.getAccuracy()+" 米");//获取精度信息
                    Log.v("joe", "lat :-- " + lat + " lon :--" + lon);
                    Log.v("joe", "Country : " + amapLocation.getCountry() + " province : " + amapLocation.getProvince() + " City : " + amapLocation.getCity() + " District : " + amapLocation.getDistrict());
                    //清空地图的绘图
                    aMap.clear();

                    if( isFirstLoc ) {
                        // 设置显示的焦点，即当前地图显示为当前位置
                        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 18));
                        //aMap.moveCamera(CameraUpdateFactory.zoomTo(18));//这两行代码和上一行代码等价
                        //aMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));

                        //点击定位按钮 能够将地图的中心移动到定位点
                        //监听器执行后定位值，为监听器设置参数
                        mListener.onLocationChanged(amapLocation);
                        isFirstLoc=false;
                    }

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(lat, lon));
                    markerOptions.title("我的位置");
                    markerOptions.snippet("I'm here");
                    markerOptions.visible(true);
                    BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_location));
                    markerOptions.icon(bitmapDescriptor);
                    markerOptions.draggable(true);
                    Marker marker = aMap.addMarker(markerOptions);
                    marker.showInfoWindow();

                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Log.e("joe", "location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
        }
    };

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
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }
}







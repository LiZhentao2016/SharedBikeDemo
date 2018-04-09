package com.devil.mapdemo.bikeutils;

import com.amap.api.maps2d.model.LatLng;

import java.util.ArrayList;

/**
 * Created by devil on 2018/4/7.
 */

public class MapUtils {
    private double lat, lng;//定位中心经纬度

    public MapUtils(double lat, double lng){
        this.lat=lat;this.lng=lng;
    }

    public ArrayList<LatLng> getNearByBike() {
        ArrayList<LatLng> latLngs = new ArrayList<>();


        latLngs.add(new LatLng(lat+0.0002, lng+0.0002));
        latLngs.add(new LatLng(lat+0.0003, lng+0.0003));
        latLngs.add(new LatLng(lat+0.0004, lng+0.0004));
        latLngs.add(new LatLng(lat+0.0005, lng+0.0005));

        latLngs.add(new LatLng(lat+0.0002, lng-0.0002));
        latLngs.add(new LatLng(lat+0.0003, lng-0.0003));
        latLngs.add(new LatLng(lat+0.0004, lng-0.0004));
        latLngs.add(new LatLng(lat+0.0005, lng-0.0005));

        latLngs.add(new LatLng(lat-0.0002, lng+0.0002));
        latLngs.add(new LatLng(lat-0.0003, lng+0.0003));
        latLngs.add(new LatLng(lat-0.0004, lng+0.0004));
        latLngs.add(new LatLng(lat-0.0005, lng+0.0005));

        latLngs.add(new LatLng(lat-0.0002, lng-0.0002));
        latLngs.add(new LatLng(lat-0.0003, lng-0.0003));
        latLngs.add(new LatLng(lat-0.0004, lng-0.0004));
        latLngs.add(new LatLng(lat-0.0005, lng-0.0005));




        return latLngs;
    }

    public LatLng getMyLocation() {
        return new LatLng(lat, lng);
    }
}

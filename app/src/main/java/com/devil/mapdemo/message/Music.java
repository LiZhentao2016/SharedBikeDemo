package com.devil.mapdemo.message;

/**
 * Created by devil on 2018/4/16.
 */

public class Music {

    private String title;
    private String singer;
    private String publice_url;

    public Music() {
    }

    public Music(String title, String singer, String publice_url) {
        this.title = title;
        this.singer = singer;
        this.publice_url=publice_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getPublice_url(){return publice_url;}
    public void setPublice_url(String publice_url){this.publice_url=publice_url;}
}

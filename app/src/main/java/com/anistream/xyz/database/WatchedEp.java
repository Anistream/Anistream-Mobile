package com.anistream.xyz.database;

import io.realm.RealmObject;

public class WatchedEp extends RealmObject {

    private String title;
    private String epNum;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEpNum() {
        return epNum;
    }

    public void setEpNum(String epNum) {
        this.epNum = epNum;
    }
}

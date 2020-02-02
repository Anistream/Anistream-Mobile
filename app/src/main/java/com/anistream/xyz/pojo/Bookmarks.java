package com.anistream.xyz.pojo;

public class Bookmarks {
    private String title;
    private String imageLink;
    private String siteLink;
    private String epNum;
    private int animeState;

    public int getAnimeState() {
        return animeState;
    }

    public void setAnimeState(int animeState) {
        this.animeState = animeState;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getSiteLink() {
        return siteLink;
    }

    public void setSiteLink(String siteLink) {
        this.siteLink = siteLink;
    }

    public String getEpNum() {
        return epNum;
    }

    public void setEpNum(String epNum) {
        this.epNum = epNum;
    }
}

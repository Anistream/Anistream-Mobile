package com.anistream.xyz.updater;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdatePojo {

    @SerializedName("version")
    private String version;

    @SerializedName("URL")
    private String url;

    @SerializedName("apkName")
    private String fileName;

    private List<String> releaseNotes;

    public List<String> getReleaseNotes() {
        return releaseNotes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setReleaseNotes(List<String> releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

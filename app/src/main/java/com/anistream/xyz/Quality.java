package com.anistream.xyz;

import androidx.annotation.NonNull;

import java.util.Map;

public class Quality {
    public static enum Format {
        HLS, Progressive
    }

    private String quality;
    private String qualityUrl;
    private Format format;
    private Map<String, String> headers;

    public Quality(Format format, String quality, String qualityUrl, Map<String, String> headers) {
        this.quality = quality;
        this.format = format;
        this.qualityUrl = qualityUrl;
        this.headers = headers;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Format getFormat() {
        return format;
    }

    public String getQuality() {
        return quality;
    }

    public String getQualityUrl() {
        return qualityUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("Quality [%s @ %s (%s)]", qualityUrl, quality, format.toString());
    }
}

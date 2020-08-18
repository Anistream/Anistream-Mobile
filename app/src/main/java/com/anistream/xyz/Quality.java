package com.anistream.xyz;

import androidx.annotation.NonNull;

public class Quality {
    public static enum Format {
        HLS, Progressive
    }

    private String quality;
    private String qualityUrl;
    private Format format;

    public Quality(Format format, String quality, String qualityUrl) {
        this.quality = quality;
        this.format = format;
        this.qualityUrl = qualityUrl;
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

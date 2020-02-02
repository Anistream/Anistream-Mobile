package com.anistream.xyz.scrapers;


import com.anistream.xyz.Quality;

import java.util.ArrayList;

public abstract class Scraper {
    public abstract ArrayList<Quality> getQualityUrls();
    public abstract  String getHost();
}

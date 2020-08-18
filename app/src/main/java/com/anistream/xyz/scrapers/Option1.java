package com.anistream.xyz.scrapers;


import android.util.Log;
import com.anistream.xyz.Quality;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//working
public class Option1 extends Scraper {
    Document gogoAnimePageDocument ;
    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\$~@!:/{};'])",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    String m3u8Link;
    public Option1(Document gogoAnimePageDocument)
    {
        this.gogoAnimePageDocument = gogoAnimePageDocument;
        m3u8Link = "";
    }
    @Override
    public  ArrayList<Quality> getQualityUrls() {
        ArrayList<Quality> qualities= new ArrayList<>();

        try
        {
            String vidStreamUrl = "https:" + gogoAnimePageDocument.getElementsByClass("play-video").get(0).getElementsByTag("iframe").get(0).attr("src");
            vidStreamUrl = vidStreamUrl.replace("streaming.php", "loadserver.php");

            Document vidStreamPageDocument = Jsoup.connect(vidStreamUrl).get();

            String html = vidStreamPageDocument.outerHtml();

            Pattern mp4urlPattern = Pattern.compile("'(.*?goto.php.*?)'");
            Matcher matcher = mp4urlPattern.matcher(html);
            if(matcher.find()) {
                String link = matcher.group(1);
                qualities.add(new Quality(Quality.Format.Progressive, "Default", link));
                return qualities;
            } else {
                return new ArrayList<>(0);
            }

        }
        catch (Exception e)
        {
            Log.i("err",e.getMessage());

            e.printStackTrace();

        }
        return qualities;
    }

    @Override
    public String getHost() {
        return m3u8Link;
    }

}

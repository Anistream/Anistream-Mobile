package com.anistream.xyz.scrapers;


import android.util.Log;
import com.anistream.xyz.Quality;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
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
            //String vidCdnUrl = vidStreamUrl.replace("streaming.php", "load.php");

            Document vidStreamPageDocument = Jsoup.connect(vidStreamUrl).get();

            String html = vidStreamPageDocument.outerHtml();
            Matcher matcher = urlPattern.matcher(html);
            String m3u8Link = "";
            while (matcher.find()) {
                int matchStart = matcher.start(1);
                int matchEnd = matcher.end();
                String link = html.substring(matchStart, matchEnd);
                if (link.contains("m3u8")) {
                    m3u8Link = link.substring(0, link.indexOf("'"));
                    Log.i("ScrapeUrl", m3u8Link);
                    break;

                }
            }
            if(!m3u8Link.equals(""))
            {
                Document m3u8Page = Jsoup.connect(m3u8Link).ignoreContentType(true).get();
                String htmlToParse = m3u8Page.outerHtml();
                Log.i("html",htmlToParse);
                Pattern qualityPattern = Pattern.compile("[0-9]{3,4}x[0-9]{3,4}");
                Pattern m3u8LinkPattern = Pattern.compile("(drive//hls/(\\w)*/(\\w)*.m3u8)|(hls/(\\w)*/(\\w)*.m3u8)|(sub\\.\\d*\\.*\\d*\\.m3u8)|(dub\\.\\d*\\.*\\d*\\.m3u8)");
                Matcher qualityMatcher = qualityPattern.matcher(htmlToParse);
                Matcher m3u8LinkMatcher = m3u8LinkPattern.matcher(htmlToParse);

                int index = m3u8Link.lastIndexOf("/hls/");

                String baseUrl = m3u8Link.substring(0, index +"/hls/".length() );
                Log.i("baseUrl",baseUrl);

                while (qualityMatcher.find() && m3u8LinkMatcher.find()) {
                    String quality = htmlToParse.substring(qualityMatcher.start(), qualityMatcher.end());
                    String qualityUrl = baseUrl + htmlToParse.substring(m3u8LinkMatcher.start(), m3u8LinkMatcher.end());
                    qualities.add(new Quality(quality,qualityUrl) );
                }
                Log.i("qualities length",""+qualities.size());

                if(qualities.size()==0)
                    qualities.add(new Quality("Unknown",m3u8Link));
            }

        }
        catch (Exception e)
        {
            Log.i("err",e.getMessage());

            e.printStackTrace();

        }
        if(qualities.size()==0) {

            qualities.add(new Quality("Unknown", m3u8Link));
            Log.i("ScrapeUrl", m3u8Link);
        }
        return qualities;
    }

    @Override
    public String getHost() {
        return "";
    }

}

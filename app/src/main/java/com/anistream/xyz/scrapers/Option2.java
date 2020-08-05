package com.anistream.xyz.scrapers;


import android.util.Log;
import com.anistream.xyz.Quality;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Option2 extends Scraper {
    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\$~@!:/{};'])",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private  static  final Pattern vidCdnM3u8Pattern = Pattern.compile("((sub|dub)\\\\.[0-9]*\\\\.[0-9]*\\\\.m3u8)|(((sub)|(dub))\\.\\d*\\.\\d*\\.m3u8)");
    private  static  final Pattern vidCdnQualityPattern = Pattern.compile("[0-9]*p");

    public Option2(Document gogoAnimePageDocument) {
        this.gogoAnimePageDocument = gogoAnimePageDocument;
    }

    public String getHost() {
        return host;
    }

    private  String host;
    private Document gogoAnimePageDocument;
    @Override
    public ArrayList<Quality> getQualityUrls()
    {

        ArrayList<Quality> qualities = new ArrayList<>();
        try {
            String vidStreamUrl = "https:" + gogoAnimePageDocument.getElementsByClass("play-video").get(0).getElementsByTag("iframe").get(0).attr("src");
            String vidCdnUrl = vidStreamUrl.replace("streaming.php", "loadserver.php");
            Document vidCdnPageDocument = Jsoup.connect(vidCdnUrl).get();
            Log.i("vidcdn","vidcdn is "+vidCdnUrl);
            String htmlToParse = vidCdnPageDocument.outerHtml();
            // Log.i("m3u8html",htmlToParse);
            Matcher matcher = urlPattern.matcher(htmlToParse);
            String m3u8Link="";
            while (matcher.find()) {
                int matchStart = matcher.start();
                int matchEnd = matcher.end();
                String link = htmlToParse.substring(matchStart, matchEnd);
                if (link.contains("m3u8")) {
                    m3u8Link = link.substring(1,link.length()-1);
                    break;
                }

            }
            if(!m3u8Link.equals(""))
            {
                URL url = new URL(m3u8Link);
                Log.i("m3u8Url",m3u8Link);
                host =  url.getHost();
                String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36";
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "*/*");
                headers.put("Accept-Encoding", "gzip,deflate,br");
                headers.put("Accept-Language", "en-IN,en;q=0.9,ur-IN;q=0.8,ur;q=0.7,en-GB;q=0.6,en-US;q=0.5");
                headers.put("Connection", "keep-alive");
                headers.put("Origin", "https://vidstreaming.io");
                headers.put("Referer", "https://vidstreaming.io");
                headers.put("Sec-Fetch-Mode", "cors");
                headers.put("Sec-Fetch-Site", "cross-site");
                headers.put("User-Agent", userAgent);
                headers.put("Host", host);
                String path = url.getFile().substring(0, url.getFile().lastIndexOf('/'));
                String base = url.getProtocol() + "://" + url.getHost() + path;
                Log.i("baseIs",base);
                Document m3u8PageDocument = Jsoup.connect(m3u8Link).ignoreContentType(true).userAgent(userAgent).headers(headers).get();
                System.out.println(m3u8PageDocument.outerHtml());
                htmlToParse = m3u8PageDocument.outerHtml();
                Matcher m3u8Matcher = vidCdnM3u8Pattern.matcher(htmlToParse);
                Matcher qualityMatcher = vidCdnQualityPattern.matcher(htmlToParse);

                while (qualityMatcher.find() && m3u8Matcher.find())
                {
                    System.out.println(htmlToParse.substring(m3u8Matcher.start(),m3u8Matcher.end()));
                    String qualityUrl = base+"/" + htmlToParse.substring(m3u8Matcher.start(),m3u8Matcher.end());
                    Log.i("ScrapeUrl",qualityUrl);
                    String quality = htmlToParse.substring(qualityMatcher.start(),qualityMatcher.end());

                    qualities.add(new Quality(quality,qualityUrl));
                }
                if(qualities.size()==0) {
                    qualities.add(new Quality("Unknown", m3u8Link));
                    Log.i("ScrapeUrl",m3u8Link);
                }
            }
        } catch (Exception e) {
            Log.i("m3u8Url",e.getMessage());

            e.printStackTrace();
        }
        return qualities;
    }

}
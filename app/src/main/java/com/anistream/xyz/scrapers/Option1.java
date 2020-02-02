package com.anistream.xyz.scrapers;





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
    public Option1(Document gogoAnimePageDocument)
    {
        this.gogoAnimePageDocument = gogoAnimePageDocument;
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
                    break;

                }
            }
            if(!m3u8Link.equals(""))
            {
                Document m3u8Page = Jsoup.connect(m3u8Link).get();
                String htmlToParse = m3u8Page.outerHtml();
                Pattern qualityPattern = Pattern.compile("[0-9]{3,4}x[0-9]{3,4}");
                Pattern m3u8LinkPattern = Pattern.compile("(drive\\/\\/hls\\/(\\w)*\\/(\\w)*.m3u8)|(hls\\/(\\w)*\\/(\\w)*.m3u8)");
                Matcher qualityMatcher = qualityPattern.matcher(htmlToParse);
                Matcher m3u8LinkMatcher = m3u8LinkPattern.matcher(htmlToParse);
                int index = m3u8Link.indexOf("/hls");
                String baseUrl = m3u8Link.substring(0, index + 1);
                while (qualityMatcher.find() && m3u8LinkMatcher.find()) {
                    String quality = htmlToParse.substring(qualityMatcher.start(), qualityMatcher.end());
                    String qualityUrl = baseUrl + htmlToParse.substring(m3u8LinkMatcher.start(), m3u8LinkMatcher.end());
                    qualities.add(new Quality(quality,qualityUrl) );


                }
                if(qualities.size()==0)
                    qualities.add(new Quality("Unknown",m3u8Link));
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return qualities;
    }

    @Override
    public String getHost() {
        return "";
    }

}

package com.anistream.xyz;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class Downloader extends AsyncTask<Void,Void,Void> {
    private String baseurl;
      Context context;
    Dialog mBottomSheetDialog;
    ProgressBar bar;
    ArrayList<String> links=new ArrayList<>();
    ArrayList<String> quality=new ArrayList<>();
      Activity activity;
      String animename,episodeno;
    public Downloader(String baseurl, Context context, Activity activity,String animename,String episodeno) {
        this.baseurl=baseurl;
        this.context=context;
        this.activity=activity;
        this.animename=animename;
        this.episodeno=episodeno;
    }
int flag=0; // flag=1 means no download links
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mBottomSheetDialog=new Dialog(activity,R.style.MaterialDialogSheet);
        mBottomSheetDialog.setContentView(R.layout.downloadsheet);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
        bar=mBottomSheetDialog.findViewById(R.id.loader);
        bar.setVisibility(View.VISIBLE);
        mBottomSheetDialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Document document;
        Log.i("downloadlinks",baseurl);
        try {
            if (baseurl.equals(Constants.url+"ansatsu-kyoushitsu-tv--episode-1"))  //edge case
                document = Jsoup.connect(Constants.url+"ansatsu-kyoushitsu-episode-1").get();
            else
                document = Jsoup.connect(baseurl).get();
            String vidstreamlink=document.select("div[class=play-video]").select("iframe").attr("src");
            int indexofid=vidstreamlink.indexOf("id=");
            int indexofand=vidstreamlink.indexOf("&");
            String id=vidstreamlink.substring(indexofid,indexofand);
            String viddownload="https://vidstreaming.io/download?"+id;
            Log.i("downloadlinks",viddownload);
            document=Jsoup.connect(viddownload).get();
            Elements possibledownloadlinks=document.select("div[class=dowload]");
            //First check for direct downloadlinks
                int i=0;
                while (possibledownloadlinks.eq(i).select("a").attr("href").contains("google")) {
                    links.add(possibledownloadlinks.eq(i).select("a").attr("href"));
                    quality.add(possibledownloadlinks.eq(i).select("a").text());
                    i++;
                }
                if(i==0) // No google links
                {
                    //Try to get rapidvideo links;
                    //Get rapidvideo index;
                    for(i=0;i<possibledownloadlinks.size();i++)
                    {
                        if(possibledownloadlinks.eq(i).select("a").text().equals("Download Rapidvideo"))
                            break;
                    }
                }
                if(i==0) // No rapidvideo links

                    flag=1;
                else
                {
                    document=Jsoup.connect(possibledownloadlinks.eq(i).select("a").attr("href")).get();
                    Elements video=document.select("div[class=video]");
                    if(video.size()>0)
                    {
                        Elements rapidvideolinks=video.eq(video.size()-1).select("a");
                        if(rapidvideolinks.size()==0)
                            flag=1;
                        else
                        {
                            for(int j=0;j<rapidvideolinks.size();j++)
                            {
                                links.add(rapidvideolinks.eq(j).attr("href"));
                                quality.add(rapidvideolinks.eq(j).text());
                            }
                        }
                    }
                }
            for(i=0;i<links.size();i++)
            {
                Log.i("downloadlinks",links.get(i));
            }
            return null;
        }catch (Exception e)
        {
            Log.i("downloadlinks","soja");
            flag=1;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(flag==1)
            showmessage(context);
        else
        {


            ListView listView =mBottomSheetDialog.findViewById(R.id.listview);
            QualityAdapter qualityAdapter=new QualityAdapter(context,quality,links,animename,episodeno);
            listView.setAdapter(qualityAdapter);
            bar.setVisibility(View.GONE);
        }
    }



    public static void showmessage(Context context)
    {
        Toast.makeText(context,"Cannot find download link",Toast.LENGTH_SHORT).show();

    }

}

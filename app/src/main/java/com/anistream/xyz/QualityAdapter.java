package com.anistream.xyz;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class QualityAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> quality,links;
int position;
long downloadID;
    String animename,episodeno;
LayoutInflater inflater;
    public QualityAdapter(Context context, ArrayList<String> quality, ArrayList<String> links,String animename,String episodeno) {
        this.context = context;
        this.quality = quality;
        this.links = links;
        this.animename=animename;
        this.episodeno=episodeno;
        inflater=(LayoutInflater.from(context));
    }
    @Override
    public int getCount()
    {
        return quality.size();
    }
    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.downloadqualitybutton, null);
        Button button =        view.findViewById(R.id.buttondownload);
        button.setText(quality.get(i));
        position=i;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url=links.get(position);
            /*    DownloadManager.Request request=new DownloadManager.Request(Uri.parse(url));
                String description=animename+" Episode "+episodeno;
                request.setDescription(description);
                request.setTitle("Downloading");
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, animename+" Episode" +episodeno+".mp4");
                DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                 downloadID =    manager.enqueue(request);
                context.registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                    */
              //  Notification notification=new NotificationCompat.Builder(context.getResources(),0);

            }
        });
        return view;
    }
    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show();
            }
        }
    };


}

package com.anistream.xyz;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.anistream.xyz.database.WatchedEp;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import io.realm.Realm;


public class DataAdapter extends RecyclerView.Adapter<DataAdapter.MyViewHolder> {

    private ArrayList<String> mAnimeList;
    private ArrayList<String> mSiteLink;
    private ArrayList<String> mImageLink;
    private ArrayList<String> mEpisodeList;
    private boolean isBookmark = false;
    int size;
    private Context context;
    SQLiteDatabase recent;
    int lastPosition = -1;
    private Realm realm;
    private Activity activity;

    DataAdapter(Context context, ArrayList<String> AnimeList, ArrayList<String> SiteList, ArrayList<String> ImageList, ArrayList<String> EpisodeList, Activity activity) {
        this.mAnimeList = AnimeList;
        this.mSiteLink = SiteList;
        this.context = context;
        this.mImageLink = ImageList;
        this.mEpisodeList = EpisodeList;
        this.activity = activity;

    }

    DataAdapter(Context context, ArrayList<String> AnimeList, ArrayList<String> SiteList, ArrayList<String> ImageList, ArrayList<String> EpisodeList, Activity activity, Realm realm) {
        this.mAnimeList = AnimeList;
        this.mSiteLink = SiteList;
        this.context = context;
        this.mImageLink = ImageList;
        this.mEpisodeList = EpisodeList;
        this.activity = activity;
        this.realm = realm;

    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView title, episodeno;
        private Uri animeuri, imageuri;
        private ImageView imageofanime;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.animename);
            episodeno = view.findViewById(R.id.episodeno);
            imageofanime = view.findViewById(R.id.img);
            cardView = view.findViewById(R.id.cardview);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_data, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.title.setText(mAnimeList.get(position));
        try {
        if (isBookmark) {
            WatchedEp watchedlist = realm.where(WatchedEp.class).equalTo("title", mAnimeList.get(position)).findFirst();

            if (watchedlist != null){
                String ep = watchedlist.getEpNum();
                List<String> elephantList = new LinkedList<>(Arrays.asList(ep.split(",")));
                elephantList.removeAll(Arrays.asList(null,""));
             //   List<String> elephantLists = Arrays.asList(mEpisodeList.get(position).split("/"));
               // Log.e("ep",elephantList.size()+"   "+mEpisodeList.get(position));
                holder.episodeno.setText(elephantList.size()+"/"+mEpisodeList.get(position));
            }else {
              //  Log.e("ep","   "+mEpisodeList.get(position));

                    if (mEpisodeList.get(position).equals("Not Aired")){
                        holder.episodeno.setText(mEpisodeList.get(position));

                    }else {
                        holder.episodeno.setText("0/" + mEpisodeList.get(position));
                    }


            }


        }else {
            holder.episodeno.setText(mEpisodeList.get(position));

        }
        }catch (Exception e){
            Log.e("crash",e.getMessage());
        }
        holder.animeuri = Uri.parse(mSiteLink.get(position));
        recent = context.openOrCreateDatabase("recent", Context.MODE_PRIVATE, null);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isBookmark) {
                    Intent intent = new Intent(context, WatchVideo.class);
                    intent.putExtra("link", mSiteLink.get(position));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    int ep = holder.episodeno.getText().toString().lastIndexOf(" ");
                    size = 0;
                    recent.execSQL("delete from anime where EPISODELINK='" + holder.animeuri.toString() + "'");
                    String z = "'" + holder.title.getText().toString().replaceAll("'","''")  + "','" + holder.episodeno.getText().toString() + "','" + holder.animeuri.toString() + "','" + mImageLink.get(position) + "'"; //sql string
                    recent.execSQL("INSERT INTO anime VALUES(" + z + ");");
                    intent.putExtra("noofepisodes", holder.episodeno.getText().toString().substring(ep + 1));
                    intent.putExtra("animename", holder.title.getText().toString());
                    intent.putExtra("imagelink", mImageLink.get(position));
                    intent.putExtra("size", size);
                    intent.putExtra("camefrom", "mainactivity");

                    context.getApplicationContext().startActivity(intent);

                }else {
                    Log.e("linkA",mSiteLink.get(position));
                    Intent intent = new Intent(context, selectEpisode.class);
                    intent.putExtra("link", mSiteLink.get(position));
                    intent.putExtra("animename", mAnimeList.get(position));
                    intent.putExtra("imageurl", "https://images.gogoanime.tv/cover/yuuyuuhakusho-specials.png");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.getApplicationContext().startActivity(intent);
                    activity.overridePendingTransition(R.anim.anime_slide_in_top, R.anim.anime_slide_out_top);
                }
            }
        });

        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.up_from_bottom
                        : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        lastPosition = position;

        Log.i("imageurlindexis", String.valueOf(position));
        Log.i("imageurl", mImageLink.get(position));
//        Picasso.get().load(mImageLink.get(position)).into(holder.imageofanime);
        Picasso.get().load(mImageLink.get(position)).
                fit().centerCrop().into(holder.imageofanime);

    }
    void setFilter(ArrayList<String> animelist, ArrayList<String> animelink, ArrayList<String> imagelist, ArrayList<String> EpisodeList) {
        mAnimeList = new ArrayList<>();
        mAnimeList.addAll(animelist);
        mSiteLink = new ArrayList<>();
        mSiteLink.addAll(animelink);
        mImageLink = new ArrayList<>();
        mImageLink.addAll(imagelist);
        mEpisodeList = new ArrayList<>();
        mEpisodeList.addAll(EpisodeList);
        notifyDataSetChanged();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return mAnimeList.size();
    }

    public void setBookmark(boolean isBookmark){
        this.isBookmark = isBookmark;
    }

}

package com.anistream.xyz;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class animefinderadapter extends RecyclerView.Adapter<animefinderadapter.MyViewHolder> {

    private ArrayList<String> mAnimeList = new ArrayList<>();
    private ArrayList<String> mSiteLink = new ArrayList<>();
    private ArrayList<String> mImageLink = new ArrayList<>();
    private ArrayList<String> mEpisodeList = new ArrayList<>();

    private Context context;
    private Activity activity;
    private int lastPosition = -1;

    animefinderadapter(Context context, ArrayList<String> AnimeList, ArrayList<String> SiteList, ArrayList<String> ImageList, ArrayList<String> EpisodeList, Activity activity) {
        this.mAnimeList = AnimeList;
        this.mSiteLink = SiteList;
        this.context = context;
        this.activity = activity;
        this.mImageLink = ImageList;
        this.mEpisodeList = EpisodeList;
    }

    animefinderadapter() {

    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;

        private TextView title, episodeno;
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
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.title.setText(mAnimeList.get(position));
        holder.episodeno.setText(mEpisodeList.get(position));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, selectEpisode.class);
                intent.putExtra("link", mSiteLink.get(position));
                intent.putExtra("animename", mAnimeList.get(position));
                intent.putExtra("imageurl", mImageLink.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);
                activity.overridePendingTransition(R.anim.anime_slide_in_top, R.anim.anime_slide_out_top);
            }
        });
        Picasso.get().load(mImageLink.get(position)).fit().centerCrop().into(holder.imageofanime);
        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.up_from_bottom
                        : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        lastPosition = position;
    }

    @Override
    public int getItemCount() {
        return mAnimeList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}


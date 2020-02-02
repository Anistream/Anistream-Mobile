package com.anistream.xyz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AnimeFindAdapter extends RecyclerView.Adapter<AnimeFindAdapter.MyViewHolder> {

    private ArrayList<String> mAnimeList = new ArrayList<>();
    private ArrayList<String> mSiteLink = new ArrayList<>();
    private ArrayList<String> mImageLink = new ArrayList<>();
    private Context context;
    Activity activity;
    private int lastPosition = -1;

    AnimeFindAdapter(Context context, ArrayList<String> AnimeList, ArrayList<String> SiteList, ArrayList<String> mImageLink, Activity activity) {
        this.mAnimeList = AnimeList;
        this.mSiteLink = SiteList;
        this.mImageLink = mImageLink;
        this.context = context;
        this.activity = activity;
    }

    public AnimeFindAdapter() {

    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout;

        private TextView title;
        private Uri animeuri;
        private ImageView imageView;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.animen);
            layout = view.findViewById(R.id.layout);
            imageView = view.findViewById(R.id.animeimage);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapterforanimelist, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.title.setText(mAnimeList.get(position));
        holder.animeuri = Uri.parse(mSiteLink.get(position));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("linkA",mSiteLink.get(position));
                Intent intent = new Intent(context, selectEpisode.class);
                intent.putExtra("link", mSiteLink.get(position));
                intent.putExtra("animename", mAnimeList.get(position));
                intent.putExtra("imageurl", "https://images.gogoanime.tv/cover/yuuyuuhakusho-specials.png");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);
                activity.overridePendingTransition(R.anim.anime_slide_in_top, R.anim.anime_slide_out_top);
            }
        });
        if (AnimeList.loadimages == 1) {    // load images only when searching
            holder.setIsRecyclable(false);
            holder.imageView.setImageDrawable(null);
            holder.imageView.setVisibility(View.VISIBLE);
            Picasso.get().load(mImageLink.get(position)).into(holder.imageView);
        } else {
            holder.setIsRecyclable(true);
            holder.imageView.setVisibility(View.GONE);
        }

       /* Animation animation = AnimationUtils.loadAnimation(context,
                R.anim.up_from_bottom
        );
        holder.itemView.startAnimation(animation);*/
//        lastPosition = position;
    }


    @Override
    public int getItemCount() {
        return mAnimeList.size();
    }

    //change recyclerview items when searching
    void setFilter(ArrayList<String> animelist, ArrayList<String> animelink, ArrayList<String> imagelist) {
        mAnimeList = new ArrayList<>();
        mAnimeList.addAll(animelist);
        mSiteLink = new ArrayList<>();
        mSiteLink.addAll(animelink);
        mImageLink = new ArrayList<>();
        mImageLink.addAll(imagelist);
        notifyDataSetChanged();
    }

   /* @Override
    public void onViewDetachedFromWindow(@NonNull MyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }*/

}



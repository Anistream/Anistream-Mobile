package com.anistream.xyz;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anistream.xyz.database.WatchedEp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;


public class episodeadapter extends RecyclerView.Adapter<episodeadapter.MyViewHolder> {
    private ArrayList<String> mSiteLink;
    private ArrayList<String> mEpisodeList;
    String animename;
    Activity activity;
    private Context context;
    SQLiteDatabase recent;
    private String imagelink;
    private int lastPosition = -1;
    private List<String> tempList;
    private boolean usingTemp = true;
    private List<String> warhcedep;
    private Realm realm;
    SelectEpisodeListener selectEpisodeListener;

    episodeadapter(Context context, ArrayList<String> SiteList, ArrayList<String> EpisodeList,
                   String imagelink, String animename, Activity activity,List<String> warhcedep,SelectEpisodeListener selectEpisodeListener) {
        this.mSiteLink = SiteList;
        this.context = context;
        this.animename = animename;
        this.mEpisodeList = EpisodeList;
        this.imagelink = imagelink;
        this.activity = activity;
        this.warhcedep = warhcedep;
        this.selectEpisodeListener =selectEpisodeListener;
        realm = RealmController.with(activity).getRealm();

        if (mEpisodeList.size() > 12) {
            tempList = mEpisodeList.subList(0, 12);
            Log.d("Kamran", "episodeadapter: " + tempList.size());
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView button;
        private Button download;
        private LinearLayout layout;
        private TextView expandButton;

        MyViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.linearlayouta);
            button = view.findViewById(R.id.notbutton);
            download = view.findViewById(R.id.downloadchoice);
            expandButton = view.findViewById(R.id.expandbutton);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapterforepisode, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
//        holder.button.setText(animename + " Episode " + (position + 1));
       
        if (usingTemp && mEpisodeList.size() > 12) {
            if (position == 11) {

                holder.button.setVisibility(View.GONE);
                holder.expandButton.setVisibility(View.VISIBLE);

            } else {
                if (mEpisodeList.size() == 12 && position == 12) {
                    Log.i("Khanu", "item number" + position + " is not visible" + mEpisodeList.size());
                    holder.button.setVisibility(View.GONE);
                } else {
                    holder.button.setText((position + 1) + "");
                }


            }
        } else {

            if (getItemCount() == position + 1 && mEpisodeList.size() > 12) {
                holder.button.setVisibility(View.GONE);
                holder.expandButton.setVisibility(View.VISIBLE);
                holder.expandButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
            }
            holder.button.setText((position + 1) + "");
        }

        if (warhcedep.contains(holder.button.getText().toString().trim())){
            Log.e("ep",holder.button.getText().toString().trim());
            holder.button.setBackgroundResource(R.drawable.finish_background);
            holder.button.setTag("completed");
        }else {
            holder.button.setBackgroundResource(R.drawable.round_background);
        }

        holder.button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WatchedEp watchedlist = realm.where(WatchedEp.class).equalTo("title", animename).findFirst();
                if (holder.button.getTag() != null && holder.button.getTag().equals("completed")){

                if (watchedlist!= null) {
                    String ep = watchedlist.getEpNum();
                    String finalep = "";
                    List<String> elephantList = Arrays.asList(ep.split(","));
                    List<String> finaleps = new ArrayList<>();
                    for (int i = 0; i < elephantList.size(); i++) {
                        if (!elephantList.get(i).trim().equals(holder.button.getText().toString().trim())) {
                            finaleps.add(elephantList.get(i));
                        }
                    }
                    for (int i = 0; i < finaleps.size(); i++) {
                        if (i == 0) {
                            finalep = finaleps.get(i);
                        } else {
                            finalep = finalep + "," + finaleps.get(i);
                        }

                    }

                    realm.beginTransaction();
                    watchedlist.setEpNum(finalep);
                    realm.commitTransaction();
                    holder.button.setBackgroundResource(R.drawable.round_background);
                    holder.button.setTag("");
                    warhcedep.remove(holder.button.getText().toString().trim());
                    selectEpisodeListener.onStateChanged(-1);
                }
                }else {
                    realm.beginTransaction();
                    if (watchedlist != null){
                        String ep = watchedlist.getEpNum();
                        List<String> elephantList = Arrays.asList(ep.split(","));
                        if (!elephantList.contains(String.valueOf(holder.button.getText()).trim())) {
                            watchedlist.setEpNum(watchedlist.getEpNum() + "," + holder.button.getText());
                        }
                    }else {
                        WatchedEp watchedEp = new WatchedEp();
                        watchedEp.setTitle(animename);
                        watchedEp.setEpNum(holder.button.getText()+"");
                        realm.copyToRealm(watchedEp);
                    }
                    realm.commitTransaction();

                    holder.button.setBackgroundResource(R.drawable.finish_background);
                    holder.button.setTag("completed");
                    warhcedep.add(holder.button.getText().toString().trim());
                    selectEpisodeListener.onStateChanged(+1);
                }
                return true;
            }
        });


        holder.expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.button.setVisibility(View.VISIBLE);
                holder.expandButton.setVisibility(View.GONE);
                if (usingTemp) {
                    usingTemp = false;
                } else {
                    usingTemp = true;
                }

                notifyDataSetChanged();
            }
        });

//        Log.d("Kamran", "onBindViewHolder: " + position);
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WatchVideo.class);
                intent.putExtra("link", mSiteLink.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                recent = context.openOrCreateDatabase("recent", Context.MODE_PRIVATE, null);
                String z = "'" + animename.replaceAll("'","''") + "','Episode " + (position + 1) + "','" + mSiteLink.get(position) + "','" + imagelink + "'";
                intent.putExtra("animename", animename);
                intent.putExtra("imagelink", imagelink);
                recent.execSQL("delete from anime where EPISODELINK='" + mSiteLink.get(position) + "'");
                recent.execSQL("INSERT INTO anime VALUES(" + z + ");");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("noofepisodes", String.valueOf(mEpisodeList.size()));
                intent.putExtra("animenames", animename);
                intent.putExtra("selectepisodelink", mSiteLink.get(position));
                intent.putExtra("camefrom", "selectepisode");
                holder.button.setBackgroundResource(R.drawable.finish_background);
                if (!"completed".equals(holder.button.getTag())) {
                    selectEpisodeListener.onStateChanged(+1);
                }
                holder.button.setTag("completed");
                warhcedep.add(holder.button.getText().toString().trim());
                context.getApplicationContext().startActivity(intent);
            }
        });
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Downloader(mSiteLink.get(position), context, activity, animename, String.valueOf(position + 1)).execute();

            }
        });

        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.up_from_bottom
                        : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);

        lastPosition = position;
    }

    @Override
    public int getItemCount() {
        if (mEpisodeList.size() > 12 && usingTemp) {
            return tempList.size();
        } else if (mEpisodeList.size() <= 12) {
            mEpisodeList.size();
        } else {
            return mEpisodeList.size() + 1;
        }
        return mEpisodeList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}


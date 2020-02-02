package com.anistream.xyz.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.anistream.xyz.RealmController;

import java.util.ArrayList;

import io.realm.Realm;

public class BookmarkAdapter {
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

    BookmarkAdapter(Context context, ArrayList<String> AnimeList, ArrayList<String> SiteList, ArrayList<String> ImageList, ArrayList<String> EpisodeList, Activity activity){
        this.mAnimeList = AnimeList;
        this.mSiteLink = SiteList;
        this.context = context;
        this.mImageLink = ImageList;
        this.mEpisodeList = EpisodeList;
        this.realm = RealmController.with(activity).getRealm();
    }
}

package com.anistream.xyz;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anistream.xyz.database.AnimeBookmark;
import com.anistream.xyz.database.WatchedEp;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class selectEpisode extends AppCompatActivity {
    String link;
    String animename, animenameforrecents;
    private ArrayList<String> mEpisodeList = new ArrayList<>();
    ProgressDialog mProgressDialog;
    SQLiteDatabase recent;
    private ArrayList<String> mSiteLink = new ArrayList<>();
    private String animeState = "";
    episodeadapter mDataAdapter;
    String imagelink;
    ImageView imageofanime;
    TextInputEditText editText;
    TextView plotsummary;
    ConstraintLayout linearLayout;
    String summary;
    ProgressBar bar;
    RecyclerView mRecyclerView;
    String cameback = "false";
    RelativeLayout relativeLayout;
    WatchedEp watchedlist;
    LinearLayout bookmarkline;
    TextView bookmarkbtn,epnumber;
    boolean isBooked = false;
    SelectEpisodeListener selectEpisodeListener;

    private Realm realm;
    private  Realm backgroundRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newepisodelayout);
        recent = openOrCreateDatabase("recent", MODE_PRIVATE, null);
        animenameforrecents = getIntent().getStringExtra("animename");
        Toolbar toolbar = findViewById(R.id.actiontool);
        linearLayout = findViewById(R.id.linear);
        relativeLayout = findViewById(R.id.rel);
        plotsummary = findViewById(R.id.summary);
        imageofanime = findViewById(R.id.animeimage);
        bookmarkline = (LinearLayout)findViewById(R.id.bookmarks);
        bookmarkbtn = findViewById(R.id.bookmark);
        epnumber = findViewById(R.id.notbutton);
        realm = Realm.getDefaultInstance();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(animenameforrecents);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_slide_out_bottom);

            }
        });

        selectEpisodeListener = new SelectEpisodeListener() {
            @Override
            public void onStateChanged(int number) {

                String epCounter = epnumber.getText().toString();
                Log.e("epnumber", epCounter);
                String[] separated = epCounter.split("\\/");
                List<String> elephantList = Arrays.asList(epCounter.split("/"));
                if (separated.length>0) {
                    Log.e("first", separated[0]);
                    int newCounter = Integer.parseInt(separated[0]) + number;
                    if (newCounter != -1) {
                        epnumber.setText(newCounter + "/" + separated[1]);
                    }else {
                        epnumber.setText(0 + "/" + separated[1]);                    }
                }



            }
        };

        if (!realm.where(AnimeBookmark.class).findAll().isEmpty()){
            AnimeBookmark result = realm.where(AnimeBookmark.class).equalTo("title", animenameforrecents).findFirst();
            if (result != null){
                bookmarkbtn.setBackgroundResource(R.drawable.bookmarked);
                isBooked = true;
            }else {
                bookmarkbtn.setBackgroundResource(R.drawable.round_background);
                isBooked = false;

            }
        }
        link = getIntent().getStringExtra("link");
        if (getIntent().getStringExtra("cameback") != null)
            cameback = getIntent().getStringExtra("cameback");
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < link.length(); i++) {
            if (link.charAt(i) == 'y') {
                if (link.charAt(i + 1) == '/') {
                    for (int j = i + 2; j < link.length(); j++)
                        b.append(String.valueOf(link.charAt(j)));
                    break;
                }
            }

        }
        animename = b.toString();
        new Searching().execute();
        editText = findViewById(R.id.episodeno);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
        ImageView button = findViewById(R.id.episodeselector);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText.getText().toString().equals("")) {
                    int episodeno = Integer.parseInt(String.valueOf(editText.getText()));
                    Intent intent = new Intent(getApplicationContext(), WatchVideo.class);
                    intent.putExtra("link", mSiteLink.get(episodeno - 1));
                    intent.putExtra("noofepisodes", String.valueOf(mEpisodeList.size()));
                    String z = "'" + animenameforrecents + "','Episode " + episodeno + "','" + mSiteLink.get(episodeno - 1) + "','" + imagelink + "'";
                    recent.execSQL("delete from anime where EPISODELINK='" + mSiteLink.get(episodeno - 1) + "'");
                    recent.execSQL("INSERT INTO anime VALUES(" + z + ");");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("imagelink", imagelink);
                    intent.putExtra("animename", animenameforrecents);
                    intent.putExtra("animenames", animenameforrecents);
                    intent.putExtra("camefrom", "selectepisode");
                    intent.putExtra("selectepisodelink", link);
                    getApplicationContext().startActivity(intent);
                } else {
                    editText.requestFocus();
                    editText.setError("Enter episode no first");
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class Searching extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            linearLayout.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.GONE);
            mRecyclerView = (RecyclerView) findViewById(R.id.xyza);
            mRecyclerView.setVisibility(View.GONE);
            plotsummary.setVisibility(View.GONE);
            imageofanime.setVisibility(View.GONE);
            bar = findViewById(R.id.loading);
            bar.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                org.jsoup.nodes.Document searching = Jsoup.connect(link).get();
                Log.e("link",link);
                Elements li = searching.select("div[class=anime_video_body]").select("ul[id=episode_page]").select("li");
                imagelink = searching.select("div[class=anime_info_body_bg]").select("img").attr("src");
                summary = searching.select("div[class=anime_info_body_bg]").select("p[class=type]").eq(1).text();
                String State = searching.select("div[class=anime_info_body_bg]").select("p[class=type]").eq(4).text();
                String a = String.valueOf(li.select("a").eq(li.size() - 1).html());
                String[] parts = State.split(":");
                animeState = parts[1].trim();
                Log.e("linkis", parts[1].trim());
                double x;
                // Getting number of episodes
                if (a.contains("-")) {
                    StringBuilder b = new StringBuilder();
                    for (int i = 0; i < a.length(); i++) {
                        if (a.charAt(i) == '-') {
                            for (int j = i + 1; j < a.length(); j++)
                                b.append(a.charAt(j));
                        }
                    }
                    x = Double.parseDouble(b.toString());
                } else
                    x = Double.parseDouble(a);
                if (x != 0)
                    for (int i = 1; i <= x; i++) {
                        String c = Constants.url + animename + "-episode-" + i;
                        mEpisodeList.add(String.valueOf(i));

                        mSiteLink.add(c);
                    }
                else {
                    String c = Constants.url + animename + "-episode-" + 0;
                    mEpisodeList.add(String.valueOf(0));

                    mSiteLink.add(c);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mRecyclerView.setVisibility(View.VISIBLE);

            plotsummary.setVisibility(View.VISIBLE);
            imageofanime.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.VISIBLE);
            bar.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
            bookmarkline.setVisibility(View.VISIBLE);
//            findViewById(R.id.view).setVisibility(View.VISIBLE);
            findViewById(R.id.text12).setVisibility(View.VISIBLE);
            List<String> elephantList = null;
            try {
                 backgroundRealm = Realm.getDefaultInstance();
                watchedlist = backgroundRealm.where(WatchedEp.class).equalTo("title", animenameforrecents).findFirst();
                Log.e("anime",animenameforrecents);
                if (watchedlist!= null) {
                    String ep = watchedlist.getEpNum();
                    elephantList = new LinkedList<>(Arrays.asList(ep.split(",")));
                }else {
                    elephantList = new LinkedList<>();
                    Log.e("anime","empty");
                } 
            }catch (Exception e){
                Log.e("exps",e.getMessage());
            }finally {
                backgroundRealm.close();
            }
            elephantList.removeAll(Arrays.asList(null,""));
            if (elephantList.size() == 0 && mEpisodeList.size() == 0){
                epnumber.setText("Not Aired");

            }else {
                epnumber.setText(elephantList.size() + "/" + mEpisodeList.size());
            }
            Log.e("showep",elephantList+"");
            mDataAdapter = new episodeadapter(getApplicationContext(), mSiteLink, mEpisodeList, imagelink, animenameforrecents, selectEpisode.this,elephantList,selectEpisodeListener);
//            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            GridLayoutManager linearLayoutManager = new GridLayoutManager(getApplicationContext(), 4);

            //linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
            plotsummary.setText(summary);

            makeTextViewResizable.makeTextViewResizable(plotsummary, 3, "See More", true);
            Picasso.get().load(imagelink).into(imageofanime);


            mRecyclerView.setHasFixedSize(true);
            /*if (mEpisodeList.size() == 1) {
                Intent intent = new Intent(getApplicationContext(), WatchVideo.class);
                intent.putExtra("link", mSiteLink.get(0));
                intent.putExtra("noofepisodes", String.valueOf(mEpisodeList.size()));
                String z = "'" + animenameforrecents + "','Episode " + 1 + "','" + mSiteLink.get(0) + "','" + imagelink + "'";
                recent.execSQL("delete from anime where EPISODELINK='" + mSiteLink.get(0) + "'");

                recent.execSQL("INSERT INTO anime VALUES(" + z + ");");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("animename", animenameforrecents);
                intent.putExtra("imagelink", imagelink);
                intent.putExtra("camefrom", "selectepisode");
                intent.putExtra("animenames", animenameforrecents);
                getApplicationContext().startActivity(intent);
            }*/
            mRecyclerView.setLayoutManager(linearLayoutManager);

            mRecyclerView.setAdapter(mDataAdapter);
            editText.setHint("Episode no between 1 to " + mEpisodeList.size());
            editText.setFilters(new InputFilter[]{
                    new InputFilterMinMax(0, mEpisodeList.size())
            });
            editText.requestFocus();

            bookmarkbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isBooked){
                        RealmResults<AnimeBookmark> result = realm.where(AnimeBookmark.class)
                                .equalTo("title", animenameforrecents).findAll();
                        realm.beginTransaction();
                        result.deleteAllFromRealm();
                        realm.commitTransaction();
                        isBooked = false;
                        bookmarkbtn.setBackgroundResource(R.drawable.round_background);
                    }else {

                        AnimeBookmark animeBookmark = new AnimeBookmark();
                        if (watchedlist != null) {
                            String ep = watchedlist.getEpNum();
                            List<String> elephantList = Arrays.asList(ep.split(","));
                            animeBookmark.setEpNum(elephantList.size()+"/"+mEpisodeList.size());
                        }else {

                            animeBookmark.setEpNum("0" + "/" + mEpisodeList.size());

                        }


                        animeBookmark.setTitle(animenameforrecents);
                        animeBookmark.setImageLink(imagelink);
                        animeBookmark.setSiteLink( link);
                        if (animeState.equals("Completed")){
                            animeBookmark.setAnimeState(1);

                        }else {
                            animeBookmark.setAnimeState(0);

                        }


                        realm.beginTransaction();
                        realm.copyToRealm(animeBookmark);
                        realm.commitTransaction();

                        isBooked = true;
                        bookmarkbtn.setBackgroundResource(R.drawable.bookmarked);

                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!cameback.equals("false")) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

        }
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_slide_out_bottom);
        return false;
    }


}

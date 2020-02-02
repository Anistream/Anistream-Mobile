package com.anistream.xyz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.anistream.xyz.database.AnimeBookmark;
import com.anistream.xyz.pojo.Bookmarks;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;

public class BookmarkAnime extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ArrayList<String> animename=new ArrayList<>();
    private  ArrayList<String> animelink=new ArrayList<>();
    private  ArrayList<String> animeimages=new ArrayList<>();
    private  ArrayList<String> animeEp=new ArrayList<>();
    ArrayList<String> animeeplist = new ArrayList<>();
    DataAdapter mDataAdapter;
    Toolbar toolbar;
    RecyclerView recyclerView;
    private Realm realm;
    public static int loadimages=0;
    ProgressBar progressBar;
    private Realm backgroundRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark_anime);
        toolbar = findViewById(R.id.tool);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        progressBar = findViewById(R.id.progress2);
        recyclerView=findViewById(R.id.animelistrecyclerview);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_right);
            }
        });
        realm = Realm.getDefaultInstance();
        List<AnimeBookmark> animeBookmarks = realm.where(AnimeBookmark.class).findAll();


       // realm.close();


          new Searching().execute();





    }
    @Override
    public  boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.animelist, menu);
        MenuItem search=menu.findItem(R.id.anime_list_search);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(this);
        return true;
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
      try {
          newText = newText.toLowerCase();
          if (newText.length() == 0) {
              loadimages = 0;

          } else
              loadimages = 1;
          ArrayList<String> newlinklist = new ArrayList<>();
          ArrayList<String> newanimelist = new ArrayList<>();
          ArrayList<String> newimagelist = new ArrayList<>();
          ArrayList<String> animeep = new ArrayList<>();
          for (int i = 0; i < animename.size(); i++) {
              if (animename.get(i).toLowerCase().contains(newText)) {
                  newanimelist.add(animename.get(i));
                  newlinklist.add(animelink.get(i));
                  newimagelist.add(animeimages.get(i));
                  animeep.add(animeeplist.get(i));


              }

          }
          mDataAdapter.setFilter(newanimelist, newlinklist, newimagelist, animeep);
      }catch (Exception e){

      }




        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recyclerView != null && mDataAdapter != null) {
            realm = RealmController.with(this).getRealm();
            List<AnimeBookmark> animeBookmarks = realm.where(AnimeBookmark.class).findAll();
           // realm.close();
            animeimages = new ArrayList<>();
            animeEp = new ArrayList<>();
            animelink = new ArrayList<>();
            animename = new ArrayList<>();

            for (int i = 0; i < animeBookmarks.size(); i++) {
                animename.add(animeBookmarks.get(i).getTitle());
                animelink.add(animeBookmarks.get(i).getSiteLink());
                animeEp.add(animeeplist.get(i));
                animeimages.add(animeBookmarks.get(i).getImageLink());
            }
            mDataAdapter.setFilter(animename,animelink,animeimages,animeEp);

        }
    }



    @SuppressLint("StaticFieldLeak")
    private class Searching extends AsyncTask<Void, Void, Void> {

        List<AnimeBookmark> animeBookmarks;
        List<Bookmarks> bookmarks;


        String state = "";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bookmarks = new ArrayList<>();
            recyclerView.setVisibility(View.GONE);

            progressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                backgroundRealm = Realm.getDefaultInstance();
               animeBookmarks = backgroundRealm.where(AnimeBookmark.class).findAll();
               backgroundRealm.beginTransaction();
                Log.e("listsize",animeBookmarks.size()+" size");
                for (int l = 0; l < animeBookmarks.size(); l++){
                    Bookmarks bookmarks1 = new Bookmarks();
                    ArrayList<String> mEpisodeList = new ArrayList<>();



                    bookmarks1.setImageLink(Objects.requireNonNull(animeBookmarks.get(l)).getImageLink());
                    bookmarks1.setSiteLink(Objects.requireNonNull(animeBookmarks.get(l)).getSiteLink());
                    bookmarks1.setTitle(Objects.requireNonNull(animeBookmarks.get(l)).getTitle());

                    if (animeBookmarks.get(l).getAnimeState() != 1) {
                        org.jsoup.nodes.Document searching = null;
                        try {
                             searching = Jsoup.connect(animeBookmarks.get(l).getSiteLink()).get();
                        }catch (Exception e){

                        }

                        Elements li = searching.select("div[class=anime_video_body]").select("ul[id=episode_page]").select("li");
                        String State = searching.select("div[class=anime_info_body_bg]").select("p[class=type]").eq(4).text();
                        String[] parts = State.split(":");
                        if (parts[1].trim().equals("Completed")){
                            bookmarks1.setAnimeState(1);
                            animeBookmarks.get(l).setAnimeState(1);
                        }else {
                            bookmarks1.setAnimeState(0);
                            animeBookmarks.get(l).setAnimeState(0);

                        }

                        if (!li.isEmpty()) {
                            String a = String.valueOf(li.select("a").eq(li.size() - 1).html());

                            double x;
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
                                    mEpisodeList.add(String.valueOf(i));

                                }
                            else {
                                mEpisodeList.add(String.valueOf(0));


                            }
                        } else {
                            mEpisodeList.add("Not Aired");
                        }

                        if (mEpisodeList.get(0).equals("Not Aired")) {
                            animeBookmarks.get(l).setEpNum("Not Aired");
                            bookmarks1.setEpNum("Not Aired");
                        } else {
                            animeBookmarks.get(l).setEpNum(mEpisodeList.size() + "");
                            bookmarks1.setEpNum(mEpisodeList.size() + "");
                        }
                    }else {
                        Log.e("epnumber", animeBookmarks.get(l).getEpNum());
                        if (animeBookmarks.get(l).getEpNum().contains("/")){
                            bookmarks1.setEpNum(Objects.requireNonNull(animeBookmarks.get(l)).getEpNum().split("/")[1]);
                        }else {
                            bookmarks1.setEpNum(Objects.requireNonNull(animeBookmarks.get(l)).getEpNum());
                        }

                        bookmarks1.setAnimeState(Objects.requireNonNull(animeBookmarks.get(l)).getAnimeState());

                    }

                   bookmarks.add(bookmarks1);
                }
            backgroundRealm.commitTransaction();

            } catch (Exception e) {
                Log.e("error", e.getMessage());
                e.printStackTrace();
            }finally {
                backgroundRealm.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            for (int i = 0; i < bookmarks.size();i++){
                animename.add(bookmarks.get(i).getTitle());
                animelink.add(bookmarks.get(i).getSiteLink());
                animeeplist.add(bookmarks.get(i).getEpNum());
                animeimages.add(bookmarks.get(i).getImageLink());
            }

            progressBar.setVisibility(View.GONE);


            Log.e("animename", animename.size()+" size");
            mDataAdapter = new DataAdapter(getApplicationContext(), animename, animelink,animeimages,animeeplist,BookmarkAnime.this,realm);
            mDataAdapter.setBookmark(true);
            // RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);

            recyclerView.setHasFixedSize(true);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(mDataAdapter);

            recyclerView.setVisibility(View.VISIBLE);
        }
    }


}

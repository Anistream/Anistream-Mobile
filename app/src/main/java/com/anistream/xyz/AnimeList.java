package com.anistream.xyz;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class AnimeList extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private ArrayList<String> animename=new ArrayList<>();
    private  ArrayList<String> animelink=new ArrayList<>();
    private  ArrayList<String> animeimages=new ArrayList<>();
    AnimeFindAdapter mDataAdapter;
    public static int loadimages=0;
    Toolbar toolbar;
    RecyclerView recyclerView;
    @Override
    protected  void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animelistrecyclerview);
        toolbar = findViewById(R.id.tool);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_right);
            }
        });
        try
        {
            // Load json file
            InputStream is = getResources().openRawResource(R.raw.animelist);
            String s = IOUtils.toString(is);
            IOUtils.closeQuietly(is);
            JSONArray jsonArray=new JSONArray(s);
      for(int i=0;i<jsonArray.length();i++)
      {
          JSONObject a=jsonArray.getJSONObject(i);
          JSONObject anime=a.getJSONObject("anime");
          animelink.add((String)anime.get("link"));
          animename.add((String) anime.get("Anime name"));
          animeimages.add((String) anime.get("imagelink"));
      }
                   }
                   catch (Exception e) {
            e.printStackTrace();
        }
         recyclerView=findViewById(R.id.animelistrecyclerview);
           mDataAdapter = new AnimeFindAdapter(getApplicationContext(), animename, animelink,animeimages,AnimeList.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mDataAdapter);
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
        newText=newText.toLowerCase();
        if(newText.length()==0) {
            loadimages = 0;

        }
        else
            loadimages=1;
        ArrayList<String> newlinklist=new ArrayList<>();
        ArrayList<String> newanimelist=new ArrayList<>();
        ArrayList<String > newimagelist=new ArrayList<>();
        for(int i=0;i<animename.size();i++)
        {
            if(animename.get(i).toLowerCase().contains(newText))
            {
                newanimelist.add(animename.get(i));
                newlinklist.add(animelink.get(i));
                newimagelist.add(animeimages.get(i));

            }
        }
        mDataAdapter.setFilter(newanimelist,newlinklist,newimagelist);


        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_right);
    }

}


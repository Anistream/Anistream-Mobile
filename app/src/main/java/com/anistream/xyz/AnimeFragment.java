package com.anistream.xyz;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class AnimeFragment extends Fragment {
    private String url;
    private ArrayList<String> dubAnimeList = new ArrayList<>();
    private ArrayList<String> dubSiteLink = new ArrayList<>();
    private ArrayList<String> dubImageLink = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<String> dubEpisodeList = new ArrayList<>();
    View view;
    int flag = 0;
    int initial = 1;

    static AnimeFragment newInstance(String url) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        AnimeFragment dubFragment = new AnimeFragment();
        dubFragment.setArguments(bundle);
        return dubFragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            url = bundle.getString("url");

        }
    }


    private ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dublayout, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);

        new Dub().execute();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new Dub().execute();
            }
        });
        return view;
    }

    @SuppressLint("StaticFieldLeak")
    private class Dub extends AsyncTask<Void, Void, Void> {
        String desc;
        String err = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            readBundle(getArguments());
            if (initial == 1) {
                progressBar = view.findViewById(R.id.progress);
                progressBar.setVisibility(View.VISIBLE);
                //     swipeRefreshLayout.setRefreshing(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                org.jsoup.nodes.Document searching;
                Log.e("url",url);
                searching = Jsoup.connect(url).get();
                Elements li = searching.select("div[class=last_episodes loaddub]").select("ul[class=items]").select("li");
                int size = li.size();
                dubAnimeList = new ArrayList<>();
                dubSiteLink = new ArrayList<>();
                dubImageLink = new ArrayList<>();
                dubEpisodeList = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    Elements mElementAnimeName = searching.select("p[class=name]").select("a").eq(i);
                    String mAnimenName = mElementAnimeName.text();
                    if (mAnimenName.contains("[email protect  ed]"))
                        mAnimenName = mAnimenName.replace("[email protected]", "IDOLM@STER");
                    String mlink = searching.select("p[class=name]").select("a").eq(i).attr("abs:href");
                    // String imagelink = searching.select("div[class=img]").select("img").eq(2*i).attr("src"); // Use if getting cache error
                    String imagelink = searching.select("div[class=img]").select("img").eq(i).attr("src"); // Use normally
                    String episodeno = searching.select("p[class=episode]").eq(i).text();
                    dubAnimeList.add(mAnimenName);
                    dubSiteLink.add(mlink);
                    dubImageLink.add(imagelink);
                    dubEpisodeList.add(episodeno);
                }
            } catch (IOException e) {
                err = e.getMessage();
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (err.contains("Unable to resolve host")){
               // Toast.makeText(getContext(),"your ISP block the content",Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Something is blocking your connection, use a VPN")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                //do things
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
            RecyclerView mRecyclerView = view.findViewById(R.id.act_recyclerview);
            DataAdapter mDataAdapter = new DataAdapter(view.getContext(), dubAnimeList, dubSiteLink, dubImageLink, dubEpisodeList,getActivity());
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(view.getContext(), 2);

            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setDrawingCacheEnabled(true);
            mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            mRecyclerView.setItemViewCacheSize(20);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mDataAdapter);
            initial = 0;
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}

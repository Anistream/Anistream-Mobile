package com.anistream.xyz;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.BoardiesITSolutions.FileDirectoryPicker.DirectoryPicker;
import com.BoardiesITSolutions.FileDirectoryPicker.OpenFilePicker;
import com.anistream.xyz.updater.DownloadActivity;
import com.anistream.xyz.updater.GetDataService;
import com.anistream.xyz.updater.RetrofitClientInstance;
import com.anistream.xyz.updater.UpdatePojo;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.internal.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    LinearLayout noanime;
    String searchurl;
    AppBarLayout appBarLayout;
    private ArrayList<String> mAnimeList = new ArrayList<>();
    private ArrayList<String> mSiteLink = new ArrayList<>();
    private ArrayList<String> mImageLink = new ArrayList<>();
    private ArrayList<String> mEpisodeList = new ArrayList<>();
    RecyclerView recyclerView;
    DataAdapter mDataAdapter;
    MenuItem prevMenuItem;

    FrameLayout frameLayout;
    ViewPagerAdapter viewPagerAdapter;
    TabLayout tabLayout;
    BottomNavigationView bottomNavigationView;
    ViewPager viewPager;
    animefinderadapter DataAdapter;
    Searching x = new Searching();
    ProgressBar progressBar;
    private String mChosenFile;
    GridLayoutManager gridLayoutManager;
    private Realm realm;
    private String[] mFileList;
    private static final int REQUEST_DIRECTORY_PICKER = 1;
    private static final int REQUEST_OPEN_FILE_DIALOG = 2;
    private boolean write = false;
    private boolean read = false;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        Realm.init(this);
        // realm = Realm.getDefaultInstance();
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(2)
                .migration(new RealmMigrations())
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);


        SharedPreferences preferences = getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = preferences.edit();
        recyclerView = findViewById(R.id.recyclerview2);
        gridLayoutManager = new GridLayoutManager(MainActivity.this,
                2);

        recyclerView.setLayoutManager(gridLayoutManager);

        if (!haveNetworkConnection(getApplicationContext())) {
            LinearLayout linearLayout1 = findViewById(R.id.notvisiblelinearlayout);
            linearLayout1.setVisibility(View.VISIBLE);
            viewPager = findViewById(R.id.viewPager);
            viewPager.setVisibility(View.GONE);
            bottomNavigationView = findViewById(R.id.bottom_navigation);

            bottomNavigationView.setVisibility(View.GONE);
            appBarLayout = findViewById(R.id.appbar);
            appBarLayout.setVisibility(View.GONE);
        } else {
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            bottomNavigationView.setOnNavigationItemSelectedListener(
                    new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.DUB:
                                    viewPager.setCurrentItem(0);
                                    break;
                                case R.id.SUB:
                                    viewPager.setCurrentItem(1);
                                    break;
                                case R.id.recent:
                                    viewPager.setCurrentItem(2);
                                    break;
                            }
                            return false;
                        }
                    });
            toolbar = findViewById(R.id.tool);
            setSupportActionBar(toolbar);

            noanime = findViewById(R.id.noanime);
            //  appBarLayout=findViewById(R.id.tabtoolbar);
            ViewPagerAdapter viewPagerAdapter;
            viewPager = (ViewPager) findViewById(R.id.viewPager);
            //    appBarLayout.setVisibility(View.VISIBLE);
            SQLiteDatabase recent = openOrCreateDatabase("recent", MODE_PRIVATE, null);
            recent.execSQL("CREATE TABLE IF NOT EXISTS anime(Animename VARCHAR,Episodeno VARCHAR,EPISODELINK VARCHAR,IMAGELINK VARCHAR)");
            progressBar = findViewById(R.id.progress2);
            viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
            viewPager.setOffscreenPageLimit(3);
            viewPager.setPageTransformer(true, new DepthPageTransformer());
            viewPager.setAdapter(viewPagerAdapter);

            // tabLayout = (TabLayout) findViewById(R.id.tabs);
            //  tabLayout.setupWithViewPager(viewPager);
            viewPager.setCurrentItem(1);
            bottomNavigationView.getMenu().getItem(1).setChecked(true);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null)
                        prevMenuItem.setChecked(false);
                    else
                        bottomNavigationView.getMenu().getItem(0).setChecked(false);

                    bottomNavigationView.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = bottomNavigationView.getMenu().getItem(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
            Call<UpdatePojo> call = service.getCurrentVersion();
            call.enqueue(new Callback<UpdatePojo>() {
                @Override
                public void onResponse(Call<UpdatePojo> call, final Response<UpdatePojo> response) {
                    Log.e("retro","finish");
                    Log.e("updater", "current verion is: "+Constants.APP_VER);
                    if (Integer.parseInt(response.body().getVersion()) > Constants.APP_VER){
                        new AlertDialog.Builder(context)
                                .setTitle("Update available!")
                                .setMessage("There is a new version available for Anistream. Press OK to install the new version")

                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                // The dialog is automatically dismissed when a dialog button is clicked.
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Continue with delete operation
                                        /*Intent updateIntent = new Intent(Intent.ACTION_VIEW,
                                                Uri.parse("https://cobaltmedia.xyz/updater/anistream.apk"));
                                        startActivity(updateIntent);*/
                                        Intent intent = new Intent(context, DownloadActivity.class);
                                        intent.putStringArrayListExtra("releasenote", (ArrayList<String>) response.body().getReleaseNotes());
                                        intent.putExtra("FILEURL",  response.body().getUrl());
                                        intent.putExtra("FILENAME", response.body().getFileName());
                                        startActivity(intent);


                                    }
                                })

                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setNegativeButton(android.R.string.no, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<UpdatePojo> call, Throwable t) {
                    Log.e("retro","failed "+t);
                }
            });





        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.drawer, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        MenuItem bookmark = menu.findItem(R.id.bookmark_menu);
        MenuItem animelist = menu.findItem(R.id.animelist);


        bookmark.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent(getApplicationContext(), BookmarkAnime.class);
                startActivity(i);
                overridePendingTransition(R.anim.anim_slide_in_left,R.anim.anim_slide_out_left);
                return false;
            }
        });

        animelist.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent i = new Intent(getApplicationContext(), AnimeList.class);
                startActivity(i);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                return false;
            }
        });
        MenuItem settings = menu.findItem(R.id.settings);
        settings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
                return false;
            }
        });
        MenuItem donate = menu.findItem(R.id.donate);
        donate.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.anistream.xyz/donate.html"));
                startActivity(browserIntent);
                return false;
            }
        });
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.toLowerCase();
                noanime.setVisibility(View.GONE);

                if (newText.length() >= 3) {

                    recyclerView.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.GONE);
                    bottomNavigationView.setVisibility(View.GONE);
                    //   appBarLayout.setVisibility(View.GONE);
                    searchurl = Constants.url + "/search.html?keyword=" + newText;
                    if (x.getStatus() == AsyncTask.Status.RUNNING)
                        x.cancel(true);
                    x = new Searching();
                    x.execute();

                } else {
                    if (x.getStatus() == AsyncTask.Status.RUNNING)
                        x.cancel(true);
                    recyclerView.setVisibility(View.GONE);
                    progressBar = findViewById(R.id.progress2);
//                    appBarLayout.setVisibility(View.VISIBLE);

                    viewPager.setVisibility(View.VISIBLE);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
                return false;
            }
        });
        return true;
    }

    public static boolean haveNetworkConnection(android.content.Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private class Searching extends AsyncTask<Void, Void, Void> {
        String desc;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            recyclerView.setVisibility(View.GONE);

            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                org.jsoup.nodes.Document searching = Jsoup.connect(searchurl).get();
                DataAdapter = new animefinderadapter();
                DataAdapter.notifyItemRangeRemoved(0, mAnimeList.size());


                mAnimeList.clear();
                mSiteLink.clear();
                mImageLink.clear();
                mEpisodeList.clear();
                Elements li = searching.select("div[class=main_body]").select("div[class=last_episodes]").select("ul[class=items]").select("li");
                for (int i = 0; i < li.size(); i++) {
                    String animelink = li.select("div[class=img]").eq(i).select("a").attr("abs:href");
                    String animename = li.select("div[class=img]").eq(i).select("a").attr("title");
                    String imagelink = li.select("div[class=img]").eq(i).select("img").attr("src");
                    mAnimeList.add(animename);
                    mImageLink.add(imagelink);
                    mSiteLink.add(animelink);
                    mEpisodeList.add("");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressBar.setVisibility(View.GONE);
            if (mAnimeList.size() == 0)
                noanime.setVisibility(View.VISIBLE);
            else {
                recyclerView.setVisibility(View.VISIBLE);

                DataAdapter = new animefinderadapter(getApplicationContext(), mAnimeList, mSiteLink, mImageLink, mEpisodeList, MainActivity.this);


                recyclerView.setDrawingCacheEnabled(true);
                recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                recyclerView.setItemViewCacheSize(30);

                recyclerView.setAdapter(DataAdapter);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 110:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //export();
                    Intent intent = new Intent(MainActivity.this, DirectoryPicker.class);
                    startActivityForResult(intent, REQUEST_DIRECTORY_PICKER);

                } else {
                    new AlertDialog.Builder(getBaseContext())
                            .setTitle("Unable to Export/Import")
                            .setMessage("You do not have the valid permission for this database!")

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton("Give permission", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);
                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                break;
            case 111:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MainActivity.this, OpenFilePicker.class);
                    startActivityForResult(intent, REQUEST_OPEN_FILE_DIALOG);
                }else {
                    new AlertDialog.Builder(getBaseContext())
                            .setTitle("Unable to Export/Import")
                            .setMessage("we cant export or import the database unless you give the permission")

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton("give permission", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 110);

                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                break;
        }
    }

    public void export(String exportdestination){
        File exportRealmFile;
        File path = new File(exportdestination);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        String EXPORT_REALM_FILE_NAME = "Anistream"+formattedDate+".realm";
        try {
            path.mkdirs();

            // create a backup file
            exportRealmFile = new File(path, EXPORT_REALM_FILE_NAME);

            // if backup file already exists, delete it
            //exportRealmFile.delete();

            // copy current realm to backup file
            realm = RealmController.with(this).getRealm();
            //if (realm != null)
            realm.writeCopyTo(exportRealmFile);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            realm.close();
        }

        String msg = "File exported to Path: " + path + "/" + EXPORT_REALM_FILE_NAME;
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    public void importd(String data){

        Log.e("endwith",data.endsWith(".realm")+"");
        if (data.endsWith(".realm")) {

            try {
                realm = RealmController.with(this).getRealm();
                RealmConfiguration tempConfig = realm.getConfiguration();
                realm.close();
                Realm.deleteRealm(tempConfig);

                File file = new File(MainActivity.this.getApplicationContext().getFilesDir(), Realm.DEFAULT_REALM_NAME);

                FileOutputStream outputStream = new FileOutputStream(file);

                FileInputStream inputStream = new FileInputStream(new File(data));

                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, bytesRead);
                }
                outputStream.close();
                restartApp();
                // Realm.init(this);
                // return file.getAbsolutePath();
            } catch (IOException | java.io.IOException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(MainActivity.this, "invalid backup", Toast.LENGTH_LONG).show();
        }


    }

    private void restartApp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_DIRECTORY_PICKER:
                if (resultCode == Activity.RESULT_OK)
                {
                    String currentPath = data.getStringExtra(DirectoryPicker.BUNDLE_CHOSEN_DIRECTORY);
                    export(data.getStringExtra(DirectoryPicker.BUNDLE_CHOSEN_DIRECTORY));
                    //Do whatever you need to in this directory
                }

                break;
            case REQUEST_OPEN_FILE_DIALOG:
                if (resultCode == Activity.RESULT_OK)
                {
                    String selectedFile = data.getStringExtra(DirectoryPicker.BUNDLE_SELECTED_FILE);
                    importd(selectedFile);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (!(viewPager.getCurrentItem() == 1) && viewPager.getVisibility() == View.VISIBLE)
                viewPager.setCurrentItem(1);
            else
                super.onBackPressed();
        } catch (Exception e) {
            e.printStackTrace();
            super.onBackPressed();
        }
    }
}

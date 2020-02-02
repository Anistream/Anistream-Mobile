package com.anistream.xyz.updater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anistream.xyz.R;
import com.google.android.material.snackbar.Snackbar;
import com.race604.drawable.wave.WaveDrawable;


import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class DownloadActivity extends AppCompatActivity {

    TextView txtProgressPercent;
   // ElasticDownloadView progressBar;
    DownloadZipFileTask downloadZipFileTask;
    private ImageView imageView;
    private WaveDrawable mWaveDrawable;
    private Snackbar snackbar;
    private TextView releasenote;
    private ArrayList<String> releaseNotes;
    private String fileURL = "";
    private String fileName = "";
    private static final String TAG = "DownloadActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);


        txtProgressPercent = findViewById(R.id.txtProgressPercent);
        imageView = findViewById(R.id.image);
        releasenote = findViewById(R.id.releasenote);

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.logo);
         mWaveDrawable = new WaveDrawable(drawable);
        imageView.setImageDrawable(mWaveDrawable);

        //progressBar = findViewById(R.id.progressBar);

        try {
            Bundle extras = getIntent().getExtras();
            releaseNotes = getIntent().getStringArrayListExtra("releasenote");
            fileURL = extras.getString("FILEURL");
            fileName = extras.getString("FILENAME");
        }catch (Exception e){

        }

        String notes = "";
        for (int i = 0; i < releaseNotes.size(); i++){

            notes += releaseNotes.get(i)+" \n";



        }
        releasenote.setText(notes);
        snackbar =  Snackbar
                .make(findViewById(android.R.id.content), "Error", Snackbar.LENGTH_INDEFINITE)
                .setAction("TRY AGAIN", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (snackbar.isShown()){
                            snackbar.dismiss();
                        }
                        if (hasPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE})){
                            downloadZipFile();
                        }else {
                            askForPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 101);

                        }
                    }
                });

        if (hasPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE})){
            downloadZipFile();
        }else {
            askForPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 101);

        }



    }

    private void downloadZipFile() {

        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
        final Uri uri = Uri.parse("file://" + destination);
        File file = new File(destination);
        if (file.exists()) {
            //file.delete() - test this, I think sometimes it doesnt work
            boolean deleted = file.delete();
        }

        GetDataService downloadService = createService(GetDataService.class, fileURL);
        Call<ResponseBody> call = downloadService.downloadFileByUrl(fileName);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Got the body for the file");

                    Toast.makeText(getApplicationContext(), "Downloading...", Toast.LENGTH_SHORT).show();

                    downloadZipFileTask = new DownloadZipFileTask(fileName);
                    downloadZipFileTask.execute(response.body());

                } else {
                    Log.d(TAG, "Connection failed " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.e(TAG, t.getMessage());
            }
        });
    }


    public <T> T createService(Class<T> serviceClass, String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(new OkHttpClient.Builder().build())
                .build();
        return retrofit.create(serviceClass);
    }


    private class DownloadZipFileTask extends AsyncTask<ResponseBody, Pair<Integer, Long>, String> {

        private boolean done = true;
        private String fileName = "";

        public DownloadZipFileTask(String fileName){
            this.fileName = fileName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressBar.startIntro();
          //  mWaveDrawable.setWaveLength(50);
          //  mWaveDrawable.setWaveSpeed(50);
          //  mWaveDrawable.setWaveAmplitude(50);
            mWaveDrawable.setIndeterminate(false);

        }

        @Override
        protected String doInBackground(ResponseBody... urls) {
            //Copy you logic to calculate progress and call
            saveToDisk(urls[0], "anistream.apk");
            return null;
        }

        protected void onProgressUpdate(Pair<Integer, Long>... progress) {

            Log.d("API123", progress[0].second + " ");

            if (progress[0].first == 100) {
                Toast.makeText(getApplicationContext(), "File downloaded successfully", Toast.LENGTH_SHORT).show();
                //progressBar.success();
            }


            if (progress[0].second > 0) {
                int currentProgress = (int) ((double) progress[0].first / (double) progress[0].second * 100);
               // progressBar.setProgress(currentProgress);
                mWaveDrawable.setLevel(currentProgress * 100);

                txtProgressPercent.setText("Progress " + currentProgress + "%");

            }

            if (progress[0].first == -1) {
                done = false;
                Toast.makeText(getApplicationContext(), "Download failed", Toast.LENGTH_SHORT).show();
                //progressBar.fail();
            }

        }

        public void doProgress(Pair<Integer, Long> progressDetails) {
            publishProgress(progressDetails);
        }

        @Override
        protected void onPostExecute(String result) {
            if (done){
                try {


                    String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
                    String fileName = this.fileName;
                    destination += fileName;
                    final Uri uri = Uri.parse("file://" + destination);


                    //Delete update file if exists
                    File file = new File(destination);
                    if (file.exists()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri data = FileProvider.getUriForFile(DownloadActivity.this, "com.anistream.xyz.provider", file);
                            Intent install = new Intent(Intent.ACTION_VIEW);
                            install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            install.setDataAndType(data,
                                    "application/vnd.android.package-archive");
                            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(install);
                            finish();
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(file),
                                    "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                }catch (Exception e){
                    snackbar.setText("Error: "+ e.getMessage());
                    snackbar.show();
                }
            }else {
                snackbar.setText("Error: failed to download the file");
                snackbar.show();
            }

        }
    }

    private void saveToDisk(ResponseBody body, String filename) {
        try {

            File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);


            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(destinationFile);
                byte data[] = new byte[4096];
                int count;
                int progress = 0;
                long fileSize = body.contentLength();
                Log.d(TAG, "File Size=" + fileSize);
                while ((count = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                    progress += count;
                    Pair<Integer, Long> pairs = new Pair<>(progress, fileSize);
                    downloadZipFileTask.doProgress(pairs);
                    Log.d(TAG, "Progress: " + progress + "/" + fileSize + " >>>> " + (float) progress / fileSize);
                }

                outputStream.flush();

                Log.d(TAG, destinationFile.getParent());
                Pair<Integer, Long> pairs = new Pair<>(100, 100L);
                downloadZipFileTask.doProgress(pairs);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Pair<Integer, Long> pairs = new Pair<>(-1, Long.valueOf(-1));
                downloadZipFileTask.doProgress(pairs);
                Log.d(TAG, "Failed to save the file!");
                snackbar.setText("Error file 02 : Failed to save file!");
                snackbar.show();
                return;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            snackbar.setText("Error file 01 : Failed to save file!");
            snackbar.show();
            Log.d(TAG, "Failed to save the file!");
            return;
        }
    }

    private boolean hasPermissions(String[] permission){
        for (String permissions : permission) {
            if (ActivityCompat.checkSelfPermission(DownloadActivity.this, permissions) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }

        }
        return true;
    }

    private void askForPermission(String[] permission, Integer requestCode) {
        ActivityCompat.requestPermissions(DownloadActivity.this, permission, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {

            if (requestCode == 101) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                downloadZipFile();
            }
        } else {
            snackbar.setText("Permission denied: we can't download it without permissions");
            snackbar.show();
        }
    }

}

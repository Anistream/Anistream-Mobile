package com.anistream.xyz;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.BoardiesITSolutions.FileDirectoryPicker.DirectoryPicker;
import com.BoardiesITSolutions.FileDirectoryPicker.OpenFilePicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.internal.IOException;

public class Settings extends AppCompatActivity {

    private Realm realm;
    private static final int REQUEST_DIRECTORY_PICKER = 1;
    private static final int REQUEST_OPEN_FILE_DIALOG = 2;

    Button buttonbackup,buttonrestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        Toolbar toolbar=findViewById(R.id.settingstoolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        SharedPreferences preferences=getSharedPreferences("settings",0);
        final SharedPreferences.Editor editor=preferences.edit();
        buttonbackup = findViewById(R.id.buttonbackup);
        buttonrestore = findViewById(R.id.buttonrestore);

        buttonbackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(Settings.this, DirectoryPicker.class);
                    startActivityForResult(intent, REQUEST_DIRECTORY_PICKER);



                }else {
                    ActivityCompat.requestPermissions(Settings.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);
                }
            }
        });

        buttonrestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(Settings.this, OpenFilePicker.class);
                    startActivityForResult(intent, REQUEST_OPEN_FILE_DIALOG);


                }else {
                    ActivityCompat.requestPermissions(Settings.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 111);
                }
            }
        });



    }

    public void discord (View view) { discordurl ( "https://discord.gg/kami"); }

    private void discordurl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    public void importd(String data){

        Log.e("endwith",data.endsWith(".realm")+"");
        if (data.endsWith(".realm")) {

            try {
                realm = RealmController.with(this).getRealm();
                RealmConfiguration tempConfig = realm.getConfiguration();
                realm.close();
                Realm.deleteRealm(tempConfig);

                File file = new File(Settings.this.getApplicationContext().getFilesDir(), Realm.DEFAULT_REALM_NAME);

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
            Toast.makeText(Settings.this, "invalid backup", Toast.LENGTH_LONG).show();
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
        Toast.makeText(Settings.this, msg, Toast.LENGTH_LONG).show();
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 110:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //export();
                    Intent intent = new Intent(Settings.this, DirectoryPicker.class);
                    startActivityForResult(intent, REQUEST_DIRECTORY_PICKER);

                } else {
                    new AlertDialog.Builder(getBaseContext())
                            .setTitle("Unable to Export/Import")
                            .setMessage("we cant export or import the database unless you give the permission")

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton("give permission", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(Settings.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);

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
                    Intent intent = new Intent(Settings.this, OpenFilePicker.class);
                    startActivityForResult(intent, REQUEST_OPEN_FILE_DIALOG);
                }else {
                    new AlertDialog.Builder(getBaseContext())
                            .setTitle("Unable to Export/Import")
                            .setMessage("we cant export or import the database unless you give the permission")

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton("give permission", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(Settings.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 110);

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


}

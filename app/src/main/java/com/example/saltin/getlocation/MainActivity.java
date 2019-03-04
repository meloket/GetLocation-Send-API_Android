package com.example.saltin.getlocation;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    private Button btn_start, btn_stop;
    private TextView textView;
    private BroadcastReceiver broadcastReceiver;
    private String user_id;
    private String user_token;
    private WebView mWebView;

    AlertDialog.Builder builder;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void pushNotification(String ptitle, String psubject, String pbody){
        String tittle=ptitle.trim();
        String subject=psubject.trim();
        String body=pbody.trim();

        NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notify=new Notification.Builder
                (getApplicationContext()).setContentTitle(tittle).setContentText(body).
                setContentTitle(subject).setSmallIcon(R.drawable.ic_alert).build();

        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));

        setMockLocationSettings();

        builder = new AlertDialog.Builder(this);

        builder.setMessage("Did you allow all permissions ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent dashIntent = new Intent(getApplicationContext(), DashActivity.class);
                        startActivity(dashIntent);
                        Toast.makeText(getApplicationContext(),"Welcome!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                        finish();
                        Toast.makeText(getApplicationContext(),"Please allow all permissions",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Get Location...");
        alert.show();



        /*btn_start = (Button) findViewById(R.id.btnStartService);
        btn_stop = (Button) findViewById(R.id.btnStopService);
        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());*/


    }

    public void setMockLocationSettings() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(getApplicationContext())) {
                // Do stuff here
                try {
                    Settings.Secure.putInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 1);
                    Toast.makeText(this, "Mock Location mode activated!", Toast.LENGTH_LONG);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

            }
            else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
        //startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
    }

    private void restoreMockLocationSettings(int restore_value) {
        try {
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION, restore_value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
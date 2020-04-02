package com.waffel.fsend;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Thread initF = new Thread(new Runnable() {
            @Override
            public void run() {
                FileExchanger.init("haha");
            }
        });
        initF.start();

        intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if(Intent.ACTION_SEND.equals(action) && type != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        initF.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handleIntent(intent);
                }
            }).start();
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void handleIntent(Intent intent) {
        final Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        final String filename = getFileName(uri);
        final InputStream is;
        try {
            is = getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        final String[] ips = FileExchanger.queryIPs();
        System.out.println("Finished Query!");

        final ViewGroup layout = (ViewGroup) findViewById(R.id.list_layout);

        for(int i = 0; i < ips.length; i++) {
            final int j = i;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Button b = new Button(MainActivity.this);
                    b.setText(ips[j]);
                    b.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        FileExchanger.sendIS(is, filename, ips[j]);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    });
                    layout.addView(b);
                }
            });
        }

    }
}

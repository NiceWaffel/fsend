package com.waffel.fsend;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Intent intent;
    private List<String[]> ips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Thread initF = new Thread(new Runnable() {
            @Override
            public void run() {
                FileExchanger.init("", MainActivity.this);
            }
        });
        initF.start();
        try {
            initF.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Button scanB = findViewById(R.id.scan_button);
        scanB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ips = FileExchanger.queryIPs();
                    }
                }).start();
            }
        });

        intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if(Intent.ACTION_SEND.equals(action) && type != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
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
        ips = FileExchanger.queryIPs();
        System.out.println("Finished Query!");

        final ViewGroup layout = (ViewGroup) findViewById(R.id.list_layout);

        for(int i = 0; i < ips.size(); i++) {
            final int j = i;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Button b = new Button(MainActivity.this);
                    b.setText(ips.get(j)[0] + " @ " + ips.get(j)[1]);
                    b.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        FileExchanger.sendIS(is, filename, ips.get(j)[1]);
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

    public void writeFile(String filename, byte[] fileContents) {
        try {
            File dir = getDefaultDirectory();
            initializeFsendDirectory(dir.getAbsolutePath());

            // TODO REQUEST PERMISSION

            File file = new File(dir, filename);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileContents);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void initializeFsendDirectory(String path) throws IOException {
        // Create specified directory if it doesn't exit
        File dir = new File(path);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create Fsend directory");
        }
        if (!dir.canWrite()) {
            throw new IOException("No write access to Fsend directory");
        }
        // Add a .nomedia file to it if it doesn't exist
        File nomedia = new File(dir, ".nomedia");
        if (!nomedia.exists()) {
            try {
                nomedia.createNewFile();
            } catch (IOException e) {
                throw new IOException("Failed to create .nomedia file");
            }
        }
    }

    public static File getDefaultDirectory() {
        return new File(Environment.getExternalStorageDirectory(), "fsend");
    }
}

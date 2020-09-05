package com.example.framedump;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CursorAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private int completeCount;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    100
            );
        }

        Button button = findViewById(R.id.start);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startGrabbingMedia();

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startGrabbingMedia() {

        ArrayList<File> medias = getAllMedia(this);


        if(medias.size() == 0)
            return;

        int i = completeCount = 0;
        for (File f : medias) {

            ThreadPool.getInstance().getPoolService().submit(
                    new FrameExtractionJob(
                            i++,
                            f.getAbsolutePath(),
                            f.getName(),
                            new ExtractionCompleteListener() {
                                @Override
                                public void onExtractionComplete(int videoIndex) {
                                    Log.i(TAG, "COMPLETED: " + ++completeCount);
                                }
                            }
                    )
            );

        }


    }

    private ArrayList<File> getAllMedia(Context context) {

        HashSet<File> fileHashSet = new HashSet<>();

        String where =
                MediaStore.Files.FileColumns.MIME_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?";

        String mp4 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp4");
        String avi = MimeTypeMap.getSingleton().getMimeTypeFromExtension("avi");
        String _3gp = MimeTypeMap.getSingleton().getMimeTypeFromExtension("3gp");

        String[] args = new String[] {mp4, avi, _3gp};
        String[] projection = {
                MediaStore.MediaColumns.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Files.FileColumns.DATA
        };

        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection,
                where,
                args,
                null
        )) {
            File f;

            assert cursor != null;
            cursor.moveToFirst();


            while (cursor.moveToNext()) {
                f = new File(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        MediaStore.Video.Media.DATA
                                )
                        )
                );

                fileHashSet.add(f);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e(TAG, fileHashSet.size() + "");

        return new ArrayList<>(fileHashSet);

    }

}
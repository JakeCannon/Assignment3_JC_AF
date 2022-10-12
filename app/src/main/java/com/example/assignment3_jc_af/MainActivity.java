package com.example.assignment3_jc_af;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

// 159336 Mobile Application Development
// Assignment 3 - Voice Recorder / Soundboard App
// Name: Jake Cannon - ID: 20008958
// Name: Arnold Fruish  - ID:

public class MainActivity extends AppCompatActivity {

    // Adapter for tile data in GridView
    private TileAdapter mTileAdapter;

    // initial amount of tiles in grid view
    private static int NTILES = 1;

    // default amount of columns - set later depending on device orientation
    private static int NCOLS = 3;

    private GridView gridView;

    // Cursor for use in MainAcvitiy for sound bites
    private Cursor mCursor;

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private static final int WRITE_EXTERNAL_STORAGE = 1;
    //private static String fileName = null;

    //private RecordButton recordButton = null;
    private MediaRecorder recorder = null;

    //private PlayButton   playButton = null;
    private MediaPlayer player = null;

    private ContentResolver resolver;

    private Uri audioUri;

    private ContentValues newAudioTrack;

    // Requesting permission to RECORD_AUDIO
    //private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    //private ArrayList<String> files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = findViewById(R.id.gridview);

        // Set number of columns based on device's orientation
        int orientation = MainActivity.this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            NCOLS = 3;
        } else {
            NCOLS = 5;
        }
        gridView.setNumColumns(NCOLS);

        // Record to the external cache directory for visibility
        // = getExternalCacheDir().getAbsolutePath();
        //fileName += "/audiorecordtest.3gp";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        ActivityCompat.requestPermissions(this, permissions, WRITE_EXTERNAL_STORAGE);

//        LinearLayout ll = new LinearLayout(this);
//        recordButton = new RecordButton(this);
//        ll.addView(recordButton,
//                new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        0));
//        playButton = new PlayButton(this);
//        ll.addView(playButton,
//                new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        0));
//        setContentView(ll);

        //files = context.fileList();
    }

    private boolean CheckAudioPermission() {
        return ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void alterDocument(Uri uri) {
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());
            fos.write(("Overitten at " + System.currentTimeMillis() + "\n").getBytes());
            fos.close();
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        if (audioUri != null) {
            player = new MediaPlayer();
            try {
                player.setDataSource(getApplicationContext(), audioUri);
                player.prepare();
                player.start();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        createFileFromAudio();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
        makeAudioAccessible();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void oldcreateFileFromAudio() {
        Uri uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        resolver = getApplicationContext().getContentResolver();
        ContentValues newFileDetails = new ContentValues();
        newFileDetails.put(MediaStore.Files.FileColumns.DISPLAY_NAME, "myfile.txt");

        Uri contentUri = resolver.insert(uri, newFileDetails);
        alterDocument(contentUri);
    }

    private void createFileFromAudio() {
        resolver = getApplicationContext().getContentResolver();

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        newAudioTrack = new ContentValues();
        newAudioTrack.put(MediaStore.Audio.Media.DISPLAY_NAME, "Recorded Clip.mp3");

        // make sure file not accesible until its finished being written
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            newAudioTrack.put(MediaStore.Audio.Media.IS_PENDING, 1);
        }

        audioUri = resolver.insert(uri, newAudioTrack);

        try (ParcelFileDescriptor pfd = resolver.openFileDescriptor(audioUri, "w", null)) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setOutputFile(pfd.getFileDescriptor());
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            System.out.println(audioUri.getPath());
            try {
                recorder.prepare();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }

            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeAudioAccessible() {
        if (newAudioTrack != null) {
            newAudioTrack.clear();
            // make file accessible now that it has been written
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                newAudioTrack.put(MediaStore.Audio.Media.IS_PENDING, 0);
            }
        }
        if (resolver != null && audioUri != null) {
            resolver.update(audioUri, newAudioTrack, null, null);
        }
    }


    @SuppressLint("Range")
    void init() {
        // Setup the cursor to query the devices image media
        Uri externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.MediaColumns.TITLE,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.MediaColumns.RELATIVE_PATH,
                MediaStore.Images.Media.ORIENTATION
        };
        mCursor = getApplicationContext().getContentResolver().query(externalUri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        // set adapter for tile data
        mTileAdapter = new TileAdapter();
        gridView.setAdapter(mTileAdapter);
        // on click listener for click event of photo tile
        gridView.setOnItemClickListener(((adapterView, view, i, l) -> {
            // do something when clicked on
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // if app is reopened - redo the process of finding media on the phone
        //init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    boolean mStartPlaying = true;
    public void PlayRecorded(View view) {

        onPlay(mStartPlaying);
        if (mStartPlaying) {
            ((AppCompatButton)view).setText("Stop playing");
        } else {
            ((AppCompatButton)view).setText("Start playing");
        }
        mStartPlaying = !mStartPlaying;
    }

    boolean mStartRecording = true;
    public void startStopRecord(View view) {
        if (CheckAudioPermission()) {
            onRecord(mStartRecording);
            if (mStartRecording) {
                ((AppCompatButton)view).setText("Stop recording");
            } else {
                ((AppCompatButton)view).setText("Start recording");
            }
            mStartRecording = !mStartRecording;
        } else {
            //startPermissionRequest(new )
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    // view adapter to create tiles for the photos
    public class TileAdapter extends BaseAdapter {

        class ViewHolder {
            int position;
            ImageView image;
        }

        @Override
        public int getCount() {
            return NTILES;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        // populate the view
        @SuppressLint("StaticFieldLeak")
        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder vh;
            if (convertView == null) {
                // inflate from xml, as long as not recylced
                convertView = getLayoutInflater().inflate(R.layout.tile, viewGroup, false);
                // convertView is a linearLayout
                vh = new ViewHolder();
                vh.image = convertView.findViewById(R.id.tilebtn);
                // update tag
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            // Set size to be square
            convertView.setMinimumWidth(gridView.getWidth() / gridView.getNumColumns());
            // ensure it is not rotated
            vh.image.setRotationY(0);
            vh.position = i;
            vh.image.setImageBitmap(null);

            // Set image height properties
            vh.image.setMaxHeight(gridView.getWidth() / NCOLS);
            vh.image.setMinimumHeight(gridView.getWidth() / NCOLS);
            return null;
        }
    }
}
package com.example.assignment3_jc_af;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

// 159336 Mobile Application Development
// Assignment 3 - Voice Recorder / Soundboard App
// Name: Jake Cannon - ID: 20008958
// Name: Arnold Fruish  - ID: 19028792

//IDEAS:
// could try filter by comparing filenames until a match - test performance of this
// allow user to change sorting of files to get their recordings near the top
// maybe have different colour for default files
// allow toggling of default files

public class MainActivity extends AppCompatActivity {

    // Adapter for tile data in GridView
    private TileAdapter mTileAdapter;

    // initial amount of tiles in grid view
    private static int NTILES = 13;

    // default amount of columns - set later depending on device orientation
    private static int NCOLS = 3;

    private GridView gridView;

    // Cursor for use in MainAcvitiy for sound bites
    private Cursor mCursor;

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private static final int WRITE_EXTERNAL_STORAGE = 1;

    private MediaRecorder recorder = new MediaRecorder();

    private MediaPlayer player = new MediaPlayer();

    private ContentResolver resolver;

    private Uri audioUri;

    private ContentValues newAudioTrack;

    private String [] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private String fileName = "";

    private SeekBar seekBarPitch;

    private boolean displayDefaults = true;

    private Switch showDefaultsSwitch;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        MenuItem item = menu.findItem(R.id.myswitch);
        item.setActionView(R.layout.actionbar_switch);

        showDefaultsSwitch = item.getActionView().findViewById(R.id.switchForActionBar);
        showDefaultsSwitch.setChecked(true);
        showDefaultsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something based on isChecked
                displayDefaults = !displayDefaults;
                System.out.println(displayDefaults);
                init();
            }
        });
        //return true;
        //return true;
        return super.onCreateOptionsMenu(menu);
    }

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

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        ActivityCompat.requestPermissions(this, permissions, WRITE_EXTERNAL_STORAGE);

        seekBarPitch = findViewById(R.id.seekBarPitch);
        seekBarPitch.setMax(20);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBarPitch.setMin(2);
        }
        seekBarPitch.setProgress(10);

        //showDefaultsSwitch = findViewById(R.id.switchForActionBar);
        //showDefaultsSwitch.setChecked(true);
        //System.out.println(showDefaultsSwitch.isChecked());
    }

    private boolean CheckAudioPermission() {
        return ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        createFileFromAudio();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
        makeAudioAccessible();
        init();
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

    private void createFileFromAudio() {
        EditText editTextFileName = findViewById(R.id.editTextFileName);
        fileName = editTextFileName.getText().toString();
        fileName = fileName + ".mp3";
        resolver = getApplicationContext().getContentResolver();

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        //File path = new File(String.valueOf(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) + "/audio_clips/");
        //File path = new File(uri.getPath() + "/audio_clips/");

        //uri = Uri.fromFile(path);
        //uri = uri + Uri.parse("/audio_clips/");
        //Uri.Builder.appendPath()

        //Uri appendedUri = uri.buildUpon().appendPath("audio_clips").build();
        //System.out.println(uri.buildUpon().appendPath("audio_clips").build());

//        String uriString = uri.toString() + "/audio_clips/";
//        Uri appendedUri = Uri.parse(uriString);
//        System.out.println(appendedUri.getPath().toString());
//        System.out.println(uri.getPath().toString());
        //Uri.fromFile(new File(uri.getPath()+"/audio_clips/"));

        //String folder = "audio_clips";
//        DocumentFile dir = DocumentFile.fromTreeUri(getApplicationContext(), uri);
//        if (dir != null) {
//            DocumentFile nFolder = dir.createDirectory(folder);
//        }
//        File file = new File(Environment.getExternalStorageDirectory(), folder);
//        if(!file.exists())
//        {
//            file.mkdirs();
//        }
        newAudioTrack = new ContentValues();
        newAudioTrack.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);

        // make sure file not accessible until its finished being written
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            newAudioTrack.put(MediaStore.Audio.Media.IS_PENDING, 1);
        }
        audioUri = resolver.insert(uri, newAudioTrack);
        System.out.println("Writing to: " + audioUri);

        try (ParcelFileDescriptor pfd = resolver.openFileDescriptor(audioUri, "w", null)) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // improve quality of audio recording
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(96000);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setOutputFile(pfd.getFileDescriptor());
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
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

    @SuppressLint({"UseCompatLoadingForColorStateLists", "SetTextI18n"})
    public void buttonFunctionality(View view) {
        if (player.isPlaying()) {
            player.stop();
            ((AppCompatButton) view).setBackgroundTintList(getResources().getColorStateList(R.color.red));
            ((AppCompatButton) view).setText("Start recording");
        } else {
            startStopRecord(view);
        }
    }

    boolean mStartRecording = true;
    @SuppressLint("SetTextI18n")
    public void startStopRecord(View view) {
        EditText editTextFileName = findViewById(R.id.editTextFileName);
        if (CheckAudioPermission()) {
            // make sure a file name is given before starting recording
            if (!editTextFileName.getText().toString().equals("")) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    ((AppCompatButton) view).setText("Stop recording");
                } else {
                    ((AppCompatButton) view).setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @SuppressLint("UseCompatLoadingForColorStateLists, SetTextI18n")
    void enablePausePlayback() {
        Button multiButton = findViewById(R.id.startrecord);
        System.out.println(player.isPlaying());
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                multiButton.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                multiButton.setText("Start recording");
            }
        });
        if (player.isPlaying()) {
            multiButton.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
            multiButton.setText("Stop playback");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // if app is reopened - redo the process of finding media on the phone
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @SuppressLint("Range")
    void init() {
        // Setup the cursor to query the devices audio media
        Uri externalUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            externalUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DISPLAY_NAME
        };
        mCursor = getApplicationContext().getContentResolver().query(externalUri, projection, null, null, MediaStore.Audio.Media.DATE_ADDED + " DESC");

        // set adapter for tile data
        mTileAdapter = new TileAdapter();
        gridView.setAdapter(mTileAdapter);
    }

    // view adapter to create tiles for the audio clips
    public class TileAdapter extends BaseAdapter {

        class ViewHolder {
            int position;
            ImageView image;
            Button audioButton;
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

            //Button audioButton = convertView.findViewById(R.id.button);
            vh.audioButton = convertView.findViewById(R.id.button);

            // load default media
            new AsyncTask<ViewHolder,Void,String>() {
                @SuppressLint({"UseCompatLoadingForColorStateLists", "SetTextI18n"})
                @Override
                protected String doInBackground(ViewHolder... viewHolders) {
                   // player params holds value of pitch slider
                    PlaybackParams params = new PlaybackParams();
                    if (displayDefaults) {
                        switch (i) {
                            case 0:
                                vh.audioButton.setBackgroundTintList(getResources().getColorStateList(R.color.blue_darker));
                                vh.audioButton.setText("Applause");
                                vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        player = MediaPlayer.create(getApplicationContext(), R.raw.applause);
                                        float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                        params.setPitch(pitch);
                                        player.setPlaybackParams(params);
                                        player.start();
                                        enablePausePlayback();
                                    }
                                });
                                break;
                            case 1:
                                vh.audioButton.setText("Bicycle Bell");
                                vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        player = MediaPlayer.create(getApplicationContext(), R.raw.bicycle_bell);
                                        float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                        params.setPitch(pitch);
                                        player.setPlaybackParams(params);
                                        player.start();
                                        enablePausePlayback();
                                    }
                                });
                                break;
                            case 2:
                                vh.audioButton.setText("Boooooo");
                                vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        player = MediaPlayer.create(getApplicationContext(), R.raw.boooooo);
                                        float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                        params.setPitch(pitch);
                                        player.setPlaybackParams(params);
                                        player.start();
                                        enablePausePlayback();
                                    }
                                });
                                break;
                            case 3:
                                vh.audioButton.setText("Cheering");
                                vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        player = MediaPlayer.create(getApplicationContext(), R.raw.cheering);
                                        float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                        params.setPitch(pitch);
                                        player.setPlaybackParams(params);
                                        player.start();
                                        enablePausePlayback();
                                    }
                                });
                                break;
                            case 4:
                                vh.audioButton.setText("Duck");
                                vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        player = MediaPlayer.create(getApplicationContext(), R.raw.duck);
                                        float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                        params.setPitch(pitch);
                                        player.setPlaybackParams(params);
                                        player.start();
                                        enablePausePlayback();
                                    }
                                });
                                break;
                            case 5:
                                vh.audioButton.setText("Fanfare");
                                vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        player = MediaPlayer.create(getApplicationContext(), R.raw.fanfare);
                                        float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                        params.setPitch(pitch);
                                        player.setPlaybackParams(params);
                                        player.start();
                                        enablePausePlayback();
                                    }
                                });
                                break;
                            case 6:
                                vh.audioButton.setText("Gong");
                                vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        player = MediaPlayer.create(getApplicationContext(), R.raw.gong);
                                        float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                        params.setPitch(pitch);
                                        player.setPlaybackParams(params);
                                        player.start();
                                        enablePausePlayback();
                                    }
                                });
                                break;
                            case 7:
                                vh.audioButton.setText("Gunshot");
                                vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        player = MediaPlayer.create(getApplicationContext(), R.raw.gunshot);
                                        float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                        params.setPitch(pitch);
                                        player.setPlaybackParams(params);
                                        player.start();
                                        enablePausePlayback();
                                    }
                                });
                                break;
                            case 8:
                                vh.audioButton.setText("Hail to the king");
                                vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        player = MediaPlayer.create(getApplicationContext(), R.raw.hail_to_the_king);
                                        float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                        params.setPitch(pitch);
                                        player.setPlaybackParams(params);
                                        player.start();
                                        enablePausePlayback();
                                    }
                                });
                                break;
                            case 9:
                                vh.audioButton.setText("I feel good");
                                vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        player = MediaPlayer.create(getApplicationContext(), R.raw.i_feel_good);
                                        float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                        params.setPitch(pitch);
                                        player.setPlaybackParams(params);
                                        player.start();
                                        enablePausePlayback();
                                    }
                                });
                                break;
                            case 10:
                                vh.audioButton.setText("Laugh");
                                vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        player = MediaPlayer.create(getApplicationContext(), R.raw.laugh);
                                        float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                        params.setPitch(pitch);
                                        player.setPlaybackParams(params);
                                        player.start();
                                        enablePausePlayback();
                                    }
                                });
                                break;
                            case 11:
                                vh.audioButton.setText("Ricochet");
                                vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        player = MediaPlayer.create(getApplicationContext(), R.raw.ricochet);
                                        float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                        params.setPitch(pitch);
                                        player.setPlaybackParams(params);
                                        player.start();
                                        enablePausePlayback();
                                    }
                                });
                                break;
                            case 12:
                                vh.audioButton.setText("Sheep");
                                vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        player = MediaPlayer.create(getApplicationContext(), R.raw.sheep);
                                        float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                        params.setPitch(pitch);
                                        player.setPlaybackParams(params);
                                        player.start();
                                        enablePausePlayback();
                                    }
                                });
                                break;
                            default:
                                vh.audioButton.setText("Clip");
                        }


                        // get count of how many audio clips there are to know how many tiles to add to defaults
                        mCursor.moveToLast();
                        NTILES = 13 + mCursor.getPosition();
                        // Add on user files once defaults have been generated
                        if (i >= 13) {
                            // Create a tile for each audio file
                            mCursor.moveToPosition(i - 13);
                            // get file name for each audio file for each tile generated
                            String fileName = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                            vh.audioButton.setText(fileName);
                            vh.audioButton.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
                            // make each audio button on user sound tiles have on click listener
                            vh.audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    // move cursor back to the exact tile they click on (after generating on screen tiles)
                                    mCursor.moveToPosition(i - 13);
                                    // get audioUri for specific tile button clicked on
                                    audioUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                            mCursor.getInt(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)));
                                    // play the track
                                    player = MediaPlayer.create(getApplicationContext(), audioUri);
                                    // apply pitch effect to playback
                                    float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                    params.setPitch(pitch);
                                    player.setPlaybackParams(params);
                                    player.start();
                                    enablePausePlayback();
                                }
                            });
                        } else {
                            vh.audioButton.setBackgroundTintList(getResources().getColorStateList(R.color.blue_darker));
                        }
                    } else {
                        // get count of how many audio clips there are to know how many tiles to add to defaults
                        mCursor.moveToLast();
                        NTILES = mCursor.getPosition();
                        // Add on user files once defaults have been generated
                        // Create a tile for each audio file
                        mCursor.moveToPosition(i);
                        // get file name for each audio file for each tile generated
                        String fileName = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                        vh.audioButton.setText(fileName);
                        vh.audioButton.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
                        // make each audio button on user sound tiles have on click listener
                        vh.audioButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                // move cursor back to the exact tile they click on (after generating on screen tiles)
                                mCursor.moveToPosition(i);
                                // get audioUri for specific tile button clicked on
                                audioUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        mCursor.getInt(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)));
                                // play the track
                                player = MediaPlayer.create(getApplicationContext(), audioUri);
                                // apply pitch effect to playback
                                float pitch = (float) seekBarPitch.getProgress() / 10.0f;
                                params.setPitch(pitch);
                                player.setPlaybackParams(params);
                                player.start();
                                enablePausePlayback();
                            }
                        });
                    }
                    return null;
                }
            }.execute(vh);//executeOnExecutor(mExecutor,vh);
            return convertView;
        }
    }
}
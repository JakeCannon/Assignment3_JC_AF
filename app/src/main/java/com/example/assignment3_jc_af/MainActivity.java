package com.example.assignment3_jc_af;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

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

    private MediaRecorder recorder = null;

    private MediaPlayer player = null;

    private ContentResolver resolver;

    private Uri audioUri;

    private ContentValues newAudioTrack;

    private String [] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private String fileName = "";

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
        try {
            populateDefaultAudioClips();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void populateDefaultAudioClips() throws IOException {
        resolver = getApplicationContext().getContentResolver();
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        //int numOfAudioClips = 13;
//        Field[] fields = R.raw.class.getFields();
//        for (Field field : fields) {
//            //R.raw.Applause
//            newAudioTrack = new ContentValues();
//            newAudioTrack.put(MediaStore.Audio.Media.DISPLAY_NAME, field.getName());
//            //newAudioTrack.put(R.raw.Applause);
//            resolver.insert(uri, newAudioTrack);
//        }
        //Field[] fields = R.raw.class.getFields();
        //for (Field field : fields) {

        // create dummy file to create the directory
//        newAudioTrack = new ContentValues();
//        newAudioTrack.put(MediaStore.Audio.Media.DISPLAY_NAME, "Recorded Clip.mp3");
//        //System.out.println(uri);
//        audioUri = resolver.insert(uri, newAudioTrack);
//        try (ParcelFileDescriptor pfd = resolver.openFileDescriptor(audioUri, "w", null)) {
//
//        }

            //File dir = new File(uri.getPath());

        // create dummy directory to prevent crash
        File file = new File(uri.getPath());
        if (!file.exists()) {
            file.mkdir();
        }


//        try (ParcelFileDescriptor pfd = resolver.openFileDescriptor(audioUri, "w", null)) {
//
//
//        }

        // transfer raw files to directory for default clips

//        AssetManager mngr = getAssets();
//        InputStream path = mngr.open("music/music1.mp3");
//
//        BufferedInputStream bis = new BufferedInputStream(path,1024);
//        //get the bytes one by one
//        int current = 0;
//
//        while ((current = bis.read()) != -1) {
//
//            baf.append((byte) current);
//        }
//    }
//    byte[] bitmapdata  = baf.toByteArray();

        //File test = new File(R.raw.applause);
//        InputStream in = getResources().openRawResource(R.raw.applause);
//        InputStreamReader inr = new InputStreamReader(in);
//        BufferedReader br = new BufferedReader(inr, 8192);
//        FileOutputStream out = new FileOutputStream(uri.getPath());
//        try {
//            String test;
//            while (true){
//                test = br.readLine();
//                // readLine() returns null if no more lines in the file
//                if(test == null) break;
//                tv.append("\n"+"    "+test);
//            }
//            inr.close();
//            in.close();
//            br.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        tv.append("\n\nThat is all");

//        System.out.println("URI IS: " + uri.getPath());
//        byte[] buff = new byte[1024 * 1024 * 10];
//        int read = 0;
//        try {
//            while ((read = in.read(buff)) > 0) {
//                out.write(buff, 0, read);
//            }
//        } finally {
//            in.close();
//            out.close();
//        }
        //copyFiletoExternalStorage(R.raw.applause, "Applause.mp3");
    }

    private void copyFiletoExternalStorage(int resourceId, String resourceName){
        String pathSDCard = Environment.getExternalStorageDirectory() + "/Android/data/" + resourceName;
        try{
            InputStream in = getResources().openRawResource(resourceId);
            FileOutputStream out = null;
            out = new FileOutputStream(pathSDCard);
            byte[] buff = new byte[1024];
            int read = 0;
            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } finally {
                in.close();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFileFromAudio() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter File Name");

        // Set up the input
        final EditText input = new EditText(getApplicationContext());
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fileName = input.getText().toString();
                //System.out.println(input.getText().toString());
                //System.out.println(newAudioTrack.get(MediaStore.Audio.Media.DISPLAY_NAME));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fileName = input.getText().toString();
                dialog.cancel();
            }

        });

        fileName = input.getText().toString();
        //System.out.println(newAudioTrack.get(MediaStore.Audio.Media.DISPLAY_NAME));
        builder.show();


        resolver = getApplicationContext().getContentResolver();

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        newAudioTrack = new ContentValues();



        newAudioTrack.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
        System.out.println(fileName);
        //newAudioTrack.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);

        // make sure file not accessible until its finished being written
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            newAudioTrack.put(MediaStore.Audio.Media.IS_PENDING, 1);
        }
        //System.out.println(uri);
        audioUri = resolver.insert(uri, newAudioTrack);
        //System.out.println(audioUri);

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            externalUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        //Uri externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DISPLAY_NAME
                //MediaStore.Images.Media.DATE_ADDED,
                //MediaStore.MediaColumns.TITLE,
                //MediaStore.Images.Media.MIME_TYPE,
                //MediaStore.MediaColumns.RELATIVE_PATH,
                //MediaStore.Images.Media.ORIENTATION
        };
        mCursor = getApplicationContext().getContentResolver().query(externalUri, projection, null, null, MediaStore.Audio.Media.DATE_ADDED + " DESC");

        // set adapter for tile data
        mTileAdapter = new TileAdapter();
        gridView.setAdapter(mTileAdapter);
        // on click listener for click event of photo tile
//        gridView.setOnItemClickListener(((adapterView, view, i, l) -> {
//            // do something when clicked on
//            System.out.println("clicked on one");
//        }));
    }

    // view adapter to create tiles for the audio clips
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

            Button audioButton = convertView.findViewById(R.id.button);


            //FileDescriptor fd = getResources().openRawResourceFd(R.raw.applause).getFileDescriptor();
            //System.out.println(fd.toString());
            //for (int j = 0; j < 13; j++)
            //{
            //    getResources().raw
            //}

//            ViewHolder vhDefault;
//
//            Field[] fields = R.raw.class.getFields();
//            for (Field field : fields) {
//                InputStream is = getResources().openRawResource(R.raw.applause);
//            }

            // load default media
            new AsyncTask<ViewHolder,Void,String>() {
                private ViewHolder vh;
                Uri mediaPath = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.applause);
                @Override
                protected String doInBackground(ViewHolder... viewHolders) {
                    //String mimeType = getContentResolver().getType(mediaPath);
                    //System.out.println(mediaPath.getPath());
                    //Cursor cursor = getContentResolver().query(mediaPath, null, null, null, null);
                    //int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    //int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    //cursor.moveToFirst();
                    //TextView nameView = (TextView) findViewById(R.id.filename_text);
                    //TextView sizeView = (TextView) findViewById(R.id.filesize_text);
                    //nameView.setText(returnCursor.getString(nameIndex));
                    //sizeView.setText(Long.toString(returnCursor.getLong(sizeIndex)));
                    //audioButton.setText(cursor.getString(nameIndex));
                    NTILES = 13;

                    switch (i) {
                        case 0:
                            audioButton.setText("Applause");
                            audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MediaPlayer playerDefault;
                                    playerDefault = MediaPlayer.create(getApplicationContext(), R.raw.applause);
                                    playerDefault.start();
                                }
                            });
                            break;
                        case 1:
                            audioButton.setText("Bicycle Bell");
                            audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MediaPlayer playerDefault;
                                    playerDefault = MediaPlayer.create(getApplicationContext(), R.raw.bicycle_bell);
                                    playerDefault.start();
                                }
                            });
                            break;
                        case 2:
                            audioButton.setText("Boooooo");
                            audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MediaPlayer playerDefault;
                                    playerDefault = MediaPlayer.create(getApplicationContext(), R.raw.boooooo);
                                    playerDefault.start();
                                }
                            });
                            break;
                        case 3:
                            audioButton.setText("Cheering");
                            audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MediaPlayer playerDefault;
                                    playerDefault = MediaPlayer.create(getApplicationContext(), R.raw.cheering);
                                    playerDefault.start();
                                }
                            });
                            break;
                        case 4:
                            audioButton.setText("Duck");
                            audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MediaPlayer playerDefault;
                                    playerDefault = MediaPlayer.create(getApplicationContext(), R.raw.duck);
                                    playerDefault.start();
                                }
                            });
                            break;
                        case 5:
                            audioButton.setText("Fanfare");
                            audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MediaPlayer playerDefault;
                                    playerDefault = MediaPlayer.create(getApplicationContext(), R.raw.fanfare);
                                    playerDefault.start();
                                }
                            });
                            break;
                        case 6:
                            audioButton.setText("Gong");
                            audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MediaPlayer playerDefault;
                                    playerDefault = MediaPlayer.create(getApplicationContext(), R.raw.gong);
                                    playerDefault.start();
                                }
                            });
                            break;
                        case 7:
                            audioButton.setText("Gunshot");
                            audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MediaPlayer playerDefault;
                                    playerDefault = MediaPlayer.create(getApplicationContext(), R.raw.gunshot);
                                    playerDefault.start();
                                }
                            });
                            break;
                        case 8:
                            audioButton.setText("Hail to the king");
                            audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MediaPlayer playerDefault;
                                    playerDefault = MediaPlayer.create(getApplicationContext(), R.raw.hail_to_the_king);
                                    playerDefault.start();
                                }
                            });
                            break;
                        case 9:
                            audioButton.setText("I feel good");
                            audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MediaPlayer playerDefault;
                                    playerDefault = MediaPlayer.create(getApplicationContext(), R.raw.i_feel_good);
                                    playerDefault.start();
                                }
                            });
                            break;
                        case 10:
                            audioButton.setText("Laugh");
                            audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MediaPlayer playerDefault;
                                    playerDefault = MediaPlayer.create(getApplicationContext(), R.raw.laugh);
                                    playerDefault.start();
                                }
                            });
                            break;
                        case 11:
                            audioButton.setText("Ricochet");
                            audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MediaPlayer playerDefault;
                                    playerDefault = MediaPlayer.create(getApplicationContext(), R.raw.ricochet);
                                    playerDefault.start();
                                }
                            });
                            break;
                        case 12:
                            audioButton.setText("Sheep");
                            audioButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MediaPlayer playerDefault;
                                    playerDefault = MediaPlayer.create(getApplicationContext(), R.raw.sheep);
                                    playerDefault.start();
                                }
                            });
                            break;
                        default:
                            audioButton.setText("Clip");
                    }




                    return mediaPath.getPath();
                }
            }.execute(vh);//executeOnExecutor(mExecutor,vh);
            return convertView;

            // make an AsyncTask to load the user files
//            new AsyncTask<ViewHolder,Void,String>() {
//                private ViewHolder vh;
//                //Uri mediaPath = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.applause);
//
//                @Override
//                protected String doInBackground(ViewHolder... params) {
//
//
//
//                    // get count of how many audio clips there are to know how many tiles needed
//                    mCursor.moveToLast();
//                    NTILES = mCursor.getPosition();
//
//                    // Create a tile for each audio file
//                    mCursor.moveToPosition(i);
//                    String id;
//                    //String imageOrientation;
//                    InputStream is = null;
//                    id=mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID));
//                    String fileName = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
//                    //System.out.println(fileName);
//                    audioButton.setText(fileName);
//                    //imageOrientation = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION));
//                    //Bitmap bitmap = null;
//                    //try {
////                        is=getContentResolver().openInputStream(Uri.withAppendedPath(MediaStore.Images.Media.
////                                EXTERNAL_CONTENT_URI,id));
////
////                        // decode and downscale the image to create a preview for thumbnails
////                        BitmapFactory.Options options = new BitmapFactory.Options();
////                        options.inSampleSize = 8;
////                        bitmap = BitmapFactory.decodeStream(is, null, options);
////                        //Bitmap bitmapCopy = bitmap;
////                        // scale all bitmaps created to a set size for consistency in gridview display
////                        bitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, false);
////
////                        int rotateDeg = 0;
////                        // Check if image needs rotating
////                        if (imageOrientation != null) {
////                            // based on given orientation, rotate it back to be upright
////                            if (imageOrientation.equals("90")) {
////                                rotateDeg = 90;
////                            }
////                            if (imageOrientation.equals("-90")) {
////                                rotateDeg = -90;
////                            }
////                            if (imageOrientation.equals("180")) {
////                                rotateDeg = 180;
////                            }
////                            Bitmap bitmapRotate = null;
////                            // perform rotation using calculated degree above
////                            Matrix matrix = new Matrix();
////                            matrix.postRotate(rotateDeg);
////                            // create rorated bitmap and update original bitmap with this new one
////                            bitmapRotate = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
////                            bitmap = bitmapRotate;
////                        }
//
//                    //} catch (FileNotFoundException e) {
//                        //e.printStackTrace();
//                    //}
//                    vh=params[0];
//                    return null;
//                }
//            }.execute(vh);//executeOnExecutor(mExecutor,vh);
//            return convertView;
//            //return null;
        }
    }
}
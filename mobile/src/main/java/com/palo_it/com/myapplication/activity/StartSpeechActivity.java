package com.palo_it.com.myapplication.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.palo_it.com.myapplication.R;
import com.palo_it.com.myapplication.actions.TextToSpeechAction;
import com.palo_it.com.myapplication.drone.*;
import com.palo_it.com.myapplication.speech.activation.SpeechActivationService;
import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;

import java.util.concurrent.atomic.AtomicBoolean;

@Deprecated
public class StartSpeechActivity extends AppCompatActivity {

    public static final String TAG = "speech";
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    public static final int DATA_CHECK_CODE = 0;
    public static final String WAKE_UP_PHRASE = "ok robot";
    private TextView mText;
    private TextToSpeechAction tts;
    private SpeechRecognizer speechRecognizer;
    private JSDrone mJSDrone;
    private ProgressDialog progressDialog;
    private AtomicBoolean recreating = new AtomicBoolean(false);

    //    private Handler droneActionsHandler;
    private DroneService droneService;
    //    private TextInterpreter textInterpreter;
    private AsyncTask<String, Void, Void> runDroneTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        try {
//            textInterpreter = TextInterpreter.getInstance(getAssets().open("sumonto.owl"));
//        } catch (IOException e) {
//            Log.e(TAG, "Error opening ontology file", e);
//        }
//        droneActionsHandler = new Handler(Looper.getMainLooper());
        setContentView(R.layout.activity_start_speech);
        setupActions();
        droneService = DroneService.getInstance();
        final Handler handler = new Handler(this.getMainLooper());
        if (mJSDrone == null) {
            progressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Waiting for drone to come online...");
            progressDialog.show();
            droneService.initDiscoveryService(this, new DroneReadyListener() {
                @Override
                public void onDroneReady(JSDrone drone) {
                    mJSDrone = drone;
                    drone.setAsyncListener(new JSDroneStatusListener() {
                        @Override
                        public void asyncReceiver(Runnable task) {
                            handler.post(task);
                        }
                    });
                    isDroneConnected();
                    drone.addListener(new JSDroneListener() {
                        @Override
                        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
                        }

                        @Override
                        public void onBatteryChargeChanged(int batteryPercentage) {
                            Log.d(TAG, "Drone battery update: " + batteryPercentage);
                        }

                        @Override
                        public void onPictureTaken
                                (ARCOMMANDS_JUMPINGSUMO_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {

                        }

                        @Override
                        public void onAudioStateReceived(boolean inputEnabled, boolean outputEnabled) {

                        }

                        @Override
                        public void configureDecoder(ARControllerCodec codec) {

                        }

                        @Override
                        public void onFrameReceived(ARFrame frame) {

                        }

                        @Override
                        public void configureAudioDecoder(ARControllerCodec codec) {
                        }

                        @Override
                        public void onAudioFrameReceived(ARFrame frame) {

                        }

                        @Override
                        public void onMatchingMediasFound(int nbMedias) {

                        }

                        @Override
                        public void onDownloadProgressed(String mediaName, int progress) {

                        }

                        @Override
                        public void onDownloadComplete(String mediaName) {

                        }
                    });
                    doConnectDrone();
                }

                @Override
                public boolean doConnectDrone() {
                    if (mJSDrone.connect()) {
                        View viewById = findViewById(R.id.jumpBt);
                        viewById.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                v.setPressed(true);
                                mJSDrone.doJump(JSDrone.JumpingStyle.HIGH);
                            }
                        });
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean isDroneConnected() {
                    if (mJSDrone.getConnectionState().equals(ARCONTROLLER_DEVICE_STATE_ENUM
                            .ARCONTROLLER_DEVICE_STATE_RUNNING)) {
                        progressDialog.setMessage("Drone online!");
                        View viewById = findViewById(R.id.jumpBt);
                        viewById.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                        setupActions();
                        return true;
                    }
                    return false;
                }
            });
            mText = (TextView) findViewById(R.id.spokenText);
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                    PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        } else {
//            doConnectDrone();
//            setupActions();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
        if (!recreating.get()) {
//            new AsyncTask() {
//                @Override
//                protected Object doInBackground(Object[] params) {
//                    if (mJSDrone != null) {
////                    progressDialog.setMessage("Disconnecting ...");
////                    progressDialog.show();
//                        if (mJSDrone.disconnect()) {
////                        progressDialog.dismiss();
//                            droneService.closeServices();
//                        }
//                    }
//                    return null;
//                }
//            }.execute();
        }
        if (tts != null) {
            tts.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
            speechRecognizer.destroy();
        }
//        Intent intent = new Intent(this, WakeUpWordActivity.class);
//        startActivity(intent);
    }

    private void setupActions() {
//        check for TTS data
//        Intent checkTTSIntent = new Intent();
//        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//        startActivityForResult(checkTTSIntent, DATA_CHECK_CODE);

        Intent intent = SpeechActivationService.makeStartServiceIntent(this);
        StartSpeechActivity.this.startService(intent);
//        Log.d(TAG, "started service for " + SpeechActivationService.WAKE_UP_PHRASES);

//        speechRecognizer = SpeechToTextAction.createRecognizer(this, new SpeechActionListener() {
//            @Override
//            public boolean partialResult(final String partialText) {
//                mText.setText(partialText);
//                final String matchedText = textInterpreter.matchText(partialText);
//                if (matchedText != null) {
//                    Log.d(TAG, "Running Action: " + matchedText);
//                    say(matchedText);
//                    runDroneTask = new AsyncTask<String, Void, Void>() {
//                        private long startTime;
//
//                        @Override
//                        protected void onPreExecute() {
//                            Log.d(TAG, "Starting execute robot command... ");
//                            startTime = System.currentTimeMillis();
//                        }
//
//                        @Override
//                        protected Void doInBackground(String... params) {
//                            if (mJSDrone != null) {
//                                for (String text : params) {
//                                    mJSDrone.doSomething(EnumUtils.getEnum(JSDrone.ACTIONS.class, text.toUpperCase
// ()));
//                                }
//                            }
//                            Log.d(TAG, "robot command executed in:  " + (System.currentTimeMillis() - startTime) +
// " ms");
//                            return null;
//                        }
//                    }.execute(matchedText);
////                    droneActionsHandler.post(new Runnable() {
////                        @Override
////                        public void run() {
////
////                        }
////                    });
//                    return true;
//                }
//                return false;
//            }

//            @Override
//            public void finalResult(String finalText) {
//                try {
//                    Thread.sleep(2000);
//                    recreating.set(true);
//                    if (runDroneTask != null && !runDroneTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
//                        runDroneTask.get();
//                    }
//                    recreate();
//                    recreating.set(false);
//                } catch (InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, WAKE_UP_PHRASES);
    }

    private void say(String partialText) {
        if (tts != null) {
            if (TextToSpeech.ERROR == tts.speak(partialText)) {
                Log.d(TAG, "Can't speak!");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeechAction(this);
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start_speech, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

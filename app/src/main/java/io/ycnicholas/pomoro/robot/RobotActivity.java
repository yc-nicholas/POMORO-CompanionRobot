package io.ycnicholas.pomoro.robot;

import io.ycnicholas.pomoro.constant.Command;
import io.ycnicholas.pomoro.constant.DirectionState;
import io.ycnicholas.pomoro.constant.ExtraKey;
import io.ycnicholas.pomoro.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter;


public class RobotActivity extends Activity implements CameraManager.CameraManagerListener, Callback,
        SocketConnectionManager.ConnectionListener, SocketConnectionManager.ControllerCommandListener, SocketConnectionManager.SendCommandListener,
        BluetoothService.OnBluetoothEventCallback,
        TextToSpeech.OnInitListener  {

    public static final String TAG = "RobotActivity";

    private static final int TAKE_PICTURE_COOLDOWN = 1000;
    private RelativeLayout layoutParent;
    private SurfaceView surfacePreview;
    private AnimationDrawable blinkAnimation, talkAnimation;
    private ImageView emojiBlinkView, emojiTalkView;

    private int movementSpeed = 0;
    private int lastPictureTakenTime = 0;
    private int directionState = DirectionState.STOP;

    private SocketConnectionManager socketConnectionManager;
    private CameraManager cameraManager;
    private OrientationManager orientationManager;
    private TextToSpeech tts;
    private BluetoothService bluetoothService;
    private BluetoothWriter bluetoothWriter;

    private int imageQuality;
    private boolean isControllerConnected = false;

    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        setContentView(R.layout.activity_robot);

        String password = getIntent().getExtras().getString(ExtraKey.OWN_PASSWORD);
        int selectedPreviewSize = getIntent().getExtras().getInt(ExtraKey.PREVIEW_SIZE);
        imageQuality = getIntent().getExtras().getInt(ExtraKey.QUALITY);

        surfacePreview = (SurfaceView) findViewById(R.id.surface_preview);
        surfacePreview.getHolder().addCallback(this);
        surfacePreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        layoutParent = (RelativeLayout) findViewById(R.id.layout_parent);
        layoutParent.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cameraManager.requestAutoFocus();
            }
        });

        // Load the ImageView that will host the animation and
        // set its foreground to our AnimationDrawable XML resource.
        emojiBlinkView = (ImageView) findViewById(R.id.emojiBlinkView);
        emojiBlinkView.setImageDrawable(getResources().getDrawable(
                R.drawable.emoji_blink_animation));
        emojiBlinkView.setBackgroundColor(Color.rgb(253, 209, 100));

        emojiTalkView = (ImageView) findViewById(R.id.emojiTalkView);
        emojiTalkView.setImageDrawable(getResources().getDrawable(
                R.drawable.emoji_talk_animation));
        emojiTalkView.setBackgroundColor(Color.rgb(253, 209, 100));
        emojiTalkView.setVisibility(View.INVISIBLE);

        // Get the drawables, which has been compiled to an AnimationDrawable object
        blinkAnimation = (AnimationDrawable) emojiBlinkView.getDrawable();
        talkAnimation = (AnimationDrawable) emojiTalkView.getDrawable();

        blinkAnimation.start();

        bluetoothService = BluetoothService.getDefaultInstance();
        bluetoothWriter = new BluetoothWriter(bluetoothService);

        socketConnectionManager = new SocketConnectionManager(password);
        socketConnectionManager.setConnectionListener(this);
        socketConnectionManager.setCommandListener(this);
        socketConnectionManager.setSendCommandListener(this);

        orientationManager = new OrientationManager(this);
        cameraManager = new CameraManager(selectedPreviewSize);
        cameraManager.setCameraManagerListener(this);

        tts = new TextToSpeech(this, this);
        tts.setOnUtteranceProgressListener(new ttsUtteranceListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothService.setOnEventCallback(this);
    }

    class ttsUtteranceListener extends UtteranceProgressListener {

        @Override
        public void onDone(String utteranceId) {
            startBlinkAnimation();
        }

        @Override
        public void onError(String utteranceId) {
        }

        @Override
        public void onStart(String utteranceId) {
            startTalkAnimation();
        }
    }

    public void onStop() {
        super.onStop();
        socketConnectionManager.stop();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        finish();
    }

    private void startBlinkAnimation() {
        runOnUiThread(new Thread(new Runnable() {
            public void run() {
                // Stop talking animation
                talkAnimation.stop();
                emojiTalkView.setVisibility(View.INVISIBLE);
                // Resume blink animation
                emojiBlinkView.setVisibility(View.VISIBLE);
                blinkAnimation.start();
            }
        }));
    }

    private void startTalkAnimation() {
        runOnUiThread(new Thread(new Runnable() {
            public void run() {
                // Stop current animation
                blinkAnimation.stop();
                emojiBlinkView.setVisibility(View.INVISIBLE);
                emojiTalkView.setVisibility(View.VISIBLE);
                // Start talking animation
                talkAnimation.start();
            }
        }));
    }

    private void speak(String text) {
        if (text != null) {
            HashMap<String, String> myHashSpeech = new HashMap<String, String>();
            myHashSpeech.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                    "HELLO");
            if (!tts.isSpeaking()) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, myHashSpeech);
            }

        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }else{
                speak("Hey, human! You are looking great!");
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.enable_tts), Toast.LENGTH_SHORT).show();
            Log.e("TTS", "Initilization Failed!");
        }

        // onInit won't fired until after an asynctask has finished execution. Hence, we only start socket connection from here
        socketConnectionManager.start();
    }


    @Override
    public void onDeviceName(String deviceName) {
        Log.d(TAG, "onDeviceName: " + deviceName);
    }

    @Override
    public void onToast(String message) {
        Log.d(TAG, "onToast");
    }

    @Override
    public void onStatusChange(BluetoothStatus status) {
        Log.d(TAG, "onStatusChange: " + status);
        if (status == BluetoothStatus.NONE){
            showToast("Bluetooth connection is down!");
            finish();
        }
    }

    @Override
    public void onDataRead(byte[] buffer, int length) {
        Log.d(TAG, "onDataRead: " + new String(buffer, 0, length));
    }

    @Override
    public void onDataWrite(byte[] buffer) {
        Log.d(TAG, "onDataWrite: " + new String(buffer));
    }

    @Override
    public void onDataIncoming() {
        //clearCheckBox();
    }

    @Override
    public void onControllerConnected() {
        isControllerConnected = true;
        socketConnectionManager.sendCommand(Command.ACCEPT_CONNECTION);
    }

    @Override
    public void onWrongPassword() {
        socketConnectionManager.sendCommand(Command.WRONG_PASSWORD);
        socketConnectionManager.restart();
    }

    @Override
    public void onControllerDisconnected() {
        showToast(getString(R.string.connection_down));
    }

    @Override
    public void onControllerClosed() {
        isControllerConnected = false;
    }

    @Override
    public void onFlashCommand(String command) {
        if (cameraManager.isFlashAvailable()) {
            if (command.equals(Command.LED_ON)) {
                cameraManager.requestFlashOn();
            } else if (command.equals(Command.LED_OFF)) {
                cameraManager.requestFlashOff();
            }
        } else {
            socketConnectionManager.sendCommand(Command.FLASH_UNAVAILABLE);
        }
    }

    @Override
    public void onRequestTakePicture() {
        double currentTimeSeconds = System.currentTimeMillis();
        if (currentTimeSeconds - lastPictureTakenTime > TAKE_PICTURE_COOLDOWN) {
            lastPictureTakenTime = (int) currentTimeSeconds;
            cameraManager.requestTakePicture();
        }
    }

    @Override
    public void onRequestAutoFocus() {
        cameraManager.requestAutoFocus();
    }

    @Override
    public void onMoveForwardCommand(int movementSpeed) {
        directionState = DirectionState.UP;
        bluetoothWriter.write("f");
    }

    @Override
    public void onMoveForwardRightCommand(int movementSpeed) {
        directionState = DirectionState.UPRIGHT;
        bluetoothWriter.write("x");
    }

    @Override
    public void onMoveForwardLeftCommand(int movementSpeed) {
        directionState = DirectionState.UPLEFT;
        bluetoothWriter.write("w");
    }

    @Override
    public void onMoveBackwardCommand(int movementSpeed) {
        directionState = DirectionState.DOWN;
        bluetoothWriter.write("b");
    }

    @Override
    public void onMoveBackwardRightCommand(int movementSpeed) {
        directionState = DirectionState.DOWNRIGHT;
        bluetoothWriter.write("z");
    }

    @Override
    public void onMoveBackwardLeftCommand(int movementSpeed) {
        directionState = DirectionState.DOWNLEFT;
        bluetoothWriter.write("y");
    }

    @Override
    public void onMoveLeftCommand(int movementSpeed) {
        directionState = DirectionState.LEFT;
        bluetoothWriter.write("l");
    }

    @Override
    public void onMoveRightCommand(int movementSpeed) {
        directionState = DirectionState.RIGHT;
        bluetoothWriter.write("r");
    }

    @Override
    public void onMoveStopCommand() {
        directionState = DirectionState.STOP;
        bluetoothWriter.write("s");
    }

    @Override
    public void onSendCommandSuccess() {
    }

    @Override
    public void onSendCommandFailure() {
        isControllerConnected = false;
    }

    @SuppressWarnings("deprecation")
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        if (surfacePreview == null)
            return;

        cameraManager.stopCameraPreview();
        cameraManager.initCameraParameter();

        setupPreviewLayout();

        cameraManager.setCameraOrientation(orientationManager.getOrientation());
        cameraManager.startCameraPreview(surfacePreview);
    }

    @SuppressWarnings("deprecation")
    public void setupPreviewLayout() {
        Display display = getWindowManager().getDefaultDisplay();
        LayoutParams lp = layoutParent.getLayoutParams();

        float previewWidth = cameraManager.getPreviewSize().width;
        float previewHeight = cameraManager.getPreviewSize().height;

        int orientation = orientationManager.getOrientation();
        float ratio = 0;
        if (orientation == OrientationManager.LANDSCAPE_NORMAL
                || orientation == OrientationManager.LANDSCAPE_REVERSE) {
            ratio = previewWidth / previewHeight;
        } else if (orientation == OrientationManager.PORTRAIT_NORMAL
                || orientation == OrientationManager.PORTRAIT_REVERSE) {
            ratio = previewHeight / previewWidth;
        }
        if ((int) ((float) surfacePreview.getWidth() / ratio) >= display.getHeight()) {
            lp.height = (int) ((float) surfacePreview.getWidth() / ratio);
            lp.width = surfacePreview.getWidth();
        } else {
            lp.height = surfacePreview.getHeight();
            lp.width = (int) ((float) surfacePreview.getHeight() * ratio);
        }

        layoutParent.setLayoutParams(lp);
        int locationX = (int) (lp.width / 2.0 - surfacePreview.getWidth() / 2.0);
        layoutParent.animate().translationX(-locationX);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        cameraManager.createCameraInstance(holder);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        cameraManager.destroyCameraInstance();
    }

    @Override
    public void onPictureTaken(String filename, String path) {
        socketConnectionManager.sendCommand(Command.SNAP);
    }

    @Override
    public void onPreviewTaken(Bitmap bitmap) {
        if (isControllerConnected) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, bos);
            socketConnectionManager.sendImageData(bos.toByteArray());
        }
    }

    @Override
    public void onPreviewOutOfMemory(OutOfMemoryError e) {
        e.printStackTrace();
        showToast(getString(R.string.out_of_memory));
        finish();
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

//    class Looper extends BaseIOIOLooper {
//        DigitalOutput D1A, D1B, D2A, D2B, D3A, D3B, D4A, D4B;
//        PwmOutput PWM1, PWM2, PWM3, PWM4;
//
//        protected void setup() throws ConnectionLostException {
//            ioio_.openDigitalOutput(0, false);
//            D1A = ioio_.openDigitalOutput(1, false);
//            D1B = ioio_.openDigitalOutput(2, false);
//            D2A = ioio_.openDigitalOutput(4, false);
//            D2B = ioio_.openDigitalOutput(5, false);
//            D3A = ioio_.openDigitalOutput(16, false);
//            D3B = ioio_.openDigitalOutput(17, false);
//            D4A = ioio_.openDigitalOutput(18, false);
//            D4B = ioio_.openDigitalOutput(19, false);
//            PWM1 = ioio_.openPwmOutput(3, 100);
//            PWM1.setDutyCycle(0);
//            PWM2 = ioio_.openPwmOutput(6, 100);
//            PWM2.setDutyCycle(0);
//            PWM3 = ioio_.openPwmOutput(13, 100);
//            PWM3.setDutyCycle(0);
//            PWM4 = ioio_.openPwmOutput(14, 100);
//            PWM4.setDutyCycle(0);
//
//            showToastFromIOIO(getString(R.string.connected));
//        }
//
//        public void loop() throws ConnectionLostException, InterruptedException {
//            if (directionState == DirectionState.UP) {
//                PWM1.setDutyCycle((float) movementSpeed / 100);
//                PWM2.setDutyCycle((float) movementSpeed / 100);
//                PWM3.setDutyCycle((float) movementSpeed / 100);
//                PWM4.setDutyCycle((float) movementSpeed / 100);
//                D1A.write(true);
//                D1B.write(false);
//                D2A.write(true);
//                D2B.write(false);
//                D3A.write(true);
//                D3B.write(false);
//                D4A.write(true);
//                D4B.write(false);
//            } else if (directionState == DirectionState.DOWN) {
//                PWM1.setDutyCycle((float) movementSpeed / 100);
//                PWM2.setDutyCycle((float) movementSpeed / 100);
//                PWM3.setDutyCycle((float) movementSpeed / 100);
//                PWM4.setDutyCycle((float) movementSpeed / 100);
//                D1A.write(false);
//                D1B.write(true);
//                D2A.write(false);
//                D2B.write(true);
//                D3A.write(false);
//                D3B.write(true);
//                D4A.write(false);
//                D4B.write(true);
//            } else if (directionState == DirectionState.LEFT) {
//                PWM1.setDutyCycle((float) movementSpeed / 100);
//                PWM2.setDutyCycle((float) movementSpeed / 100);
//                PWM3.setDutyCycle((float) movementSpeed / 100);
//                PWM4.setDutyCycle((float) movementSpeed / 100);
//                D1A.write(false);
//                D1B.write(true);
//                D2A.write(false);
//                D2B.write(true);
//                D3A.write(true);
//                D3B.write(false);
//                D4A.write(true);
//                D4B.write(false);
//            } else if (directionState == DirectionState.RIGHT) {
//                PWM1.setDutyCycle((float) movementSpeed / 100);
//                PWM2.setDutyCycle((float) movementSpeed / 100);
//                PWM3.setDutyCycle((float) movementSpeed / 100);
//                PWM4.setDutyCycle((float) movementSpeed / 100);
//                D1A.write(true);
//                D1B.write(false);
//                D2A.write(true);
//                D2B.write(false);
//                D3A.write(false);
//                D3B.write(true);
//                D4A.write(false);
//                D4B.write(true);
//            } else if (directionState == DirectionState.UPRIGHT) {
//                PWM1.setDutyCycle((((float) movementSpeed / (float) 1.5) + 20) / 100);
//                PWM2.setDutyCycle((((float) movementSpeed / (float) 1.5) + 20) / 100);
//                PWM3.setDutyCycle((((float) movementSpeed / (float) 1.5) - 20) / 100);
//                PWM4.setDutyCycle((((float) movementSpeed / (float) 1.5) - 20) / 100);
//                D1A.write(true);
//                D1B.write(false);
//                D2A.write(true);
//                D2B.write(false);
//                D3A.write(true);
//                D3B.write(false);
//                D4A.write(true);
//                D4B.write(false);
//            } else if (directionState == DirectionState.UPLEFT) {
//                PWM1.setDutyCycle((((float) movementSpeed / (float) 1.5) - 20) / 100);
//                PWM2.setDutyCycle((((float) movementSpeed / (float) 1.5) - 20) / 100);
//                PWM3.setDutyCycle((((float) movementSpeed / (float) 1.5) + 20) / 100);
//                PWM4.setDutyCycle((((float) movementSpeed / (float) 1.5) + 20) / 100);
//                D1A.write(true);
//                D1B.write(false);
//                D2A.write(true);
//                D2B.write(false);
//                D3A.write(true);
//                D3B.write(false);
//                D4A.write(true);
//                D4B.write(false);
//            } else if (directionState == DirectionState.DOWNRIGHT) {
//                PWM1.setDutyCycle((((float) movementSpeed / (float) 1.5) + 20) / 100);
//                PWM2.setDutyCycle((((float) movementSpeed / (float) 1.5) + 20) / 100);
//                PWM3.setDutyCycle((((float) movementSpeed / (float) 1.5) - 20) / 100);
//                PWM4.setDutyCycle((((float) movementSpeed / (float) 1.5) - 20) / 100);
//                D1A.write(false);
//                D1B.write(true);
//                D2A.write(false);
//                D2B.write(true);
//                D3A.write(false);
//                D3B.write(true);
//                D4A.write(false);
//                D4B.write(true);
//            } else if (directionState == DirectionState.DOWNLEFT) {
//                PWM1.setDutyCycle((((float) movementSpeed / (float) 1.5) - 20) / 100);
//                PWM2.setDutyCycle((((float) movementSpeed / (float) 1.5) - 20) / 100);
//                PWM3.setDutyCycle((((float) movementSpeed / (float) 1.5) + 20) / 100);
//                PWM4.setDutyCycle((((float) movementSpeed / (float) 1.5) + 20) / 100);
//                D1A.write(false);
//                D1B.write(true);
//                D2A.write(false);
//                D2B.write(true);
//                D3A.write(false);
//                D3B.write(true);
//                D4A.write(false);
//                D4B.write(true);
//            } else if (directionState == DirectionState.STOP) {
//                PWM1.setDutyCycle(0);
//                PWM2.setDutyCycle(0);
//                PWM3.setDutyCycle(0);
//                PWM4.setDutyCycle(0);
//                D1A.write(false);
//                D1B.write(false);
//                D2A.write(false);
//                D2B.write(false);
//                D3A.write(false);
//                D3B.write(false);
//                D4A.write(false);
//                D4B.write(false);
//            }
//
//            Thread.sleep(20);
//        }
//
//        public void disconnected() {
//            showToastFromIOIO(getString(R.string.disconnected));
//        }
//
//        public void incompatible() {
//            showToastFromIOIO(getString(R.string.incompatible_firmware));
//        }
//
//        public void showToastFromIOIO(final String mesage) {
//            runOnUiThread(new Runnable() {
//                public void run() {
//                    showToast(mesage);
//                }
//            });
//        }
//    }
//
//    protected IOIOLooper createIOIOLooper() {
//        return new Looper();
//    }
}

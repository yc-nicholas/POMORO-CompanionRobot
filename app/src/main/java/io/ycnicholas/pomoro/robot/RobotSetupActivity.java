package io.ycnicholas.pomoro.robot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.ycnicholas.pomoro.R;
import io.ycnicholas.pomoro.constant.ExtraKey;
import io.ycnicholas.pomoro.utils.Utilities;

/**
 * Created by Akexorcist on 9/5/15 AD.
 * Modified by yc-nicholas on 18/07/21
 */

public class RobotSetupActivity extends Activity implements OnClickListener, OnSeekBarChangeListener, BluetoothService.OnBluetoothScanCallback, BluetoothService.OnBluetoothEventCallback {
    public static final String TAG = "RobotSetupActivity";
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int FINE_LOCATION_PERMISSION_CODE = 102;
    private static final int BACKGROUND_LOCATION_PERMISSION_CODE = 103;

    private TextView tvImageQuality;
    private TextView tvIpAddress;
    private TextView tvRobotStatus;
    private TextView tvBtStatus;
    private EditText etPassword;
    private SeekBar sbImageQuality;
    private Button btnOk;
    private Button btnConfBluetooth;
    private Button btnPreviewSizeChooser;
    private ArrayList<String> previewSizeList;
    ArrayAdapter<String> bluetoothDeviceListAdapter;

    private int selectedSizePosition;
    private String bluetoothAddress;
    private String bluetoothName;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothService bluetoothService;
    private ArrayList<BluetoothDevice> bluetoothDevices;

    private boolean bluetoothConfigured = false;
    private boolean robotConnected = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_robot_setup);

        SharedPreferences settings = getSharedPreferences(ExtraKey.SETUP_PREFERENCE, Context.MODE_PRIVATE);
        selectedSizePosition = settings.getInt(ExtraKey.PREVIEW_SIZE, 0);
        String password = settings.getString(ExtraKey.OWN_PASSWORD, "");
        int quality = settings.getInt(ExtraKey.QUALITY, 100);
        bluetoothAddress = settings.getString(ExtraKey.BLUETOOTH_ADDRESS,"");
        bluetoothName = settings.getString(ExtraKey.BLUETOOTH_NAME, "");

        etPassword = (EditText) findViewById(R.id.et_password);
        etPassword.setText(password);

        btnPreviewSizeChooser = (Button) findViewById(R.id.btn_preview_size_chooser);
        btnPreviewSizeChooser.setOnClickListener(this);

        btnConfBluetooth = (Button) findViewById(R.id.btn_bt_config);
        btnConfBluetooth.setOnClickListener(this);

        tvImageQuality = (TextView) findViewById(R.id.tv_image_quality);
        updateTextViewQuality(quality);

        sbImageQuality = (SeekBar) findViewById(R.id.sb_image_quality);
        sbImageQuality.setProgress(quality);
        sbImageQuality.setOnSeekBarChangeListener(this);

        tvIpAddress = findViewById(R.id.tv_ip_addr);
        tvIpAddress.setText(Utilities.getCurrentIP(getApplicationContext()));

        tvRobotStatus = findViewById(R.id.tv_robot_status);

        btnOk = (Button) findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(this);

        checkPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothService = BluetoothService.getDefaultInstance();
        checkBluetoothConfiguration();
        bluetoothService.setOnScanCallback(this);
        bluetoothService.setOnEventCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothService.disconnect();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_preview_size_chooser) {
            createPreviewSizeChooserDialog();
        } else if (id == R.id.btn_bt_config){
            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,FINE_LOCATION_PERMISSION_CODE);
        } else if (id == R.id.btn_ok) {
            confirmSetup();
        }
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            if(requestCode==CAMERA_PERMISSION_CODE)
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,STORAGE_PERMISSION_CODE);
            if(requestCode==STORAGE_PERMISSION_CODE)
                initCameraPreviewSize();
            if(requestCode== FINE_LOCATION_PERMISSION_CODE)
                createConfigBluetoothDialog();
        } else{
            // Requesting the permission
            ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
        }
    }

    public void createPermissionAlertDialog(String message, final boolean exitActivity){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Permission Needed");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // exit this activity
                        if(exitActivity)
                            finish();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initCameraPreviewSize();
                }  else {
                    createPermissionAlertDialog("Please grant permission for camera and storage for the app to function.", true);
                }
                return;
            case STORAGE_PERMISSION_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }  else {
                    createPermissionAlertDialog("Please grant permission for camera and storage for the app to function.", true);
                }
                return;
            case FINE_LOCATION_PERMISSION_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createConfigBluetoothDialog();
                } else{
                    createPermissionAlertDialog("Please grant permission for fine location access for the app to scan bluetooth devices.", false);
                }
                return;
        }
    }

    public void checkBluetoothConfiguration(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        bluetoothDevices = new ArrayList<>();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if(device.getAddress().equals(bluetoothAddress)){
                    bluetoothConfigured = true;
                    updateSelectedBluetoothDevice();
                    connectRobot(device);
                }
                bluetoothDevices.add(device);
            }
        }
        if(!bluetoothConfigured){
            tvRobotStatus.setText(getString(R.string.config_bluetooth));
            tvRobotStatus.setTextColor(getResources().getColor(R.color.black));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        saveImageQuality(progress);
        updateTextViewQuality(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public void updateSelectedPreviewSize() {
        String strSize = previewSizeList.get(selectedSizePosition);
        btnPreviewSizeChooser.setText(strSize);
    }

    public void updateSelectedBluetoothDevice() {
        btnConfBluetooth.setText(bluetoothName);
    }

    @SuppressLint("StringFormatMatches")
    public void updateTextViewQuality(int quality) {
        tvImageQuality.setText(getString(R.string.image_quality, quality));
    }

    public void savePassword(String password) {
        getPreferenceEditor().putString(ExtraKey.OWN_PASSWORD, password).apply();
    }

    public void saveImageQuality(int quality) {
        getPreferenceEditor().putInt(ExtraKey.QUALITY, quality).apply();
    }

    public void saveImagePreviewSize(int size) {
        getPreferenceEditor().putInt(ExtraKey.PREVIEW_SIZE, size).apply();
    }

    public void saveBluetoothName(String name){
        getPreferenceEditor().putString(ExtraKey.BLUETOOTH_NAME, name).apply();
    }

    public void saveBluetoothAddress(String address){
        getPreferenceEditor().putString(ExtraKey.BLUETOOTH_ADDRESS, address).apply();
    }

    public SharedPreferences.Editor getPreferenceEditor() {
        SharedPreferences settings = getSharedPreferences(ExtraKey.SETUP_PREFERENCE, Context.MODE_PRIVATE);
        return settings.edit();
    }

    public void goToRobotActivity() {
        Intent intent = new Intent(this, RobotActivity.class);
        intent.putExtra(ExtraKey.OWN_PASSWORD, etPassword.getText().toString());
        intent.putExtra(ExtraKey.PREVIEW_SIZE, selectedSizePosition);
        intent.putExtra(ExtraKey.QUALITY, sbImageQuality.getProgress());
        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    public void initCameraPreviewSize() {
        Camera mCamera;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open();
        } else {
            mCamera = CameraManager.getFrontCameraInstance();
        }
        initPreviewSizeList(CameraManager.getSupportedPreviewSizes(mCamera));
        mCamera.release();
    }

    @SuppressWarnings("deprecation")
    public void initPreviewSizeList(List<Size> previewSize) {
        previewSizeList = new ArrayList<>();
        for (int i = 0; i < previewSize.size(); i++) {
            String str = previewSize.get(i).width + " x " + previewSize.get(i).height;
            previewSizeList.add(str);
        }
        updateSelectedPreviewSize();
    }

    public void createConfigBluetoothDialog(){
        final Dialog configBluetoothDialog = new Dialog(this);
        configBluetoothDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        configBluetoothDialog.setContentView(R.layout.dialog_bluetooth_devices);
        configBluetoothDialog.setCancelable(true);

        ArrayList<String> bluetoothDeviceList = new ArrayList<>();
        for(int index=0; index < bluetoothDevices.size();index++) {
            BluetoothDevice btDevice = bluetoothDevices.get(index);
            bluetoothDeviceList.add(Utilities.getBluetoothDescription(btDevice.getName(),btDevice.getAddress()));
        }
        bluetoothDeviceListAdapter = new ArrayAdapter<>(this, R.layout.view_simple_textview, bluetoothDeviceList);
        ListView lvAvailableBluetoothDevices = configBluetoothDialog.findViewById(R.id.lv_available_bluetooth_devices);
        tvBtStatus = configBluetoothDialog.findViewById(R.id.tv_bt_status);
        lvAvailableBluetoothDevices.setAdapter(bluetoothDeviceListAdapter);
        lvAvailableBluetoothDevices.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                selectedSizePosition = position;
                BluetoothDevice device = bluetoothDevices.get(position);
                saveBluetoothAddress(device.getAddress());
                saveBluetoothName(device.getName());
                updateSelectedBluetoothDevice();
                connectRobot(device);
                configBluetoothDialog.cancel();
            }
        });
        configBluetoothDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                bluetoothService.stopScan();
            }
        });
        bluetoothService.startScan();
        configBluetoothDialog.show();
    }

    public void createPreviewSizeChooserDialog() {
        final Dialog dialogSize = new Dialog(this);
        dialogSize.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogSize.setContentView(R.layout.dialog_camera_size);
        dialogSize.setCancelable(true);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.view_simple_textview, previewSizeList);
        ListView lvAvailablePreviewSize = (ListView) dialogSize.findViewById(R.id.lv_available_preview_size);
        lvAvailablePreviewSize.setAdapter(adapter);
        lvAvailablePreviewSize.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                selectedSizePosition = position;
                saveImagePreviewSize(position);
                updateSelectedPreviewSize();
                dialogSize.cancel();
            }
        });
        dialogSize.show();
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void confirmSetup() {
        String strPassword = etPassword.getText().toString();
        if (strPassword.length() != 0) {
            savePassword(strPassword);
        } else {
            showToast(getString(R.string.password_is_required));
            return;
        }
        if(robotConnected){
        } else {
            showToast(getString(R.string.connect_robot_required));
            return;
        }
        goToRobotActivity();
    }

    private void connectRobot(BluetoothDevice device){
        bluetoothService.connect(device);
    }

    @Override
    public void onDataRead(byte[] buffer, int length) {
        Log.d(TAG, "onDataRead");
    }

    @Override
    public void onStatusChange(BluetoothStatus status) {
        Log.d(TAG, "onStatusChange: " + status);
        if (status == BluetoothStatus.CONNECTED) {
            tvRobotStatus.setText(getString(R.string.connected));
            tvRobotStatus.setTextColor(getResources().getColor(R.color.green));
            robotConnected = true;
        }
        if (status == BluetoothStatus.CONNECTING){
            tvRobotStatus.setText(getString(R.string.connecting));
            tvRobotStatus.setTextColor(getResources().getColor(R.color.orange));
            robotConnected = false;
        }
        if (status == BluetoothStatus.NONE){
            tvRobotStatus.setText(getString(R.string.offline));
            tvRobotStatus.setTextColor(getResources().getColor(R.color.pink));
            robotConnected = false;
        }
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
    public void onDataWrite(byte[] buffer) {
        Log.d(TAG, "onDataWrite");
    }

    @Override
    public void onDeviceDiscovered(BluetoothDevice device, int rssi) {
        Log.d(TAG, "onDeviceDiscovered: " + device.getName() + " - " + device.getAddress() + " - " + Arrays.toString(device.getUuids()));
        int index = bluetoothDevices.indexOf(device);
        if (index < 0) {
            bluetoothDevices.add(device);
        } else {
            bluetoothDevices.set(index, device);
        }
        bluetoothDeviceListAdapter.clear();
        ArrayList<String> bluetoothDeviceList = new ArrayList<>();
        for(BluetoothDevice btDevice : bluetoothDevices){
            bluetoothDeviceList.add(Utilities.getBluetoothDescription(btDevice.getName(),btDevice.getAddress()));
        }
        bluetoothDeviceListAdapter.addAll(bluetoothDeviceList);
        bluetoothDeviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStartScan() {
        Log.d(TAG, "Start BT Scan");
        tvBtStatus.setText(getString(R.string.scanning));
}

    @Override
    public void onStopScan() {
        Log.d(TAG, "Stop BT Scan");
        tvBtStatus.setText(getString(R.string.scan_completed));
    }
}

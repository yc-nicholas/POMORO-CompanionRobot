package io.ycnicholas.pomoro;

import android.app.Application;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothClassicService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothlowenergylibrary.BluetoothLeService;

import java.util.UUID;

/**
 * Created by nicholas on 11 July 2021.
 */
public class MainApplication extends Application {
    /*
     * Change for the UUID that you want.
     */
    private static final UUID UUID_DEVICE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_SERVICE = UUID.fromString("e7810a71-73ae-499d-8c15-faa9aef0c3f2");
    private static final UUID UUID_CHARACTERISTIC = UUID.fromString("bef8d6c9-9c21-4c9e-b632-bd58c1009f9f");

    @Override
    public void onCreate() {
        super.onCreate();

        BluetoothConfiguration config = new BluetoothConfiguration();

        // [TO CONFIGURE] BluetoothClassicService.class or BluetoothLeService.class
        config.bluetoothServiceClass = BluetoothClassicService.class;
        //config.bluetoothServiceClass = BluetoothLeService.class;

        config.context = getApplicationContext();
        config.bufferSize = 1024;
        config.characterDelimiter = '\n';
        config.deviceName = getResources().getString(R.string.app_name);
        config.callListenersInMainThread = true;

        // [TO CONFIGURE]
        //config.uuid = null; // When using BluetoothLeService.class set null to show all devices on scan.
        config.uuid = UUID_DEVICE; // For Classic

        //config.uuidService = UUID_SERVICE; // For BLE
        //config.uuidCharacteristic = UUID_CHARACTERISTIC; // For BLE
        //config.transport = BluetoothDevice.TRANSPORT_LE; // Only for dual-mode devices

        BluetoothService.init(config);
    }

}


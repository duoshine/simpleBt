package chenanduo.bluetoothconnect.bluetooth;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by chen on 2017  封装了和蓝牙的交互函数
 */

public interface BluetoothChangeListener {
    //当前状态
    void onCurrentState(int state);

    //收到回调
    void onBleWriteResult(byte[] result);

    //扫描回调
    void onBleScanResult(List<BluetoothDevice> device);

    //写入成功回调
    void onWriteDataSucceed();
}

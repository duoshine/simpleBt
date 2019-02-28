package cn.chenanduo.simplebt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

/**
 * Created by chen on 2017  封装了和蓝牙的交互函数
 */

public interface BluetoothChangeListener {
    //当前状态
    void onCurrentState(int state);

    //收到回调
    void onBleWriteResult(byte[] result);

    //扫描回调
    void onBleScanResult(BluetoothDevice device);

    //写入成功回调
    void onWriteDataSucceed(byte[] value);

    //当服务已经找到 具备通信条件时回调
    void findServiceSucceed();

    //用于提供uuid
     void getDisplayServices(BluetoothGatt gatt);
}

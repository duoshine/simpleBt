package chenanduo.bluetoothconnect.bluetooth;

import java.util.List;

import chenanduo.bluetoothconnect.bean.DeviceInfoBean;

/**
 * Created by chen on 2017  封装了和蓝牙的交互函数
 */

public interface BluetoothChangeListener {
    //当前状态
    void onCurrentState(int state);

    //收到回调
    void onBleWriteResult(byte[] result);

    //扫描回调
    void onBleScanResult(List<DeviceInfoBean> device);

    //写入成功回调
    void onWriteDataSucceed(byte[] value);
}

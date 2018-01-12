package cn.chenanduo.simplebt;

import java.util.List;

import cn.chenanduo.simplebt.bean.DeviceBean;


/**
 * Created by chen on 2017  封装了和蓝牙的交互函数
 */

public interface BluetoothChangeListener {
    //当前状态
    void onCurrentState(int state);

    //收到回调
    void onBleWriteResult(byte[] result);

    //扫描回调
    void onBleScanResult(List<DeviceBean> device);

    //写入成功回调
    void onWriteDataSucceed(byte[] value);
}

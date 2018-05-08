package chenanduo.bluetoothconnect.github;


/**
 * Created by chen on 2017 封装所有暴露给用户的接口 不需要关注具体实现
 */

public abstract class BleBase {

    // 初始化对本地蓝牙适配器的引用 并判断蓝牙是否是开启状态
    public abstract boolean initialize();

    //清除缓存
    public abstract boolean refreshDeviceCache();

    //返回当前设备连接状态
    public abstract int getBleConnectState();

    // 停止扫描设备
    public abstract void stopScanDevices();

    //开始搜索设备
    public abstract void startScanDevices(final boolean enable);

    //发送指令方法
    public abstract boolean writeCharacteristic(byte[] value);

    //使用完Ble后一定要调用此方法释放资源
    public abstract void close();

    //取消等待连接或断开一个现有的连接
    public abstract void disconnect();

    //根据address连接蓝牙设备
    public abstract boolean connect(final String address);

    //设置每次断开连接都清除缓存
    public abstract BluetoothBLeClass closeCleanCache(boolean isCloseCleanCache);

    //设置扫描过滤
    public abstract BluetoothBLeClass setFiltration(String filtration1,String filtration2);

    //设置断开自动连接
    public abstract BluetoothBLeClass setAutoConnect(boolean isAutoConnect);

    //设置扫描时间 不设置默认5秒
    public abstract BluetoothBLeClass setScanTime(int time);

    //通过此接口回调所有和蓝牙的交互出去给开发者
    public abstract void getBleCurrentState(BluetoothChangeListener bluetoothChangeListener);

    //用于固件升级
    public abstract void setUUID(String service_uuid, String notifi_uuid, String write_uuid);


}

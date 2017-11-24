package chenanduo.bluetoothconnect.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by chen on 5/28/17.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothBLeClass extends BleBase implements LeScanCallback{
    private static final String DISENABLE = "00002902-0000-1000-8000-00805f9b34fb";
    private static BluetoothBLeClass mBLE;
    private final static String TAG = "simpleBtTest";
    //uuid 由构造函数传入
    private static String SERVICE_UUID;
    private static String NOTIFI_UUID;
    private static String WRITE_UUID;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    //本次连接的蓝牙地址
    public String mBluetoothDeviceAddress;
    public BluetoothGatt mBluetoothGatt;
    //扫描时间
    private static long SCAN_PERIOD = 5000;
    //是否在扫描
    private boolean mScanning = false;
    private Handler mHandler = new Handler();
    // 设备连接断开
    public static final int STATE_DISCONNECTED = 0;
    // 设备正在扫描
    public static final int STATE_SCANNING = 1;
    // 设备扫描结束
    public static final int STATE_SCANNED = 2;
    // 设备正在连接
    public static final int STATE_CONNECTING = 3;
    // 设备连接成功
    public static final int STATE_CONNECTED = 4;
    // 正在尝试重连
    public static final int STATE_RESETCONNECT = 5;
    // 当前设备状态
    private int connectionState = STATE_DISCONNECTED;
    //设置自动重连
    public static boolean isAutoConnect = false;
    //蓝牙是否连接
    public static boolean isBleConnect = false;
    //定时器 处理断开自动重连
    private Timer mTimer;
    private static Context mContext;
    //存放扫描到的设备
    private static List<BluetoothDevice> mBlueTooths;
    //用来判断集合中是否已经有重复蓝牙设备
    boolean exist = false;
    //每次断开连接是否清除缓存
    public static boolean isCloseCleanCache = false;
    //写的uuid
    private BluetoothGattCharacteristic mWriteCharacteristic;

    private BluetoothChangeListener mBluetoothChangeListener;

    /**
     * 通过此接口回调所有和蓝牙的交互出去给开发者
     * @param bluetoothChangeListener
     */
    @Override
    public void getBleCurrentState(BluetoothChangeListener bluetoothChangeListener) {
        mBluetoothChangeListener = bluetoothChangeListener;
    }

    private BluetoothBLeClass(Context c) {
        mContext = c;
    }

    public static BluetoothBLeClass getInstane(Context context, String serviceuuid, String notifiuuid, String writeuuid) {
        if (mBLE == null) {
            mContext = context;
            mBLE = new BluetoothBLeClass(mContext);
            mBlueTooths = new ArrayList<>();
            SERVICE_UUID = serviceuuid;
            NOTIFI_UUID = notifiuuid;
            WRITE_UUID = writeuuid;
        }
        return mBLE;
    }

    //设置扫描时间 不设置默认5秒
    @Override
    public BluetoothBLeClass setScanTime(int time) {
        SCAN_PERIOD = time;
        return mBLE;
    }

    //设置断开自动连接
    @Override
    public BluetoothBLeClass setAutoConnect(boolean isAutoConnect) {
        this.isAutoConnect = isAutoConnect;
        return mBLE;
    }

    //设置每次断开连接都清除缓存
    @Override
    public BluetoothBLeClass closeCleanCache(boolean isCloseCleanCache) {
        this.isCloseCleanCache = isCloseCleanCache;
        return mBLE;
    }

    /**
     * 通过实现此callBack管理和Ble交互
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * 蓝牙连接状态
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //连接上蓝牙设备
                initConnected(gatt);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //断开蓝牙连接
                initDisconnected();
            }
        }

        /**
         * 搜索周边服务
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                displayGattServices(gatt);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);

        }

        /**
         * ble终端数据交互的事件
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        /**
         * 收到ble返回数据
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            final BluetoothGattCharacteristic characteristic) {
            if (mBluetoothChangeListener != null) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothChangeListener.onBleWriteResult(characteristic.getValue());
                    }
                });
            }
        }

        /**
         * 写入数据成功回调此方法
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "写入蓝牙设备成功!");
        }
    };

    /*启动通知通道并将给定描述符的值写入到远程设备*/
    private void displayGattServices(BluetoothGatt gatt) {
        if (SERVICE_UUID == null || NOTIFI_UUID == null || WRITE_UUID == null) {
            return;
        }
        BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
        if (service == null) {
            return;
        }
        BluetoothGattCharacteristic notifiCharacteristic = service.getCharacteristic(UUID.fromString(NOTIFI_UUID));
        if (notifiCharacteristic == null) {
            return;
        }
        //启用通知
        mBluetoothGatt.setCharacteristicNotification(notifiCharacteristic, true);

        BluetoothGattDescriptor descriptor = notifiCharacteristic
                .getDescriptor(UUID.fromString(DISENABLE));
        if (descriptor == null) {
            descriptor = new BluetoothGattDescriptor(
                    UUID.fromString(DISENABLE),
                    BluetoothGattDescriptor.PERMISSION_WRITE);
        }
        descriptor
                .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

     /*   BluetoothGattDescriptor descriptor = notifiCharacteristic.getDescriptor(UUID.fromString(DISENABLE));
        if (descriptor != null) {
            descriptor.setValue(new byte[]{0x01});
            //将给定描述符的值写入到远程设备。
            mBluetoothGatt.writeDescriptor(descriptor);
        }*/

        //拿到写的uuid
        mWriteCharacteristic = service.getCharacteristic(UUID.fromString(WRITE_UUID));
        if (notifiCharacteristic != null && mWriteCharacteristic != null) {
            Log.d(TAG, "通知和写特征找到,已具备通信条件!");
        }
    }

    /*处理连接上蓝牙设备的逻辑*/
    private void initConnected(BluetoothGatt gatt) {
        /**
         搜索连接设备所支持的service  需要连接上才可以 这个方法是异步操作
         在回调函数onServicesDiscovered中得到status
         通过判断status是否等于BluetoothGatt.GATT_SUCCESS来判断查找Service是否成功
         设备连接成功就开始查找该设备所有的服务 这有一点延迟
         */
        gatt.discoverServices();
        isBleConnect = true;
        runonUiThread(STATE_CONNECTED);
    }

    /*处理断开连接的逻辑*/
    private void initDisconnected() {
        isBleConnect = false;
        runonUiThread(STATE_DISCONNECTED);
        //判断用户是否开启了每次断开连接都清除缓存
        if (isCloseCleanCache) {
            refreshDeviceCache();
        }
        //如果用户开启自动重连 且蓝牙是断开连接状态会走进去
        if (isAutoConnect && !isBleConnect) {
            //开启定时器 每五秒重连一次蓝牙设备  必须判断是否为Null  始终保证只有一个定时器对象
            if (mTimer == null) {
                mTimer = new Timer();
            }
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //如果已经连接上就停止定时器
                    if (isBleConnect == true) {
                        if (mTimer != null) {
                            mTimer.cancel();
                            mTimer = null;
                        }
                    } else {
                        //设置状态正在尝试重连
                        runonUiThread(STATE_RESETCONNECT);
                        Log.d(TAG, "run : " + "正在尝试重连");
                        //连接蓝牙
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connect(mBluetoothDeviceAddress);
                            }
                        });
                    }
                }
            }, 20, 5000);
        }
    }

    /*
     * 初始化对本地蓝牙适配器的引用 并判断蓝牙是否是开启状态
     * 如果初始化成功则返回true
     */
    @Override
    public boolean initialize() {
        if (!mContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mContext, "该设备不支持BLE", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        //判断是否开启蓝牙  如果没有开启 弹窗提示用户开启蓝牙
        if (mBluetoothAdapter.isEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * 根据address连接蓝牙设备
     */
    @Override
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            return false;
        }
        stopScanDevices();
        /*设置状态连接中*/
        setBleCurrentState(STATE_CONNECTING);
        // 之前连接的设备尝试重新连接
        if (mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }
        //根据mac地址连接蓝牙
        final BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(address);
        if (device == null) {
            return false;
        }
        // 想直接连接设备传入false
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        mBluetoothDeviceAddress = address;
        return true;
    }

    /*
     *取消等待连接或断开一个现有的连接
     */
    @Override
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /*
     * 使用完Ble后一定要调用此方法释放资源
     */
    @Override
    public void close() {
        isAutoConnect = false;
        isBleConnect = false;
        if (mTimer != null) {
            Log.d(TAG, "close : " + "应用销毁 停止计时器");
            mTimer.cancel();
            mTimer = null;
        }
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /*
     * 发送指令方法
     * @param string
     * @param uuid
     * @return
     */
    @Override
    public boolean writeCharacteristic(byte[] value) {
        if (value == null || mWriteCharacteristic == null) {
            Log.d(TAG, "数据为空或者写特征为空,写入失败!");
            return false;
        }
        //TODO 待测试 数据大于20字节 可以自动分包
        //characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        //设置数据内容
        mWriteCharacteristic.setValue(value);
        //往蓝牙模块写入数据
        return mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
    }

    /*
     * 开始搜索设备
     * @param enable
     * @param mLeScanCallback
     */
    @Override
    public void startScanDevices(final boolean enable) {
        //五秒后停止扫描
        if (enable) {
            //开始扫描前清空集合 并停止上一次扫描
            mBlueTooths.clear();
            stopScanDevices();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(BluetoothBLeClass.this);
                    setScanfinish();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(BluetoothBLeClass.this);
            //设置状态扫描中
            setBleCurrentState(STATE_SCANNING);
        } else {
            mBluetoothAdapter.stopLeScan(BluetoothBLeClass.this);
            mHandler.removeCallbacksAndMessages(null);
            mScanning = false;
            setScanfinish();
        }
    }

    /*
     *停止扫描设备
     *
     */
    @Override
    public void stopScanDevices() {
        //如果当前有设备正在连接的话 先断开连接
        disconnect();
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(BluetoothBLeClass.this);
            //扫描蓝牙设备对bluetoothAdapter来说是一个非常消耗资源的工作 停止扫描时 应该要取消这一过程
            mBluetoothAdapter.cancelDiscovery();
            mScanning = false;
            setScanfinish();
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    //设置扫描结束状态
    private void setScanfinish() {
        //设置扫描结束
        setBleCurrentState(STATE_SCANNED);
    }

    /**
     * 所有蓝牙设备连接状态回调给调用者处理
     *
     * @param state
     */
    private void setBleCurrentState(int state) {
        connectionState = state;
        if (mBluetoothChangeListener != null) {
            mBluetoothChangeListener.onCurrentState(state);
        }
    }

    /*扫描结果  此方法避免耗时操作*/
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        //根据mac地质判断扫描到的设备是否已经存在集合中了
        for (int i = 0; i < mBlueTooths.size(); i++) {
            if (mBlueTooths.get(i).getAddress().equals(
                    device.getAddress())) {
                exist = true;
                break;
            }
        }
        //如果不存在 就放进集合中
        if (!exist) {
            mBlueTooths.add(device);
        }
        if (mBluetoothChangeListener != null) {
            mBluetoothChangeListener.onBleScanResult(mBlueTooths);
        }
        exist = false;
    }

    //返回当前设备连接状态
    @Override
    public int getBleConnectState() {
        return connectionState;
    }

    //清除缓存
    @Override
    public boolean refreshDeviceCache() {
        if (mBluetoothGatt != null) {
            try {
                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
                Method localMethod = localBluetoothGatt
                        .getClass()
                        .getMethod("refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt,
                            new Object[0])).booleanValue();
                    return bool;
                }
            } catch (Exception localException) {
                Log.i(TAG, "Exception localException:" + localException.getMessage());
            }
        }
        return false;
    }

    //将子线程设置的状态处理到主线程
    private void runonUiThread(final int state) {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setBleCurrentState(state);
            }
        });
    }
}

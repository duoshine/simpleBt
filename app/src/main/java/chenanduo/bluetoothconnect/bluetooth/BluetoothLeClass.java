/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;


/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLeClass implements LeScanCallback {

    private static BluetoothLeClass mBLE;
    private final static String TAG = "BluetoothLeClass";
    private static String SERVICE_UUID;
    private static String NOTIFI_UUID;
    private static String WRITE_UUID;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    public String mBluetoothDeviceAddress;
    public BluetoothGatt mBluetoothGatt;
    //扫描时间
    private static long SCAN_PERIOD = 5000;
    private boolean mScanning = false;
    private Handler mHandler = new Handler();
    private BluetoothGattCharacteristic bleGattCharacteristic;

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
    // 当前设备状态
    private int connectionState = STATE_DISCONNECTED;

    private static Context mContext;

    //当前连接的蓝牙设备的名字
    private String bleName = null;

    //当前连接的蓝牙设备的mac地址
    private String bleMac = null;

    //存放扫描到的设备
    private static List<BlueToothKey> mbBlueToothKeys;

    //用来判断集合中是否已经有重复蓝牙设备
    boolean exist = false;

    /*回调设备的连接状态等信息*/
    public interface BluetoothChangeListener {
        void onCurrentState(int state);

        void onBleWriteResult(byte[] result);

        void onBleScanResult(List<BlueToothKey> device);
    }

    private BluetoothChangeListener mBluetoothChangeListener;

    public void getBleCurrentState(BluetoothChangeListener bluetoothChangeListener) {
        mBluetoothChangeListener = bluetoothChangeListener;
    }

    private BluetoothLeClass(Context c) {
        mContext = c;
    }

    public static BluetoothLeClass getInstane(Context context, String serviceuuid, String notifiuuid, String writeuuid, long scantime) {
        if (mBLE == null) {
            mContext = context;
            mBLE = new BluetoothLeClass(mContext);
            mbBlueToothKeys = new ArrayList<>();
            SERVICE_UUID = serviceuuid;
            NOTIFI_UUID = notifiuuid;
            WRITE_UUID = writeuuid;
            SCAN_PERIOD = scantime;
        }
        return mBLE;
    }

    // Implements callback methods for GATT event s that the app cares about.
    // For
    // example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * 蓝牙连接状态
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "onConnectionStateChange:" + newState + "");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //mStartTime = System.currentTimeMillis();
                //搜索连接设备所支持的service  需要连接上才可以 这个方法是异步操作 在回调函数onServicesDiscovered中得到status
                //通过判断status是否等于BluetoothGatt.GATT_SUCCESS来判断查找Service是否成功
                gatt.discoverServices();
                setBleCurrentState(STATE_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                setBleCurrentState(STATE_DISCONNECTED);
                close();
                mBluetoothGatt = null;
            }
        }

        /**
         * 搜索周边服务  这个方法
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                displayGattServices(gatt.getServices());
            }
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
         * 发送指令成功后的回调方法
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

        }

        /**
         * 收到ble终端写入数据的回调
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (mBluetoothChangeListener != null) {
                mBluetoothChangeListener.onBleWriteResult(characteristic.getValue());
            }
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        Iterator localIterator1 = gattServices.iterator();
        while (localIterator1.hasNext()) {
            BluetoothGattService localBluetoothGattService = (BluetoothGattService) localIterator1
                    .next();
            if (localBluetoothGattService.getUuid().toString().equalsIgnoreCase(SERVICE_UUID)) {
                List localList = localBluetoothGattService.getCharacteristics();
                Iterator localIterator2 = localList.iterator();
                while (localIterator2.hasNext()) {
                    BluetoothGattCharacteristic localBluetoothGattCharacteristic = (BluetoothGattCharacteristic)
                            localIterator2.next();
                    if (localBluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(NOTIFI_UUID)) {
                        mBluetoothGatt.setCharacteristicNotification(localBluetoothGattCharacteristic, true);
                    /*//notifiction默认是关闭的  需要设置0x01打开
                        List<BluetoothGattDescriptor> descriptors = localBluetoothGattCharacteristic.getDescriptors();
						for (int i = 0; i < descriptors.size(); i++) {
							if (descriptors.get(i).getUuid().toString().equals(DISENABLE)) {
								BluetoothGattDescriptor bluetoothGattDescriptor = descriptors.get(i);
								bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.PERMISSION_READ);
								mBluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
							}
						}*/
                        BluetoothGattDescriptor descriptor = localBluetoothGattCharacteristic
                                .getDescriptor(UUID.fromString(NOTIFI_UUID));
                        if (descriptor == null) {
                            descriptor = new BluetoothGattDescriptor(
                                    UUID.fromString(NOTIFI_UUID),
                                    BluetoothGattDescriptor.PERMISSION_WRITE);
                        }
                        descriptor
                                .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBluetoothGatt.writeDescriptor(descriptor);
                        break;
                    }
                }
                break;
            }
        }
    }

    /*
     * 初始化对本地蓝牙适配器的引用 并判断蓝牙是否是开启状态
     * Initializes a reference to the local Bluetooth adapter.
     * 如果初始化成功则返回true
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        if (mBluetoothAdapter.isEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * 根据address连接蓝牙设备
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address
     *            The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The
     *         connection result is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG,
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
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
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /*
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /*
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic
     *            The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /*
     * 发送指令方法
     * * @param string
     * @param uuid
     * @return
     */
    public boolean writeCharacteristic(byte[] value) {
        BluetoothGattCharacteristic characteristic = null;
        if (mBluetoothGatt != null) {
            for (BluetoothGattService bluetoothGattService : mBluetoothGatt
                    .getServices()) {
                for (BluetoothGattCharacteristic bluetoothGattCharacteristics : bluetoothGattService
                        .getCharacteristics()) {
                    if (WRITE_UUID.equals(bluetoothGattCharacteristics.getUuid())) {
                        characteristic = bluetoothGattCharacteristics;
                        //this.setCharacteristicNotification(characteristic, true);
                        break;
                    }
                }
            }
        }
        if (characteristic == null) {
            return false;
        }
        //设置数据内容
        characteristic.setValue(value);
        //往蓝牙模块写入数据
        mBluetoothGatt.writeCharacteristic(characteristic);
        return true;
    }

    /*
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;
        return mBluetoothGatt.getServices();
    }

    /*
     * 开始搜索设备
     * @param enable
     * @param mLeScanCallback
     */
    public void startScanDevices(final boolean enable) {
        //五秒后停止扫描
        if (enable) {
            //开始扫描前清空集合
            mbBlueToothKeys.clear();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(BluetoothLeClass.this);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(BluetoothLeClass.this);
        } else {
            mBluetoothAdapter.stopLeScan(BluetoothLeClass.this);
            mHandler.removeCallbacksAndMessages(null);
            mScanning = false;
        }
    }

    /*
     *
     * @param mLeScanCallback
     */
    public void stopScanDevices() {
        //释放建立连接请求，然后处理下一个设备连接请求
        disconnect();
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(BluetoothLeClass.this);
            //扫描蓝牙设备对bluetoothAdapter来说是一个非常消耗资源的工作 停止扫描时 应该要取消这一过程
            mBluetoothAdapter.cancelDiscovery();
            mScanning = false;
        }
    }

    /**
     * 设置所有蓝牙设备连接状态回调给外部处理
     *
     * @param state
     */
    private void setBleCurrentState(int state) {
        connectionState = state;
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothChangeListener != null) {
                    mBluetoothChangeListener.onCurrentState(connectionState);
                }
            }
        });
    }

    /*扫描结果*/
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        //根据mac地质判断扫描到的设备是否已经存在集合中了
        for (int i = 0; i < mbBlueToothKeys.size(); i++) {
            if (mbBlueToothKeys.get(i).device.getAddress().equals(
                    device.getAddress())) {
                exist = true;
                break;
            }
        }
        //如果不存在 就放进集合中
        if (!exist) {
            mbBlueToothKeys
                    .add(new BlueToothKey(
                            device,
                            BlueToothKey.KeyConnectState.无连接.getValue(),
                            false));
        }
        if (mBluetoothChangeListener != null) {
            mBluetoothChangeListener.onBleScanResult(mbBlueToothKeys);
        }
        exist = false;
    }
}

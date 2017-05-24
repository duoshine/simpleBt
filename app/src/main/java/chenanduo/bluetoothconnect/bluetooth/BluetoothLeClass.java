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

import java.util.List;
import java.util.UUID;

import chenanduo.bluetoothconnect.util.Logger;
import chenanduo.bluetoothconnect.util.Util;


/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLeClass {

	private static BluetoothLeClass mBLE;
	private final static String TAG = "BluetoothLeClass";
	public static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID
			.fromString("f000c0e2-0451-4000-b000-000000000000");//接受通道
	public static UUID mUUID = UUID
			.fromString("f000c0e1-0451-4000-b000-000000000000");//发送通道
/*	public static final String notifaUUid =
			"6e400003-b5a3-f393-e0a9-e50e24dcca9e";//接受通道
	public static final String writeUUID =
			"6e400002-b5a3-f393-e0a9-e50e24dcca9e";//发送通道
	public static final String serviceUUID =
			"6e400001-b5a3-f393-e0a9-e50e24dcca9e";//服务通道*/
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	public String mBluetoothDeviceAddress;
	public BluetoothGatt mBluetoothGatt;
	private static final long SCAN_PERIOD = 5000;
	private boolean mScanning = false;
	private Handler mHandler = new Handler();
	private BluetoothGattCharacteristic notifyCharacteristic;


	public interface OnConnectListener {
		public void onConnect(BluetoothGatt gatt);
	}

	public interface OnDisconnectListener {
		public void onDisconnect(BluetoothGatt gatt);
	}

	public interface OnServiceDiscoverListener {
		public void onServiceDiscover(BluetoothGatt gatt);
	}

	public interface OnDataAvailableListener {
		public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status);

		public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic);

        public void onCharacteristicWriteSuccess(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status);
	}

	private OnConnectListener mOnConnectListener;
	private OnDisconnectListener mOnDisconnectListener;
	private OnServiceDiscoverListener mOnServiceDiscoverListener;
	private OnDataAvailableListener mOnDataAvailableListener;
	private static Context mContext;

	public void setmContext(Context mContext) {
		this.mContext = mContext;
	}

	public void setOnConnectListener(OnConnectListener l) {
		mOnConnectListener = l;
	}

	public void setOnDisconnectListener(OnDisconnectListener l) {
		mOnDisconnectListener = l;
	}

	public void setOnServiceDiscoverListener(OnServiceDiscoverListener l) {
		mOnServiceDiscoverListener = l;
	}

	public void setOnDataAvailableListener(OnDataAvailableListener l) {
		mOnDataAvailableListener = l;
	}

	private BluetoothLeClass(Context c) {
		mContext = c;
	}

	public static BluetoothLeClass getInstane(Context context) {
		if (mBLE == null) {
			mContext = context;
			mBLE = new BluetoothLeClass(mContext);
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
			Log.i(TAG,"onConnectionStateChange:"+newState + "");
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				if (mOnConnectListener != null)
					mOnConnectListener.onConnect(gatt);
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				if (mOnDisconnectListener != null)
					mOnDisconnectListener.onDisconnect(gatt);
			}
		}

		/**
		 * 搜索周边服务  这个方法
		 * @param gatt
		 * @param status
		 */
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			displayGattServices(gatt.getServices());
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
			if (mOnDataAvailableListener != null)
				mOnDataAvailableListener.onCharacteristicRead(gatt,
						characteristic, status);
		}

		/**
		 * 发送指令成功后的回调方法
		 * @param gatt
		 * @param characteristic
		 */
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
            if (mOnDataAvailableListener != null)
            {
                mOnDataAvailableListener.onCharacteristicWrite(gatt,
                        characteristic);
            }
		}

		/**
		 * 收到ble终端写入数据的回调
		 * @param gatt
		 * @param characteristic
		 * @param status
		 */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
//            super.onCharacteristicWrite(gatt, characteristic, status);
//            LogUtil.d("onCharacteristicWrite:"+ ByteUtil.Bytes2HexString(characteristic.getValue()));
            if (mOnDataAvailableListener != null)
            {
                mOnDataAvailableListener.onCharacteristicWriteSuccess(gatt,characteristic,status);
            }
        }
    };

	private void displayGattServices(List<BluetoothGattService> gattServices) {
		if (gattServices == null)
			return;
		//每一个周边BluetoothGattServer，包含多个服务Service，
		for (BluetoothGattService gattService : gattServices) {
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();
			//每一个Service包含多个特征Characteristic。
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				Logger.d(gattCharacteristic.getUuid() + "");
				if (gattCharacteristic.getUuid().equals(//和接收通道UUID匹配上的 要和中央匹配
						CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID)) {
					notifyCharacteristic = gattCharacteristic;
					//接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
					mBLE.setCharacteristicNotification(notifyCharacteristic,
							true);
					Log.i("setcharacteristic", gattCharacteristic.toString()
							+ "--" + Thread.currentThread().getName() + "\n");
					break;
				}
			}
		}
	}

	/*
	 * 初始化对本地蓝牙适配器的引用
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
		//直接打开蓝牙
		mBluetoothAdapter.enable();
		return true;
	}

	//确定蓝牙是否打开 如果没有打开提示用户打开蓝牙 如果已经是打开状态则不会开启
	public void isEnabled(Context context) {
		if (mBluetoothAdapter == null) {
			return;
		}
		if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
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
		//TODO 根据mac地址连接蓝牙
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
		Logger.d("close");
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

	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		if (enabled == true) {
			Log.i(TAG, "Enable Notification");
			UUID uuid = characteristic.getUuid();
			mBluetoothGatt.setCharacteristicNotification(characteristic, true);
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
			if (descriptor == null) {
				descriptor = new BluetoothGattDescriptor(
						CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID,
						BluetoothGattDescriptor.PERMISSION_WRITE);
			}
			descriptor
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		} else {
			Log.i(TAG, "Disable Notification");
			mBluetoothGatt.setCharacteristicNotification(characteristic, false);
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
			descriptor
					.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

/*	public void writeCharacteristic(String string) {
		BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUID.fromString(
				writeUUID)).getCharacteristic(UUID.fromString(writeUUID));
		characteristic.setValue(string);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}*/

	/*
	 * 发送指令方法
	 * * @param string
	 * @param uuid
	 * @return
	 */
	public boolean writeCharacteristic(String string, UUID uuid) {
		BluetoothGattCharacteristic characteristic = null;
		if (mBluetoothGatt != null) {
			for (BluetoothGattService bluetoothGattService : mBluetoothGatt
					.getServices()) {
				for (BluetoothGattCharacteristic bluetoothGattCharacteristics : bluetoothGattService
						.getCharacteristics()) {
					if (uuid.equals(bluetoothGattCharacteristics.getUuid())) {
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
		characteristic.setValue(Util.HexString2Bytes(string));
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
	public void startScanDevices(final boolean enable,
			final LeScanCallback mLeScanCallback) {
		//五秒后停止扫描
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_PERIOD);
			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	/*
	 *
	 * @param mLeScanCallback
	 */
	public void stopScanDevices(LeScanCallback mLeScanCallback) {

		if (mScanning) {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			mScanning = false;
		}
	}


	/**
	 * 判断蓝牙是否开启
	 * @param context
	 * @return
	 */
	public static boolean checkBluetoothOpen(Context context) {
		BluetoothManager bluetoothManager = (BluetoothManager) context.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdapter adapter = bluetoothManager.getAdapter();
		return adapter.isEnabled();
	}
}

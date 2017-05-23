package chenanduo.bluetoothconnect.bluetooth;

import android.bluetooth.BluetoothDevice;

public class BlueToothKey {
	public BluetoothDevice device;
	public int isKeyConnected;// 是否连接D 0 无连接 1 已连接 2 连接中 3 断开连接中
	public boolean isLockConnected;// 是否连接锁 是否获得信息

	public enum KeyConnectState {
		无连接(0), 已连接(1), 连接中(2), 断开连接中(3), 其他(4);
		private final int value;

		KeyConnectState(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
		public static String Tag = "KeyConnectState";
	}

	public BlueToothKey(BluetoothDevice device, int isKeyConnected,
                        boolean isLockConnected) {
		super();
		this.device = device;
		this.isKeyConnected = isKeyConnected;
		this.isLockConnected = isLockConnected;
	}

	@Override
	public String toString() {
		return "BlueToothDevice [device=" + device + ", isKeyConnected="
				+ isKeyConnected + ", isLockConnected=" + isLockConnected + "]";
	}
}

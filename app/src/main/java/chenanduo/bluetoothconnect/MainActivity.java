package chenanduo.bluetoothconnect;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import chenanduo.bluetoothconnect.bluetooth.BlueToothKey;
import chenanduo.bluetoothconnect.bluetooth.BluetoothLeClass;
import chenanduo.bluetoothconnect.bluetooth.KeysSelectDialog;
import chenanduo.bluetoothconnect.util.Logger;
import chenanduo.bluetoothconnect.util.ToastUtils;
import chenanduo.bluetoothconnect.util.Util;

public class MainActivity extends AppCompatActivity implements KeysSelectDialog.OnKeySelectedListener, View.OnClickListener, BluetoothLeClass.OnConnectListener, BluetoothAdapter.LeScanCallback, BluetoothLeClass.OnDisconnectListener, BluetoothLeClass.OnDataAvailableListener {
    private BlueToothKey mbBlueToothKey;
    private static final int REQUEST_PERMISSION_ACCESS_LOCATION = 1;
    private BluetoothLeClass mBLE;
    private List<BlueToothKey> mbBlueToothKeys;
    private KeysSelectDialog keysSelectDialog;
    private Button mBtnScan;
    private TextView mName;
    private long mStartTime;
    private TextView tv_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();

    }

    private void initView() {
        mBtnScan = (Button) findViewById(R.id.btnScan);
        mName = (TextView) findViewById(R.id.tvName);
        tv_result = (TextView) findViewById(R.id.tv_result);

        mBtnScan.setOnClickListener(this);
    }

    private void init() {
        //判断是否支持BLE
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            ToastUtils.showToast(MainActivity.this, "该设备不支持BLE");
            finish();
        }
        mBLE = BluetoothLeClass.getInstane(MainActivity.this);
        if (!mBLE.initialize()) {
            ToastUtils.showToast(MainActivity.this, "蓝牙开启失败");
            finish();
        }
        mbBlueToothKeys = new ArrayList<>();
        //初始化dialog
        keysSelectDialog = new KeysSelectDialog(MainActivity.this, this, this);
        //6.0动态申请权限
        requestPermission();
        /**
         * 蓝牙连接失败或者成功等的回调方法
         */
        mBLE.setOnConnectListener(this);//ble连接回调
        mBLE.setOnDisconnectListener(this);//ble断开连接回调
        mBLE.setOnDataAvailableListener(this);//从蓝牙设备读取信息回调
        //日志记录
        Logger.createDataLoggerFile(MainActivity.this);
        //删除上一次记录  每次进入app就删除上一次操作的记录，
        Logger.delect();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_searchkeys:
                startBluetooth();
                break;
            case R.id.btnScan:
                keysSelectDialog.showDialog();
                //点击搜索附近蓝牙设备
                startBluetooth();
                break;
            default:
                break;
        }
    }

    private void startBluetooth() {
        if (mBLE == null) {
            return;
        }
        Logger.savelog("开始扫描设备");
        //点击搜索时确定蓝牙是否打开 如果没有打开提示用户打开蓝牙 如果已经是打开状态则不会开启
        mBLE.isEnabled(MainActivity.this);
        //点击搜索附近蓝牙设备
        reSearchKeys();
    }

    //选择钥匙 或者 搜索钥匙
    private void reSearchKeys() {
        //释放建立连接请求，然后处理下一个设备连接请求
        mBLE.disconnect();
        //停止搜索
        mBLE.stopScanDevices(this);
        //开始搜索设备  根据传入的Callback确定要停止的蓝牙扫描
        mBLE.startScanDevices(true, this);
        mbBlueToothKey = null;
        mbBlueToothKeys.clear();
        keysSelectDialog.clear();
    }

    //6.0权限机制  蓝牙要精确定位位置
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkAccessFinePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);//位置
            int sdPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);//sd
            if (checkAccessFinePermission != PackageManager.PERMISSION_GRANTED ||sdPermission != PackageManager.PERMISSION_GRANTED ) {
                String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions,
                        REQUEST_PERMISSION_ACCESS_LOCATION);
                return;

            }
        }
    }

    //申请权限回调方法 处理用户是否授权
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /**
                     * 用户同意授权
                     */

                } else {
                    //用户拒绝授权 则给用户提示没有权限功能无法使用，
                    toast("拒绝授权 蓝牙功能无法使用");
                    finish();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //点击设备dialog的回调
    @Override
    public void OnKeySelected(BlueToothKey blueToothKey) {
        if (mBLE != null) {
            mBLE = BluetoothLeClass.getInstane(MainActivity.this);
        }
        //点击连接蓝牙设备后就停止搜索蓝牙设备了
        mBLE.stopScanDevices(MainActivity.this);
        mbBlueToothKey = blueToothKey;
        //断开连接 释放建立连接请求  进行下一个设备连接请求
        mBLE.disconnect();
        //连接蓝牙
        mBLE.connect(mbBlueToothKey.device.getAddress());
        //隐藏dialog
        keysSelectDialog.dismiss();
    }

    //扫描结果回调  该方法被回调多次  该方法中尽量少做操作 需要尽快返回 不然会报错
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        // 设备搜索完毕
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean exist = false;
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
                if (keysSelectDialog.isShowing()) {
                    //设备搜索完毕后 将数据
                    keysSelectDialog.notifyDataSetChanged(mbBlueToothKeys);
                } else {
                    //可能用户直接点击空白处销毁了dialog  那就停止扫描
                    mBLE.stopScanDevices(MainActivity.this);
                }
            }
        });
    }


    //连接设备成功回调
    @Override
    public void onConnect(BluetoothGatt gatt) {
        //mStartTime = System.currentTimeMillis();
        //搜索连接设备所支持的service  需要连接上才可以 这个方法是异步操作 在回调函数onServicesDiscovered中得到status
        //通过判断status是否等于BluetoothGatt.GATT_SUCCESS来判断查找Service是否成功
        gatt.discoverServices();
        final String name = gatt.getDevice().getName();
        //更新连接后界面
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(name)) {
                    Logger.savelog("连接到设备的:" + name);
                    mName.setText("连接到设备:" + name);
                } else {
                    mName.setText("连接到设备:--");
                }
            }
        });
    }


    //断开设备连接后回调
    @Override
    public void onDisconnect(BluetoothGatt gatt) {
        /*//小于1秒内断开视为自动断开 自动重新连接
        long endTime = System.currentTimeMillis();
        if (endTime - mStartTime < 1000) {
            //连接蓝牙
            Logger.d("小于1000ms自动重连设备");
            if ("null".equals(mbBlueToothKey.device.getAddress())) {
                return;
            }
            mBLE.connect(mbBlueToothKey.device.getAddress());
        }*/

        //每次断开连接都close  如果每次断开连接都close那么就无法实现上面的自动重连 不close好像有bug
        mBLE.close();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showToast(MainActivity.this, "断开连接");
                mName.setText("没有连接设备");
            }
        });
    }

    //ble终端数据交互的事件
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Logger.d("onCharacteristicRead：" + characteristic.getValue());
    }


    //收到ble返回数据
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
        Logger.d("收到蓝牙返回数据：" + characteristic.getValue());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_result.setText(Util.Bytes2HexString(characteristic.getValue()));
            }
        });
    }

    //写入成功回调
    @Override
    public void onCharacteristicWriteSuccess(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Logger.d("onCharacteristicWriteSuccess：" + characteristic.getValue());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBLE != null) {
            mBLE.disconnect();
            mBLE.close();
            mBLE = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBLE != null) {
            mBLE.startScanDevices(false, this);
            mbBlueToothKeys.clear();
            mBLE.disconnect();
        }
    }

    private void toast(String text) {
        ToastUtils.showToast(MainActivity.this, text);
    }

    /*发送指令*/
    public void btn_send(View view) {
        byte[] bytes = {0x01, 0x00, (byte) 0xFF};
        write(bytes);
    }

    /*发送指令  uuid为发送的uuid */
    public void write(byte[] bytes) {
        if (mBLE != null) {
            boolean succeeded = mBLE.writeCharacteristic(Util.Bytes2HexString_noblack(bytes), BluetoothLeClass.mUUID);
            if (succeeded) {
                Logger.d("发送成功");
            } else {
                Logger.d("发送失败");
            }
        } else {
            Logger.d("MbLE为null");
        }
    }
}

package chenanduo.bluetoothconnect;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import chenanduo.bluetoothconnect.bean.DeviceBean;
import chenanduo.bluetoothconnect.bluetooth.BluetoothBLeClass;
import chenanduo.bluetoothconnect.bluetooth.BluetoothChangeListener;
import chenanduo.bluetoothconnect.util.DeviceShowDialog;
import chenanduo.bluetoothconnect.util.Util;

/**
 * Created by chen on 5/25/17...
 */
public class MainActivity extends AppCompatActivity implements DeviceShowDialog.OnKeySelectedListener, View.OnClickListener {
    private static final int REQUEST_PERMISSION_ACCESS_LOCATION = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final String TAG = "simpleBtTest";
    private BluetoothBLeClass mBLE;
    private DeviceShowDialog keysSelectDialog;
    private Button mBtnScan;
    private TextView mName;
    private TextView tv_result;
    private ProgressDialog mDialog;
    private Handler handler = new Handler();

    //当前正在连接的蓝牙设备名称
    private static String currentConnectBle = null;

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
        mBLE = BluetoothBLeClass.getInstane(MainActivity.this, "F000C0E0-0451-4000-B000-000000000000", "F000C0E1-0451-4000-B000-000000000000", "F000C0E2-0451-4000-B000-000000000000")
                .setScanTime(5000)//设置扫描时间为5秒 不设置默认5秒
                .setAutoConnect(false)//设置断开后自动连接
                .closeCleanCache(true)//设置每次断开连接都清除缓存
                .setFiltration(null, null);//设置过滤条件
        if (!mBLE.initialize()) {
            //弹窗显示开启蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //初始化dialog
        keysSelectDialog = new DeviceShowDialog(MainActivity.this, this, this);

        //6.0动态申请权限
        requestPermission();
        /**
         * 交互状态
         */
        mBLE.getBleCurrentState(new BluetoothChangeListener() {
            //蓝牙连接状态
            @Override
            public void onCurrentState(int state) {
                bleCurrentState(state);
            }

            //收到蓝牙设备返回的数据
            @Override
            public void onBleWriteResult(byte[] result) {
                tv_result.setText(Util.Bytes2HexString(result));
            }

            //扫描回调  集合就是扫描到的附近的设备
            @Override
            public void onBleScanResult(List<DeviceBean> device) {
                if (keysSelectDialog.isShowing()) {
                    keysSelectDialog.notifyDataSetChanged(device);
                }
            }

            /**
             * 写入蓝牙设备成功回调
             * @param value
             */
            @Override
            public void onWriteDataSucceed(byte[] value) {

            }

            /**
             * 已经具备通信条件
             */
            @Override
            public void findServiceSucceed() {
                Log.d(TAG, "findServiceSucceed : 具备通信条件");
            }

            /**
             * 服务查找完毕
             * @param gatt
             */
            @Override
            public void getDisplayServices(BluetoothGatt gatt) {
                Log.d(TAG, "getDisplayServices : 服务已经查找完毕该服务有:" + gatt.getServices().size());
            }
        });

        //取消显示设备的dialog就停止扫描
        keysSelectDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mBLE != null) {
                    mBLE.startScanDevices(false);
                }
            }
        });
    }


    /*和蓝牙设备交互的状态*/
    private void bleCurrentState(int state) {
        // 设备连接成功
        if (state == BluetoothBLeClass.STATE_CONNECTED) {
            System.out.println("状态:连接成功");
            /*隐藏连接进度*/
            hiedDialog();
            if (!TextUtils.isEmpty(currentConnectBle)) {
                mName.setText("连接到设备:" + currentConnectBle);
            } else {
                mName.setText("连接到设备:--");
            }
        } else if (state == BluetoothBLeClass.STATE_DISCONNECTED) { //设备连接断开
            System.out.println("状态:连接断开");
            if (!TextUtils.isEmpty(currentConnectBle)) {
                Toast.makeText(MainActivity.this, "设备" + currentConnectBle + "已断开连接", Toast.LENGTH_SHORT).show();
            }
            mName.setText("没有连接设备");
        } else if (state == BluetoothBLeClass.STATE_SCANNING) {
            System.out.println("状态:正在扫描");
        } else if (state == BluetoothBLeClass.STATE_SCANNED) {
            System.out.println("状态:扫描结束");
        } else if (state == BluetoothBLeClass.STATE_CONNECTING) {
            System.out.println("状态:正在连接");
        } else if (state == BluetoothBLeClass.STATE_RESETCONNECT) {
            System.out.println("状态:正在尝试重连中");
            mName.setText("正在尝试重连中");
        }
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
        //点击搜索附近蓝牙设备
        reSearchKeys();
    }

    //选择
    private void reSearchKeys() {
        //开始扫描
        mBLE.startScanDevices(true);
        //一定要做清除状态
        keysSelectDialog.clear();
    }

    //6.0权限机制  蓝牙要精确定位位置
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkAccessFinePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);//位置
            int sdPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);//sd
            if (checkAccessFinePermission != PackageManager.PERMISSION_GRANTED || sdPermission != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
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
                    Toast.makeText(MainActivity.this, "拒绝授权 蓝牙功能无法使用", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //点击设备开始连接
    @Override
    public void OnKeySelected(DeviceBean blueToothKey) {
        //记录当前连接的蓝牙设备名称
        currentConnectBle = blueToothKey.getName();
        //连接蓝牙
        mBLE.connect(blueToothKey.getAddress());
        mName.setText("正在连接:" + currentConnectBle);
        //隐藏dialog
        keysSelectDialog.dismiss();
        /*显示连接进度*/
        showDialog();
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
            mBLE.stopScanDevices();
        }
    }

    /*发送指令*/
    public void btn_send(View view) {
        byte[] bytes = {0x01, 0x00, (byte) 0xFF};
        write(bytes);
    }

    /*发送指令 */
    public void write(byte[] bytes) {
        if (mBLE != null) {
            mBLE.writeCharacteristic(bytes);
        }
    }

    private void showDialog() {
        /*六秒后如果还没连接上就认为连接超时*/
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mDialog.isShowing()) {
                    hiedDialog();
                    Toast.makeText(MainActivity.this, "连接超时...", Toast.LENGTH_SHORT).show();
                    mName.setText("连接超时");
                }
            }
        }, 6000);
        mDialog = new ProgressDialog(MainActivity.this);
        mDialog.setMessage("正在连接...");
        mDialog.show();
    }

    private void hiedDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    public void text(View view) {
        String str = "b:A0：50：";
        if (str.contains("B")) {
            Log.d(TAG, "text : 我包含");
        } else {
            Log.d(TAG, "text : 我不包含");
        }
    }
}

package chenanduo.bluetoothconnect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import chenanduo.bluetoothconnect.adapter.BTAdapter;
import chenanduo.bluetoothconnect.bluetooth.BluetoothBTClass;


public class BTActivity extends AppCompatActivity {
    private List<BluetoothDevice> mDatas = new ArrayList<>();
    private List<BluetoothDevice> bondDevicesList = new ArrayList<>();
    private static final String TAG = "BluetoothBTClass";
    private RecyclerView mRecyclerView;
    private RecyclerView bondDevices;
    private BTAdapter mAdapter;
    private BTAdapter bondadapter;
    private BluetoothBTClass mBt;

    //接收收到的数据
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothBTClass.MESSAGE_READ:
                    Toast.makeText(BTActivity.this, String.valueOf(msg.obj), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    //接收设备扫描结果 配对结果 扫描结束
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mDatas.add(device);
                    mAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                Log.d(TAG, "onReceive : " + "扫描完成");
                setTitle("选择连接的设备");
            }
            /*if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                //再次得到的action，会等于PAIRING_REQUEST{
                Log.d(TAG, "onReceive : " + "其实我收到结果了");
                if (device.getName().contains("SXTT")) {
                    try {
                        //1.确认配对
                        ClsUtils.setPairingConfirmation(device.getClass(), device, true);
                        //2.终止有序广播
                        abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    Log.e("提示信息", "这个设备不是目标蓝牙设备");
            }*/
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_br);
        initView();
        //找到设备
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        //设备扫描结束
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        //初始化
        mBt = new BluetoothBTClass(BTActivity.this, mHandler);
        //获取配对设备
        getBondDevices();
        //开始扫描
        mBt.startScan();
        //作为服务器端监听打开
        mBt.startAccept();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        bondDevices = (RecyclerView) findViewById(R.id.bondDevices);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(BTActivity.this));
        bondDevices.setLayoutManager(new LinearLayoutManager(BTActivity.this));
        mAdapter = new BTAdapter(mDatas, BTActivity.this);
        bondadapter = new BTAdapter(bondDevicesList, BTActivity.this);
        mRecyclerView.setAdapter(mAdapter);
        bondDevices.setAdapter(bondadapter);
        mAdapter.setOnItemClickListener(new BTAdapter.OnItemListener() {
            @Override
            public void onItemClick(int position) {
                mBt.stopScan();
                mBt.connect(mDatas.get(position));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //断开连接
        mBt.disconnect();
        mBt.stopAccept();
    }

    //获取配对过的设备
    public void getBondDevices() {
        Set<BluetoothDevice> bondDevices = mBt.getBondDevices();
        Iterator<BluetoothDevice> iterator = bondDevices.iterator();
        while (iterator.hasNext()) {
            BluetoothDevice next = iterator.next();
            bondDevicesList.add(next);
        }
        mAdapter.notifyDataSetChanged();
    }
}

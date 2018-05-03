package chenanduo.bluetoothconnect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class BluetoothBTClass {

    private static final String TAG = "BluetoothBTClass";
    private static final String NAME = "BluetoothBTClass";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final int MESSAGE_READ = 0x11;
    private final BluetoothAdapter bluetoothAdapter;
    private Context mContext;
    private Handler mHandler;
    private ConnectThread mConnectThread;
    private AcceptThread mAcceptThread;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     */
    public BluetoothBTClass(Context context, Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mContext = context;
        this.mHandler = handler;
    }

    //开始扫描
    public void startScan() {
        stopScan();
        bluetoothAdapter.startDiscovery();
    }

    //连接为服务器
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code  有秘钥有配对
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
                //tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {

            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (mmServerSocket == null) {
                Log.d(TAG, "mmServerSocket : " + mmServerSocket);
                return;
            }
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    //开始侦听连接请求 阻塞请求 连接被接收或者发生异常时返回
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    //manageConnectedSocket(socket);
                    try {
                        //如不过需要更多的连接 否则请调用close 这将释放服务器套接字及其所有资源 但不会关闭accept返回的socket
                        //RFCOMM 一次只允许每个通道有一个已连接的客户端
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /**
         * 提供主动结束线程方法
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }

    //连接设备
    public void connect(BluetoothDevice device) {
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    //断开连接
    public void disconnect() {
        if (mConnectThread == null) {
            return;
        }
        mConnectThread.cancel();
        Log.d(TAG, "disconnect : " + "断开连接");
    }

    //停止监听
    public void stopAccept() {
        if (mAcceptThread == null) {
            return;
        }
        mAcceptThread.cancel();
    }

    //连接为客户端
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            // 得到一个与给定BluetoothDevice BluetoothSocket连接
            try {
                // 使用的Uuid必须与开放服务器的uuid一致
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                //tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            try {
                //连接为阻塞方法 超过12秒将会抛出异常
                mmSocket.connect();
                if (mmSocket.isConnected()) {
                    Log.d(TAG, "连接成功");
                } else {
                    Log.d(TAG, "连接失败");
                }
            } catch (IOException connectException) {
                //关闭已连接的套接字并清理所有内部资源
                Log.d(TAG, "连接出错:" + connectException.getMessage());
                try {
                    mmSocket.close();
                } catch (IOException closeException) {

                }
                return;
            }
            // Do work to manage the connection (in a separate thread)
            //            manageConnectedSocket(mmSocket);
        }

        /**
         * 提供主动关闭线程的方法
         */
        public void cancel() {
            try {
                //关闭已连接的套接字并清理所有内部资源
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    //获取配对设备
    public Set<BluetoothDevice> getBondDevices() {
        if (bluetoothAdapter == null) {
            return null;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        return pairedDevices;
    }

    //连接为服务器 执行这一步必须确定蓝牙已打开  否则nullpoint
    public void startAccept() {
        if (bluetoothAdapter.isEnabled()) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        } else {
            Log.d(TAG, "蓝牙都没有打开");
        }
    }

    //管理连接
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // 存储缓存的流
            int bytes; // bytes returned from read()
            // 一直读直到发生异常
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    // 将得到的字节发送到ui线程去处理
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* 将数据发送到远程设备 一般不阻塞 不需要放在子线程 */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* 提供主动关闭线程的方法*/
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    //停止扫描
    public void stopScan() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }
}

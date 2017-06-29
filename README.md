# BlueToothConnect-使用
#### 一:拷出我Demo中的BluetoothLeClass,我已经将所有操作都封装在该类中--单一职责。
#### 二:初始化
    //第一个参数是上下文 第二三四是uuid(service notifi write) 第五个参数是设置扫描蓝牙设备时间  扫描蓝牙设备非常消耗性能的一个工作,建议时间设置短一点
    BluetoothLeClass mBLE = BluetoothLeClass.getInstane(MainActivity.this, "", "", "", 5000);
#### 三:当然要判断一下设备是否支持ble(Android4.3，蓝牙4.0)
     //判断是否支持BLE
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "该设备不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }
#### 四:6.0设备需要申请下定位权限，不然找不到蓝牙设备，部分机型可能没有弹窗申请，手动开启gps即可
#### 五:设置和蓝牙交互的状态接收回调，有三个方法
    mBLE.getBleCurrentState(new BluetoothLeClass.BluetoothChangeListener() {
            //蓝牙连接状态
            @Override
            public void onCurrentState(int state) {
                
            }

            //收到蓝牙设备返回的数据
            @Override
            public void onBleWriteResult(byte[] result) {

            }

            //扫描回调  集合就是扫描到的附近的设备
            @Override
            public void onBleScanResult(List<BlueToothKey> device) {
                
            }
        });
#### 六:开始扫描/停止扫描
      //开始扫描  true是开始扫描，false是停止扫描  扫描到的蓝牙设备会在onBleScanResult()方法中回调,该接口上面已经实现了，该方法回调次数是附近蓝牙设备数和扫描时间决定
        mBLE.startScanDevices(true);
      //停止扫描
        stopScanDevices();
#### 七:连接蓝牙
      //连接蓝牙  mac地址在扫描到的device中可以通过device.getAddress()获取到
        mBLE.connect(mac地址);
#### 八:发送指令
    //bytes是你要发送的数据,根据你的协议来,发送成功为true,接收回调在onBleWriteResult()方法中，该接口上面已经实现了
    mBLE.writeCharacteristic(bytes);
#### 九:最后Destroy时
    if (mBLE != null) {
            //断开连接
            mBLE.disconnect();
            mBLE.close();
            mBLE = null;
        }
#### 十:回调处理都已在主线程执行，状态码
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












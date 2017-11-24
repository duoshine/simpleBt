# simpleBt-使用
#### 一:项目根 build.gradle 添加

	allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
app的 build.gradle添加

	dependencies {
	       compile 'com.github.duoshine:simpleBt:2.1.0'
	}


#### 二:初始化
    //第一个参数是上下文 第二三四是uuid(service notifi write) 扫描时间默认5秒 可以自定义  
    //扫描蓝牙设备非常消耗性能的一个工作,建议时间设置短一点 可以设置断开后自动重连,这个要看应用场景,如果你
    //的应用场景是用户主动断开,那么你需要决定是否开启此功能,如果返回对象为null，那么就是该手机不支持ble(低于Android4.3)
    BluetoothBLeClass mBLE = BluetoothBLeClass.getInstane(MainActivity.this, "", "", "")
    .setScanTime(5000)
    .setAutoConnect(true)//设置断开后自动连接
    .closeCleanCache(true);//设置每次断开连接都清除缓存 无特殊情况 不建议开启
#### 三:当然要判断一下蓝牙是否开启
    if (!mBLE.initialize()) {
        //弹窗显示开启蓝牙 返回false则蓝牙尚未开启 返回true则蓝牙已经开启
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
#### 四:6.0设备需要申请下定位权限，不然找不到蓝牙设备，部分机型可能没有弹窗申请，手动开启gps即可
#### 五:设置和蓝牙交互的状态接收回调，有四个方法
    	 /**
         * 交互状态 所有回调都已在ui线程执行
         */
        mBLE.getBleCurrentState(new BluetoothChangeListener() {
            //蓝牙连接状态
            @Override
            public void onCurrentState(int state) {
            	//状态码在本文结尾处.
            }

            //收到下位机返回的数据
            @Override
            public void onBleWriteResult(byte[] result) {

            }

            //扫描回调  集合就是扫描到的附近的设备
            @Override
            public void onBleScanResult(List<BluetoothDevice> device) {
            
            }

            /**
             * 写入下位机设备成功回调(上位机发送到下位机成功,不代表数据正确和错误,不代表一定能收到回调)
             */
            @Override
            public void onWriteDataSucceed() {

            }
        });
#### 六:开始扫描/停止扫描
      //开始扫描  true是开始扫描，false是停止扫描  扫描到的蓝牙设备会在onBleScanResult()方法中回调,该接口上面已经实现了，该方法回调次数是附近蓝牙设备数和扫描时间决定,每次扫描前都会自动断开当前的连接和上一次扫描,所以不需要重复调用(设备在连接中消耗较大，所以不支持连接中扫描！！！)
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
            //一定要调用 释放资源  一定要调用 释放资源  一定要调用 释放资源
            mBLE.close();
            mBLE = null;
        }
#### 十:状态码，
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

#### 十一:工具类中测试log过滤为：simpleBtTest
工具类是单例的，主要为了方便可能多个页面需要蓝牙操作










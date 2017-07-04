package chenanduo.bluetoothconnect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import chenanduo.bluetoothconnect.bluetooth.BluetoothBRClass;

public class BRActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_br);
        BluetoothBRClass brClass = new BluetoothBRClass(BRActivity.this);
        brClass.start();

    }
}

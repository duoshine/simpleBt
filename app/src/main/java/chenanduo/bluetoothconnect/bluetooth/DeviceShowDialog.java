package chenanduo.bluetoothconnect.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import chenanduo.bluetoothconnect.R;
import chenanduo.bluetoothconnect.adapter.BlueToothAdapter;


public class DeviceShowDialog extends AlertDialog
{
    private View view;
    private Context context;
    private DisplayMetrics displayMetrics;
    private ViewHoder layout;
    private List<BluetoothDevice> mbBlueToothKeys;
    private BlueToothAdapter mKeysAdapter;
    private View.OnClickListener onClickListener;
    private OnKeySelectedListener onKeySelectedListener;

    public DeviceShowDialog(Context context,
                            View.OnClickListener onClickListener,
                            final OnKeySelectedListener onKeySelectedListener)
    {
        super(context);
        this.context = context;
        this.onClickListener = onClickListener;
        this.onKeySelectedListener = onKeySelectedListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        view = LayoutInflater.from(context).inflate(R.layout.dialog_keys, null);
        displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
        setContentView(view);
        getWindow().setLayout(displayMetrics.widthPixels * 4 / 5,
                displayMetrics.heightPixels * 2 / 3);
        layout = new ViewHoder(view);
        layout.btn_searchkeys.setOnClickListener(onClickListener);
        layout.lv_keys.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3)
            {
                if (onKeySelectedListener != null)
                {
                    onKeySelectedListener.OnKeySelected(mbBlueToothKeys.get(arg2));
                }
            }
        });
        mbBlueToothKeys = new ArrayList<BluetoothDevice>();
        mKeysAdapter = new BlueToothAdapter(context, mbBlueToothKeys);
        layout.lv_keys.setAdapter(mKeysAdapter);
    }

    public void showDialog(List<BluetoothDevice> blueToothKeys)
    {
        this.notifyDataSetChanged(blueToothKeys);
        this.show();
    }

    public void showDialog()
    {
        this.show();
    }

    public void clear()
    {
        this.mbBlueToothKeys.clear();
        this.mKeysAdapter.notifyDataSetChanged();
    }

    public void notifyDataSetChanged(List<BluetoothDevice> mbBlueToothKeys2)
    {
        if (mbBlueToothKeys2 != null && mbBlueToothKeys2.size() > 0)
        {
            layout.tv_nokeys.setVisibility(View.GONE);
            layout.lv_keys.setVisibility(view.VISIBLE);
            this.mbBlueToothKeys.clear();
            this.mbBlueToothKeys.addAll(mbBlueToothKeys2);
            this.mKeysAdapter.notifyDataSetChanged();
        }
        else
        {
            layout.tv_nokeys.setVisibility(View.VISIBLE);
            layout.lv_keys.setVisibility(view.GONE);
        }
    }

    private class ViewHoder
    {
        public Button btn_searchkeys;// 搜索钥匙
        public TextView tv_nokeys;// 没有钥匙
        public ListView lv_keys;// 钥匙列表

        public ViewHoder(View view)
        {
            btn_searchkeys = (Button) view.findViewById(R.id.btn_searchkeys);
            tv_nokeys = (TextView) view.findViewById(R.id.tv_nokeys);
            lv_keys = (ListView) view.findViewById(R.id.lv_keys);
        }
    }

    public interface OnKeySelectedListener
    {
        public void OnKeySelected(BluetoothDevice blueToothKey);
    }

}

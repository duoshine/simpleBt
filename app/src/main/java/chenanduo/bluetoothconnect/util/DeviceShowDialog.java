package chenanduo.bluetoothconnect.util;

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
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import chenanduo.bluetoothconnect.R;
import chenanduo.bluetoothconnect.adapter.BLEAdapter;

/**
 * Created by chen on 5/28/17.
 */

public class DeviceShowDialog extends AlertDialog {
    private static final String TAG = "simpleBtTest";
    private View view;
    private Context context;
    private DisplayMetrics displayMetrics;
    private ViewHoder layout;
    private List<BluetoothDevice> mbBlueToothKeys;
    private BLEAdapter mKeysAdapter;
    private View.OnClickListener onClickListener;
    private OnKeySelectedListener onKeySelectedListener;

    public DeviceShowDialog(Context context,
                            View.OnClickListener onClickListener,
                            final OnKeySelectedListener onKeySelectedListener) {
        super(context);
        this.context = context;
        this.onClickListener = onClickListener;
        this.onKeySelectedListener = onKeySelectedListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        //item点击事件
        layout.listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                if (onKeySelectedListener != null && mbBlueToothKeys != null) {
                    onKeySelectedListener.OnKeySelected(mbBlueToothKeys.get(position));
                }
            }
        });
        mbBlueToothKeys = new ArrayList<>();
        mKeysAdapter = new BLEAdapter(context, mbBlueToothKeys);
        layout.listView.setAdapter(mKeysAdapter);
    }

    public void showDialog() {
        this.show();
    }

    public void clear() {
        this.mbBlueToothKeys.clear();
        this.mKeysAdapter.notifyDataSetChanged();
        layout.ProgressBar.setVisibility(View.VISIBLE);
        layout.listView.setVisibility(view.GONE);
        layout.tv_scanresult_view.setVisibility(View.GONE);
        layout.FrameLayout.setVisibility(View.VISIBLE);
    }

    public void notifyDataSetChanged(List<BluetoothDevice> mbBlueToothKeys2) {
        if (mbBlueToothKeys2 != null && mbBlueToothKeys2.size() > 0) {
            layout.listView.setVisibility(view.VISIBLE);
            layout.FrameLayout.setVisibility(View.GONE);
            layout.ProgressBar.setVisibility(View.GONE);
            layout.tv_scanresult_view.setVisibility(View.GONE);
            this.mbBlueToothKeys.clear();
            this.mbBlueToothKeys.addAll(mbBlueToothKeys2);
            this.mKeysAdapter.notifyDataSetChanged();
        } else {
            layout.FrameLayout.setVisibility(View.VISIBLE);
            layout.ProgressBar.setVisibility(View.GONE);
            layout.tv_scanresult_view.setVisibility(View.VISIBLE);
            layout.listView.setVisibility(View.GONE);
        }
    }

    private class ViewHoder {
        public Button btn_searchkeys;// 搜索钥匙
        public ProgressBar ProgressBar;// 扫描进度
        public ListView listView;// 钥匙列表
        public TextView tv_scanresult_view;// 扫描结果
        public FrameLayout FrameLayout;// 用于隐藏扫描状态

        public ViewHoder(View view) {
            btn_searchkeys = (Button) view.findViewById(R.id.btn_searchkeys);
            ProgressBar = (ProgressBar) view.findViewById(R.id.ProgressBar);
            tv_scanresult_view = (TextView) view.findViewById(R.id.tv_scanresult_view);
            listView = (ListView) view.findViewById(R.id.lv_keys);
            FrameLayout = (FrameLayout) view.findViewById(R.id.FrameLayout);
        }
    }

    public interface OnKeySelectedListener {
        public void OnKeySelected(BluetoothDevice device);
    }
}

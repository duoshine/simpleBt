package chenanduo.bluetoothconnect.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import chenanduo.bluetoothconnect.R;

/**
 * Created by chen on 2017
 */

public class BTAdapter extends RecyclerView.Adapter<BTAdapter.MyViewHolder> {
    private List<BluetoothDevice> mDatas = new ArrayList<>();
    private Context mContext;

    public BTAdapter(List<BluetoothDevice> datas, Context context) {
        mDatas = datas;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.bt_item, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        BluetoothDevice device = mDatas.get(position);
        holder.tvName.setText(device.getName());
        holder.tvAddress.setText(device.getAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemListener != null) {
                    mOnItemListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvAddress;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvAddress = (TextView) itemView.findViewById(R.id.tv_address);
        }
    }

    public interface OnItemListener {
        void onItemClick(int position);
    }

    private OnItemListener mOnItemListener;

    public void setOnItemClickListener(OnItemListener onItemListener) {
        mOnItemListener = onItemListener;
    }

}

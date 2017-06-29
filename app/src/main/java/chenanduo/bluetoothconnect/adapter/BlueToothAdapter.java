package chenanduo.bluetoothconnect.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import chenanduo.bluetoothconnect.R;


public class BlueToothAdapter extends BaseAdapter {
    private Context context;

    private List<BluetoothDevice> mmBlueToothKeys;

    public BlueToothAdapter(Context context,
                            List<BluetoothDevice> mBlueToothKeys) {
        this.context = context;
        this.mmBlueToothKeys = mBlueToothKeys;
    }

    @Override
    public int getCount() {
        return mmBlueToothKeys.size();
    }

    @Override
    public Object getItem(int position) {
        return mmBlueToothKeys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.device_dialog_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.setShow(mmBlueToothKeys.get(position));
        return convertView;
    }

    private class ViewHolder {
        private TextView tv_devicename;
        private TextView tv_devicesaddress;
        private ImageView btn_img;

        public ViewHolder(View view) {
            this.tv_devicename = (TextView) view
                    .findViewById(R.id.tv_devicename);
            this.tv_devicesaddress = (TextView) view
                    .findViewById(R.id.tv_devicesaddress);
            this.btn_img = (ImageView) view.findViewById(R.id.btn_img);
        }

        public void setShow(BluetoothDevice key) {
            /*if (key.isBleKey()) {
                String name = key.device.getName();
				if (!TextUtils.isEmpty(name)) {
					this.tv_devicename.setText("name："+ name);
					this.btn_img.setImageResource(R.mipmap.ic_launcher);
					this.tv_devicesaddress.setText("address："
							+ key.device.getAddress());
				}
			} else {
				this.tv_devicename.setText("name：" + key.device.getName());
				this.btn_img.setImageResource(R.mipmap.ic_launcher);
				this.tv_devicesaddress.setText("address："
						+ key.device.getAddress());
			}*/
            String name = key.getName();
            this.btn_img.setImageResource(R.mipmap.ic_launcher);
            if (!TextUtils.isEmpty(name)) {
                this.tv_devicename.setText("name：" + name);
                this.tv_devicesaddress.setText("address：" + key.getAddress());
            } else {
                this.tv_devicename.setText("name：--");
                this.tv_devicesaddress.setText("address：--");
            }
        }
    }
}

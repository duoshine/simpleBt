package chenanduo.bluetoothconnect.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;


/**
 * Created by chen on 2017  不需要pw的就可以直接删除
 */
public class SelectPicPopupWindow extends PopupWindow {

    private View mMenuView;
    private Context context;
    private int width;//宽度由外部传入
    private int layoutId;//布局由外部传入
    public SelectPicPopupWindow(Context context, int width, int layoutId) {
        super(context);
        this.context = context;
        this.width = width;
        this.layoutId = layoutId;
        initView();
        setting();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(layoutId, null);

    }

    private void setting() {
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(width);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        //this.setAnimationStyle(R.style.main_popwin_anim_style);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        /*mMenuView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y=(int) event.getY();
                if(event.getAction()==MotionEvent.ACTION_UP){
                    if(y<height){
                        dismiss();
                    }
                }
                return true;
            }
        });*/
    }

    /**
     * 自动查找控件
     */
    private SparseArray<View> mArray = new SparseArray<>();
    public View autoView(int layoutId) {
        View view = mArray.get(layoutId);
        if (view == null) {
            view = mMenuView.findViewById(layoutId);
            mArray.put(layoutId, view);
        }
        return view;
    }
}
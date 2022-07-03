package popview;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.java.mashihe.R;

import util.ScreenUtil;

public class LoginPopView {
    private Context mContext;
    private View anchor;
    private LayoutInflater mInflater;
    private PopupWindow mPopupWindow;
    WindowManager.LayoutParams params;
    WindowManager windowManager;
    Window window;

    /**
     * @param context
     * @param anchor  依附在哪个View下面
     */
    public LoginPopView(Activity context, View anchor) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.anchor = anchor;
        windowManager = context.getWindowManager();
        window = context.getWindow();
        params = context.getWindow().getAttributes();
        init();
    }

    public void init() {
        View view = mInflater.inflate(R.layout.login_pop_window, null);
        params.dimAmount = 0.5f;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        mPopupWindow = new PopupWindow(view, ScreenUtil.getScreenWidth(mContext), LinearLayout.LayoutParams.WRAP_CONTENT);
        //监听PopupWindow的dismiss，当dismiss时屏幕恢复亮度
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                params.alpha = 1.0f;
                window.setAttributes(params);
            }
        });
        mPopupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        // 动画效果
        mPopupWindow.setAnimationStyle(R.style.popWindow_animation_from_top);
    }

    /**
     * 显示底部对话框
     */
    public void show() {
        mPopupWindow.showAtLocation(anchor, Gravity.TOP, 0, 0);
        params.alpha = 0.5f;
        window.setAttributes(params);
    }


    public void dismiss(){
        if(mPopupWindow!=null && mPopupWindow.isShowing()){
            mPopupWindow.dismiss();
        }
    }
}


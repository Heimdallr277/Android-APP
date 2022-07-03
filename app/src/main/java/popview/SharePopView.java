package popview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.java.mashihe.MainActivity;
import com.java.mashihe.R;

import background.ShareUtils;
import util.ScreenUtil;

public class SharePopView {
    private Context mContext;
    private View anchor;
    private LinearLayout mWeibo, mWechat, mMoments, mOthers;
    private LayoutInflater mInflater;
    private TextView mTvCancel;
    private PopupWindow mPopupWindow;
    WindowManager.LayoutParams params;
    WindowManager windowManager;
    Window window;

    String url;
    String title;
    String text;
    String imageUrl;

    /**
     * @param context
     * @param anchor  依附在哪个View下面
     */
    public SharePopView(Activity context, View anchor, String url, String title, String text, String imageUrl) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.anchor = anchor;
        windowManager = context.getWindowManager();
        window = context.getWindow();
        params = context.getWindow().getAttributes();
        this.url = url;
        this.title = title;
        this.text = text;
        this.imageUrl = imageUrl;
        init();
    }

    public void init() {
        View view = mInflater.inflate(R.layout.share_pop_window, null);
        params.dimAmount = 0.5f;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mTvCancel = view.findViewById(R.id.tv_share_cancel);
        mWeibo = view.findViewById(R.id.weibo);
        mWechat = view.findViewById(R.id.wechat);
        mMoments = view.findViewById(R.id.moments);
        mOthers = view.findViewById(R.id.others);

        // 分享到微博
        mWeibo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (!MainActivity.isNetworkConnected(mContext)) {
                    Toast.makeText(mContext, "网络连接错误！", Toast.LENGTH_SHORT).show();
                    return;
                }
                ShareUtils.shareToWeibo(mContext, title, text, imageUrl, url);
            }
        });

        // 分享到微信
        mWechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (!MainActivity.isNetworkConnected(mContext)) {
                    Toast.makeText(mContext, "网络连接错误！", Toast.LENGTH_SHORT).show();
                    return;
                }
                ShareUtils.shareToWechat(mContext, title, text, imageUrl, url);
            }
        });

        // 分享到朋友圈
        mMoments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (!MainActivity.isNetworkConnected(mContext)) {
                    Toast.makeText(mContext, "网络连接错误！", Toast.LENGTH_SHORT).show();
                    return;
                }
                ShareUtils.shareToWechatMoments(mContext, title, text, imageUrl, url);
            }
        });

        // 原生API分享
        mOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (!MainActivity.isNetworkConnected(mContext)) {
                    Toast.makeText(mContext, "网络连接错误！", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, getShareText());
                shareIntent.setType("text/plain");
                mContext.startActivity(Intent.createChooser(shareIntent, "分享到..."));
//                Snackbar.make(anchor, "分享成功", Snackbar.LENGTH_LONG).show();
            }
        });

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

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
        // 动画效果 从底部弹起
        mPopupWindow.setAnimationStyle(R.style.popWindow_animation);
    }

    /**
     * 显示底部对话框
     */
    public void show() {
        mPopupWindow.showAtLocation(anchor, Gravity.BOTTOM, 0, 0);
        params.alpha = 0.5f;
        window.setAttributes(params);
    }


    public void dismiss(){
        if(mPopupWindow!=null && mPopupWindow.isShowing()){
            mPopupWindow.dismiss();
        }
    }

    private String getShareText() {
        return "【"+title+"】 " + text + "... 原文链接：" + url;
    }

}


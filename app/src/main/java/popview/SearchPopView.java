package popview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.java.mashihe.MainActivity;
import com.java.mashihe.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adapter.SearchHistoryAdapter;
import util.ScreenUtil;

import static com.java.mashihe.MainActivity.toList;

public class SearchPopView {
    private Context mContext;
    private View anchor;
    private LayoutInflater mInflater;
    private ImageView mIvSearch;
    private ImageView mIvBack;
    private EditText mEtInput;
    private ListView mLvHistory;
    private LinearLayout mClearHistory;
    private SearchHistoryAdapter mAdapter;
    private PopupWindow mPopupWindow;
    WindowManager.LayoutParams params;
    WindowManager windowManager;
    Window window;
    ArrayList<String> history;
    LayoutAnimationController controller;
    String keyword;

    /**
     * @param context
     * @param anchor  依附在哪个View下面
     */
    public SearchPopView(Activity context, View anchor) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.anchor = anchor;
        windowManager = context.getWindowManager();
        window = context.getWindow();
        params = context.getWindow().getAttributes();
        this.history = toList(MainActivity.dataBaseManager.getAllSearchHistory());
        init();
    }

    public void init() {
        View view = mInflater.inflate(R.layout.search_pop_window, null);
        params.dimAmount = 0.5f;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mIvSearch = view.findViewById(R.id.iv_search);
        mIvBack = view.findViewById(R.id.iv_back);
        mEtInput = view.findViewById(R.id.et_searchinput);
        mClearHistory = view.findViewById(R.id.ll_clearhistory);
        mLvHistory = view.findViewById(R.id.lv_searchhistory);

        @SuppressLint("ResourceType") Animation animation = AnimationUtils.loadAnimation(mContext, R.animator.anim_item);
        controller = new LayoutAnimationController(animation);
        controller.setDelay(0.5f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        mLvHistory.setLayoutAnimation(controller);

        mEtInput.setMaxLines(1);
        mEtInput.setWidth(ScreenUtil.getScreenWidth(mContext)-100);
        InputFilter typeFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern p = Pattern.compile("[a-zA-Z|\u4e00-\u9fa5|\\d]+");
                Matcher m = p.matcher(source.toString());
                if (!m.matches()) return "";
                return null;
            }
        };
        mEtInput.setFilters(new InputFilter[]{typeFilter});
        mEtInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    mIvSearch.callOnClick();
                }
                return false;
            }
        });

        mAdapter = new SearchHistoryAdapter(mContext, history);
        mLvHistory.setAdapter(mAdapter);
        //单击输入
        mLvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                keyword = (String)mAdapter.getItem(i);
                mEtInput.setText(keyword);
                if (!MainActivity.isNetworkConnected(mContext)) {
                    Toast.makeText(mContext, "网络连接错误！", Toast.LENGTH_SHORT).show();
                    return;
                }
                MainActivity.dataBaseManager.addSearchHistory(keyword);
                onSearchButtonClick();
                dismiss();
            }
        });
        //长按删除
        mLvHistory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, final int i, long l) {
                final String item = (String)mAdapter.getItem(i);
                deleteItem(view, i);
                Snackbar.make(anchor, "已删除", Snackbar.LENGTH_LONG).show();
                return true;
            }
        });

        mIvSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!MainActivity.isNetworkConnected(mContext)) {
                    Toast.makeText(mContext, "网络连接错误！", Toast.LENGTH_SHORT).show();
                    return;
                }

                String text = mEtInput.getText().toString();
                if (text.isEmpty()) {
                    Toast.makeText(mContext, "输入内容不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (history.contains(text)) {
                    history.remove(text);
                    history.add(0, text);
                } else {
                    history.add(0, text);
                }
                MainActivity.dataBaseManager.addSearchHistory(text);
                keyword = text;
                // TODO Auto-generated method stub
                onSearchButtonClick();
                dismiss();
            }
        });

        mClearHistory.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                onClearButtonClick();
            }
        });

        mIvBack.setOnClickListener(new View.OnClickListener() {
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

    /**
     * 搜索按钮被点击的回调
     */
    public void onSearchButtonClick() {
    };

    /**
     * 清空搜索记录按钮被点击的回调
     */
    public void onClearButtonClick() {
        MainActivity.dataBaseManager.clearSearchHistory();
        history.clear();
        mAdapter.notifyDataSetChanged();
    };


    public void dismiss(){
        if(mPopupWindow!=null && mPopupWindow.isShowing()){
            mPopupWindow.dismiss();
        }
    }

    //删除listview中的一个项目，包含动画
    private void deleteItem(View view, final int position) {

        Animation.AnimationListener al = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            //动画结束以后才真正删除
            @Override
            public void onAnimationEnd(Animation animation) {
                String item = history.get(position);
                MainActivity.dataBaseManager.removeSearchHistory(item);
                history.remove(item);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        collapse(view, al);
    }

    public static void collapse(final View view, Animation.AnimationListener al) {
        final int originHeight = view.getMeasuredHeight();
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1.0f) {
                    view.getLayoutParams().height = originHeight;//更改部分避免删除两个Item
                } else {
                    view.getLayoutParams().height = originHeight - (int) (originHeight * interpolatedTime);
                }
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        if (al != null) {
            animation.setAnimationListener(al);
        }
        animation.setDuration(300);
        view.startAnimation(animation);
    }

    public String getKeyword() {
        return keyword;
    }
}


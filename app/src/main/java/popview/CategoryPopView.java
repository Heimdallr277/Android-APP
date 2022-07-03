package popview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.java.mashihe.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import adapter.CategoryAdapter;
import background.DataBaseManager;
import util.ScreenUtil;

public class CategoryPopView {
    private Context mContext;
    private View anchor;
    private LayoutInflater mInflater;
    private ListView mLvAdded;
    private ListView mLvDeleted;
    private CategoryAdapter mAddedCategoryAdapter;
    private CategoryAdapter mDeletedCategoryAdapter;
    private TextView mTvSave;
    private TextView mTvCancel;
    private PopupWindow mPopupWindow;
    LayoutAnimationController controller;
    WindowManager.LayoutParams params;
    WindowManager windowManager;
    Window window;
    ArrayList<String> added;
    ArrayList<String> deleted;
    static String[] allTitles = new String[]{"全部","娱乐","军事","教育","文化","健康","财经","体育","汽车","科技","社会"};

    /**
     * @param context
     * @param anchor  依附在哪个View下面
     */
    public CategoryPopView(Activity context, View anchor, String[] titles) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.anchor = anchor;
        windowManager = context.getWindowManager();
        window = context.getWindow();
        params = context.getWindow().getAttributes();
        setCurrentCategory(titles);
        init();
    }

    public void init() {
        View view = mInflater.inflate(R.layout.category_pop_window, null);
        params.dimAmount = 0.5f;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mLvAdded = view.findViewById(R.id.lv_added);
        mLvDeleted = view.findViewById(R.id.lv_deleted);
        mTvCancel = view.findViewById(R.id.tv_cancel);
        mTvSave = view.findViewById(R.id.tv_save);
        mLvAdded = view.findViewById(R.id.lv_added);
        mLvDeleted = view.findViewById(R.id.lv_deleted);

        @SuppressLint("ResourceType") Animation animation = AnimationUtils.loadAnimation(mContext, R.animator.anim_item);
        controller = new LayoutAnimationController(animation);
        controller.setDelay(0.5f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        mLvAdded.setLayoutAnimation(controller);
        mLvDeleted.setLayoutAnimation(controller);

        mAddedCategoryAdapter = new CategoryAdapter(mContext, added);
        mLvAdded.setAdapter(mAddedCategoryAdapter);
        mLvAdded.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        //长按进行删除操作，有动画
        mLvAdded.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String)mAddedCategoryAdapter.getItem(i);
                deleteItem(view, i, added, deleted, mAddedCategoryAdapter, mDeletedCategoryAdapter, mLvDeleted);
                DataBaseManager.getInstance(mContext).removeCategory(item);
                Snackbar.make(view, "已删除："+item, Snackbar.LENGTH_LONG).show();
                return true;
            }
        });

        mDeletedCategoryAdapter = new CategoryAdapter(mContext, deleted);
        mLvDeleted.setAdapter(mDeletedCategoryAdapter);
        mLvDeleted.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        //长按进行添加操作，有动画
        mLvDeleted.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String)mDeletedCategoryAdapter.getItem(i);
                deleteItem(view, i, deleted, added, mDeletedCategoryAdapter, mAddedCategoryAdapter, mLvAdded);
                DataBaseManager.getInstance(mContext).addCategory(item);
                Snackbar.make(view, "已添加："+item, Snackbar.LENGTH_LONG).show();
                return true;
            }
        });

        mTvSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                onSaveButtonClick();
                dismiss();
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

    /**
     * 保存被点击的回调
     */
    public void onSaveButtonClick() {

    };

    public void dismiss(){
        if(mPopupWindow!=null && mPopupWindow.isShowing()){
            mPopupWindow.dismiss();
        }
    }

    public void setCurrentCategory(String[] titles) {
        added = new ArrayList<String>();
        deleted = new ArrayList<String>();
        for (int i = 1; i < titles.length; i++) {
            added.add(titles[i]);
        }
        for (int i = 1; i < allTitles.length; i++) {
            if (!added.contains(allTitles[i])) {
                deleted.add(allTitles[i]);
            }
        }
    }

    //删除listview中的一个项目，包含动画
    private void deleteItem(View view, final int position, final ArrayList<String> listToDelete, final ArrayList<String> listToAdd, final CategoryAdapter adapterTodelete, final CategoryAdapter adapterToAdd, final ListView listViewToAdd) {

        Animation.AnimationListener al = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            //动画结束以后才真正删除
            @Override
            public void onAnimationEnd(Animation animation) {
                String item = listToDelete.get(position);
                listToDelete.remove(position);
                listToAdd.add(item);
                adapterToAdd.notifyDataSetChanged();
                adapterTodelete.notifyDataSetChanged();
                listViewToAdd.setLayoutAnimation(controller);
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
}


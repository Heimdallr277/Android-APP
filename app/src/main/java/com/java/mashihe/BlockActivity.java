package com.java.mashihe;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adapter.BlockedWordsAdapter;
import util.ScreenUtil;

import static com.java.mashihe.MainActivity.toList;

public class BlockActivity extends AppCompatActivity {

    private ImageView mIvBack;
    private ImageView mIvAdd;
    private EditText mEtInput;
    private ListView mLvBlockedWords;
    private BlockedWordsAdapter blockedWordsAdapter;
    private LayoutAnimationController controller;

    ArrayList<String> blockedwords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);
        mIvAdd = findViewById(R.id.iv_block_add);
        mIvBack = findViewById(R.id.iv_block_back);
        mEtInput = findViewById(R.id.et_block_input);
        mLvBlockedWords = findViewById(R.id.lv_blockedlist);

        Bundle bd = getIntent().getExtras();
        int currentNightMode = bd.getInt("nightMode");
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        blockedwords = toList(MainActivity.dataBaseManager.getAllForbiddenWords());
        blockedWordsAdapter = new BlockedWordsAdapter(BlockActivity.this, blockedwords);
        mLvBlockedWords.setAdapter(blockedWordsAdapter);

        mEtInput.setMaxLines(1);
        mEtInput.setWidth(ScreenUtil.getScreenWidth(this)-100);
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
                    //先隐藏键盘
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getCurrentFocus()
                                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    //其次再做相应操作
                    mIvAdd.callOnClick();
                }
                return false;
            }
        });

        @SuppressLint("ResourceType") Animation animation = AnimationUtils.loadAnimation(BlockActivity.this, R.animator.anim_item);
        controller = new LayoutAnimationController(animation);
        controller.setDelay(0.5f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        mLvBlockedWords.setLayoutAnimation(controller);

        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mIvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = mEtInput.getText().toString();
                if (text.isEmpty()) {
                    Toast.makeText(BlockActivity.this, "输入内容不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (blockedwords.contains(text)) {
                    Toast.makeText(BlockActivity.this, "该词已添加！", Toast.LENGTH_SHORT).show();
                } else {
                    blockedwords.add(text);
                    Snackbar.make(view, "已添加屏蔽词", Snackbar.LENGTH_LONG).show();
                    mEtInput.setText("");
                }
                MainActivity.dataBaseManager.setForbiddenWords(new String[]{text});
                blockedWordsAdapter.notifyDataSetChanged();
            }
        });

        mLvBlockedWords.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                deleteItem(view, i);
                Snackbar.make(view, "已删除", Snackbar.LENGTH_LONG).show();
                return true;
            }
        });

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
                String item = blockedwords.get(position);
                blockedwords.remove(item);
                MainActivity.dataBaseManager.removeForbiddenWords(new String[]{item});
                blockedWordsAdapter.notifyDataSetChanged();
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

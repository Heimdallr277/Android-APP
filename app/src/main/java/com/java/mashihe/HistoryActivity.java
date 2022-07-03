package com.java.mashihe;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import adapter.EmptyFragment;
import adapter.NewsListFragment;
import background.Data;
import background.DataBaseManager;
import util.ScreenUtil;

import static com.java.mashihe.MainActivity.toList;

public class HistoryActivity extends AppCompatActivity {

    private ImageView mIvBack;
    private ImageView mIvClearAll;
    private TextView mTvTitle;
    private FrameLayout mFlResult;
    private NewsListFragment newsListFragment;
    private FragmentManager fragmentManager;
    private DataBaseManager dataBaseManager;
    private ArrayList<Data> history_list;
    private int index;
    private boolean clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        fragmentManager = getSupportFragmentManager();
        mIvBack = findViewById(R.id.iv_history_backtomain);
        mFlResult = findViewById(R.id.fl_history_result);
        mIvClearAll = findViewById(R.id.iv_history_clearall);
        mTvTitle = findViewById(R.id.tv_history_title);
        clear = false;

        mTvTitle.setWidth(ScreenUtil.getScreenWidth(HistoryActivity.this)-100);
        history_list = toList(MainActivity.dataBaseManager.getLatestBrowseHistory(20));
        index = 20;

        newsListFragment = new NewsListFragment(HistoryActivity.this);
        newsListFragment.setParser(null);
        newsListFragment.setfManager(fragmentManager);
        newsListFragment.setDatas(history_list);
        if (newsListFragment.isEmpty()) {
            fragmentManager.beginTransaction().replace(R.id.fl_history_result, new EmptyFragment(), "emptyresult").commitAllowingStateLoss();
        } else {
            fragmentManager.beginTransaction().replace(R.id.fl_history_result, newsListFragment, "result").commitAllowingStateLoss();
        }

        Bundle bd = getIntent().getExtras();
        int currentNightMode = bd.getInt("nightMode");
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // 返回键
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // 清空键
        mIvClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                builder.setTitle("确认清空浏览记录？")
                        .setIcon(R.drawable.ic_info_outline_grey_800_36dp)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 清空浏览记录
                                MainActivity.dataBaseManager.removeAllBrowseHistory();
                                fragmentManager.beginTransaction().replace(R.id.fl_history_result, new EmptyFragment()).commitAllowingStateLoss();
                                Snackbar.make(view, "已清空", Snackbar.LENGTH_SHORT).show();
                                clear = true;
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 取消

                            }
                        }).show();
            }
        });



    }

    // 返回键
    @Override
    public void onBackPressed() {
        if (clear) {
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    public int getIndex() {
        return index;
    }

    public void addIndex(int i) {
        index = index + i;
    }

    public void setClear() {
        clear = true;
    }
}

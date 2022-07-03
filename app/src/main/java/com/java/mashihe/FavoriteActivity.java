package com.java.mashihe;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;

import adapter.EmptyFragment;
import adapter.NewsListFragment;
import background.Data;
import background.DataBaseManager;
import util.ScreenUtil;

import static com.java.mashihe.MainActivity.toList;

public class FavoriteActivity extends AppCompatActivity {

    private ImageView mIvBack;
    private ImageView mIvClearAll;
    private TextView mTvTitle;
    private FrameLayout mFlResult;
    private NewsListFragment newsListFragment;
    private FragmentManager fragmentManager;
    private DataBaseManager dataBaseManager;
    private ArrayList<Data> fav_list;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        fragmentManager = getSupportFragmentManager();
        mIvBack = findViewById(R.id.iv_fav_backtomain);
        mFlResult = findViewById(R.id.fl_fav_result);
        mIvClearAll = findViewById(R.id.iv_fav_clearall);
        mTvTitle = findViewById(R.id.tv_fav_title);

        mTvTitle.setWidth(ScreenUtil.getScreenWidth(FavoriteActivity.this)-100);
        fav_list = toList(DataBaseManager.getInstance(FavoriteActivity.this).getLatestFavorite(20));
        index = 20;

        newsListFragment = new NewsListFragment(FavoriteActivity.this);
        newsListFragment.setParser(null);
        newsListFragment.setDatas(fav_list);
        newsListFragment.setfManager(fragmentManager);

        if (newsListFragment.isEmpty()) {
            fragmentManager.beginTransaction().replace(R.id.fl_fav_result, new EmptyFragment(), "emptyresult").commitAllowingStateLoss();
        } else {
            fragmentManager.beginTransaction().replace(R.id.fl_fav_result, newsListFragment, "result").commitAllowingStateLoss();
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

    }

    // 返回键
    @Override
    public void onBackPressed() {
        finish();
    }

    public void changeToEmpty() {
        while (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStack();
        }
        fragmentManager.beginTransaction().replace(R.id.fl_fav_result, new EmptyFragment()).commitAllowingStateLoss();
    }

    public int getIndex() {
        return index;
    }

    public void addIndex(int i) {
        index = index + i;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            recreate();
            Log.d("FavActivity", "-----------------Remove----------------");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

package com.java.mashihe;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;

import adapter.EmptyFragment;
import adapter.NewsListFragment;
import popview.SearchPopView;
import util.ScreenUtil;

public class SearchActivity extends AppCompatActivity {

    private ImageView mIvBack;
    private ImageView mIvSearch;
    private TextView mTvTitle;
    private FrameLayout mFlResult;
    private NewsListFragment newsListFragment;
    private SearchPopView searchPopView;
    private FragmentManager fragmentManager;

    String keyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        fragmentManager = getSupportFragmentManager();

        mIvBack = findViewById(R.id.iv_backtomain);
        mIvSearch = findViewById(R.id.iv_search_in_searchpage);
        mTvTitle = findViewById(R.id.tv_search_title);
        mFlResult = findViewById(R.id.fl_search_result);

        mTvTitle.setWidth(ScreenUtil.getScreenWidth(SearchActivity.this)-100);

        Bundle bundle = getIntent().getExtras();
        keyword = bundle.getString("keyword");

        int currentNightMode = bundle.getInt("nightMode");
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

        doSearch();

        // 搜索键
        mIvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchPopView = new SearchPopView(SearchActivity.this, mTvTitle) {
                    @Override
                    public void onSearchButtonClick() {
                        keyword = getKeyword();
                        //TODO: Do Search
                        doSearch();
                    }
                };
                searchPopView.show();
            }
        });


    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void doSearch() {
        while (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStack();
        }
        newsListFragment = new NewsListFragment(SearchActivity.this);
        newsListFragment.getSearchResult(keyword);
        newsListFragment.setfManager(fragmentManager);
        if (newsListFragment.isEmpty()) {
            fragmentManager.beginTransaction().replace(R.id.fl_search_result, new EmptyFragment(), "emptyresult").commitAllowingStateLoss();
        } else {
            fragmentManager.beginTransaction().replace(R.id.fl_search_result, newsListFragment, "result").commitAllowingStateLoss();
        }
        Toast.makeText(SearchActivity.this, "搜索完成", Toast.LENGTH_SHORT).show();
    }
}

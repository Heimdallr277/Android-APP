package com.java.mashihe;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import adapter.NewsContentFragment;
import adapter.NewsImageAdapter;
import popview.SharePopView;
import util.ImageSaver;

public class NewsPageScrollingActivity extends AppCompatActivity {

    private NewsContentFragment newsContentFragment;
    private RecyclerView mRvImages;
    private String newsID;
    private MenuItem menuItem;
    private int itemID;
    private String[] images;
    private SharePopView sharePopView;

    String url;
    String title;
    String text;
    String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_page_scrolling);
//        mImageView = findViewById(R.id.news_image);
//        menuItem = findViewById(R.id.action_star);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置返回键和菜单栏可用可见
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        newsContentFragment = new NewsContentFragment();
        Bundle bundle = getIntent().getExtras();
        newsContentFragment.setArguments(bundle);
        newsID = bundle.getString("newsID");
        itemID = bundle.getInt("itemID");
        images = bundle.getStringArray("image");
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_news_content, newsContentFragment, "news").commitAllowingStateLoss();

        url = bundle.getString("url");
        title = bundle.getString("title");
        text = bundle.getString("content").substring(0, 50);
        String imageholderUrl = "https://wx3.sinaimg.cn/mw690/80d53d49ly1g6opfwpo6aj209v08ywf9.jpg";
        if (images!=null&&images.length!=0) {
            imageUrl = images[0];
        } else {
            imageUrl = imageholderUrl;
            images = new String[]{imageUrl};
        }

        if (MainActivity.isNetworkConnected(NewsPageScrollingActivity.this)) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap[] bitmaps = new Bitmap[images.length];
                    for (int i = 0; i < images.length; i++) {
                        bitmaps[i] = ImageSaver.returnBitMap(images[i]);
                    }
                    MainActivity.dataBaseManager.addImageCache(images, bitmaps);
                }
            });
            thread.start();
        }



        mRvImages = (RecyclerView)findViewById(R.id.rv_image_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NewsPageScrollingActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvImages.setLayoutManager(linearLayoutManager);
        mRvImages.setAdapter(new NewsImageAdapter(NewsPageScrollingActivity.this, images));

        // 悬浮球点击事件
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 分享
                // TODO: Share
                sharePopView = new SharePopView(NewsPageScrollingActivity.this, mRvImages, url, title, text, imageUrl);
                sharePopView.show();
            }
        });

        int currentNightMode = bundle.getInt("nightMode");
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

//        if (bundle.getStringArray("image")!=null && bundle.getStringArray("image").length!=0) {
////            mImageView.setImageBitmap(BitmapTools.compress(BitmapTools.getBitmapFromUrl(bundle.getStringArray("image")[0]),300,200));
//            Glide.with(this).load(bundle.getStringArray("image")[0]).into(mImageView);
//        }




    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_star:
                if (MainActivity.dataBaseManager.isFavorite(newsID)) {
                    MainActivity.dataBaseManager.removeFavorite(newsID);
                    Snackbar.make(mRvImages, "已取消收藏", Snackbar.LENGTH_LONG).show();
                    menuItem.setTitle("收藏");
                } else {
                    MainActivity.dataBaseManager.addFavoriteByID(newsID);
                    Snackbar.make(mRvImages, "已收藏", Snackbar.LENGTH_LONG).show();
                    menuItem.setTitle("取消收藏");
                }
                return true;
            case R.id.action_share:
                //TODO: Share
                sharePopView = new SharePopView(NewsPageScrollingActivity.this, mRvImages, url, title, text, imageUrl);
                sharePopView.show();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news_page_scrolling, menu);
        menuItem = menu.findItem(R.id.action_star);
        menuItem.setTitle(MainActivity.dataBaseManager.isFavorite(newsID)?"取消收藏":"收藏");
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        Bundle bundle1 = new Bundle();
        bundle1.putString("removeFav", newsID);
        bundle1.putInt("itemID", itemID);
        intent.putExtras(bundle1);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}

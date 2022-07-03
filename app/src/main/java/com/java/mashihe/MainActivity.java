package com.java.mashihe;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.astuetz.PagerSlidingTabStrip;
import com.billy.android.preloader.PreLoader;
import com.billy.android.preloader.interfaces.DataListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import adapter.LoadingFragment;
import adapter.NewsListFragment;
import adapter.OptionAdapter;
import adapter.TextTabAdapter;
import background.Data;
import background.DataBaseManager;
import background.JSONParser;
import popview.CategoryPopView;
import popview.SearchPopView;
import titlebar.TitleFragment;

//import com.sina.weibo.sdk.WbSdk;
//import com.sina.weibo.sdk.auth.AuthInfo;

public class MainActivity extends AppCompatActivity {

    private FrameLayout fl_title;
    private RelativeLayout rl_left;
    private DrawerLayout drawerLayout;
    private Context mContext;
    private FragmentManager fManager = null;
    private long exitTime = 0;

    private Calendar calendar;
    private TextView mTvGreeting;
    private TitleFragment titleFragment;
    private ListView option_list;

    private ImageView mIvAddCategory;
    private CategoryPopView categoryPopView;
    private SearchPopView searchPopView;

    private ViewPager pager;
    private PagerSlidingTabStrip tabs;
    private TextTabAdapter textTabAdapter;
    public static DataBaseManager dataBaseManager;

    private static String[] allTitles = new String[]{"全部","娱乐","军事","教育","文化","健康","财经","体育","汽车","科技","社会"};
    String[] titles;
    ArrayList<Fragment> fragmentList;
    HashMap<String, Fragment> map;
    HashMap<String, JSONParser> parserHashMap;

    private boolean[] clearOptions = {true, true, true, true};

    //数据加载完成后，会调用DataListener.onDataArrived(...)来处理加载后的数据
    class Listener implements DataListener<Pair<Data[][], JSONParser[]>> {
        @Override
        public void onDataArrived(Pair<Data[][], JSONParser[]> data) {
            if (!isNetworkConnected(MainActivity.this)) {
                Toast.makeText(MainActivity.this, "网络连接失败", Toast.LENGTH_LONG).show();
            }


            //此方法在主线程中运行，无需使用Handler切换线程运行
            Log.d("StartLoading", "-----------------Start---------------------");
            Data[][] newsdata = data.first;
            JSONParser[] parsers = data.second;

            for (int i = 0; i < newsdata.length; i++) {
                NewsListFragment fragment = new NewsListFragment(MainActivity.this);
                fragment.setfManager(getSupportFragmentManager());
                fragment.setDatas(toList(newsdata[i]));
                fragment.setParser(parsers[i]);
                map.put(allTitles[i], fragment);
                parserHashMap.put(allTitles[i], parsers[i]);
            }
            buildFragmentList();
            reloadTabs(titles);
            Log.d("FinishLoading", "-----------------Finish---------------------");
            PreLoader.destroy(getTaskId());
        }
    }

    public MainActivity() {
        if (dataBaseManager == null) {
            dataBaseManager = DataBaseManager.getInstance(MainActivity.this);
        }
        this.titles = dataBaseManager.getAllCategory();
        this.fragmentList = new ArrayList<Fragment>();
        for (int i = 0; i < titles.length; i++) {
            LoadingFragment fragment = new LoadingFragment();
            fragmentList.add(fragment);
        }
        this.map = new HashMap<String, Fragment>();
        this.parserHashMap = new HashMap<>();
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static ArrayList<Data> toList(Data[] datas) {
        if (datas == null) {
            return new ArrayList<Data>();
        }
        return new ArrayList<Data>(Arrays.asList(datas));
    }

    public static ArrayList<String> toList(String[] datas) {
        if (datas == null) {
            return new ArrayList<String>();
        }
        return new ArrayList<String>(Arrays.asList(datas));
    }

    public String[] toArray(ArrayList<String> datas) {
        String[] strings = new String[datas.size()];
        datas.toArray(strings);
        return strings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MainActivity", "----------onCreate---------------");
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        fManager = getSupportFragmentManager();
        bindViews();
//        WbSdk.install(this,new AuthInfo(this, SyncStateContract.Constants.APP_KEY, SyncStateContract.Constants.REDIRECT_URL,
//                SyncStateContract.Constants.SCOPE));

        if (dataBaseManager == null) {
            dataBaseManager = DataBaseManager.getInstance(MainActivity.this);
        }

        rl_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour<11) {
            mTvGreeting.setText("早上好！");
        } else if (hour < 13) {
            mTvGreeting.setText("中午好！");
        } else if (hour < 18) {
            mTvGreeting.setText("下午好！");
        } else {
            mTvGreeting.setText("晚上好！");
        }

        textTabAdapter = new TextTabAdapter(getSupportFragmentManager(), titles, fragmentList);
        pager.setAdapter(textTabAdapter);
        setPageTabs();

        //左边菜单栏的点击事件
        option_list.setAdapter(new OptionAdapter(MainActivity.this));
        option_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                Intent intent = new Intent();
                drawerLayout.closeDrawer(Gravity.LEFT);
                switch (i) {
                    case 0:
                        // 我的收藏
                        intent = new Intent(MainActivity.this, FavoriteActivity.class);
                        intent.putExtra("nightMode", getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
                        startActivity(intent);
                        break;
                    case 1:
                        // 浏览历史
                        intent = new Intent(MainActivity.this, HistoryActivity.class);
                        intent.putExtra("nightMode", getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
                        startActivityForResult(intent, 3);
                        break;
                    case 2:
                        // 屏蔽设置
                        intent = new Intent(MainActivity.this, BlockActivity.class);
                        intent.putExtra("nightMode", getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
                        startActivity(intent);
                        break;
                    case 3:
                        // 清空缓存
                        final String[] array = new String[]{"浏览历史", "搜索历史", "屏蔽词", "全部缓存"};
                        boolean[] isSelected = new boolean[]{true, true, true, true};
                        clearOptions = isSelected;
                        AlertDialog.Builder builder4 = new AlertDialog.Builder(MainActivity.this);
                        builder4.setTitle("选择你要清空的内容！");
                        builder4.setMultiChoiceItems(array, isSelected, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                clearOptions[i] = b;
                            }
                        }).setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (clearOptions[0]) {
                                    dataBaseManager.removeAllBrowseHistory();
                                }
                                if (clearOptions[1]) {
                                    dataBaseManager.clearSearchHistory();
                                }
                                if (clearOptions[2]) {
                                    dataBaseManager.removeAllForbiddenWords();
                                }
                                if (clearOptions[3]) {
                                    dataBaseManager.removeAllNewsAndBrowserHistory();
                                }
                                Snackbar.make(view, "已清空", Snackbar.LENGTH_SHORT).show();
                                if (clearOptions[0]||clearOptions[3]) {
                                    recreate();
                                }

                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                        break;

                    case 4:
                        // 夜间模式
                        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
//                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                        } else {
//                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                        recreate();
                        break;

                }
            }
        });

        //添加分类按钮点击事件
        mIvAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                categoryPopView = new CategoryPopView(MainActivity.this, view, titles) {
                    @Override
                    public void onSaveButtonClick() {
                        Snackbar.make(view, "已保存修改", Snackbar.LENGTH_LONG).show();
                        reloadTabs(dataBaseManager.getAllCategory());
                        pager.getAdapter().notifyDataSetChanged();
                    }
                };
                categoryPopView.show();
            }
        });


        titleFragment = new TitleFragment();
        fManager.beginTransaction().add(R.id.fl_title, titleFragment, "title").commitAllowingStateLoss();


        Bundle bundle = getIntent().getExtras();
        int preLoaderId = bundle.getInt("preLoaderId");
        ArrayList<Integer> curPages = bundle.getIntegerArrayList("currentPages");
        PreLoader.listenData(preLoaderId, new Listener());

    }


    private void bindViews() {
        drawerLayout = findViewById(R.id.dl);
        fl_title = findViewById(R.id.fl_title);
        rl_left = findViewById(R.id.rl_left);
        option_list = findViewById(R.id.option_list);
        mIvAddCategory = findViewById(R.id.iv_addcat);
        pager = findViewById(R.id.pager);
        tabs = findViewById(R.id.tabs);
        mTvGreeting = findViewById(R.id.greeting);
    }

    //设置导航栏的样式
    private void setPageTabs() {
        // 设置Tab底部选中的指示器 Indicator的颜色
//        tabs.setIndicatorColor(Color.RED);
        //设置Tab标题文字的颜色
//        tabs.setTextColor(R.color.colorPrimaryDark);
        // 设置Tab标题文字的大小
        tabs.setTextSize(40);
        //设置Tab底部分割线的颜色
//        tabs.setUnderlineColor(R.color.colorPrimary);
//        tabs.setUnderlineHeight(20);
        // 设置点击某个Tab时的背景色,设置为0时取消背景色
        tabs.setTabBackground(Color.TRANSPARENT);
        // 设置Tab是自动填充满屏幕的
        tabs.setShouldExpand(true);
        //!!!设置选中的Tab文字的颜色!!!

//        tabs.setSelectedTextColor(Color.GREEN);
        //tab间的分割线
//        tabs.setDividerColor(R.color.colorPrimary);
//        tabs.setDividerPadding(5);
        //底部横线与字体宽度一致
//        tabs.setIndicatorFollower(true);
        //与ViewPager关联，这样指示器就可以和ViewPager联动
        tabs.setViewPager(pager);

    }


    //点击回退键的处理：判断Fragment栈中是否有Fragment
    //没，双击退出程序，否则像是Toast提示
    //有，popbackstack弹出栈
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }
        if (fManager.getBackStackEntryCount() == 0) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }
        } else {
            fManager.popBackStack();
        }
    }

    public void showMenu() {
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    //设置导航栏
    public void reloadTabs(String[] t) {
        this.titles = t;
        pager.getAdapter().notifyDataSetChanged();
        buildFragmentList();
        pager.getAdapter().notifyDataSetChanged();
        textTabAdapter = new TextTabAdapter(getSupportFragmentManager(), titles, fragmentList);
        pager.setAdapter(textTabAdapter);
        setPageTabs();
    }

    public void showSearchBox() {
        searchPopView = new SearchPopView(MainActivity.this, fl_title) {
            @Override
            public void onSearchButtonClick() {

                String keyword = getKeyword();

                //TODO: Do Search
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("keyword", keyword);
                bundle.putInt("nightMode", getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
                intent.putExtras(bundle);
                startActivity(intent);

                Toast.makeText(MainActivity.this, "搜索完成", Toast.LENGTH_SHORT).show();
            }
        };
        searchPopView.show();
    }

    public void buildFragmentList() {
        ArrayList<Fragment> list = new ArrayList<Fragment>();
        for (int i = 0; i < titles.length; i++) {
            list.add(map.get(titles[i]));
        }
        fragmentList.clear();
        fragmentList.addAll(list);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            recreate();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void recreate() {
            try {//避免重启太快恢复
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                for (Fragment fragment : fragmentList) {
                    fragmentTransaction.remove(fragment);
                }
                fragmentTransaction.commitAllowingStateLoss();
            } catch (Exception e) {
            }
            super.recreate();
    }

}

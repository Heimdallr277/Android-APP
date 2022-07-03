package com.java.mashihe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.billy.android.preloader.PreLoader;
import com.billy.android.preloader.interfaces.DataLoader;

import java.util.ArrayList;
import java.util.Arrays;

import background.Data;
import background.DataBaseManager;
import background.JSONParser;
import background.QueryResult;

class Loader implements DataLoader<Pair<Data[][], JSONParser[]>> {

    private Context mContext;
    private static String[] titles = new String[]{"全部","娱乐","军事","教育","文化","健康","财经","体育","汽车","科技","社会"};
    private Data[][] preloadedData;
    private JSONParser[] mParsers;

    public Loader(Context context) {
        mContext = context;
    }

    public <T> ArrayList<T> toList(T[] datas) {
        if (datas == null) {
            return new ArrayList<T>();
        }
        return new ArrayList<T>(Arrays.asList(datas));
    }

    @Override
    public Pair<Data[][], JSONParser[]> loadData() {
        //此方法在线程池中运行，无需再开子线程去加载数据
        preloadedData = new Data[titles.length][];
        mParsers = new JSONParser[titles.length];

        Log.d("PreLoading", "-----------------Start---------------------");

        if (MainActivity.isNetworkConnected(mContext)) {
            // 如果联网，就从网络上加载最新新闻

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Bundle bundle = new Bundle();
                    JSONParser parser = new JSONParser(bundle);
                    parser.setManager(SplashActivity.dataBaseManager);
                    QueryResult res = parser.next();
                    mParsers[0] = parser;
                    preloadedData[0] = res.getData();

                    Thread[] threads = new Thread[titles.length];
                    for (int i = 1; i < titles.length; i++) {
                        final int id = i;
                        threads[i] = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bundle bd = new Bundle();
                                bd.putString("categories", titles[id]);
                                JSONParser parser1 = new JSONParser(bd);
                                parser1.setManager(SplashActivity.dataBaseManager);
                                QueryResult r = parser1.next();
                                mParsers[id] = parser1;
                                preloadedData[id] = r.getData();
                            }
                        });
                        threads[i].start();
                    }

                    for (int i = 1; i < titles.length; i++) {
                        try {
                            threads[i].join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            thread.start();

            while(thread.isAlive()) {

            }

        } else {
            // 如果未联网，先加载本地新闻
            preloadedData[0] = SplashActivity.dataBaseManager.getLatestInsertedNews(null, 20);
            Bundle bundle = new Bundle();
            JSONParser parser = new JSONParser(bundle);
            parser.setManager(SplashActivity.dataBaseManager);
            mParsers[0] = parser;
            for (int i = 1; i < titles.length; i++) {
                Bundle bd = new Bundle();
                bd.putString("categories", titles[i]);
                JSONParser parser1 = new JSONParser(bd);
                parser1.setManager(SplashActivity.dataBaseManager);
                mParsers[i] = parser1;
                preloadedData[i] = SplashActivity.dataBaseManager.getLatestInsertedNews(titles[i], 20);
            }
        }

        Log.d("PreLoading", "-----------------Finish---------------------");

        return new Pair<Data[][], JSONParser[]>(preloadedData, mParsers);
    }

}

public class SplashActivity extends AppCompatActivity {

    public static DataBaseManager dataBaseManager;
    private ImageView mIvSplashScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        mIvSplashScreen = findViewById(R.id.iv_splash_screen);

        if (dataBaseManager == null) {
            dataBaseManager = DataBaseManager.getInstance(SplashActivity.this);
        }

        Thread myThread = new Thread() {
            @Override
            public void run() {
                try {
                    int preLoaderId = PreLoader.preLoad(new Loader(getApplicationContext()));
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("preLoaderId", preLoaderId);
                    sleep(2000);    //开屏页面，程序休眠5秒
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        myThread.start();
    }

}

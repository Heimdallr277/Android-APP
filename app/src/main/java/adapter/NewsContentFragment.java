package adapter;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.java.mashihe.MainActivity;
import com.java.mashihe.R;

import java.util.ArrayList;

import background.Data;
import popview.BottomPopView;

public class NewsContentFragment extends Fragment {

    private TextView mTvContent;
    private TextView mTvTitle;
    private TextView mTvPublisher;
    private TextView mTvPublishTime;
    private FrameLayout mFlNewsRecommended;
    private FrameLayout mFlVideo;
    private RecommendedNewsListFragment recommendedNewsListFragment;
    private VideoFragment videoFragment;
    private FragmentManager fragmentManager;
    private LoadingFragment loadingFragment ;
    private NetWorkErrorFragment netWorkErrorFragment;

    private BottomPopView bottomPopView;
    private ArrayList<Data> recommendDatas;
    private String newsID;
    private String keywords;
    private String category;
    private String videoUrl;
    private Handler mHandler;

    public NewsContentFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.newspage, container, false);
        mTvContent = view.findViewById(R.id.txt_content);
        mTvTitle = view.findViewById(R.id.news_title);
        mTvPublisher = view.findViewById(R.id.news_publisher);
        mTvPublishTime = view.findViewById(R.id.news_publishTime);
        mFlNewsRecommended = view.findViewById(R.id.fl_recommended);
        mFlNewsRecommended.getLayoutParams().height = 2600;
        mFlVideo = view.findViewById(R.id.fl_video);

        //getArgument获取传递过来的Bundle对象
        mTvContent.setText(getArguments().getString("content"));
        mTvTitle.setText(getArguments().getString("title"));
        mTvPublisher.setText(getArguments().getString("publisher"));
        mTvPublishTime.setText(getArguments().getString("publishTime"));
        newsID = getArguments().getString("newsID");
        keywords = getArguments().getString("keywords");
        category = getArguments().getString("category");
        videoUrl = getArguments().getString("video");

        fragmentManager = getActivity().getSupportFragmentManager();

        if (MainActivity.isNetworkConnected(getContext())) {
            loadingFragment = new LoadingFragment();
            fragmentManager.beginTransaction().replace(R.id.fl_recommended, loadingFragment, "recommended").commitAllowingStateLoss();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    recommendDatas = MainActivity.toList(MainActivity.dataBaseManager.relatedNews(keywords, category, newsID));
                    Message message = new Message();
                    message.what = 1;
                    mHandler.sendMessage(message);
                }
            });

            thread.start();

            if (!videoUrl.isEmpty()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mFlVideo.getLayoutParams().height = 500;
                        videoFragment = new VideoFragment(videoUrl);
                        Log.d("Video", "-----------"+videoUrl);
                        fragmentManager.beginTransaction().replace(R.id.fl_video, videoFragment, "video").commitAllowingStateLoss();
                    }
                }).start();
            }

        } else {
            netWorkErrorFragment = new NetWorkErrorFragment();
            fragmentManager.beginTransaction().replace(R.id.fl_recommended, netWorkErrorFragment, "recommended").commitAllowingStateLoss();
        }



        mHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 1:
                        recommendedNewsListFragment = new RecommendedNewsListFragment(getActivity());
                        recommendedNewsListFragment.setDatas(recommendDatas);
                        recommendedNewsListFragment.setfManager(fragmentManager);
                        fragmentManager.beginTransaction().replace(R.id.fl_recommended, recommendedNewsListFragment).commitAllowingStateLoss();
                        break;
                }
                super.handleMessage(msg);
            }
        };



        return view;
    }

}
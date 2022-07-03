package adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;
import com.java.mashihe.MainActivity;
import com.java.mashihe.NewsPageScrollingActivity;
import com.java.mashihe.R;

import java.util.ArrayList;

import background.Data;
import popview.BottomPopView;
import popview.SharePopView;

import static com.java.mashihe.MainActivity.dataBaseManager;

public class RecommendedNewsListFragment extends Fragment implements AdapterView.OnItemClickListener {
    private FragmentManager fManager;
    private ArrayList<Data> recommendDatas;
    private ListView list_news;
    private NewsListAdapter newsListAdapter;
    private BottomPopView bottomPopView;
    private Activity mActivity;

    public RecommendedNewsListFragment() {

    }

    public RecommendedNewsListFragment(Activity activity){
        mActivity = activity;
    }

    public void setDatas(ArrayList<Data> arr){
        recommendDatas=arr;
    }

    public void setfManager(FragmentManager manager){
        fManager=manager;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.newslist_recommended, container, false);
        list_news = view.findViewById(R.id.recommended_list_news);
        newsListAdapter = new NewsListAdapter(recommendDatas, getActivity());

        list_news.setAdapter(newsListAdapter);
        list_news.setOnItemClickListener(this);
        list_news.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, int i, long l) {
                final int id = i;
                Data target = (Data) newsListAdapter.getItem(i);
                final String newsID = target.getNewsID();
                final boolean isFav = MainActivity.dataBaseManager.isFavorite(newsID);

                final String url = target.getUrl();
                final String title = target.getTitle();
                final String text = target.getContent().substring(0, 50);
                final String imageholderUrl = "https://wx3.sinaimg.cn/mw690/80d53d49ly1g6opfwpo6aj209v08ywf9.jpg";
                final String imageUrl = (target.getImage()!=null&&target.getImage().length!=0)?target.getImage()[0]:imageholderUrl;

                //底部弹出的布局 收藏与分享
                bottomPopView = new BottomPopView(getActivity(), view) {
                    @Override
                    public void onTopButtonClick() {
                        if (isFav) {
                            MainActivity.dataBaseManager.removeFavorite(newsID);
                            Snackbar.make(view, "已取消收藏", Snackbar.LENGTH_LONG).show();
                        } else {
                            MainActivity.dataBaseManager.addFavorite((Data) newsListAdapter.getItem(id));
                            Snackbar.make(view, "已收藏", Snackbar.LENGTH_LONG).show();
                        }
                        bottomPopView.dismiss();
                    }

                    @Override
                    public void onBottomButtonClick() {
                        //TODO: Share
                        bottomPopView.dismiss();
                        SharePopView sharePopView = new SharePopView(mActivity, view, url, title, text, imageUrl);
                        sharePopView.show();
                    }
                };
                bottomPopView.setTopText(isFav ? "取消收藏" : "收藏");
                bottomPopView.setBottomText("分享");
                // 显示底部菜单
                bottomPopView.show();
                return true;
                }
        });

        list_news.getLayoutParams().height = 2600;
        return view;
    }


    // 打开详情页面
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Data target = (Data)newsListAdapter.getItem(position);
        dataBaseManager.addNews(new Data[]{target});
        dataBaseManager.addBrowseHistory(target.getNewsID());
        newsListAdapter.getView(position, view, parent);

        Intent intent = new Intent(getActivity(), NewsPageScrollingActivity.class);
        Bundle bundle = new Bundle();

        MainActivity.dataBaseManager.addNews(new Data[]{recommendDatas.get(position)});

        bundle.putString("content", recommendDatas.get(position).getContent());
        bundle.putString("title", recommendDatas.get(position).getTitle());
        bundle.putString("publisher", recommendDatas.get(position).getPublisher());
        bundle.putString("publishTime", recommendDatas.get(position).getPublishTime());
        bundle.putStringArray("image", recommendDatas.get(position).getImage());
        bundle.putString("newsID", recommendDatas.get(position).getNewsID());
        bundle.putInt("nightMode", getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
        bundle.putString("keywords", recommendDatas.get(position).getAllKeywords());
        bundle.putString("category", recommendDatas.get(position).getCategory());
        bundle.putInt("itemID", position);
        bundle.putString("video", recommendDatas.get(position).getVideo());
        bundle.putString("url", recommendDatas.get(position).getUrl());

        intent.putExtras(bundle);
        startActivityForResult(intent, 0);

    }



}

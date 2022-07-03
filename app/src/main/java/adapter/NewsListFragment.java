package adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;
import com.java.mashihe.FavoriteActivity;
import com.java.mashihe.HistoryActivity;
import com.java.mashihe.MainActivity;
import com.java.mashihe.NewsPageScrollingActivity;
import com.java.mashihe.R;
import com.java.mashihe.SearchActivity;
import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;

import java.util.ArrayList;

import background.Data;
import background.DataBaseManager;
import background.JSONParser;
import background.QueryResult;
import popview.BottomPopView;
import popview.SharePopView;

import static com.java.mashihe.MainActivity.dataBaseManager;
import static com.java.mashihe.MainActivity.toList;

public class NewsListFragment extends Fragment implements AdapterView.OnItemClickListener {
    private FragmentManager fManager;
    private ArrayList<Data> datas;
    private ListView list_news;
    private NewsListAdapter myAdapter;
    private BottomPopView bottomPopView;
    private SharePopView sharePopView;
    private Activity mActivity;
    private Handler mHandler;
    private PullToRefreshLayout pullToRefreshLayout;
    private JSONParser parser;

    public NewsListFragment() {

    }

    public NewsListFragment(Activity activity){
        mActivity = activity;
    }

    public void setDatas(ArrayList<Data> arr){
        datas=arr;
    }

    public void setfManager(FragmentManager manager){
        fManager=manager;
    }

    public void setParser(JSONParser parser) {
        this.parser=parser;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.newslist, container, false);
        pullToRefreshLayout = view.findViewById(R.id.ptrl_newslist);
        list_news = view.findViewById(R.id.list_news);
        myAdapter = new NewsListAdapter(datas, getActivity());

        list_news.setAdapter(myAdapter);
        list_news.setOnItemClickListener(this);
        list_news.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, int i, long l) {
                final int id = i;
                final Data target = (Data)myAdapter.getItem(i);
                final String newsID = target.getNewsID();
                final boolean isFav = MainActivity.dataBaseManager.isFavorite(newsID);

                final String url = target.getUrl();
                final String title = target.getTitle();
                final String text = target.getContent().substring(0, 50);
                final String imageholderUrl = "https://wx3.sinaimg.cn/mw690/80d53d49ly1g6opfwpo6aj209v08ywf9.jpg";
                final String imageUrl = (target.getImage()!=null&&target.getImage().length!=0)?target.getImage()[0]:imageholderUrl;

                if (mActivity instanceof HistoryActivity) {
                    //底部弹出的布局 清除浏览记录与分享
                    bottomPopView = new BottomPopView(getActivity(), view) {
                        @Override
                        public void onTopButtonClick() {
                            MainActivity.dataBaseManager.removeBrowseHistory(newsID);
                            deleteItem(view, id);
                            Snackbar.make(view, "已删除", Snackbar.LENGTH_LONG).show();
                            ((HistoryActivity)mActivity).setClear();
                            bottomPopView.dismiss();
                        }

                        @Override
                        public void onBottomButtonClick() {
                            // TODO: Share
                            bottomPopView.dismiss();
                            sharePopView = new SharePopView(getActivity(), view, url, title, text, imageUrl);
                            sharePopView.show();
                        }
                    };
                    bottomPopView.setTopText("删除此条记录");
                    bottomPopView.setBottomText("分享");
                    // 显示底部菜单
                    bottomPopView.show();
                    return true;

                } else {
                    //底部弹出的布局 收藏与分享
                    bottomPopView = new BottomPopView(getActivity(), view) {
                        @Override
                        public void onTopButtonClick() {
                            if (isFav) {
                                MainActivity.dataBaseManager.removeFavorite(newsID);
                                if (mActivity instanceof FavoriteActivity) {
                                    deleteItem(view, id);
                                    Log.d("FavFragment", "--------------"+datas.size()+"---------------");
                                    if (datas.size()==1) {
                                        Log.d("FavFragment", "--------------Empty---------------");
                                        ((FavoriteActivity) mActivity).changeToEmpty();
                                    }
                                }
                                Snackbar.make(view, "已取消收藏", Snackbar.LENGTH_LONG).show();
                            } else {
                                MainActivity.dataBaseManager.addFavorite(target);
                                if (mActivity instanceof FavoriteActivity) {
                                    datas.add(target);
                                }
                                Snackbar.make(view, "已收藏", Snackbar.LENGTH_LONG).show();
                            }
                            bottomPopView.dismiss();
                        }

                        @Override
                        public void onBottomButtonClick() {
                            //TODO: Share
                            bottomPopView.dismiss();
                            sharePopView = new SharePopView(getActivity(), view, url, title, text, imageUrl);
                            sharePopView.show();
                        }
                    };
                    bottomPopView.setTopText(isFav?"取消收藏":"收藏");
                    bottomPopView.setBottomText("分享");
                    // 显示底部菜单
                    bottomPopView.show();
                    return true;
                }


            }
        });

        pullToRefreshLayout.setRefreshListener(new BaseRefreshListener() {

            @Override
            public void refresh() {
                list_news.setClickable(false);
                if (mActivity instanceof MainActivity || mActivity instanceof SearchActivity) {    // MainActivity或SearchActivity中
                    if (!MainActivity.isNetworkConnected(getContext())) {   // 如果没网就直接提示
                        Message message = new Message();
                        message.what = 3;
                        mHandler.sendMessage(message);
                        pullToRefreshLayout.finishRefresh();
                        return;
                    }
                    if (parser == null) {
                        list_news.setClickable(true);
                        pullToRefreshLayout.finishRefresh();
                    } else {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                QueryResult res = parser.refresh();
                                if (res.getData().length == 0) {
                                    Message message = new Message();
                                    message.what = 4;
                                    mHandler.sendMessage(message);
                                    return;
                                }
                                datas.addAll(0, toList(res.getData()));
                                Message message = new Message();
                                message.what = 2;
                                mHandler.sendMessage(message);
                            }
                        });
                        thread.start();
                        myAdapter.notifyDataSetChanged();
                        pullToRefreshLayout.finishRefresh();
                    }

                } else {
                    list_news.setClickable(true);
                    pullToRefreshLayout.finishRefresh();
                }

            }

            @Override
            public void loadMore() {
                list_news.setClickable(false);
                if (mActivity instanceof SearchActivity || mActivity instanceof MainActivity) {   // MainActivity或SearchActivity中
                    if (!MainActivity.isNetworkConnected(getContext())) {   // 如果没网就直接提示
                        Message message = new Message();
                        message.what = 3;
                        mHandler.sendMessage(message);
                        pullToRefreshLayout.finishLoadMore();
                        return;
                    }
                    if (parser == null) {
                        list_news.setClickable(true);
                        pullToRefreshLayout.finishLoadMore();
                    } else {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                QueryResult res = parser.next();
                                if (res.getData().length == 0) {
                                    Message message = new Message();
                                    message.what = 1;
                                    mHandler.sendMessage(message);
                                    return;
                                }
                                datas.addAll(toList(res.getData()));
                                Message message = new Message();
                                message.what = 2;
                                mHandler.sendMessage(message);
                            }
                        });
                        thread.start();
                        myAdapter.notifyDataSetChanged();
                        pullToRefreshLayout.finishLoadMore();
                    }
                } else {


                    if (mActivity instanceof FavoriteActivity) {
                        int id = ((FavoriteActivity) mActivity).getIndex();
                        Data[] res = dataBaseManager.getLatestFavoriteInRange(id, id+20);
                        if (res.length==0) {
                            Toast.makeText(mActivity, "已加载全部", Toast.LENGTH_SHORT).show();
                        } else {
                            datas.addAll(toList(res));
                            ((FavoriteActivity) mActivity).addIndex(res.length);
                        }

                    } else if (mActivity instanceof HistoryActivity) {
                        int id = ((HistoryActivity) mActivity).getIndex();
                        Data[] res = dataBaseManager.getLatestBrowseHistoryInRange(id, id+20);
                        if (res.length==0) {
                            Toast.makeText(mActivity, "已加载全部", Toast.LENGTH_SHORT).show();
                        } else {
                            datas.addAll(toList(res));
                            ((HistoryActivity) mActivity).addIndex(res.length);
                        }
                    }

                    list_news.setClickable(true);
                    myAdapter.notifyDataSetChanged();
                    pullToRefreshLayout.finishLoadMore();
                }
            }
        });

        mHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 1:
                        Toast.makeText(mActivity, "已加载全部", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        myAdapter.notifyDataSetChanged();
                        list_news.setClickable(true);
                        break;
                    case 3:
                        myAdapter.notifyDataSetChanged();
                        list_news.setClickable(true);
                        Toast.makeText(mActivity, "网络连接错误", Toast.LENGTH_LONG).show();
                        break;
                    case 4:
                        Toast.makeText(mActivity, "已是最新", Toast.LENGTH_SHORT).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };


        return view;
    }


    // 打开详情页面
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Data target = (Data)myAdapter.getItem(position);
        dataBaseManager.addNews(new Data[]{target});
        dataBaseManager.addBrowseHistory(target.getNewsID());

        myAdapter.getView(position, view, parent);

        Intent intent = new Intent(mActivity, NewsPageScrollingActivity.class);
        Bundle bundle = new Bundle();

        MainActivity.dataBaseManager.addNews(new Data[]{datas.get(position)});
        view.setBackgroundColor(mActivity.getResources().getColor(R.color.colorGrey));
        bundle.putString("content", datas.get(position).getContent());
        bundle.putString("title", datas.get(position).getTitle());
        bundle.putString("publisher", datas.get(position).getPublisher());
        bundle.putString("publishTime", datas.get(position).getPublishTime());
        bundle.putStringArray("image", datas.get(position).getImage());
        bundle.putString("newsID", datas.get(position).getNewsID());
        bundle.putInt("nightMode", getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
        bundle.putString("keywords", datas.get(position).getAllKeywords());
        bundle.putString("category", datas.get(position).getCategory());
        bundle.putInt("itemID", position);
        bundle.putString("video", datas.get(position).getVideo());
        bundle.putString("url", datas.get(position).getUrl());

        intent.putExtras(bundle);
        startActivityForResult(intent, 0);

    }

    // 在searchActivity中代替setdata
    public void getSearchResult(final String Keyword) {
        if (mActivity instanceof SearchActivity) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Bundle bundle = new Bundle();
                    bundle.putString("words", Keyword);
                    parser = new JSONParser(bundle);
                    parser.setManager(dataBaseManager==null ? DataBaseManager.getInstance(mActivity):dataBaseManager);
                    QueryResult res = parser.next();
                    setDatas(toList(res.getData()));
                }
            });
            thread.start();

            while (thread.isAlive()) {

            }
        }
    }

    public boolean isEmpty() {
        if (datas==null||datas.size()==0) {
            return true;
        } else {
            return false;
        }
    }

    //删除listview中的一个项目，包含动画
    public void deleteItem(View view, final int position) {

        Animation.AnimationListener al = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            //动画结束以后才真正删除
            @Override
            public void onAnimationEnd(Animation animation) {
                Data item = datas.get(position);
                datas.remove(item);
                myAdapter.notifyDataSetChanged();
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

package util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import android.widget.AbsListView;

public class ListViewUtil extends ListView implements AbsListView.OnScrollListener {
    //item总数
    private int mTotalItemCount;
    // 是否正在加载
    private boolean isLoading;
    // 加载接口
    private OnLoadMoreListener mLoadingListener;

    // FooterView 需要通过 addFooterView(View v) 方法添加进来
    private View mLoadingView;

    public ListViewUtil(Context context) {
        this(context, null);
    }

    public ListViewUtil(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListViewUtil(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 取消滑动到边界的弧形阴影
        setOverScrollMode(OVER_SCROLL_NEVER);
        // 设置滑动监听
        setOnScrollListener(this);
    }

    // 设置自定义接口 OnLoadMoreListener
    public void setOnLoadMoreListener(OnLoadMoreListener listener){
        this.mLoadingListener = listener;
    }

    /**
     * 数据加载完成后，调用此方法
     * 将正在加载标记置为 false
     * 并且将 FooterView 移除掉
     */
    public void setLoadCompleted() {
        isLoading = false;
        removeFooterView(mLoadingView);
    }

    @Override
    public void onScrollStateChanged(AbsListView listView, int scrollState) {
        // 获取最后一个可见item的 position
        int lastVisibleIndex = listView.getLastVisiblePosition();
        // 如果当前未加载，并且滚动停止，并且最后一个可见item是当前list的最后一个
        if (!isLoading && scrollState == SCROLL_STATE_IDLE && lastVisibleIndex == mTotalItemCount-1){
            isLoading = true;
            // 显示LoadingView ，并且回调 onLoadMore() 方法
            addFooterView(mLoadingView);
            if (mLoadingListener != null){
                mLoadingListener.onLoadMore();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mTotalItemCount = totalItemCount;
    }

    @Override
    public void addFooterView(View v) {
        mLoadingView = v;
        super.addFooterView(mLoadingView);
    }

    public interface OnLoadMoreListener {
        public void onLoadMore();
    }


}

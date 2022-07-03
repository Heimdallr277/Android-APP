package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.java.mashihe.R;

import java.util.ArrayList;

public class SearchHistoryAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> history;

    public SearchHistoryAdapter(Context context, ArrayList<String> titles) {
        this.mContext = context;
        this.history = titles;
    }

    @Override
    public int getCount() {
        return history.size();
    }

    @Override
    public Object getItem(int i) {
        return history.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    static class ViewHolder {
        public ImageView imageView;
        public TextView textView;

    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view==null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.search_history_entry, null);
            holder = new ViewHolder();
            holder.imageView = view.findViewById(R.id.sh_ic);
            holder.textView = view.findViewById(R.id.sh_title);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        //给控件赋值
        holder.textView.setText(history.get(i));

        return view;
    }



}

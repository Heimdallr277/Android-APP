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

public class BlockedWordsAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> blockedwords;

    public BlockedWordsAdapter(Context context, ArrayList<String> titles) {
        this.mContext = context;
        this.blockedwords = titles;
    }

    @Override
    public int getCount() {
        return blockedwords.size();
    }

    @Override
    public Object getItem(int i) {
        return blockedwords.get(i);
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
            view = LayoutInflater.from(mContext).inflate(R.layout.blocked_words_entry, null);
            holder = new ViewHolder();
            holder.imageView = view.findViewById(R.id.bw_ic);
            holder.textView = view.findViewById(R.id.bw_title);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        //给控件赋值
        holder.textView.setText(blockedwords.get(i));

        return view;
    }



}

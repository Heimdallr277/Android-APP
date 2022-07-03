package adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.java.mashihe.R;

import java.util.ArrayList;
import java.util.HashMap;

public class CategoryAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<String> titles;
    static HashMap<String, Drawable> map;

    public CategoryAdapter(Context context, ArrayList<String> titles) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.titles = titles;
        if (map == null) {
            map = new HashMap<>();
            map.put("财经", mContext.getResources().getDrawable(R.drawable.cat_ic_caijing));
            map.put("健康", mContext.getResources().getDrawable(R.drawable.cat_ic_jiankang));
            map.put("教育", mContext.getResources().getDrawable(R.drawable.cat_ic_jiaoyu));
            map.put("军事", mContext.getResources().getDrawable(R.drawable.cat_ic_junshi));
            map.put("科技", mContext.getResources().getDrawable(R.drawable.cat_ic_keji));
            map.put("汽车", mContext.getResources().getDrawable(R.drawable.cat_ic_qiche));
            map.put("社会", mContext.getResources().getDrawable(R.drawable.cat_ic_shehui));
            map.put("体育", mContext.getResources().getDrawable(R.drawable.cat_ic_tiyu));
            map.put("文化", mContext.getResources().getDrawable(R.drawable.cat_ic_wenhua));
            map.put("娱乐", mContext.getResources().getDrawable(R.drawable.cat_ic_yule));
        }
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public Object getItem(int i) {
        return titles.get(i);
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
        CategoryAdapter.ViewHolder holder = null;
        if (view==null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.category_entry, null);
            holder = new CategoryAdapter.ViewHolder();
            holder.imageView = view.findViewById(R.id.cat_ic);
            holder.textView = view.findViewById(R.id.cat_title);
            view.setTag(holder);
        } else {
            holder = (CategoryAdapter.ViewHolder) view.getTag();
        }
        //给控件赋值
        holder.imageView.setImageDrawable(map.get(titles.get(i)));
        holder.textView.setText(titles.get(i));

        return view;
    }
}

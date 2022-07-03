package adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.java.mashihe.R;

import java.util.ArrayList;

class Option {
    private Drawable imagesrc;
    private String text;

    Option(Drawable src, String text) {
        this.imagesrc = src;
        this.text = text;
    }

    public Drawable getImagesrc() {
        return this.imagesrc;
    }

    public String getText() {
        return this.text;
    }
}

public class OptionAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Option> options;

    public OptionAdapter(Context context) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);

        options = new ArrayList<Option>();
        options.add(new Option(mContext.getResources().getDrawable(R.drawable.ic_star_border_grey_700_36dp), "我的收藏"));
        options.add(new Option(mContext.getResources().getDrawable(R.drawable.ic_history_grey_700_36dp), "浏览历史"));
        options.add(new Option(mContext.getResources().getDrawable(R.drawable.ic_block_grey_700_36dp), "屏蔽设置"));
        options.add(new Option(mContext.getResources().getDrawable(R.drawable.ic_delete_sweep_grey_700_36dp), "清空缓存"));
        String UIMode = (mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO ? "夜":"日";
        options.add(new Option(mContext.getResources().getDrawable(R.drawable.ic_remove_red_eye_grey_700_36dp), UIMode+"间模式"));
    }

    @Override
    public int getCount() {
        return options.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
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
            view = LayoutInflater.from(mContext).inflate(R.layout.option_entry, null);
            holder = new ViewHolder();
            holder.imageView = view.findViewById(R.id.option_icon);
            holder.textView = view.findViewById(R.id.option_text);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        //给控件赋值
        holder.imageView.setImageDrawable(options.get(i).getImagesrc());
        holder.textView.setText(options.get(i).getText());

        return view;
    }
}

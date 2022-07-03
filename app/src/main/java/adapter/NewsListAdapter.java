package adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.java.mashihe.MainActivity;
import com.java.mashihe.R;

import java.util.List;

import background.Data;
import background.DataBaseManager;

public class NewsListAdapter extends BaseAdapter {

    private List<Data> mData;
    private Context mContext;

    public NewsListAdapter(List<Data> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        if (mData == null) {
            return 0;
        } else
            return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.news_entry,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.txt_item_title = convertView.findViewById(R.id.txt_item_title);
            viewHolder.txt_item_abstract = convertView.findViewById(R.id.txt_item_abstract);
            viewHolder.txt_item_source = convertView.findViewById(R.id.txt_item_source);
            viewHolder.txt_item_time = convertView.findViewById(R.id.txt_item_time);
            viewHolder.txt_item_image = convertView.findViewById(R.id.txt_item_image);
            viewHolder.ll_news_entry = convertView.findViewById(R.id.ll_news_entry);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.txt_item_title.setText(mData.get(position).getTitle());
        viewHolder.txt_item_abstract.setText(mData.get(position).getContent());
        viewHolder.txt_item_source.setText(mData.get(position).getPublisher());
        viewHolder.txt_item_time.setText(mData.get(position).getPublishTime());

        DataBaseManager dataBaseManager = (MainActivity.dataBaseManager==null)?DataBaseManager.getInstance(mContext):MainActivity.dataBaseManager;
        if (dataBaseManager.hasRead(new Data[]{mData.get(position)})[0]) {
            viewHolder.ll_news_entry.setBackgroundColor(mContext.getResources().getColor(R.color.colorGreyLight));
        } else {
            viewHolder.ll_news_entry.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
        }

        String[] url = mData.get(position).getImage();
        if (url == null || url.length == 0 ) {
            viewHolder.txt_item_image.setImageDrawable(mContext.getDrawable(R.drawable.news_image_holder));
        } else {
//            viewHolder.txt_item_image.setImageBitmap(BitmapTools.compress(BitmapTools.getBitmapFromUrl(url[0]),50,50));
            Glide.with(mContext).load(url[0]).into(viewHolder.txt_item_image);
        }


        return convertView;
    }

    private class ViewHolder{
        TextView txt_item_title;
        TextView txt_item_abstract;
        TextView txt_item_source;
        TextView txt_item_time;
        ImageView txt_item_image;
        LinearLayout ll_news_entry;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}

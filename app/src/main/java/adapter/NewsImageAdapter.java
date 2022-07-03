package adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.java.mashihe.MainActivity;
import com.java.mashihe.R;

import util.ImageSaver;

public class NewsImageAdapter extends RecyclerView.Adapter<NewsImageAdapter.LinearViewHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private String[] image_list;

    public NewsImageAdapter(Context context, String[] list) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        this.image_list = list;
    }
    @NonNull
    @Override
    public NewsImageAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LinearViewHolder(mLayoutInflater.inflate(R.layout.news_image_entry,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final NewsImageAdapter.LinearViewHolder holder, final int position) {
        if (image_list!=null && image_list.length!=0) {
//            mImageView.setImageBitmap(BitmapTools.compress(BitmapTools.getBitmapFromUrl(bundle.getStringArray("image")[0]),300,200));
            if (MainActivity.isNetworkConnected(mContext)) {
                Glide.with(mContext).load(image_list[position]).into(holder.imageView);
            } else {
                holder.imageView.setImageBitmap(MainActivity.dataBaseManager.getImageCache(image_list[position]));
            }
        }
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final String[] array = new String[]{"保存图片"};
                AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
                builder2.setItems(array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            if (image_list!=null && image_list.length!=0 && !image_list[position].isEmpty()) {
                                ImageSaver imageSaver = new ImageSaver(mContext, holder.imageView);
                                if (MainActivity.isNetworkConnected(mContext)) {
                                    imageSaver.saveImageWithUrl(image_list[position]);
                                } else {
                                    Bitmap bitmap = MainActivity.dataBaseManager.getImageCache(image_list[position]);
                                    if (bitmap == null) {
                                        Toast.makeText(mContext, "图片保存失败！", Toast.LENGTH_SHORT).show();
                                    } else {
                                        imageSaver.saveImage(bitmap);
                                    }
                                }
                            }
                        }
                    }
                }).show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (image_list == null) {
            return 0;
        } else {
            return image_list.length;
        }
    }

    class LinearViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public LinearViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.news_image);
        }
    }

}

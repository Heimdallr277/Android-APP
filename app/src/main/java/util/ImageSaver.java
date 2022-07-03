package util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ImageSaver {
    private Handler mHandler;
    private Context mContext;
    private View view;
    private static final int SAVE_SUCCESS = 0;//保存图片成功
    private static final int SAVE_FAILURE = 1;//保存图片失败
    private static final int SAVE_BEGIN = 2;//开始保存图片
    private Uri imageUri;

    public ImageSaver(Context context, View v) {
        this.mContext = context;
        this.view = v;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SAVE_BEGIN:
                        Toast.makeText(mContext, "开始保存图片...", Toast.LENGTH_SHORT).show();
                        view.setClickable(true);
                        break;
                    case SAVE_SUCCESS:
                        Toast.makeText(mContext, "图片保存成功，请到相册查找", Toast.LENGTH_SHORT).show();
                        view.setClickable(true);
                        break;
                    case SAVE_FAILURE:
                        Toast.makeText(mContext, "图片保存失败，请稍后再试...", Toast.LENGTH_SHORT).show();
                        view.setClickable(true);
                        break;
                }
            }
        };
    }

    /**
     * 将URL转化成bitmap形式
     *
     * @param url
     * @return bitmap type
     */
    public final static Bitmap returnBitMap(String url) {
        Log.d("ImageSaver", "-------------Converting to Bitmap--------------");
        URL myFileUrl;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
            URLConnection conn;
            conn = (URLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 保存二维码到本地相册
     */
    private void saveImageToPhotos(Context context, Bitmap bmp) {
        Log.d("ImageSaver", "-------------Saving--------------");
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "news_images");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mHandler.obtainMessage(SAVE_FAILURE).sendToTarget();
            return;
        }
        // 最后通知图库更新
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        imageUri = FileProvider.getUriForFile(mContext, mContext.getPackageName()+ ".fileprovider", file);
        intent.setData(uri);
        context.sendBroadcast(intent);
        mHandler.obtainMessage(SAVE_SUCCESS).sendToTarget();
    }

    public void saveImageWithUrl(final String imageUrl) {
        view.setClickable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.obtainMessage(SAVE_BEGIN).sendToTarget();
                Bitmap bp = returnBitMap(imageUrl);
                saveImageToPhotos(mContext, bp);
            }
        }).start();
    }

    public void saveImage(final Bitmap bitmap) {
        view.setClickable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.obtainMessage(SAVE_BEGIN).sendToTarget();
                saveImageToPhotos(mContext, bitmap);
            }
        }).start();
    }

    /*
    * 将布局转化为bitmap
        这里传入的是你要截的布局的根View
    * */
    public Bitmap getBitmapByView(View headerView) {
        int h = headerView.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(headerView.getWidth(), h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        headerView.draw(canvas);
        return bitmap;
    }

    public void saveScreenShot() {
        view.setClickable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.obtainMessage(SAVE_BEGIN).sendToTarget();
                Bitmap bp = getBitmapByView(view);
                saveImageToPhotos(mContext, bp);
            }
        }).start();
    }
}

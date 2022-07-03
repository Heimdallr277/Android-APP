package background;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

class Tool implements  Runnable{
    private String url;
    private Bitmap[] bitmap;
//    private MainActivity.MyHandler handler;
    private ImageView view;
    private DataBaseManager manager;
    @Override
    public void run(){
        try{
            URL address=new URL(url);
            if(address.getProtocol().equals("http")) {
                HttpURLConnection httpConnection = (HttpURLConnection) address.openConnection();
                bitmap[0] = BitmapFactory.decodeStream(httpConnection.getInputStream());
                httpConnection.disconnect();
            }
            else if(address.getProtocol().equals("https")){
                HttpsURLConnection httpsConnection=(HttpsURLConnection)address.openConnection();
                bitmap[0]=BitmapFactory.decodeStream(httpsConnection.getInputStream());
                httpsConnection.disconnect();
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return;
        }
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                view.setImageBitmap(bitmap[0]);
//            }
//        });
    }
    public void setUrl(String url){
        this.url=url;
    }
    public void setBitmap(Bitmap[] bitmap){
        this.bitmap=bitmap;
    }
//    public void setHandler(MainActivity.MyHandler handler){this.handler=handler;}
    public void setView(ImageView imageView){this.view=imageView;}
    public void setManager(DataBaseManager manager){this.manager=manager;}
}

public class BitmapTools {//提供Url->Bitmap,byte[]<-->Bitmap的转换
    private static DataBaseManager manager;
    public static void setManager(DataBaseManager manager){BitmapTools.manager=manager;}
    public static Bitmap getBitmapFromUrl(String url){
        Tool tool=new Tool();
        Bitmap[] dst=new Bitmap[1];
        tool.setBitmap(dst);
        tool.setUrl(url);
        Thread thread=new Thread(tool);
        thread.start();
        while(thread.isAlive()){

        }
        return dst[0];
    }
    public static byte[] castBitmapToBytes(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        return baos.toByteArray();
    }
    public static Bitmap castBytesToBitmap(byte[] bytes){
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
    public static Bitmap compress(Bitmap bitmap, int target_width, int target_height){
        double ratio=Math.max(bitmap.getWidth()/(double)target_width,bitmap.getHeight()/(double)target_height);
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inSampleSize=(int)(ratio+0.5);
        byte[] bytes=castBitmapToBytes(bitmap);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
    }
//    public static void setBitmapToView(MainActivity.MyHandler handler, ImageView view, String url){
//        Tool tool=new Tool();
//        Bitmap[] arr=new Bitmap[1];
//        tool.setUrl(url);
//        tool.setBitmap(arr);
//        tool.setHandler(handler);
//        tool.setView(view);
//        tool.setManager(manager);
//        new Thread(tool).start();
//    }
}

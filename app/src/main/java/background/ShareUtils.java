package background;

import android.content.Context;
import android.widget.Toast;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class ShareUtils {
    public static void shareToWechat(final Context context, String title, String text, String imageUrl, String newsUrl){
        Platform.ShareParams params=new Platform.ShareParams();
        params.setShareType(Platform.SHARE_WEBPAGE);
        params.setImageUrl(imageUrl);
        params.setText(text);
        params.setTitle(title);
        params.setUrl(newsUrl);
        Platform wechat= ShareSDK.getPlatform(Wechat.NAME);
        wechat.setPlatformActionListener(new MyPlatformActionListener(context));
        wechat.share(params);
    }
    public static void shareToWechatMoments(final Context context, String title, String text, String imageUrl, String newsUrl){
        Platform.ShareParams params=new Platform.ShareParams();
        params.setShareType(Platform.SHARE_WEBPAGE);
        params.setImageUrl(imageUrl);
        params.setText(text);
        params.setTitle(title);
        params.setUrl(newsUrl);
        Platform wechat= ShareSDK.getPlatform(WechatMoments.NAME);
        wechat.setPlatformActionListener(new MyPlatformActionListener(context));
        wechat.share(params);
    }
    public static void shareToWeibo(final Context context, String title, String text, String imageUrl, String newsUrl){
        Platform.ShareParams params=new Platform.ShareParams();
        params.setShareType(Platform.SHARE_WEBPAGE);
        params.setImageUrl(imageUrl);
        params.setText(text+" "+newsUrl);
        params.setTitle(title);
        Platform wechat= ShareSDK.getPlatform(SinaWeibo.NAME);
        wechat.setPlatformActionListener(new MyPlatformActionListener(context));
        wechat.share(params);
    }

}

class MyPlatformActionListener implements PlatformActionListener{
    Context context;
    MyPlatformActionListener(Context context){
        this.context=context;
    }
    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        Toast.makeText(context,"分享成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        Toast.makeText(context,"分享失败:"+throwable,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel(Platform platform, int i) {
        Toast.makeText(context,"取消分享",Toast.LENGTH_SHORT).show();
    }
}
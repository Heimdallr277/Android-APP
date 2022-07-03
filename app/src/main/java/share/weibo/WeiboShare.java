//package share.weibo;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.BitmapFactory;
//import android.view.View;
//import android.widget.Toast;
//
//import com.java.mashihe.MainActivity;
//import com.sina.weibo.sdk.WbSdk;
//import com.sina.weibo.sdk.api.TextObject;
//import com.sina.weibo.sdk.api.WeiboMultiMessage;
//import com.sina.weibo.sdk.auth.AccessTokenKeeper;
//import com.sina.weibo.sdk.auth.AuthInfo;
//import com.sina.weibo.sdk.auth.Oauth2AccessToken;
//import com.sina.weibo.sdk.auth.WbAuthListener;
//import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
//import com.sina.weibo.sdk.auth.sso.SsoHandler;
//import com.sina.weibo.sdk.share.WbShareCallback;
//import com.sina.weibo.sdk.share.WbShareHandler;
//import com.sina.weibo.sdk.utils.Utility;
//
//public class WeiboShare implements WbShareCallback{
//    private SsoHandler ssoHandler;
//    private Oauth2AccessToken mAccessToken;
//    private AuthListener authListener=new AuthListener();
//    private Activity activity;
//    private static WeiboShare instance=null;
//    private WbShareHandler shareHandler;
//
//    public void setActivity(Activity activity){
//        this.activity=activity;
//    }
//
//    public static WeiboShare getInstance(){
//        if(instance==null)
//            instance=new WeiboShare();
//        return instance;
//    }
//
//    public void authorize(){
//        WbSdk.install(activity,new AuthInfo(activity,Constants.APP_KEY,Constants.REDIRECT_URL,Constants.SCOPE));
//        ssoHandler=new SsoHandler(activity);
//        shareHandler=new WbShareHandler(activity);
//        shareHandler.registerApp();
//    }
//
//    private WeiboShare(){
//
//    }
//
//    private TextObject getTextObj() {
//        TextObject textObject = new TextObject();
//        textObject.identify= Utility.generateGUID();
//        textObject.text = "this is the text to share";
//        textObject.title = "xxxx";
//        textObject.actionUrl = "http://www.baidu.com";
//        textObject.description="description";
//        return textObject;
//    }
//
//    private void sendMultiMessage() {
//
//
//        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
////        if(webpage.isChecked())
////            weiboMessage.mediaObject=getWebpageObj();
////        if(image.isChecked())
////            weiboMessage.imageObject=getImageObj();
////        if(text.isChecked())
//        weiboMessage.textObject=getTextObj();
////        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
////                == PackageManager.PERMISSION_DENIED) {
////
////            Log.d("permission", "permission denied to SEND_SMS - requesting it");
////            String[] permissions = {Manifest.permission.READ_PHONE_STATE};
////
////            requestPermissions(permissions, 1);
////
////        }
//        shareHandler.shareMessage(weiboMessage, false);
//    }
//
//    @Override
//    public void onWbShareSuccess() {
//        Toast.makeText(activity,"分享成功",Toast.LENGTH_LONG).show();
//    }
//
//    @Override
//    public void onWbShareCancel() {
//        Toast.makeText(activity,"取消分享",Toast.LENGTH_LONG).show();
//    }
//
//    @Override
//    public void onWbShareFail() {
//        Toast.makeText(activity,"分享失败",Toast.LENGTH_LONG).show();
//    }
//
////    protected void onNewIntent(Intent intent){
////        super.onNewIntent(intent);
////        shareHandler.doResultIntent(intent,this);
////    }
////
////    @Override
////    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
////        activity.onActivityResult(requestCode, resultCode, data);
////        shareHandler.doResultIntent(data,this);
////        ssoHandler.authorizeCallBack(requestCode,resultCode,data);
////    }
//
//    private class AuthListener implements WbAuthListener {
//        @Override
//        public void onSuccess(final Oauth2AccessToken oauth2AccessToken) {
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mAccessToken = oauth2AccessToken;
//                    if (mAccessToken.isSessionValid()) {
//                        // 保存 Token 到 SharedPreferences
//                        AccessTokenKeeper.writeAccessToken(activity, mAccessToken);
//                        Toast.makeText(activity,
//                                "token success", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        }
//
//        @Override
//        public void cancel() {
//            Toast.makeText(activity,"token canceled",Toast.LENGTH_LONG).show();
//        }
//
//        @Override
//        public void onFailure(WbConnectErrorMessage wbConnectErrorMessage) {
//            Toast.makeText(activity,"token failed",Toast.LENGTH_LONG).show();
//        }
//    }
//}

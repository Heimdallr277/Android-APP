package background;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseOpenHelper extends SQLiteOpenHelper {
    public DataBaseOpenHelper(Context context){
        super(context,"news.db",null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //新闻内容+是否已读
        sqLiteDatabase.execSQL("CREATE TABLE NEWS(category text not null, content text not null, newsId text primary key not null, url text, publishTime text not null, title text not null, publisher text not null, video text, image text, keywords text not null, hasRead int default 0)");
        sqLiteDatabase.execSQL("CREATE TABLE IMAGE(imageUrl text not null primary key, imageBin blob)");//图片缓存
        sqLiteDatabase.execSQL("CREATE TABLE KEYWORDS(keyword text not null primary key, score real)");//关键词权重
        sqLiteDatabase.execSQL("CREATE TABLE FORBID(keyword text not null primary key)");//屏蔽词
        sqLiteDatabase.execSQL("CREATE TABLE SEARCH(keyword text not null primary key)");//搜索记录
        sqLiteDatabase.execSQL("CREATE TABLE BROWSE(newsId text not null)");//浏览记录，可以重复
        sqLiteDatabase.execSQL("CREATE TABLE FAVORITE(category text not null, content text not null, newsId text primary key not null, url text, publishTime text not null, title text not null, publisher text not null, video text, image text, keywords text not null, hasRead int default 0)");
        sqLiteDatabase.execSQL("CREATE TABLE TAGS(tag text not null primary key)");

        sqLiteDatabase.beginTransaction();
        ContentValues cv=new ContentValues();
        cv.put("tag","全部");
        sqLiteDatabase.insert("TAGS",null,cv);
        String[] tags = new String[]{"娱乐", "军事", "教育", "文化", "健康", "财经", "体育", "汽车", "科技", "社会"};
        for (String s : tags) {
            cv.clear();
            cv.put("tag",s);
            sqLiteDatabase.insert("TAGS",null,cv);
        }
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

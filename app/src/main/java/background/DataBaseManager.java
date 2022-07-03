package background;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;

public class DataBaseManager {//方法都是线程安全的
    static private DataBaseManager instance=null;
    static private int maxkey=100;
    private DataBaseOpenHelper helper=null;
    private SQLiteDatabase db=null;
    private Context mContext;
    public Context getContext(){return mContext;}
    static final private Boolean news_lock=false,favorite_lock=false,image_lock=false,keywords_lock=false,forbid_lock=false,search_lock=false,browse_lock=false,tags_lock=false;
    static public DataBaseManager getInstance(Context context){//通过这个静态方法获得实例，数据库操作均从实例调用
        if(instance==null)
            instance=new DataBaseManager(context);
        return instance;
    }
    private DataBaseManager(Context context){
        helper=new DataBaseOpenHelper(context);
        db=helper.getWritableDatabase();
        mContext=context;
    }
    private void addNews(Data news){
        ContentValues cv=new ContentValues();
        cv.put("category",news.getCategory());
        cv.put("content",news.getContent());
        cv.put("newsId",news.getNewsID());
        cv.put("url",news.getUrl());
        cv.put("publishTime",news.getPublishTime());
        cv.put("title",news.getTitle());
        cv.put("publisher",news.getPublisher());
        cv.put("video",news.getVideo());
        cv.put("image",news.getAllImage());
        cv.put("keywords",news.getAllKeywords());
//        cv.put("hasRead",news.getRead());
        synchronized (news_lock) {
            db.insertWithOnConflict("NEWS", null, cv,SQLiteDatabase.CONFLICT_REPLACE);
        }
    }
    public void addNews(Data[] news){//添加一组新闻，且默认越靠前的新闻获取时间越晚（影响getLatestNews的结果）。如果该新闻已经存在（newsId相同）,则会先删除旧纪录再添加
        synchronized (news_lock){
            synchronized (browse_lock){
                synchronized (favorite_lock){
                    synchronized (forbid_lock){
                        synchronized (image_lock){
                            synchronized (keywords_lock){
                                synchronized (search_lock){
                                    synchronized (tags_lock) {
                                        db.beginTransaction();
                                        int n = news.length;
                                        for (int i = n - 1; i >= 0; i--)
                                            addNews(news[i]);
                                        db.setTransactionSuccessful();
                                        db.endTransaction();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public Data[] getLatestInsertedNews(String category, int limit){//返回特定类别中获取时间最近的limit条新闻，越晚获取的新闻位置越靠前，没有则返回null。不指定类别使用category=null
        Cursor cursor=null;
        synchronized (news_lock) {
            if(category==null)
                cursor = db.query("NEWS", null, null, null, null, null, "rowid desc", String.valueOf(limit));
            else
                cursor=db.query("NEWS",null,"category=?",new String[]{category},null,null,"rowid desc",String.valueOf(limit));
        }
        return readCursor(cursor);
    }
    private Data readInNews(Cursor cursor){
        Data tmp=new Data();
        tmp.setCategory(cursor.getString(cursor.getColumnIndex("category")));
        tmp.setContent(cursor.getString(cursor.getColumnIndex("content")));
        tmp.setNewsID(cursor.getString(cursor.getColumnIndex("newsId")));
        tmp.setUrl(cursor.getString(cursor.getColumnIndex("url")));
        tmp.setPublishTime(cursor.getString(cursor.getColumnIndex("publishTime")));
        tmp.setTitle(cursor.getString(cursor.getColumnIndex("title")));
        tmp.setPublisher(cursor.getString(cursor.getColumnIndex("publisher")));
        tmp.setVideo(cursor.getString(cursor.getColumnIndex("video")));
        tmp.setImage(cursor.getString(cursor.getColumnIndex("image")));
//        tmp.setRead(cursor.getInt(cursor.getColumnIndex("hasRead"))==1);
        String[] keys=cursor.getString(cursor.getColumnIndex("keywords")).split("\\|");
        if(keys.length==1&&keys[0].isEmpty()){
            tmp.setKeywords(null);
            return tmp;
        }
        Keyword[] keywords=new Keyword[keys.length];
        int n=keys.length;
        for(int i=0;i<n;i++) {
            keywords[i] = new Keyword();
            String[] s=keys[i].split(",");
            keywords[i].setWord(s[0]);
            keywords[i].setScore(Double.valueOf(s[1]));
        }
        tmp.setKeywords(keywords);
        return tmp;
    }
    private Data[] readCursor(Cursor cursor){
        if(cursor.moveToFirst()){
            Data[] ans=new Data[cursor.getCount()];
            int pos=0;
            do{
                try {
                    ans[pos++]=readInNews(cursor);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }while(cursor.moveToNext());
            return ans;
        }
        else
            return new Data[0];
    }
    public Data[] getLatestNewsInRange(String category,int from,int to){//如果把所有category类的新闻按获取时间从晚到早排序，获取[from,to)之间的新闻，下标从0开始，没有则返回null.category=null表示所有分类
        Cursor cursor=null;
        synchronized (news_lock) {
            if(category==null) {
                Cursor prob=db.rawQuery("select count(rowid) from NEWS",null);
                prob.moveToFirst();
                int count=prob.getInt(0);
                prob.close();
                if(count<=from) {
                    return new Data[0];
                }
                cursor = db.rawQuery("select*from NEWS where rowid<=(select min(rowid) from(select rowid from NEWS order by rowid desc limit ?))order by rowid desc limit ?", new String[]{String.valueOf(from + 1), String.valueOf(to - from)});
            }
            else {
                Cursor prob=db.rawQuery("select count(rowid) from NEWS where category=?",new String[]{category});
                prob.moveToFirst();
                int count=prob.getInt(0);
                prob.close();
                if(count<=from) {
                    return new Data[0];
                }
                cursor = db.rawQuery("select*from NEWS where category=? and rowid<=(select min(rowid) from(select rowid from NEWS where category=? order by rowid desc limit ?))order by rowid desc limit ?", new String[]{category, category, String.valueOf(from + 1), String.valueOf(to - from)});
            }
        }
        Data[] ans=readCursor(cursor);
        cursor.close();
        return ans;
    }
    public void removeAllNewsAndBrowserHistory(){//注意会同时清空搜索记录
        synchronized (news_lock){
            db.execSQL("delete from NEWS");
        }
        synchronized (search_lock){
            db.execSQL("delete from SEARCH");
        }
    }
    public boolean isFavorite(String newsId){
        synchronized (favorite_lock){
            Cursor cursor=db.query("FAVORITE",new String[]{"rowid"},"newsId=?",new String[]{newsId},null,null,null);
            boolean ans=cursor.moveToFirst();
            cursor.close();
            return ans;
        }
    }
    public boolean addFavorite(Data data){//添加一条新闻到收藏，返回是否成功
        ContentValues cv=new ContentValues();
        cv.put("category",data.getCategory());
        cv.put("content",data.getContent());
        cv.put("newsId",data.getNewsID());
        cv.put("url",data.getUrl());
        cv.put("publishTime",data.getPublishTime());
        cv.put("title",data.getTitle());
        cv.put("publisher",data.getPublisher());
        cv.put("video",data.getVideo());
        cv.put("image",data.getAllImage());
        cv.put("keywords",data.getAllKeywords());
        synchronized (favorite_lock) {
            long res = db.insert("FAVORITE", null, cv);
            return res != -1;
        }
    }
    public void addFavoriteByID(String newsID){
        synchronized (favorite_lock){
            db.execSQL("insert into FAVORITE select*from NEWS where newsId=\""+newsID+'\"');
        }
    }
    public boolean removeFavorite(String newsId){//移除一条收藏，返回是否成功
        synchronized (favorite_lock) {
            int ans = db.delete("FAVORITE", "newsId=?", new String[]{newsId});
            return ans == 1;
        }
    }
    public Data[] getLatestFavorite(int limit){//返回最近获得的limit条新闻，越晚获取的新闻位置越靠前，没有则返回null
        Cursor cursor=null;
        synchronized (news_lock) {
            cursor = db.query("FAVORITE", null, null, null, null, null, "rowid desc", String.valueOf(limit));
        }
        return readCursor(cursor);
    }
    public Data[] getLatestFavoriteInRange(int from,int to){//返回[from,to)之间的新闻，越晚获取的新闻位置越靠前，没有则返回null
        Cursor cursor=null;
        synchronized (news_lock) {
            Cursor prob=db.rawQuery("select count(rowid) from FAVORITE",null);
            prob.moveToFirst();
            int count=prob.getInt(0);
            prob.close();
            if(count<=from) {
                return new Data[0];
            }
            cursor = db.rawQuery("select*from FAVORITE where rowid<=(select min(rowid) from(select rowid from FAVORITE order by rowid desc limit ?))order by rowid desc limit ?", new String[]{String.valueOf(from+1), String.valueOf(to - from)});
        }
        Data[] ans=readCursor(cursor);
        cursor.close();
        return ans;
    }
    public void updateKeywords(Keyword[] keywords){//更新关键词的权重
        synchronized (news_lock){
            synchronized (browse_lock){
                synchronized (favorite_lock){
                    synchronized (forbid_lock){
                        synchronized (image_lock){
                            synchronized (keywords_lock){
                                synchronized (search_lock){
                                    synchronized (tags_lock) {
                                        db.beginTransaction();
                                        for (Keyword k : keywords) {
                                            ContentValues cv = new ContentValues();
                                            cv.put("keyword", k.getWord());
                                            cv.put("score", k.getScore());
                                            long res = db.insert("KEYWORDS", null, cv);
                                            if (res == -1) {
                                                db.execSQL("update KEYWORDS set score=score+" + k.getScore() + " where keyword=\"" + k.getWord() + "\"");
                                            }
//            db.rawQuery("insert into KEYWORDS values(?,?) on conflict(name) do update set score=score+?",new String[]{k.getWord(),String.valueOf(k.getScore()),String.valueOf(k.getScore())}).close();
                                        }
                                        Cursor tmpcs = db.rawQuery("select count(*) from KEYWORDS", null);
                                        tmpcs.moveToFirst();
                                        int total = tmpcs.getInt(0);
                                        tmpcs.close();
                                        if (total > maxkey)//不记录全部关键词，给用户改变口味的空间
                                            db.rawQuery("delete from KEYWORDS where score<=(select max(score) from (select*from KEYWORDS order by score asc limit ?))", new String[]{String.valueOf(total - maxkey)}).close();
                                        db.execSQL("update KEYWORDS set score=score*(select count(*)from KEYWORDS)/(select sum(score)from KEYWORDS)");//归一化
                                        db.setTransactionSuccessful();
                                        db.endTransaction();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public String[] getTopKeywords(int n){//返回权重最高的limit个关键词，没有则返回null
        Cursor cursor=null;
        synchronized (keywords_lock) {
            cursor = db.query("KEYWORDS",new String[]{"keyword"} , null, null, null, null, "score desc", String.valueOf(n));
        }
        if(cursor.moveToFirst()){
            String[] ans=new String[cursor.getCount()];
            int pos=0;
            do{
                ans[pos++]=cursor.getString(cursor.getColumnIndex("keyword"));
            }while(cursor.moveToNext());
            cursor.close();
            return ans;
        }
        else {
            cursor.close();
            return new String[0];
        }
    }
    public void setForbiddenWords(String[] words){//添加一组屏蔽词
        synchronized (news_lock){
            synchronized (browse_lock){
                synchronized (favorite_lock){
                    synchronized (forbid_lock){
                        synchronized (image_lock){
                            synchronized (keywords_lock){
                                synchronized (search_lock){
                                    synchronized (tags_lock) {
                                        db.beginTransaction();
                                        for (String s : words) {
                                            ContentValues cv = new ContentValues();
                                            cv.put("keyword", s);
                                            db.insert("FORBID", null, cv);
                                        }
                                        db.setTransactionSuccessful();
                                        db.endTransaction();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public void removeForbiddenWords(String[] words){//移除一组屏蔽词
        StringBuilder sb=new StringBuilder();
        sb.append('(');
        for(String s : words)
            sb.append('\"'+s+"\",");
        sb.setCharAt(sb.length()-1,')');
        synchronized (forbid_lock) {
            db.delete("FORBID", "keyword in " + sb.toString(), null);
        }
    }
    public boolean isForbidden(String word){//测试一个词是否为屏蔽词，不推荐使用，请使用getAllForbiddenWords
        Cursor cursor=null;
        synchronized (forbid_lock) {
            cursor = db.query("FORBID", null, "keyword=?", new String[]{word}, null, null, null);
        }
        boolean ans=cursor.moveToFirst();
        cursor.close();
        return ans;
    }
    public void removeAllForbiddenWords(){//清空屏蔽词
        synchronized (forbid_lock){
            db.execSQL("delete from FORBID");
        }
    }
    public String[] getAllForbiddenWords(){//返回所有屏蔽词，没有则返回null
        Cursor cursor=null;
        synchronized (forbid_lock) {
            cursor = db.query("FORBID", null, null, null, null, null, null);
        }
        if(cursor.moveToFirst()){
            String[] ans=new String[cursor.getCount()];
            int pos=0;
            do{
                ans[pos++]=cursor.getString(cursor.getColumnIndex("keyword"));
            }while(cursor.moveToNext());
            cursor.close();
            return ans;
        }
        else {
            cursor.close();
            return new String[0];
        }
    }
    public boolean addSearchHistory(String record){//添加一条搜索记录，返回是否成功。如果该记录已经存在，其会被移到首位
        synchronized (search_lock) {
//            removeSearchHistory(record);
            ContentValues cv = new ContentValues();
            cv.put("keyword", record);
            long ans = db.insertWithOnConflict("SEARCH", null, cv,SQLiteDatabase.CONFLICT_REPLACE);
            return ans != -1;
        }
    }
    public boolean removeSearchHistory(String record){//移除一条搜索记录，返回是否成功
        synchronized (search_lock) {
            int ans = db.delete("SEARCH", "keyword=?", new String[]{record});
            return ans == 1;
        }
    }
    public void clearSearchHistory(){//清空搜索记录
        synchronized (search_lock) {
            db.execSQL("delete from SEARCH");
        }
    }
    public String[] getAllSearchHistory(){//返回所有搜索记录，如果没有则返回null
        Cursor cursor=null;
        synchronized (search_lock){
            cursor=db.query("SEARCH",null,null,null,null,null,"rowid desc");
        }
        if(cursor.moveToFirst()){
            String[] ans=new String[cursor.getCount()];
            int pos=0;
            do{
                ans[pos++]=cursor.getString(cursor.getColumnIndex("keyword"));
            }while(cursor.moveToNext());
            cursor.close();
            return ans;
        }
        else {
            cursor.close();
            return new String[0];
        }
    }
    public boolean addBrowseHistory(String newsId){//添加一条浏览记录，返回是否成功。浏览记录允许重复
        ContentValues cv=new ContentValues();
        cv.put("newsId",newsId);
        synchronized (browse_lock) {
            long ans = db.insert("BROWSE", null, cv);
            return ans != -1;
        }
    }
    public boolean removeBrowseHistory(String newsId){//溢出一条浏览记录，返回是否成功
        synchronized (browse_lock) {
            int ans = db.delete("BROWSE", "newsId=?", new String[]{newsId});
            return ans >= 1;
        }
    }
    public void removeAllBrowseHistory(){//清空浏览记录
        synchronized (browse_lock) {
            db.execSQL("delete from BROWSE");
        }
    }
    public Data[] getLatestBrowseHistory(int limit){//返回最近获得的limit条浏览记录，越晚的浏览记录位置越靠前，没有则返回null
        synchronized (news_lock){
            synchronized (browse_lock){
                synchronized (favorite_lock){
                    synchronized (forbid_lock){
                        synchronized (image_lock){
                            synchronized (keywords_lock){
                                synchronized (search_lock){
                                    synchronized (tags_lock) {
                                        Cursor cursor = db.query("BROWSE", null, null, null, null, null, "rowid desc");
                                        if (cursor.moveToFirst()) {
                                            Data[] ans = new Data[cursor.getCount()];
                                            int pos = 0;
                                            db.beginTransaction();
                                            do {
                                                Cursor c = db.query("NEWS", null, "newsId=?", new String[]{cursor.getString(cursor.getColumnIndex("newsId"))}, null, null, null);
                                                if (c.moveToFirst()) {
                                                    ans[pos++] = readInNews(c);
                                                } else {
                                                    System.out.println("Invalid record in browse table");
                                                    db.endTransaction();
                                                    cursor.close();
                                                    return new Data[0];
                                                }
                                            } while (cursor.moveToNext());
                                            db.setTransactionSuccessful();
                                            db.endTransaction();
                                            cursor.close();
                                            return ans;
                                        } else
                                            return new Data[0];
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public Data[] getLatestBrowseHistoryInRange(int from,int to){//返回[from,to)之间的浏览记录，越晚的浏览记录位置越靠前，没有则返回null
        synchronized (news_lock){
            synchronized (browse_lock){
                synchronized (favorite_lock){
                    synchronized (forbid_lock){
                        synchronized (image_lock){
                            synchronized (keywords_lock){
                                synchronized (search_lock){
                                    synchronized (tags_lock) {
                                        Cursor prob = db.rawQuery("select count(rowid) from BROWSE", null);
                                        prob.moveToFirst();
                                        int count = prob.getInt(0);
                                        prob.close();
                                        if (count <= from) {
                                            return new Data[0];
                                        }
                                        Cursor cursor = db.rawQuery("select*from BROWSE where rowid<=(select min(rowid) from(select rowid from BROWSE order by rowid desc limit ?))order by rowid desc limit ?", new String[]{String.valueOf(from + 1), String.valueOf(to - from)});
                                        if (cursor.moveToFirst()) {
                                            Data[] ans = new Data[cursor.getCount()];
                                            int pos = 0;
                                            db.beginTransaction();
                                            do {
                                                Cursor c = db.query("NEWS", null, "newsId=?", new String[]{cursor.getString(cursor.getColumnIndex("newsId"))}, null, null, null);
                                                if (c.moveToFirst()) {
                                                    ans[pos++] = readInNews(c);
                                                } else {
                                                    System.out.println("Invalid record in browse table");
                                                    db.endTransaction();
                                                    cursor.close();
                                                    return new Data[0];
                                                }
                                            } while (cursor.moveToNext());
                                            db.setTransactionSuccessful();
                                            db.endTransaction();
                                            cursor.close();
                                            return ans;
                                        } else {
                                            db.endTransaction();
                                            return new Data[0];
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private boolean hasRead(Data data){//返回一条新闻是否已读
        synchronized (browse_lock){
            Cursor cursor=db.rawQuery("select count(*) from BROWSE where newsId=?",new String[]{data.getNewsID()});
            cursor.moveToFirst();
            return cursor.getInt(0)>=1;
        }
    }
    public boolean[] hasRead(Data[] data){//返回一组新闻是否已读，返回顺序和参数顺序相同
        boolean[] ans=new boolean[data.length];
        synchronized (news_lock){
            synchronized (browse_lock){
                synchronized (favorite_lock){
                    synchronized (forbid_lock){
                        synchronized (image_lock){
                            synchronized (keywords_lock){
                                synchronized (search_lock){
                                    synchronized (tags_lock) {
                                        db.beginTransaction();
                                        int n = data.length;
                                        for (int i = 0; i < n; i++)
                                            ans[i] = hasRead(data[i]);
                                        db.setTransactionSuccessful();
                                        db.endTransaction();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ans;
    }

    private void addImageCache(String url, Bitmap bitmap){
        byte[] bytes=BitmapTools.castBitmapToBytes(bitmap);
        synchronized (image_lock){
            ContentValues cv=new ContentValues();
            cv.put("imageUrl",url);
            cv.put("imageBin",bytes);
            db.insert("IMAGE",null,cv);
        }
    }

    public void addImageCache(String[] url, Bitmap[] bitmap){//添加一组图片缓存
        synchronized (news_lock){
            synchronized (browse_lock){
                synchronized (favorite_lock){
                    synchronized (forbid_lock){
                        synchronized (image_lock){
                            synchronized (keywords_lock){
                                synchronized (search_lock){
                                    synchronized (tags_lock) {
                                        db.beginTransaction();
                                        int n = url.length;
                                        for (int i = 0; i < n; i++)
                                            addImageCache(url[i], bitmap[i]);
                                        db.setTransactionSuccessful();
                                        db.endTransaction();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public Bitmap getImageCache(String url){//获取图片缓存，如果没有则返回null
        Cursor cursor=null;
        synchronized (image_lock){
            cursor=db.query("IMAGE",new String[]{"imageBin"},"imageUrl=?",new String[]{url},null,null,null);
        }
        if(cursor.moveToFirst()){
            Bitmap ans=BitmapTools.castBytesToBitmap(cursor.getBlob(0));
            cursor.close();
            return ans;
        }
        else{
            cursor.close();
            return null;
        }
    }

    public void clearImageCache(){//清空图片缓存
        synchronized (image_lock){
            db.execSQL("delete from IMAGE");
        }
    }

    //WARNING DO NOT USE THIS METHOD IN RELEASE
    public void clear_all(){//全 部 木 大
        db.execSQL("delete from NEWS");
        db.execSQL("delete from FAVORITE");
        db.execSQL("delete from IMAGE");
        db.execSQL("delete from KEYWORDS");
        db.execSQL("delete from FORBID");
        db.execSQL("delete from SEARCH");
        db.execSQL("delete from BROWSE");
    }

    //最后再改：用db存储记录标签顺序
    public void addCategory(String category) {
        synchronized (tags_lock){
            ContentValues cv=new ContentValues();
            cv.put("tag",category);
            db.insert("TAGS",null,cv);
        }
    }

    public void removeCategory(String category){
        synchronized (tags_lock){
            db.delete("TAGS","tag=?",new String[]{category});
        }
    }

    public String[] getAllCategory() {
        synchronized (tags_lock){
            Cursor cursor=db.query("TAGS",null,null,null,null,null,"rowid asc");
            if(cursor.moveToFirst()){
                String[] ans=new String[cursor.getCount()];
                int pos=0;
                do{
                    ans[pos++]=cursor.getString(cursor.getColumnIndex("tag"));
                }while(cursor.moveToNext());
                cursor.close();
                return ans;
            }
            else{
                cursor.close();
                return new String[0];
            }
        }
    }

    private Data[] sameCategory(String category,String newsId,int n){
        Bundle bundle=new Bundle();
        bundle.putString("categories",category);
        final JSONParser parser=new JSONParser(bundle);
        parser.setManager(this);
        final QueryResult[] dst=new QueryResult[1];
        Thread thread=new Thread(){
            @Override
            public void run(){
                dst[0]=parser.next();
            }
        };
        thread.start();
        while (thread.isAlive()){};
        Data[] tmp=dst[0].getData();
        Data[] ans=new Data[n];
        int j=0;
        for(int i=0;i<n;i++){
            if(tmp[j].getNewsID().equals(newsId))
                j++;
            ans[i]=tmp[j++];
        }
        return ans;
    }

    public Data[] relatedNews(String keywordString,String category,String newsId){
        if(keywordString.isEmpty()){
            return sameCategory(category,newsId,3);
        }
        String[] keys=keywordString.split("\\|");
        String[] k0=keys[0].split(",");
        Bundle bundle=new Bundle();
        bundle.putString("categories",category);
        bundle.putString("words",k0[0]);
        final JSONParser parser=new JSONParser(bundle);
        parser.setManager(this);
        final QueryResult[] dst=new QueryResult[1];
        Thread thread=new Thread(){
            @Override
            public void run(){
                dst[0]=parser.next();
            }
        };
        thread.start();
        while(thread.isAlive()){};
        if(dst[0].getTotal()<4){//单关键词+同类失败，变成同类
                Data[] ans=new Data[3];
                Data[] sc=sameCategory(category,newsId,3);
                for(int i=0;i<3;i++)
                    ans[i]=sc[i];
                return ans;
        }
        else{//单关键词+同类成功
            Data[] data=dst[0].getData();
            Data[] ans=new Data[3];
            int j=0;
            for(int i=0;i<3;i++){
                if(j<data.length&&data[j].getNewsID().equals(newsId))
                    j++;
                ans[i]=data[j++];
            }
            return ans;
        }
    }

    final private Boolean thumbnail_lock=false;

    public Bitmap getThumbNail(String url){
        synchronized (thumbnail_lock){
            Cursor cursor=db.query("THUMBNAIL",new String[]{"image"},"url=?",new String[]{url},null,null,null);
            if(cursor.moveToFirst()){
                Bitmap ans=BitmapTools.castBytesToBitmap(cursor.getBlob(0));
                cursor.close();
                return ans;
            }
            else{
                cursor.close();
                return null;
            }
        }
    }
    public void addThumbNail(String url,Bitmap bitmap){
        synchronized (thumbnail_lock){
            ContentValues cv=new ContentValues();
            cv.put("url",url);
            cv.put("image",BitmapTools.castBitmapToBytes(bitmap));
            db.insert("THUMBNAIL",null,cv);
        }
    }
}
//、、、、、、、、、
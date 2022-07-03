package background;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.*;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;


public class JSONParser implements Parcelable {
    private int size;
    private Date startDate,endDate;
    private String words;
    private String categories;
    private int page;

    private int totalPage=0;
    private int offset=0;//当前page前offset条新闻已读，从data[offset]开始读取
    private DataBaseManager manager;
    private boolean firstquery=true;

    protected JSONParser(Parcel in) {
        size = in.readInt();
        words = in.readString();
        categories = in.readString();
        page = in.readInt();
        totalPage = in.readInt();
        private_address = in.readString();
        startDate=null;
        endDate=null;
    }

    public static final Creator<JSONParser> CREATOR = new Creator<JSONParser>() {
        @Override
        public JSONParser createFromParcel(Parcel in) {
            return new JSONParser(in);
        }

        @Override
        public JSONParser[] newArray(int size) {
            return new JSONParser[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(size);
        parcel.writeString(words);
        parcel.writeString(categories);
        parcel.writeInt(page);
        parcel.writeInt(totalPage);
        parcel.writeString(private_address);
    }

//    private void updatePage(int newTotal){
//        if(newTotal!=totalPage){
//            if(totalPage==0){
//                totalPage=newTotal;
//                return;
//            }
//            offset+=newTotal-totalPage;
//            offset_flag=true;
//            page+=offset/size;
//            offset%=size;
//        }
//    }

    static public enum category {
        entertainment("娱乐"),
        military("军事"),
        education("教育"),
        culture("文化"),
        health("健康"),
        sports("体育"),
        finance("财经"),
        cars("汽车"),
        technology("科技"),
        social("社会"),
        blank("");
        private String name;
        private category(String name) {
            this.name=name;
        }
        public String getName() {
            return name;
        }};
    private static String address="https://api2.newsminer.net/svc/news/queryNewsList?";
    private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss");
    private String private_address;
    public JSONParser(Bundle bundle) {//参数: pageSize,startData,endData,words,categories,categories类型是JSONParser.category
        size=bundle.getInt("pageSize",15);
        startDate=(Date)bundle.get("startDate");//default is null
        endDate=(Date)bundle.get("endDate");//default is null
        words=bundle.getString("words","");
        categories=bundle.getString("categories");//default is null
        page=1;
        StringBuilder sb=new StringBuilder();
        sb.append(address);
        sb.append("size=");
        sb.append(size);
        sb.append("&startDate=");
        sb.append(startDate==null ? "" : sdf.format(startDate));
        sb.append("&endDate=");
        sb.append(endDate==null ? sdf.format(new Date()) : sdf.format(endDate));
        sb.append("&words=");
        sb.append(words);//
        sb.append("&categories=");
        sb.append(categories==null ? "" : categories);
        sb.append("&page=");
        private_address=sb.toString();
    }
    public void setManager(DataBaseManager manager){
        this.manager=manager;
    }
    public void setPage(int page){
        this.page=page;
    }
    private Data[] query(int localpage){
        URL url;
        QueryResult res;
        try {
            url=new URL(private_address+localpage);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new Data[0];
        }
        try {
            InputStream is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                json.append(line);
            }
            br.close();
            res = JSONObject.parseObject(json.toString(), QueryResult.class);
        }
        catch(IOException e){
            e.printStackTrace();
            return new Data[0];
        }
        totalPage=res.getTotal();
        return res.getData();
    }
    public QueryResult next(){//获得之后的pageSize条新闻，data有可能为空
        String blockwords=String.join("|",manager.getAllForbiddenWords());
        ArrayList<Data> list=new ArrayList<Data>();
        int remain=size;
        QueryResult res=new QueryResult();
        boolean firstround=true;
        boolean firstpage=true;
        while((firstround||offset+size*page<totalPage)&&(remain>0)){
            int i=0;
            Data[] data;
            if(firstround){
                firstround=false;
                int oldTotal=totalPage;
                data=query(page);
                if(oldTotal!=totalPage){
                    if(!firstquery) {
                        offset += totalPage - oldTotal;
                        page += offset / size;
                        offset %= size;
                        continue;
                    }
                    else{
                        firstquery=false;
                        page++;
                    }
                }
                else {
                    firstpage=false;
                    i=offset;
                    page++;
                }
            }
            else
                data=query(page++);
            if(firstpage) {
                i = offset;
                firstpage=false;
            }
            if(data.length==0)//错误？
                break;
            for(;i<data.length;i++){
                if(blockwords.length()==0||!data[i].getAllKeywords().matches(blockwords)){
                    list.add(data[i]);
                    remain--;
                    if(remain==0){
                        offset=i+1;
                        break;
                    }
                }
            }
        }
        if(remain==0)
            if(offset==size)//上一页全部已读
                offset=0;
            else if(offset!=0)//上一页仍有未读
                page--;
        res.setData(list.toArray(new Data[list.size()]));
        res.setPageSize(size);
        res.setCurrentPage(page);
        res.setTotal(totalPage);
        return res;
    }
    public QueryResult refresh(){
        String blockwords=String.join("|",manager.getAllForbiddenWords());
        int oldTotal=totalPage;
        int localPage=1;
        Data[] data=query(1);
        if(oldTotal!=totalPage){
            int more=totalPage-oldTotal;
            if(!firstquery) {
                offset += more;
                page += offset / size;
                offset %= size;
            }
            else{
                firstquery=false;
                more=size;
                page=2;
            }
            ArrayList<Data> list=new ArrayList<Data>();
            while(more>0) {
                for (int i = 0; i < more && i < size; i++) {
                    if (blockwords.length()==0||!data[i].getAllKeywords().matches(blockwords))
                        list.add(data[i]);
                }
                data=query(++localPage);
                more-=size;
            }
            QueryResult res=new QueryResult();
            res.setData(list.toArray(new Data[list.size()]));
            res.setCurrentPage(1);
            res.setPageSize(size);
            res.setTotal(totalPage);
            return res;
        }
        else{
            QueryResult err=new QueryResult();
            err.setData(new Data[0]);
            return err;
        }
    }
}

package background;
import java.util.ArrayList;
import java.util.regex.*;

public class Data{//Warning: return value can be NULL
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        if(content.substring(0,3).equals("原标题"))
            this.content = content.substring(content.indexOf('\n')+1);
        else
            this.content=content;
    }
    public String getNewsID() {
        return newsID;
    }
    public void setNewsID(String newsID) {
        this.newsID = newsID;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;//.replace("http","https");
    }
    public String getPublishTime() {
        return publishTime;
    }
    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    public String[] getImage() {
        return image;
    }
    public void setImage(String image) {
        if(image==null||image.isEmpty()) {
            this.image = null;
            allImage="";
            return;
        }
//        Pattern pattern=Pattern.compile("http[\\w-\\.:/]+");
        Pattern pattern=Pattern.compile("http[^,\\]\\[]+");
        ArrayList<String> al=new ArrayList<String>();
        Matcher m=pattern.matcher(image);
        while(m.find()){
            al.add(m.group(0));
        }
        this.image=al.toArray(new String[al.size()]);
        allImage=String.format("[%s]",String.join(",",this.image));
    }
    public String getVideo() {
        return video;
    }
    public void setVideo(String video) {
        this.video = video;
    }
    public Keyword[] getKeywords() {
        return keywords;
    }
    public void setKeywords(Keyword[] keywords) {
        if(keywords==null){
            this.keywords=null;
            allKeywords="";
            return;
        }
        StringBuilder sb=new StringBuilder();
        for(Keyword k : keywords)
            sb.append(k.getWord()+","+k.getScore()+"|");
        if(sb.length()>0)
            sb.deleteCharAt(sb.length()-1);
        allKeywords=sb.toString();
        this.keywords = keywords;
    }
    //    public void setRead(boolean read){
//        this.read=read;
//    }
//    public boolean getRead(){
//        return read;
//    }
    public String getAllImage(){
        return allImage;
    }
    public String getAllKeywords(){//key1,score1|key2,score2|...
        return allKeywords;
    }
    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append(String.format("category:%s\n",category));
        sb.append(String.format("content:%s\n",content));
        sb.append(String.format("newsId:%s\n",newsID));
        sb.append(String.format("url:%s\n",url));
        sb.append(String.format("publishTime:%s\n",publishTime));
        sb.append(String.format("title:%s\n",title));
        sb.append(String.format("publisher:%s\n",publisher));
        sb.append(String.format("video:%s\n",video));
        sb.append(String.format("allImage:%s\n",allImage));
        sb.append(String.format("allKeywords:%s\n",allKeywords));
//        sb.append(String.format("read:%b\n",read));
        return sb.toString();
    }

    private String category,content,newsID,url,publishTime,title,publisher;
    private String video;
    private String[] image;
    private String allImage,allKeywords;
    private Keyword[] keywords;
//    private boolean read=false;
}
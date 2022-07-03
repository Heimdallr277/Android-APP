package background;

public class Keyword{
    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }
    public String getWord() {
        return word;
    }
    public void setWord(String word) {
        this.word = word;
    }
    public Keyword(){}
//    public Keyword(String s){
//        String[] sep=s.split("\\s");
//        word=sep[0];
//        score=Double.valueOf(sep[1]);
//    }
//    public String toString(){
//        return word+" "+score;
//    }
    double score;
    String word;
}
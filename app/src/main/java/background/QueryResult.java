package background;

public class QueryResult{//不要使用set方法，只使用get方法
    public int getCurrentPage() {
        return currentPage;
    }//

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Data[] getData() {
        return data;
    }

    public void setData(Data[] data) {
        this.data = data;
    }

    private int currentPage,total,pageSize;
    private Data[] data;



}
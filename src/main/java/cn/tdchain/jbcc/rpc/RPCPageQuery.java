package cn.tdchain.jbcc.rpc;

public class RPCPageQuery {
    private Long limit = 20L;
    private Long offset = 0l;
    private Boolean sort;
    private String field;


    public boolean verify() {
        if (limit <= 0 || offset < 0) {
            return false;
        }
        return true;
    }


    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public Boolean getSort() {
        return sort;
    }

    public void setSort(Boolean sort) {
        this.sort = sort;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
    
}

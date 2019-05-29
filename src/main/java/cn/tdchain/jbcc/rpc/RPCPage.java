package cn.tdchain.jbcc.rpc;


import java.util.List;

public class RPCPage<T> {
    private long total;
    private List<T> list;


    public long getTotal() {
        return total;
    }

    public RPCPage<T> setTotal(long total) {
        this.total = total;
        return this;
    }

    public int getSize() {
        if (list == null) {
            return 0;
        }
        return list.size();
    }
    
    public List<T> getList() {
        return list;
    }

    public RPCPage<T> setList(List<T> list) {
        this.list = list;
        return this;
    }
}

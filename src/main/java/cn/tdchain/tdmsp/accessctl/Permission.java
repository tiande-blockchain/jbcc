package cn.tdchain.tdmsp.accessctl;

import java.util.List;

public class Permission {
    private List<String> readAll;
    private List<String> readSystem;
    private List<String> writeSimple;
    private List<String> writeSystem;

    public List<String> getReadAll() {
        return readAll;
    }

    public void setReadAll(List<String> readAll) {
        this.readAll = readAll;
    }

    public List<String> getReadSystem() {
        return readSystem;
    }

    public void setReadSystem(List<String> readSystem) {
        this.readSystem = readSystem;
    }

    public List<String> getWriteSimple() {
        return writeSimple;
    }

    public void setWriteSimple(List<String> writeSimple) {
        this.writeSimple = writeSimple;
    }

    public List<String> getWriteSystem() {
        return writeSystem;
    }

    public void setWriteSystem(List<String> writeSystem) {
        this.writeSystem = writeSystem;
    }
}

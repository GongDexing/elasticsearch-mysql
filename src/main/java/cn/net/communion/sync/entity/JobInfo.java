package cn.net.communion.sync.entity;

public class JobInfo {
    private String name;
    private String index;
    private String type;
    private String cron;
    private String sql;
    private long step = 1000;
    private String[] params;
    private int[] paramTypes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public int[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(int[] paramTypes) {
        this.paramTypes = paramTypes;
    }
}

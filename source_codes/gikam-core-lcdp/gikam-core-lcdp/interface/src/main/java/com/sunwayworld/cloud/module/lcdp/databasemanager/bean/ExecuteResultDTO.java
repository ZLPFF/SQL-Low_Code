package com.sunwayworld.cloud.module.lcdp.databasemanager.bean;


public class ExecuteResultDTO extends QueryDataResultDTO  {

    private static final long serialVersionUID = -1445002206642820414L;

    private String sql;

    private Object result;

    private Boolean success;

    private Boolean isSelect;

    private String usedTime;

    private int errorLineNum; // 错误行数

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Boolean getSelect() {
        return isSelect;
    }

    public void setSelect(Boolean select) {
        isSelect = select;
    }

    public String getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(String usedTime) {
        this.usedTime = usedTime;
    }

    public int getErrorLineNum() {
        return errorLineNum;
    }

    public void setErrorLineNum(int errorLineNum) {
        this.errorLineNum = errorLineNum;
    }
}

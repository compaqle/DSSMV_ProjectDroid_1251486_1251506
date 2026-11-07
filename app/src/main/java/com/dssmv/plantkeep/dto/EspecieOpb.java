package com.dssmv.plantkeep.dto;


public class EspecieOpb {

    private String pid;
    private String displayPid;
    private String alias;
    private String category;

    public EspecieOpb() {
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getDisplayPid() {
        return displayPid;
    }

    public void setDisplayPid(String displayPid) {
        this.displayPid = displayPid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

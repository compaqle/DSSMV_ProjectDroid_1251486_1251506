package com.dssmv.plantkeep.dto;

/**
 * Uma espécie devolvida pela pesquisa do OpenPlantbook.
 * Só campos e getters/setters. Nada de rede nem JSON.
 */
public class EspecieOpb {
    private String pid;         // id da espécie no OpenPlantbook
    private String displayPid;  // nome apresentável (ex. "Lycopersicon esculentum")
    private String alias;       // apelido/filtro de pesquisa
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

package com.dssmv.plantkeep.dto;

import java.util.List;

/**
 * Uma espécie devolvida pela pesquisa do OpenPlantbook.
 * Só campos e getters/setters. Nada de rede nem JSON.
 */
public class EspecieOpb {
    private String pid;         // id da espécie no OpenPlantbook
    private String displayPid;  // nome apresentável (ex. "Lycopersicon esculentum")
    private String alias;       // apelido/filtro de pesquisa
    private String category;
    private String imageUrl;    // url da imagem (pode ser vazio se não existir)
    private List<String> aliasesComuns;  // nomes comuns (ex. "Potato"), do detail

    public EspecieOpb() {
    }

    public List<String> getAliasesComuns() {
        return aliasesComuns;
    }

    public void setAliasesComuns(List<String> aliasesComuns) {
        this.aliasesComuns = aliasesComuns;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

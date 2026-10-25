package com.dssmv.plantkeep.dto;

/**
 * Dados completos de uma espécie devolvidos pelo detail do OpenPlantbook.
 * Só campos e getters/setters. Os números são Integer porque a API permite null.
 */
public class DetalheOpb {
    private String displayPid;
    private String category;
    private String origin;
    private String imageUrl;

    private Integer minTemp;
    private Integer maxTemp;
    private Integer minEnvHumid;
    private Integer maxEnvHumid;
    private Integer minLightLux;
    private Integer maxLightLux;
    private Integer minSoilMoist;
    private Integer maxSoilMoist;
    private Integer minSoilEc;
    private Integer maxSoilEc;

    public DetalheOpb() {
    }

    public String getDisplayPid() {
        return displayPid;
    }

    public void setDisplayPid(String displayPid) {
        this.displayPid = displayPid;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(Integer minTemp) {
        this.minTemp = minTemp;
    }

    public Integer getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(Integer maxTemp) {
        this.maxTemp = maxTemp;
    }

    public Integer getMinEnvHumid() {
        return minEnvHumid;
    }

    public void setMinEnvHumid(Integer minEnvHumid) {
        this.minEnvHumid = minEnvHumid;
    }

    public Integer getMaxEnvHumid() {
        return maxEnvHumid;
    }

    public void setMaxEnvHumid(Integer maxEnvHumid) {
        this.maxEnvHumid = maxEnvHumid;
    }

    public Integer getMinLightLux() {
        return minLightLux;
    }

    public void setMinLightLux(Integer minLightLux) {
        this.minLightLux = minLightLux;
    }

    public Integer getMaxLightLux() {
        return maxLightLux;
    }

    public void setMaxLightLux(Integer maxLightLux) {
        this.maxLightLux = maxLightLux;
    }

    public Integer getMinSoilMoist() {
        return minSoilMoist;
    }

    public void setMinSoilMoist(Integer minSoilMoist) {
        this.minSoilMoist = minSoilMoist;
    }

    public Integer getMaxSoilMoist() {
        return maxSoilMoist;
    }

    public void setMaxSoilMoist(Integer maxSoilMoist) {
        this.maxSoilMoist = maxSoilMoist;
    }

    public Integer getMinSoilEc() {
        return minSoilEc;
    }

    public void setMinSoilEc(Integer minSoilEc) {
        this.minSoilEc = minSoilEc;
    }

    public Integer getMaxSoilEc() {
        return maxSoilEc;
    }

    public void setMaxSoilEc(Integer maxSoilEc) {
        this.maxSoilEc = maxSoilEc;
    }
}

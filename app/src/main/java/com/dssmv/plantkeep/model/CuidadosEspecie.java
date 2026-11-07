package com.dssmv.plantkeep.model;

public class CuidadosEspecie {

    private final Integer minTemp;
    private final Integer maxTemp;
    private final Integer minEnvHumid;
    private final Integer maxEnvHumid;
    private final Integer minLightLux;
    private final Integer maxLightLux;
    private final Integer minSoilMoist;
    private final Integer maxSoilMoist;
    private final Integer minSoilEc;
    private final Integer maxSoilEc;
    private final String categoria;
    private final String origem;
    private final String imagemUrl;

    public CuidadosEspecie(Integer minTemp, Integer maxTemp,
                           Integer minEnvHumid, Integer maxEnvHumid,
                           Integer minLightLux, Integer maxLightLux,
                           Integer minSoilMoist, Integer maxSoilMoist,
                           Integer minSoilEc, Integer maxSoilEc,
                           String categoria, String origem, String imagemUrl) {
        validarIntervalo(minTemp, maxTemp, "Temperatura");
        validarIntervalo(minEnvHumid, maxEnvHumid, "Humidade do ar");
        validarIntervalo(minLightLux, maxLightLux, "Luminosidade");
        validarIntervalo(minSoilMoist, maxSoilMoist, "Humidade do solo");
        validarIntervalo(minSoilEc, maxSoilEc, "Condutividade elétrica do solo");

        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.minEnvHumid = minEnvHumid;
        this.maxEnvHumid = maxEnvHumid;
        this.minLightLux = minLightLux;
        this.maxLightLux = maxLightLux;
        this.minSoilMoist = minSoilMoist;
        this.maxSoilMoist = maxSoilMoist;
        this.minSoilEc = minSoilEc;
        this.maxSoilEc = maxSoilEc;
        this.categoria = (categoria != null) ? categoria.trim() : "";
        this.origem = (origem != null) ? origem.trim() : "";
        this.imagemUrl = (imagemUrl != null) ? imagemUrl.trim() : "";
    }

    private static void validarIntervalo(Integer min, Integer max, String nomeParametro) {
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException(
                    "CuidadosEspecie: limite mínimo maior que o máximo para " + nomeParametro);
        }
    }

    public Integer getMinTemp() {
        return minTemp;
    }

    public Integer getMaxTemp() {
        return maxTemp;
    }

    public Integer getMinEnvHumid() {
        return minEnvHumid;
    }

    public Integer getMaxEnvHumid() {
        return maxEnvHumid;
    }

    public Integer getMinLightLux() {
        return minLightLux;
    }

    public Integer getMaxLightLux() {
        return maxLightLux;
    }

    public Integer getMinSoilMoist() {
        return minSoilMoist;
    }

    public Integer getMaxSoilMoist() {
        return maxSoilMoist;
    }

    public Integer getMinSoilEc() {
        return minSoilEc;
    }

    public Integer getMaxSoilEc() {
        return maxSoilEc;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getOrigem() {
        return origem;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public boolean isTemperaturaAbaixoDoMinimo(double temp) {
        return minTemp != null && temp < minTemp;
    }

    public boolean isTemperaturaAcimaDoMaximo(double temp) {
        return maxTemp != null && temp > maxTemp;
    }

    public boolean isHumidadeSoloAcimaDoMaximo(double humidade) {
        return maxSoilMoist != null && humidade > maxSoilMoist;
    }

    @Override
    public String toString() {
        return "CuidadosEspecie{" +
                "minTemp=" + minTemp +
                ", maxTemp=" + maxTemp +
                ", minLightLux=" + minLightLux +
                ", maxLightLux=" + maxLightLux +
                ", minSoilMoist=" + minSoilMoist +
                ", maxSoilMoist=" + maxSoilMoist +
                ", categoria='" + categoria + '\'' +
                ", origem='" + origem + '\'' +
                '}';
    }
}
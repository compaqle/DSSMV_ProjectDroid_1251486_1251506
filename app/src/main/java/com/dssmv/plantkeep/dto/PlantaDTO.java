package com.dssmv.plantkeep.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO da planta. Recipiente de dados igual ao JSON. Só campos e getters/setters.
 */
public class PlantaDTO {

    private String id;       // _id do restdb
    private String nome;
    private String especie;
    private String pid;      // id da espécie no OpenPlantbook
    private long ultimaRega; // timestamp (ms) da última rega
    private List<RegistoDiarioDTO> diario;

    public PlantaDTO() {
        this.diario = new ArrayList<>();
    }

    public PlantaDTO(String id, String nome, String especie, String pid, long ultimaRega, List<RegistoDiarioDTO> diario) {
        this.id = id;
        this.nome = nome;
        this.especie = especie;
        this.pid = pid;
        this.ultimaRega = ultimaRega;
        this.diario = (diario != null) ? diario : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public long getUltimaRega() {
        return ultimaRega;
    }

    public void setUltimaRega(long ultimaRega) {
        this.ultimaRega = ultimaRega;
    }

    public List<RegistoDiarioDTO> getDiario() {
        return diario;
    }

    public void setDiario(List<RegistoDiarioDTO> diario) {
        this.diario = diario;
    }
}
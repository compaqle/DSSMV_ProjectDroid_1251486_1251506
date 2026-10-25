package com.dssmv.plantkeep.dto;

/**
 * DTO da planta. Recipiente de dados igual ao JSON. Só campos e getters/setters.
 */
public class PlantaDTO {
    private String id;      // _id do restdb
    private String nome;
    private String especie;
    private String pid;     // id da espécie no OpenPlantbook

    public PlantaDTO() {
    }

    public PlantaDTO(String id, String nome, String especie, String pid) {
        this.id = id;
        this.nome = nome;
        this.especie = especie;
        this.pid = pid;
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
}

package com.dssmv.plantkeep.dto;

import java.util.ArrayList;
import java.util.List;

public class PlantaDTO {

    private final String id;
    private final String nome;
    private final String especie;
    private final String pid;
    private final long ultimaRega;
    private final List<RegistoDiarioDTO> diario;

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

    public String getNome() {
        return nome;
    }

    public String getEspecie() {
        return especie;
    }

    public String getPid() {
        return pid;
    }

    public long getUltimaRega() {
        return ultimaRega;
    }

    public List<RegistoDiarioDTO> getDiario() {
        return diario;
    }
}
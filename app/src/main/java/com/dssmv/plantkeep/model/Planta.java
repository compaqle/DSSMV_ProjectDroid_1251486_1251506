package com.dssmv.plantkeep.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model da planta, puro, sem rede nem JSON. Tem as regras e lança exceção em dados inválidos.
 */
public class Planta {

    private String id;       // _id restdb
    private String nome;     // nome/apelido dado pelo utilizador
    private String especie;  // espécie tal como aparece no OpenPlantbook
    private String pid;      // id da espécie no OpenPlantbook
    private long ultimaRega; // timestamp (ms) da última vez que foi regada (0 se nunca)
    private List<RegistoDiario> diario; // registos do diário da planta

    private boolean isNomeValid(String nome) {
        return nome != null && nome.trim().length() >= 3;
    }

    public Planta(String nome, String especie) {
        setNome(nome);
        this.especie = especie;
        this.diario = new ArrayList<>();
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
        if (isNomeValid(nome)) {
            this.nome = nome.trim();
        } else {
            throw new IllegalArgumentException("Planta: nome '" + nome + "' deve ter pelo menos 3 caracteres");
        }
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
        if (ultimaRega < 0) {
            throw new IllegalArgumentException("Planta: ultimaRega não pode ser negativa");
        }
        this.ultimaRega = ultimaRega;
    }

    public List<RegistoDiario> getDiario() {
        return diario;
    }

    public void setDiario(List<RegistoDiario> diario) {
        this.diario = (diario != null) ? diario : new ArrayList<>();
    }

    public void adicionarRegistoDiario(RegistoDiario registo) {
        if (registo == null) {
            throw new IllegalArgumentException("Planta: registo do diário não pode ser nulo");
        }
        this.diario.add(registo);
    }
}
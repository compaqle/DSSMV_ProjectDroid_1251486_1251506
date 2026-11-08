package com.dssmv.plantkeep.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Planta {

    private String id;
    private String nome;
    private String especie;
    private String pid;
    private long ultimaRega;
    private List<RegistoDiario> diario;

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
        return Collections.unmodifiableList(diario);
    }

    public void setDiario(List<RegistoDiario> diario) {
        if (diario != null) {
            this.diario = new ArrayList<>(diario);
        } else {
            this.diario = new ArrayList<>();
        }
    }

    public void adicionarRegistoDiario(RegistoDiario registo) {
        if (registo == null) {
            throw new IllegalArgumentException("Planta: registo do diário não pode ser nulo");
        }
        this.diario.add(registo);
    }

    public boolean removerRegistoDiario(RegistoDiario registo) {
        if (registo == null) {
            return false;
        }
        return this.diario.remove(registo);
    }
}

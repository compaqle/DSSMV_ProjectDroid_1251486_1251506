package com.dssmv.plantkeep.model;

/**
 * Model da planta, puro, sem rede nem JSON. Tem as regras e lança exceçao em dados inválidos.
 */
public class Planta {
    private String id;      // _id restdb
    private String nome;
    private String especie;

    private boolean isNomeValid(String nome) {
        return nome != null && nome.trim().length() >= 3;
    }

    public Planta(String nome, String especie) {
        setNome(nome);
        this.especie = especie;
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
}

package com.dssmv.plantkeep.model;

import org.json.JSONException;
import org.json.JSONObject;

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

    /** Converte PARA JSON (usado pelo Controller quando cria/atualiza no restdb). */
    public String toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("nome", nome);
        o.put("especie", especie);
        return o.toString();
    }

    /** Converte DE JSON (usado pelo Controller quando recebe dados do restdb). */
    public static Planta fromJson(JSONObject o) throws JSONException {
        Planta p = new Planta(o.optString("nome", ""), o.optString("especie", ""));
        p.setId(o.optString("_id", null));
        return p;
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

package com.dssmv.plantkeep.controllers;

import com.dssmv.plantkeep.dbservice.RestDBClient;
import com.dssmv.plantkeep.model.Planta;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Único controller do app (um único ficheiro para o projeto todo):
 * toda a lógica de negócio + acesso ao restdb.
 * Todos os métodos são SÍNCRONOS — chamar num worker Thread, nunca no main thread.
 */
public class Controller {
    private final RestDBClient rest;

    public Controller() {
        this.rest = new RestDBClient("planta");
    }

    /** Lista todas as plantas (GET). Devolve lista vazia se não houver. */
    public List<Planta> listar() throws IOException, JSONException {
        String json = rest.list();
        List<Planta> plantas = new ArrayList<>();
        JSONArray arr = new JSONArray(json);
        for (int i = 0; i < arr.length(); i++) {
            plantas.add(fromJson(arr.getJSONObject(i)));
        }
        return plantas;
    }

    /** Cria uma planta (POST). Devolve o objeto com o _id do restdb. */
    public Planta criar(String nome, String especie) throws IOException, JSONException {
        Planta p = new Planta(nome, especie);
        String json = rest.create(toJson(p));
        p.setId(new JSONObject(json).optString("_id", null));
        return p;
    }

    /** Apaga uma planta por id (DELETE). Ignora se o id for nulo. */
    public void apagar(Planta p) throws IOException {
        if (p.getId() != null) {
            rest.delete(p.getId());
        }
    }

    private static String toJson(Planta p) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("nome", p.getNome());
        o.put("especie", p.getEspecie());
        return o.toString();
    }

    private static Planta fromJson(JSONObject o) throws JSONException {
        String nome = o.optString("nome", "");
        String especie = o.optString("especie", "");
        Planta p = new Planta(nome, especie);
        p.setId(o.optString("_id", null));
        return p;
    }
}

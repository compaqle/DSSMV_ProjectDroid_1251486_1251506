package com.dssmv.plantkeep.model;

import com.dssmv.plantkeep.dbservice.RestDBClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Container das plantas — o "modelo" do padrão MVC, como o StudentContainer do professor.
 * Guarda a coleção de plantas e oferece as operações CRUD sobre o restdb.
 */
public class PlantaContainer {
    private final RestDBClient rest;

    public PlantaContainer() {
        this.rest = new RestDBClient("planta");
    }

    /** Adiciona uma planta (POST). Devolve a planta com o _id atribuído pelo restdb. */
    public Planta add(Planta p) throws IOException, JSONException {
        return Planta.fromJson(new JSONObject(rest.create(p.toJson())));
    }

    /** Devolve todas as plantas (GET). Lista vazia se não houver. */
    public List<Planta> getAll() throws IOException, JSONException {
        List<Planta> plantas = new ArrayList<>();
        JSONArray arr = new JSONArray(rest.list());
        for (int i = 0; i < arr.length(); i++) {
            plantas.add(Planta.fromJson(arr.getJSONObject(i)));
        }
        return plantas;
    }

    /** Apaga uma planta pelo _id (DELETE). Ignora se o id for nulo. */
    public void remove(Planta p) throws IOException {
        if (p.getId() != null) {
            rest.delete(p.getId());
        }
    }
}

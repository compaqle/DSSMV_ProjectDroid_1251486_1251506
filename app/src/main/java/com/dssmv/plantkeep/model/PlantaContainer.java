package com.dssmv.plantkeep.model;

import com.dssmv.plantkeep.dbservice.RestDBClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * O "SERVICE" do padrão FSOFT — como o ClienteService. É aqui que vive a lógica
 * (rede + JSON). Corre numa thread própria e devolve o resultado via callback.
 * O Controller não faz o trabalho: só o chama.
 */
public class PlantaContainer {
    /** Callback que entrega o resultado ao Controller (em thread de fundo). */
    public interface Callback {
        void onSucesso(List<Planta> plantas);   // plantas = null quando não faz sentido
        void onErro(String msg);
    }

    private final RestDBClient rest;

    public PlantaContainer() {
        this.rest = new RestDBClient("planta");
    }

    /** Cria a planta (POST) e, ao fim, devolve a lista atualizada. */
    public void criar(final Planta p, final Callback cb) {
        new Thread(() -> {
            try {
                Planta.fromJson(new JSONObject(rest.create(p.toJson())));
                cb.onSucesso(getAll());
            } catch (Exception e) {
                cb.onErro(e.getMessage());
            }
        }).start();
    }

    /** Devolve a lista de plantas (GET). */
    public void listar(final Callback cb) {
        new Thread(() -> {
            try {
                cb.onSucesso(getAll());
            } catch (Exception e) {
                cb.onErro(e.getMessage());
            }
        }).start();
    }

    /** Apaga uma planta (DELETE) e devolve a lista atualizada. */
    public void apagar(final Planta p, final Callback cb) {
        new Thread(() -> {
            try {
                if (p.getId() != null) {
                    rest.delete(p.getId());
                }
                cb.onSucesso(getAll());
            } catch (Exception e) {
                cb.onErro(e.getMessage());
            }
        }).start();
    }

    /** Leitura interna (GET) usada pelos métodos acima. */
    private List<Planta> getAll() throws IOException, JSONException {
        List<Planta> plantas = new ArrayList<>();
        JSONArray arr = new JSONArray(rest.list());
        for (int i = 0; i < arr.length(); i++) {
            plantas.add(Planta.fromJson(arr.getJSONObject(i)));
        }
        return plantas;
    }
}

package com.dssmv.plantkeep.handler;

import com.dssmv.plantkeep.dto.PlantaDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * O tradutor de JSON só muda entre String JSON e DTO
 * e não faz HTTP nem conhece o model
 */
public class JsonHandler {

    private JsonHandler() {}   // só métodos estáticos, nunca instaciar

    /** Passa um DTO para JSON (corpo de um POST) */
    public static String serializePlantaDTO2Json(PlantaDTO dto) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("nome", dto.getNome());
        o.put("especie", dto.getEspecie());
        return o.toString();
    }

    /** Passa um JSON (aray) para List<PlantaDTO> */
    public static List<PlantaDTO> deSerializeJson2ListPlantaDTO(String json) throws JSONException {
        List<PlantaDTO> dtos = new ArrayList<>();
        JSONArray arr = new JSONArray(json);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            PlantaDTO dto = new PlantaDTO(
                    o.optString("_id", null),
                    o.optString("nome", ""),
                    o.optString("especie", "")
            );
            dtos.add(dto);
        }
        return dtos;
    }
}

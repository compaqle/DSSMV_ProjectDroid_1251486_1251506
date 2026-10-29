package com.dssmv.plantkeep.handler;

import com.dssmv.plantkeep.dto.MeteorologiaDTO;
import com.dssmv.plantkeep.dto.PlantaDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * O tradutor de JSON só muda entre String JSON e DTO
 * e não faz HTTP nem conhece o model.
 */
public class JsonHandler {

    private JsonHandler() {}   // só métodos estáticos, nunca instanciar

    /** Passa um DTO para JSON (corpo de um POST ou PUT) */
    public static String serializePlantaDTO2Json(PlantaDTO dto) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("nome", dto.getNome());
        o.put("especie", dto.getEspecie());
        o.put("pid", dto.getPid());
        o.put("ultimaRega", dto.getUltimaRega());
        return o.toString();
    }

    /** Passa um JSON (array) para List<PlantaDTO> */
    public static List<PlantaDTO> deSerializeJson2ListPlantaDTO(String json) throws JSONException {
        List<PlantaDTO> dtos = new ArrayList<>();
        JSONArray arr = new JSONArray(json);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            PlantaDTO dto = new PlantaDTO(
                    o.optString("_id", null),
                    o.optString("nome", ""),
                    o.optString("especie", ""),
                    o.optString("pid", ""),
                    o.optLong("ultimaRega", 0L)
            );
            dtos.add(dto);
        }
        return dtos;
    }

    /** Passa o JSON do Open-Meteo (objeto "hourly") para List<MeteorologiaDTO> */
    public static List<MeteorologiaDTO> deSerializeJson2ListMeteorologiaDTO(String json) throws JSONException {
        List<MeteorologiaDTO> dtos = new ArrayList<>();

        JSONObject jsonResponse = new JSONObject(json);
        JSONObject hourlyData = jsonResponse.getJSONObject("hourly");

        JSONArray times = hourlyData.getJSONArray("time");
        JSONArray temperatures = hourlyData.getJSONArray("temperature_2m");
        JSONArray precipitations = hourlyData.getJSONArray("precipitation");
        JSONArray soilMoistures = hourlyData.getJSONArray("soil_moisture_1_to_3cm");

        for (int i = 0; i < times.length(); i++) {
            String textoData = times.getString(i);
            double temperatura = temperatures.getDouble(i);
            double precipitacao = precipitations.getDouble(i);
            double humidadeSolo = soilMoistures.optDouble(i) * 100.0;

            MeteorologiaDTO dto = new MeteorologiaDTO(temperatura, precipitacao, humidadeSolo, textoData);
            dtos.add(dto);
        }
        return dtos;
    }
}
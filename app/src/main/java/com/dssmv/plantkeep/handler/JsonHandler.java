package com.dssmv.plantkeep.handler;

import com.dssmv.plantkeep.dto.DetalheOpb;
import com.dssmv.plantkeep.dto.EspecieOpb;
import com.dssmv.plantkeep.dto.MeteorologiaDTO;
import com.dssmv.plantkeep.dto.PlantaDTO;
import com.dssmv.plantkeep.dto.RegistoDiarioDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonHandler {

    private JsonHandler() {}

    // Converte uma PlantaDTO para JSON
    public static String serializePlantaDTO2Json(PlantaDTO dto) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("nome", dto.getNome());
        o.put("especie", dto.getEspecie());
        o.put("pid", dto.getPid());
        o.put("ultimaRega", dto.getUltimaRega());

        JSONArray arrDiario = new JSONArray();
        List<RegistoDiarioDTO> diario = dto.getDiario();
        if (diario != null) {
            for (int i = 0; i < diario.size(); i++) {
                RegistoDiarioDTO r = diario.get(i);
                if (r == null) {
                    continue;
                }
                JSONObject regObj = new JSONObject();
                regObj.put("texto", r.getTexto());
                regObj.put("dataHora", r.getDataHora());
                regObj.put("fotoBase64", r.getFotoBase64() != null ? r.getFotoBase64() : "");
                arrDiario.put(regObj);
            }
        }
        o.put("diario", arrDiario);
        return o.toString();
    }

    // Converte o JSON do RestDB para PlantaDTO
    public static List<PlantaDTO> deSerializeJson2ListPlantaDTO(String json) throws JSONException {
        List<PlantaDTO> dtos = new ArrayList<>();
        JSONArray arr = new JSONArray(json);

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            List<RegistoDiarioDTO> diario = new ArrayList<>();
            JSONArray arrDiario = o.optJSONArray("diario");

            if (arrDiario != null) {
                for (int j = 0; j < arrDiario.length(); j++) {
                    JSONObject regObj = arrDiario.optJSONObject(j);
                    if (regObj != null) {
                        String texto = regObj.optString("texto", "");
                        long dataHora = regObj.optLong("dataHora", System.currentTimeMillis());
                        String fotoBase64 = regObj.optString("fotoBase64", "");
                        if (!texto.isEmpty()) {
                            diario.add(new RegistoDiarioDTO(texto, dataHora, fotoBase64));
                        }
                    }
                }
            }

            dtos.add(new PlantaDTO(
                    o.optString("_id", null),
                    o.optString("nome", ""),
                    o.optString("especie", ""),
                    o.optString("pid", ""),
                    o.optLong("ultimaRega", 0L),
                    diario
            ));
        }
        return dtos;
    }

    // Converte o JSON do Open-Meteo para MeteorologiaDTO
    public static List<MeteorologiaDTO> deSerializeJson2ListMeteorologiaDTO(String json) throws JSONException {
        List<MeteorologiaDTO> dtos = new ArrayList<>();
        JSONObject hourlyData = new JSONObject(json).getJSONObject("hourly");

        JSONArray times = hourlyData.getJSONArray("time");
        JSONArray temperatures = hourlyData.getJSONArray("temperature_2m");
        JSONArray precipitations = hourlyData.getJSONArray("precipitation");
        JSONArray soilMoistures = hourlyData.getJSONArray("soil_moisture_1_to_3cm");

        for (int i = 0; i < times.length(); i++) {
            if (temperatures.isNull(i) || soilMoistures.isNull(i)) {
                continue;
            }
            String textoData = times.getString(i);
            double temperatura = temperatures.getDouble(i);
            double precipitacao = Math.max(0.0, precipitations.optDouble(i, 0.0));
            double humidadeSolo = soilMoistures.optDouble(i, 0.0) * 100.0;

            dtos.add(new MeteorologiaDTO(temperatura, precipitacao, humidadeSolo, textoData));
        }
        return dtos;
    }

    // Lê o token OAuth e o tempo de validade da resposta do OpenPlantbook
    public static String[] deSerializeOpbToken(String json) throws JSONException {
        JSONObject o = new JSONObject(json);
        String token = o.getString("access_token");
        long expires = o.optLong("expires_in", 3600);
        return new String[]{token, String.valueOf(expires)};
    }

    // Converte os resultados da pesquisa do OpenPlantbook para lista de EspecieOpb
    public static List<EspecieOpb> deSerializeJson2ListEspecieOpb(String json) throws JSONException {
        JSONArray results = new JSONObject(json).getJSONArray("results");
        List<EspecieOpb> lista = new ArrayList<>();

        for (int i = 0; i < results.length(); i++) {
            JSONObject r = results.getJSONObject(i);
            EspecieOpb e = new EspecieOpb();
            e.setPid(r.optString("pid", ""));
            e.setDisplayPid(r.optString("display_pid", ""));
            e.setAlias(r.optString("alias", ""));
            e.setCategory(r.optString("category", ""));
            lista.add(e);
        }
        return lista;
    }

    // Converte os detalhes de uma espécie do OpenPlantbook para DetalheOpb
    public static DetalheOpb deSerializeJson2DetalheOpb(String json) throws JSONException {
        JSONObject o = new JSONObject(json);
        DetalheOpb d = new DetalheOpb();
        d.setCategory(o.optString("category", ""));
        d.setOrigin(o.optString("origin", ""));
        d.setImageUrl(o.optString("image_url", ""));
        d.setMinTemp(num(o, "min_temp"));
        d.setMaxTemp(num(o, "max_temp"));
        d.setMinEnvHumid(num(o, "min_env_humid"));
        d.setMaxEnvHumid(num(o, "max_env_humid"));
        d.setMinLightLux(num(o, "min_light_lux"));
        d.setMaxLightLux(num(o, "max_light_lux"));
        d.setMinSoilMoist(num(o, "min_soil_moist"));
        d.setMaxSoilMoist(num(o, "max_soil_moist"));
        d.setMinSoilEc(num(o, "min_soil_ec"));
        d.setMaxSoilEc(num(o, "max_soil_ec"));
        return d;
    }

    // Devolve o inteiro do campo JSON ou null se não existir
    private static Integer num(JSONObject o, String campo) {
        return o.isNull(campo) ? null : o.optInt(campo);
    }
}
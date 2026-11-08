package com.dssmv.plantkeep.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.dssmv.plantkeep.dto.MeteorologiaDTO;
import com.dssmv.plantkeep.dto.PlantaDTO;
import com.dssmv.plantkeep.dto.RegistoDiarioDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.List;

/**
 * Testes unitários para a desserialização de dados (JsonHandler):
 * - Plantas e Diário (RestDB)
 * - Previsões Meteorológicas (Open-Meteo)
 */
public class JsonHandlerTest {

    // =========================================================================
    // TESTES DE DESSERIALIZAÇÃO DE PLANTAS (RestDB)
    // =========================================================================

    @Test
    public void deSerializeJson_comPlantasEDiarioValidos_deveCriarListaComAtributosCompletos() throws JSONException {
        String json = "[\n" +
                "  {\n" +
                "    \"_id\": \"planta_001\",\n" +
                "    \"nome\": \"Ficus Elastica\",\n" +
                "    \"especie\": \"Ficus elastica\",\n" +
                "    \"pid\": \"ficus-elastica\",\n" +
                "    \"ultimaRega\": 1700000000000,\n" +
                "    \"diario\": [\n" +
                "      {\n" +
                "        \"texto\": \"Regada hoje\",\n" +
                "        \"dataHora\": 1700000100000,\n" +
                "        \"fotoBase64\": \"base64stringexemplo\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        List<PlantaDTO> resultado = JsonHandler.deSerializeJson2ListPlantaDTO(json);

        assertNotNull("A lista de plantas não deve ser nula", resultado);
        assertEquals("Deve conter exatamente 1 planta", 1, resultado.size());

        PlantaDTO p = resultado.get(0);
        assertEquals("planta_001", p.getId());
        assertEquals("Ficus Elastica", p.getNome());
        assertEquals("Ficus elastica", p.getEspecie());
        assertEquals("ficus-elastica", p.getPid());
        assertEquals(1700000000000L, p.getUltimaRega());

        assertNotNull("O diário não deve ser nulo", p.getDiario());
        assertEquals("O diário deve conter 1 registo", 1, p.getDiario().size());

        RegistoDiarioDTO r = p.getDiario().get(0);
        assertEquals("Regada hoje", r.getTexto());
        assertEquals(1700000100000L, r.getDataHora());
        assertEquals("base64stringexemplo", r.getFotoBase64());
    }

    @Test
    public void deSerializeJson_plantaSemCampoDiario_deveRetornarListaDiarioVaziaNaoNula() throws JSONException {
        String json = "[{\"_id\":\"002\",\"nome\":\"Espada de São Jorge\",\"especie\":\"Sansevieria\"}]";

        List<PlantaDTO> resultado = JsonHandler.deSerializeJson2ListPlantaDTO(json);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        PlantaDTO p = resultado.get(0);
        assertNotNull("Diário deve ser inicializado mesmo que ausente no JSON", p.getDiario());
        assertTrue("Diário deve estar vazio", p.getDiario().isEmpty());
    }

    @Test
    public void deSerializeJson_registoDiarioComTextoVazio_deveFiltrarRegisto() throws JSONException {
        String json = "[\n" +
                "  {\n" +
                "    \"_id\": \"003\",\n" +
                "    \"nome\": \"Suculenta\",\n" +
                "    \"diario\": [\n" +
                "      {\"texto\": \"\", \"dataHora\": 1700000000000},\n" +
                "      {\"texto\": \"Nova folha\", \"dataHora\": 1700000200000}\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        List<PlantaDTO> resultado = JsonHandler.deSerializeJson2ListPlantaDTO(json);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        List<RegistoDiarioDTO> diario = resultado.get(0).getDiario();
        assertEquals("Registo com texto vazio deve ser ignorado", 1, diario.size());
        assertEquals("Nova folha", diario.get(0).getTexto());
    }

    @Test
    public void deSerializeJson_arrayVazio_deveRetornarListaVazia() throws JSONException {
        List<PlantaDTO> resultado = JsonHandler.deSerializeJson2ListPlantaDTO("[]");
        assertNotNull(resultado);
        assertTrue("A lista deve estar vazia quando o JSON for []", resultado.isEmpty());
    }

    @Test(expected = JSONException.class)
    public void deSerializeJson_jsonInvalido_deveLancarJSONException() throws JSONException {
        JsonHandler.deSerializeJson2ListPlantaDTO("{ isto_nao_e_um_array: true }");
    }

    @Test
    public void deSerializeJson_grandeVolumeDePlantas_deveDesserializarTodasCorretamente() throws JSONException {
        int total = 500;
        JSONArray array = new JSONArray();
        for (int i = 1; i <= total; i++) {
            JSONObject obj = new JSONObject();
            obj.put("_id", "id_" + i);
            obj.put("nome", "Planta " + i);
            obj.put("especie", "Especie " + i);
            obj.put("pid", "pid_" + i);
            obj.put("ultimaRega", 1700000000000L);
            obj.put("diario", new JSONArray());
            array.put(obj);
        }

        List<PlantaDTO> resultado = JsonHandler.deSerializeJson2ListPlantaDTO(array.toString());

        assertNotNull(resultado);
        assertEquals("Deve ter processado todas as 500 plantas", total, resultado.size());
        assertEquals("Planta 1", resultado.get(0).getNome());
        assertEquals("Planta " + total, resultado.get(total - 1).getNome());
    }

    // =========================================================================
    // TESTES DE DESSERIALIZAÇÃO METEOROLÓGICA (Open-Meteo)
    // =========================================================================

    @Test
    public void deSerializeJsonMeteo_comDadosValidos_deveConverterHorasEHumidadePercentual() throws JSONException {
        String json = "{\n" +
                "  \"hourly\": {\n" +
                "    \"time\": [\"2026-11-07T12:00\", \"2026-11-07T13:00\"],\n" +
                "    \"temperature_2m\": [18.5, 21.0],\n" +
                "    \"precipitation\": [0.0, 1.5],\n" +
                "    \"soil_moisture_1_to_3cm\": [0.28, 0.35]\n" +
                "  }\n" +
                "}";

        List<MeteorologiaDTO> resultado = JsonHandler.deSerializeJson2ListMeteorologiaDTO(json);

        assertNotNull("A lista meteorológica não deve ser nula", resultado);
        assertEquals("Deve conter exatamente 2 horas de previsão", 2, resultado.size());

        MeteorologiaDTO hora1 = resultado.get(0);
        assertEquals("2026-11-07T12:00", hora1.getDataHora());
        assertEquals(18.5, hora1.getTemperatura(), 0.001);
        assertEquals(0.0, hora1.getPrecipitacao(), 0.001);
        assertEquals("Humidade do solo deve ser convertida de m³/m³ para percentagem (*100)", 28.0, hora1.getHumidadeSolo(), 0.001);

        MeteorologiaDTO hora2 = resultado.get(1);
        assertEquals("2026-11-07T13:00", hora2.getDataHora());
        assertEquals(21.0, hora2.getTemperatura(), 0.001);
        assertEquals(1.5, hora2.getPrecipitacao(), 0.001);
        assertEquals(35.0, hora2.getHumidadeSolo(), 0.001);
    }

    @Test
    public void deSerializeJsonMeteo_comValoresNulos_deveIgnorarRegistosIncompletos() throws JSONException {
        String json = "{\n" +
                "  \"hourly\": {\n" +
                "    \"time\": [\"2026-11-07T10:00\", \"2026-11-07T11:00\", \"2026-11-07T12:00\"],\n" +
                "    \"temperature_2m\": [19.0, null, 20.0],\n" +
                "    \"precipitation\": [0.0, 0.0, 0.0],\n" +
                "    \"soil_moisture_1_to_3cm\": [0.25, 0.30, null]\n" +
                "  }\n" +
                "}";

        List<MeteorologiaDTO> resultado = JsonHandler.deSerializeJson2ListMeteorologiaDTO(json);

        assertNotNull(resultado);
        assertEquals("Apenas a hora com todos os valores preenchidos deve ser incluída", 1, resultado.size());
        assertEquals("2026-11-07T10:00", resultado.get(0).getDataHora());
        assertEquals(19.0, resultado.get(0).getTemperatura(), 0.001);
        assertEquals(25.0, resultado.get(0).getHumidadeSolo(), 0.001);
    }

    @Test
    public void deSerializeJsonMeteo_arraysVazios_deveRetornarListaVazia() throws JSONException {
        String json = "{\n" +
                "  \"hourly\": {\n" +
                "    \"time\": [],\n" +
                "    \"temperature_2m\": [],\n" +
                "    \"precipitation\": [],\n" +
                "    \"soil_moisture_1_to_3cm\": []\n" +
                "  }\n" +
                "}";

        List<MeteorologiaDTO> resultado = JsonHandler.deSerializeJson2ListMeteorologiaDTO(json);

        assertNotNull(resultado);
        assertTrue("A lista deve estar vazia quando os arrays forem vazios", resultado.isEmpty());
    }

    @Test(expected = JSONException.class)
    public void deSerializeJsonMeteo_semObjetoHourly_deveLancarJSONException() throws JSONException {
        JsonHandler.deSerializeJson2ListMeteorologiaDTO("{\"latitude\": 41.15}");
    }

    @Test
    public void deSerializeJsonMeteo_previsaoSeteDias_deveProcessar168Horas() throws JSONException {
        int totalHoras = 168; // 7 dias * 24 horas
        JSONArray times = new JSONArray();
        JSONArray temps = new JSONArray();
        JSONArray precips = new JSONArray();
        JSONArray soils = new JSONArray();

        for (int i = 0; i < totalHoras; i++) {
            times.put(String.format("2026-11-%02dT%02d:00", (i / 24) + 1, i % 24));
            temps.put(15.0 + (i % 10));
            precips.put(0.0);
            soils.put(0.30);
        }

        JSONObject hourly = new JSONObject();
        hourly.put("time", times);
        hourly.put("temperature_2m", temps);
        hourly.put("precipitation", precips);
        hourly.put("soil_moisture_1_to_3cm", soils);

        JSONObject root = new JSONObject();
        root.put("hourly", hourly);

        List<MeteorologiaDTO> resultado = JsonHandler.deSerializeJson2ListMeteorologiaDTO(root.toString());

        assertNotNull(resultado);
        assertEquals("Deve ter processado as 168 horas completas", totalHoras, resultado.size());
        assertEquals("2026-11-01T00:00", resultado.get(0).getDataHora());
        assertEquals(30.0, resultado.get(0).getHumidadeSolo(), 0.001);
    }
}

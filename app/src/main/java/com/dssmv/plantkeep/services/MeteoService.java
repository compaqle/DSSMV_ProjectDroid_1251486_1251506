package com.dssmv.plantkeep.services;

import android.util.Log;
import com.dssmv.plantkeep.model.Meteorologia;
import com.dssmv.plantkeep.model.MeteorologiaContainer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 Serviço da meteorologia. Faz o pedido HTTP e atualiza o MeteorologiaContainer.
 */
public class MeteoService {

    private static final String METEO_URL = "https://api.open-meteo.com/v1/forecast";

    private MeteoService() {} // Só métodos estáticos, igual ao PlantaService

    /** Vai à API buscar as previsões e guarda-as no MeteorologiaContainer. */
    public static void carregarMeteorologia(double latitude, double longitude) throws Exception {
        List<Meteorologia> previsoes = obterDadosMeteorologicos(latitude, longitude);
        MeteorologiaContainer.getInstance().setPrevisaoHoraria(previsoes);
    }

    /** Devolve a lista de previsões horárias da API Open-Meteo (GET). */
    public static List<Meteorologia> obterDadosMeteorologicos(double latitude, double longitude) throws Exception {
        HttpURLConnection connection = null;
        try {
            String urlString = METEO_URL + "?latitude=" + latitude +
                    "&longitude=" + longitude +
                    "&hourly=temperature_2m,precipitation";

            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject hourlyData = getHourlyData(connection);

                JSONArray times = hourlyData.getJSONArray("time");
                JSONArray temperatures = hourlyData.getJSONArray("temperature_2m");
                JSONArray precipitations = hourlyData.getJSONArray("precipitation");

                List<Meteorologia> listaPrevisao = new ArrayList<>();
                SimpleDateFormat formatador = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ROOT);

                for (int i = 0; i < times.length(); i++) {
                    String textoData = times.getString(i);
                    double temperatura = temperatures.getDouble(i);
                    double precipitacao = precipitations.getDouble(i);

                    Date dataHora = formatador.parse(textoData);

                    Meteorologia registoHora = new Meteorologia(temperatura, precipitacao, dataHora);
                    listaPrevisao.add(registoHora);
                }

                return listaPrevisao;
            } else {
                Log.e("PlantKeep", "Erro na chamada à API do Open Meteo. Código HTTP: " + responseCode);
                throw new IOException("Erro HTTP: " + responseCode);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static JSONObject getHourlyData(HttpURLConnection connection) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getJSONObject("hourly");
    }
}
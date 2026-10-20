package com.dssmv.plantkeep.services;

import android.util.Log;
import com.dssmv.plantkeep.model.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class MeteoService {
    private static final String MeteoURL = "https://api.open-meteo.com/v1/forecast";

    public MeteorologiaContainer obterDadosMeteorologicos(double latitude, double longitude) {
        try {
            // Pede a previsão de hora em hora
            String urlString = MeteoURL + "?latitude=" + latitude +
                    "&longitude=" + longitude +
                    "&hourly=temperature_2m,precipitation";

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject hourlyData = getHourlyData(connection);

                // Extrai os 3 vetores do JSON
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

                return new MeteorologiaContainer(listaPrevisao);

            } else {Log.e("PlantKeep", "Erro na chamada à API do Open Meteo. Código HTTP: " + responseCode);}

        } catch (Exception e) {Log.e("PlantKeep", "Ocorreu um erro ao obter a meteorologia: " + e.getMessage());}
        return null;
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


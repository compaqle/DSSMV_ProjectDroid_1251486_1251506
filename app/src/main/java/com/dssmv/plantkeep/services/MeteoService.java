package com.dssmv.plantkeep.services;

import com.dssmv.plantkeep.model.Meteorologia;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;


public class MeteoService {
    private static final String MeteoURL = "https://api.open-meteo.com/v1/forecast";

    public Meteorologia obterDadosMeteorologicos(double latitude, double longitude) {
        try {
            //Criar o URL
            String urlString = MeteoURL + "?latitude=" + latitude +
                    "&longitude=" + longitude +
                    "&current=temperature_2m,precipitation";

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            //Extrai os valores em JSON e coloca em variaveis
            if (responseCode == HttpURLConnection.HTTP_OK) {

                JSONObject currentData = getCurrentData(connection);

                double temperatura = currentData.getDouble("temperature_2m");
                double precipitacao = currentData.getDouble("precipitation");

                Date dataHoraAtual = new Date();

                return new Meteorologia(temperatura, precipitacao, dataHoraAtual);

            } else {
                System.out.println("Erro na chamada à API do Open Meteo. Código HTTP: " + responseCode);
            }

        } catch (Exception e) {
            System.out.println("Ocorreu um erro ao obter a meteorologia: " + e.getMessage());

        }
        return null;
    }

    private static JSONObject getCurrentData(HttpURLConnection connection) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());

        return jsonResponse.getJSONObject("current");
    }
}



package com.dssmv.plantkeep.handler;

import com.dssmv.plantkeep.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NetworkHandler {

    public static final String BASE = "https://" + BuildConfig.RESTDB_DBNAME + ".restdb.io/rest";
    private static final String APIKEY = BuildConfig.RESTDB_APIKEY;
    private static final int TIMEOUT_MS = 15000;

    private NetworkHandler() {}   // só estáticos, nunca instaciar

    /** GET: le os dados da url */
    public static String getDataInStringFromUrl(String url) throws IOException {
        return request("GET", url, null);
    }

    /** POST: manda um documento (body = JSON) */
    public static String addDataInStringFromUrl(String url, String json) throws IOException {
        return request("POST", url, json);
    }

    /** PUT: substitui um documento. */
    public static String updateDataInStringFromUrl(String url, String json) throws IOException {
        return request("PUT", url, json);
    }

    /** DELETE: apaga um documento. */
    public static String deleteDataInStringFromUrl(String url) throws IOException {
        return request("DELETE", url, null);
    }

// method (Get, Post etc); url; body (json que se vai enciar)
    private static String request(String method, String url, String body) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("x-apikey", APIKEY);
            conn.setRequestProperty("Accept", "application/json");

            if (body != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }

            int code = conn.getResponseCode();
            InputStream is;
            if (code >= 200 && code < 300) {
                // Sucesso (2xx): a resposta vem no getInputStream()
                is = conn.getInputStream();
            } else {
                // Falhou (4xx/5xx) o servidor devovle o erro aqui
                is = conn.getErrorStream();
            }
            String resp = is == null ? "" : readAll(is);

            if (code < 200 || code >= 300) {
                throw new IOException("restdb HTTP " + code + ": " + resp);
            }
            return resp;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String readAll(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}

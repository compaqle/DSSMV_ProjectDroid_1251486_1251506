package com.dssmv.plantkeep.dbservice;

import com.dssmv.plantkeep.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class RestDBClient {
    private static final String BASE = "https://" + BuildConfig.RESTDB_DBNAME + ".restdb.io/rest";
    private static final String APIKEY = BuildConfig.RESTDB_APIKEY;
    private static final int TIMEOUT_MS = 15000;

    private final String collection;

    public RestDBClient(String collection) {
        this.collection = collection;
    }

    /** GET /rest/<collection> — lista todos os documentos (array JSON). */
    public String list() throws IOException {
        return request("GET", collection, null);
    }

    /** POST /rest/<collection> — cria um documento (body = JSON). Devolve o JSON criado (com _id). */
    public String create(String json) throws IOException {
        return request("POST", collection, json);
    }

    /** GET /rest/<collection>/<id> — lê um documento. */
    public String get(String id) throws IOException {
        return request("GET", collection + "/" + id, null);
    }

    /** PUT /rest/<collection>/<id> — substitui um documento. */
    public String update(String id, String json) throws IOException {
        return request("PUT", collection + "/" + id, json);
    }

    /** DELETE /rest/<collection>/<id> — apaga um documento. */
    public String delete(String id) throws IOException {
        return request("DELETE", collection + "/" + id, null);
    }

    private String request(String method, String path, String body) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(BASE + "/" + path).openConnection();
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
            InputStream is = (code >= 200 && code < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();
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

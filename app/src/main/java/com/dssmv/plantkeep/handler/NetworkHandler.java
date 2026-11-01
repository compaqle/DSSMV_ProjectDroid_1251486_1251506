package com.dssmv.plantkeep.handler;

import com.dssmv.plantkeep.BuildConfig;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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

    private NetworkHandler() {}   // só estáticos, nunca instanciar

    /** GET: lê os dados da url */
    public static String getDataInStringFromUrl(String url) throws IOException {
        return request("GET", url, null, "application/json", null);
    }

    /** GET autenticado com Bearer Token (ex: OpenPlantbook) */
    public static String getDataWithBearerToken(String url, String token) throws IOException {
        return request("GET", url, null, "application/json", "Bearer " + token);
    }

    /** POST: manda um documento (body = JSON) */
    public static String addDataInStringFromUrl(String url, String json) throws IOException {
        return request("POST", url, json, "application/json", null);
    }

    /** POST form-urlencoded (ex: OAuth token do OpenPlantbook) */
    public static String postFormUrlEncoded(String url, String formBody) throws IOException {
        return request("POST", url, formBody, "application/x-www-form-urlencoded", null);
    }

    /** PUT: substitui um documento. */
    public static String updateDataInStringFromUrl(String url, String json) throws IOException {
        return request("PUT", url, json, "application/json", null);
    }

    /** DELETE: apaga um documento. */
    public static String deleteDataInStringFromUrl(String url) throws IOException {
        return request("DELETE", url, null, "application/json", null);
    }

    /** Descarrega os bytes de um URL (ou devolve null se falhar). Puro: sem Android. */
    public static byte[] getBytesFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (InputStream in = conn.getInputStream()) {
                return readBytes(in);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static byte[] readBytes(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return bos.toByteArray();
    }

    private static String request(String method, String url, String body,
                                  String contentType, String authorization) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);

            if (url.startsWith(BASE)) {
                conn.setRequestProperty("x-apikey", APIKEY);
            }
            if (authorization != null && !authorization.isEmpty()) {
                conn.setRequestProperty("Authorization", authorization);
            }
            conn.setRequestProperty("Accept", "application/json");

            if (body != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", contentType);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String resp = is == null ? "" : readAll(is);

            if (code < 200 || code >= 300) {
                throw new IOException("HTTP " + code + ": " + resp);
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
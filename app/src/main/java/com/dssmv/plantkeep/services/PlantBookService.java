package com.dssmv.plantkeep.services;

import com.dssmv.plantkeep.BuildConfig;
import com.dssmv.plantkeep.dto.EspecieOpb;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço do OpenPlantbook (https://open.plantbook.io).
 * Obtém o token OAuth (cacheado na memória ~1h) e pesquisa espécies.
 */
public class PlantBookService {

    private static final String BASE = "https://open.plantbook.io/api/v1";

    private static String token;
    private static long tokenExpira;

    private PlantBookService() {
    }

    /** Token OAuth client_credentials, cacheado na memória até expirar. */
    private static synchronized String getToken() throws Exception {
        if (token != null && System.currentTimeMillis() < tokenExpira) {
            return token;
        }

        String body = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(BuildConfig.OPB_CLIENT_ID, "UTF-8")
                + "&client_secret=" + URLEncoder.encode(BuildConfig.OPB_CLIENT_SECRET, "UTF-8");

        HttpURLConnection conn = (HttpURLConnection) new URL(BASE + "/token/").openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.getOutputStream().write(body.getBytes("UTF-8"));

        int code = conn.getResponseCode();
        String resp = ler(conn);
        if (code != 200) {
            throw new RuntimeException("OpenPlantbook token: HTTP " + code + " " + resp);
        }

        JSONObject o = new JSONObject(resp);
        token = o.getString("access_token");
        long expires = o.optLong("expires_in", 3600);
        tokenExpira = System.currentTimeMillis() + (expires - 60) * 1000;
        return token;
    }

    /** Pesquisa espécies pelo nome/alias. Devolve a lista encontrada. */
    public static List<EspecieOpb> pesquisar(String termo) throws Exception {
        String url = BASE + "/plant/search?alias="
                + URLEncoder.encode(termo, "UTF-8") + "&limit=20";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + getToken());

        int code = conn.getResponseCode();
        String resp = ler(conn);
        if (code != 200) {
            throw new RuntimeException("OpenPlantbook search: HTTP " + code + " " + resp);
        }

        JSONObject root = new JSONObject(resp);
        JSONArray results = root.getJSONArray("results");
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

        // a pesquisa não devolve a imagem; obtém-na do detail de cada espécie (só se existir)
        for (EspecieOpb e : lista) {
            e.setImageUrl(obterImagem(e.getPid()));
        }
        return lista;
    }

    /** Devolve o image_url da espécie (vazio se não existir ou der erro). */
    private static String obterImagem(String pid) {
        if (pid == null || pid.isEmpty()) {
            return "";
        }
        try {
            String url = BASE + "/plant/detail/" + encPath(pid) + "/";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + getToken());
            int code = conn.getResponseCode();
            String resp = ler(conn);
            if (code != 200) {
                return "";
            }
            return new JSONObject(resp).optString("image_url", "");
        } catch (Exception e) {
            return "";
        }
    }

    /** Codifica para caminho URL (espaço como %20, não como +). */
    private static String encPath(String s) throws Exception {
        return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
    }

    private static String ler(HttpURLConnection conn) throws IOException {
        InputStream in = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (in == null) {
            return "";
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String linha;
        while ((linha = r.readLine()) != null) {
            sb.append(linha);
        }
        return sb.toString();
    }
}

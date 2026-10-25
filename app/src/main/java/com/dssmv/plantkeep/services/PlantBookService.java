package com.dssmv.plantkeep.services;

import com.dssmv.plantkeep.BuildConfig;
import com.dssmv.plantkeep.dto.DetalheOpb;
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

        // filtra ruído: mantém o topo da API + quem tem o termo como palavra inteira no nome
        lista = filtraRuido(lista, termo);

        // a pesquisa não devolve imagem nem nomes comuns; obtém-nos do detail de cada espécie
        for (EspecieOpb e : lista) {
            preencherDetalhe(e);
        }
        return lista;
    }

    /**
     * Remove ruído de pesquisa fuzzy. O OpenPlantbook faz substring-match,
     * por isso "potato" também devolve "Agave potatorum" (o termo aparece lá
     * dentro por sorte). Regra: mantém sempre o 1º resultado (o match principal
     * da API) e ainda todos os que têm o termo como palavra inteira no nome;
     * esconde os restantes.
     */
    private static List<EspecieOpb> filtraRuido(List<EspecieOpb> lista, String termo) {
        if (lista.size() <= 1) {
            return lista;
        }
        String q = termo == null ? "" : termo.trim().toLowerCase();
        List<EspecieOpb> out = new ArrayList<>();
        out.add(lista.get(0)); // match principal da API fica sempre
        for (int i = 1; i < lista.size(); i++) {
            EspecieOpb e = lista.get(i);
            if (temPalavra(q, e.getDisplayPid()) || temPalavra(q, e.getAlias())) {
                out.add(e);
            }
        }
        return out;
    }

    /** True se q aparece como palavra inteira (token) no nome. */
    private static boolean temPalavra(String q, String nome) {
        if (q.isEmpty() || nome == null) {
            return false;
        }
        String[] tokens = nome.toLowerCase().split("[^a-z0-9]+");
        for (String t : tokens) {
            if (t.equals(q)) {
                return true;
            }
        }
        return false;
    }

    /** Preenche imagem e nomes comuns da espécie a partir do detail (vazio se der erro). */
    private static void preencherDetalhe(EspecieOpb e) {
        try {
            String url = BASE + "/plant/detail/" + encPath(e.getPid()) + "/";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + getToken());
            int code = conn.getResponseCode();
            String resp = ler(conn);
            if (code != 200) {
                return;
            }
            JSONObject o = new JSONObject(resp);
            e.setImageUrl(o.optString("image_url", ""));

            List<String> comuns = new ArrayList<>();
            JSONArray al = o.optJSONArray("common_names");
            if (al != null) {
                for (int i = 0; i < al.length(); i++) {
                    JSONObject n = al.optJSONObject(i);
                    if (n == null) {
                        continue;
                    }
                    String a = n.optString("name", "").trim();
                    if (!a.isEmpty() && !comuns.contains(a)) {
                        comuns.add(a);
                    }
                }
            }
            e.setAliasesComuns(comuns);
        } catch (Exception ex) {
            // sem detail não bloqueamos a lista
        }
    }

    /** Devolve os dados completos de uma espécie (cuidados + imagem). */
    public static DetalheOpb detalhe(String pid) throws Exception {
        String url = BASE + "/plant/detail/" + encPath(pid) + "/";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + getToken());

        int code = conn.getResponseCode();
        String resp = ler(conn);
        if (code != 200) {
            throw new RuntimeException("OpenPlantbook detail: HTTP " + code + " " + resp);
        }

        JSONObject o = new JSONObject(resp);
        DetalheOpb d = new DetalheOpb();
        d.setDisplayPid(o.optString("display_pid", ""));
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

    /** Lê um inteiro nullable: null se o campo não existir ou for null. */
    private static Integer num(JSONObject o, String campo) {
        return o.isNull(campo) ? null : o.optInt(campo);
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

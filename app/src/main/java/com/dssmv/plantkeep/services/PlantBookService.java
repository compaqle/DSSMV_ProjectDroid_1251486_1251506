package com.dssmv.plantkeep.services;

import com.dssmv.plantkeep.BuildConfig;
import com.dssmv.plantkeep.dto.DetalheOpb;
import com.dssmv.plantkeep.dto.EspecieOpb;
import com.dssmv.plantkeep.handler.JsonHandler;
import com.dssmv.plantkeep.handler.NetworkHandler;

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

        String resp = NetworkHandler.postFormUrlEncoded(BASE + "/token/", body);
        String[] tokenInfo = JsonHandler.deSerializeOpbToken(resp);
        token = tokenInfo[0];
        long expires = Long.parseLong(tokenInfo[1]);
        tokenExpira = System.currentTimeMillis() + (expires - 60) * 1000;
        return token;
    }

    /** Pesquisa espécies pelo nome/alias. Devolve a lista encontrada. */
    public static List<EspecieOpb> pesquisar(String termo) throws Exception {
        String url = BASE + "/plant/search?alias="
                + URLEncoder.encode(termo, "UTF-8") + "&limit=20";

        String resp = NetworkHandler.getDataWithBearerToken(url, getToken());
        List<EspecieOpb> lista = JsonHandler.deSerializeJson2ListEspecieOpb(resp);

        // filtra ruído: mantém o topo da API + quem tem o termo como palavra inteira no nome
        lista = filtraRuido(lista, termo);

        // a pesquisa não devolve imagem nem nomes comuns; obtém-nos do detail de cada espécie
        for (EspecieOpb e : lista) {
            preencherDetalhe(e);
        }
        return lista;
    }


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
            String resp = NetworkHandler.getDataWithBearerToken(url, getToken());
            JsonHandler.preencherDetalheEspecieOpb(resp, e);
        } catch (Exception ex) {
            // sem detail não bloqueamos a lista
        }
    }

    /** Devolve os dados completos de uma espécie (cuidados + imagem). */
    public static DetalheOpb detalhe(String pid) throws Exception {
        String url = BASE + "/plant/detail/" + encPath(pid) + "/";
        String resp = NetworkHandler.getDataWithBearerToken(url, getToken());
        return JsonHandler.deSerializeJson2DetalheOpb(resp);
    }

    /** Codifica para caminho URL (espaço como %20, não como +). */
    private static String encPath(String s) throws Exception {
        return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
    }
}
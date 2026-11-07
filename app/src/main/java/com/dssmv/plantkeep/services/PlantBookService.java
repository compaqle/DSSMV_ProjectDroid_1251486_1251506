package com.dssmv.plantkeep.services;

import com.dssmv.plantkeep.BuildConfig;
import com.dssmv.plantkeep.dto.DetalheOpb;
import com.dssmv.plantkeep.dto.EspecieOpb;
import com.dssmv.plantkeep.dto.Mapper;
import com.dssmv.plantkeep.handler.JsonHandler;
import com.dssmv.plantkeep.handler.NetworkHandler;
import com.dssmv.plantkeep.helper.Utils;
import com.dssmv.plantkeep.model.CuidadosEspecie;
import com.dssmv.plantkeep.model.Especie;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço do OpenPlantbook.
 * Obtém o token OAuth, pesquisa espécies e devolve models
 */
public class PlantBookService {

    private static final String BASE = Utils.OPB_BASE;
    private static String token;
    private static long tokenExpira;
    private static final Map<String, CuidadosEspecie> detalheCache = new HashMap<>();

    private PlantBookService() {
    }

    /** Token OAuth client_credentials, cacheado na memória até expirar */
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

    /** Pesquisa espécies pelo nome/alias. Devolve diretamente a lista de model Especie */
    public static List<Especie> pesquisar(String termo) throws Exception {
        String url = BASE + "/plant/search?alias="
                + URLEncoder.encode(termo, "UTF-8") + "&limit=20";

        String resp = NetworkHandler.getDataWithBearerToken(url, getToken());
        List<EspecieOpb> listaDtos = JsonHandler.deSerializeJson2ListEspecieOpb(resp);

        // filtra ruído: mantém o topo da API + quem tem o termo como palavra inteira no nome
        listaDtos = filtraRuido(listaDtos, termo);

        return Mapper.paraListaEspecies(listaDtos);
    }

    private static List<EspecieOpb> filtraRuido(List<EspecieOpb> lista, String termo) {
        if (lista.size() <= 1) {
            return lista;
        }
        String q = (termo == null) ? "" : termo.trim().toLowerCase();
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

    /** True se q aparece como palavra inteira (token) no nome */
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

    /** Devolve os dados completos de cuidados de uma espécie. Com cache por pid. */
    public static CuidadosEspecie detalhe(String pid) throws Exception {
        CuidadosEspecie emCache = detalheCache.get(pid);
        if (emCache != null) {
            return emCache;
        }
        String url = BASE + "/plant/detail/" + encPath(pid) + "/";
        String resp = NetworkHandler.getDataWithBearerToken(url, getToken());
        DetalheOpb dto = JsonHandler.deSerializeJson2DetalheOpb(resp);
        CuidadosEspecie cuidados = Mapper.paraModelo(dto);
        if (cuidados != null) {
            detalheCache.put(pid, cuidados);
        }
        return cuidados;
    }

    /** Descarrega os bytes de uma imagem de planta. */
    public static byte[] obterImagemBytes(String url) {
        return NetworkHandler.getBytesFromUrl(url);
    }

    /** Codifica para caminho URL (espaço como %20, não como +). */
    private static String encPath(String s) throws Exception {
        return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
    }
}
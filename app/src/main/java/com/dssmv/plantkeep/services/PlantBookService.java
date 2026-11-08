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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço do OpenPlantbook.
 * Obtém o token OAuth, pesquisa espécies e devolve models
 */
public class PlantBookService {

    private static final String BASE = Utils.OPB_BASE;

    private static String token;
    private static long tokenExpira;
    private static final Map<String, CuidadosEspecie> detalheCache = new ConcurrentHashMap<>();

    private PlantBookService() {}

    // Obtém o token OAuth e guarda-o em memória até expirar
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

    // Pesquisa espécies pelo nome ou apelido e devolve a lista de modelos
    public static List<Especie> pesquisar(String termo) throws Exception {
        if (termo == null || termo.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String termoLimpo = normalizarTexto(termo);
        String queryApi = termoLimpo.isEmpty() ? termo.trim() : termoLimpo;
        String url = BASE + "/plant/search?alias="
                + URLEncoder.encode(queryApi, "UTF-8") + "&limit=20";
        String resp = NetworkHandler.getDataWithBearerToken(url, getToken());
        List<EspecieOpb> listaDtos = JsonHandler.deSerializeJson2ListEspecieOpb(resp);
        listaDtos = filtraRuido(listaDtos, termoLimpo);
        return Mapper.paraListaEspecies(listaDtos);
    }

    // Mantém o primeiro resultado e todos os que correspondem à pesquisa
    private static List<EspecieOpb> filtraRuido(List<EspecieOpb> lista, String termoNorm) {
        if (lista == null || lista.size() <= 1 || termoNorm == null || termoNorm.isEmpty()) {
            return lista;
        }
        List<EspecieOpb> out = new ArrayList<>();
        out.add(lista.get(0));
        for (int i = 1; i < lista.size(); i++) {
            EspecieOpb e = lista.get(i);
            if (temPalavra(termoNorm, e.getDisplayPid()) || temPalavra(termoNorm, e.getAlias())) {
                out.add(e);
            }
        }
        return out;
    }

    // Verifica se as palavras pesquisadas existem (ou são prefixo) no nome da espécie
    private static boolean temPalavra(String qNorm, String nome) {
        String nomeNorm = normalizarTexto(nome);
        if (qNorm == null || qNorm.isEmpty() || nomeNorm.isEmpty()) {
            return false;
        }
        String[] palavrasPesquisa = qNorm.split(" ");
        String[] tokensNome = nomeNorm.split(" ");

        for (int i = 0; i < palavrasPesquisa.length; i++) {
            String p = palavrasPesquisa[i];
            if (p.isEmpty()) {
                continue;
            }
            boolean encontrou = false;
            for (int j = 0; j < tokensNome.length; j++) {
                if (tokensNome[j].startsWith(p)) {
                    encontrou = true;
                    break;
                }
            }
            if (!encontrou) {
                return false;
            }
        }
        return true;
    }

    // Remove acentos, passa a minúsculas e limpa símbolos para facilitar a pesquisa
    private static String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase().replaceAll("[^a-z0-9]+", " ").trim();
    }

    // Devolve os cuidados de uma espécie usando cache por pid
    public static CuidadosEspecie detalhe(String pid) throws Exception {
        if (pid == null || pid.trim().isEmpty()) {
            return null;
        }
        pid = pid.trim();
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

    // Descarrega os bytes de uma imagem a partir do URL
    public static byte[] obterImagemBytes(String url) {
        return NetworkHandler.getBytesFromUrl(url);
    }

    // Codifica o texto para caminho de URL
    private static String encPath(String s) throws Exception {
        return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
    }
}
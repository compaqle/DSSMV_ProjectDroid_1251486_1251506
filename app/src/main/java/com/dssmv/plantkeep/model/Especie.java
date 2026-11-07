package com.dssmv.plantkeep.model;

import androidx.annotation.NonNull;


public class Especie {

    private final String pid;
    private final String nomeCientifico;
    private final String apelido;
    private final String categoria;
    private final String imagemUrl;

    public Especie(String pid, String nomeCientifico, String apelido, String categoria, String imagemUrl) {
        if (pid == null || pid.trim().isEmpty()) {
            throw new IllegalArgumentException("Especie: o identificador 'pid' não pode ser nulo nem vazio.");
        }
        this.pid = pid.trim();
        this.nomeCientifico = (nomeCientifico != null && !nomeCientifico.trim().isEmpty())
                ? nomeCientifico.trim()
                : this.pid;
        this.apelido = (apelido != null) ? apelido.trim() : "";
        this.categoria = (categoria != null) ? categoria.trim() : "";
        this.imagemUrl = (imagemUrl != null) ? imagemUrl.trim() : "";
    }

    public String getPid() {
        return pid;
    }

    public String getNomeCientifico() {
        return nomeCientifico;
    }

    public String getApelido() {
        return apelido;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    /**
     * Devolve o nome formatado para exibição na interface:
     * Nome Científico (Apelido), evitando duplicação caso o apelido seja igual ao nome científico.
     */
    public String getNomeFormatado() {
        String texto = (nomeCientifico != null && !nomeCientifico.isEmpty()) ? nomeCientifico : pid;
        if (apelido != null && !apelido.isEmpty() && !apelido.equalsIgnoreCase(texto)) {
            return texto + " (" + apelido + ")";
        }
        return texto;
    }

    @NonNull
    @Override
    public String toString() {
        return getNomeFormatado();
    }
}
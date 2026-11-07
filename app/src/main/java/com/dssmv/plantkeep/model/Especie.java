package com.dssmv.plantkeep.model;

import androidx.annotation.NonNull;

public class Especie {

    private final String pid;
    private final String nomeCientifico;
    private final String apelido;
    private final String imagemUrl;

    public Especie(String pid, String nomeCientifico, String apelido, String imagemUrl) {
        if (pid == null || pid.trim().isEmpty()) {
            throw new IllegalArgumentException("Especie: o identificador 'pid' não pode ser nulo nem vazio.");
        }
        this.pid = pid.trim();
        this.nomeCientifico = (nomeCientifico != null && !nomeCientifico.trim().isEmpty())
                ? nomeCientifico.trim()
                : this.pid;
        this.apelido = (apelido != null) ? apelido.trim() : "";
        this.imagemUrl = (imagemUrl != null) ? imagemUrl.trim() : "";
    }

    public String getPid() {
        return pid;
    }

    public String getNomeCientifico() {
        return nomeCientifico;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    // Devolve "Nome Científico (Apelido)" se o apelido for diferente do nome
    public String getNomeFormatado() {
        if (!apelido.isEmpty() && !apelido.equalsIgnoreCase(nomeCientifico)) {
            return nomeCientifico + " (" + apelido + ")";
        }
        return nomeCientifico;
    }

    @NonNull
    @Override
    public String toString() {
        return getNomeFormatado();
    }
}

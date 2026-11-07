package com.dssmv.plantkeep.model;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class RegistoDiario {

    private String texto;
    private Date dataHora;
    private String fotoBase64; // Fotografia codificada em Base64

    public RegistoDiario(String texto, Date dataHora, String fotoBase64) {
        setTexto(texto);
        setDataHora(dataHora);
        setFotoBase64(fotoBase64);
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Diário: o texto do registo não pode ser vazio");
        }
        this.texto = texto.trim();
    }

    public Date getDataHora() {
        return new Date(dataHora.getTime());
    }

    public void setDataHora(Date dataHora) {
        if (dataHora == null) {
            throw new IllegalArgumentException("Diário: a data não pode ser nula");
        }
        this.dataHora = new Date(dataHora.getTime());
    }

    public String getFotoBase64() {
        return fotoBase64;
    }

    public void setFotoBase64(String fotoBase64) {
        if (fotoBase64 != null) {
            this.fotoBase64 = fotoBase64;
        } else {
            this.fotoBase64 = "";
        }
    }

    @NonNull
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
        return sdf.format(dataHora) + "\n" + texto;
    }

    // Permite comparar registos pelo momento em que foram criados e pelo conteúdo
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistoDiario that = (RegistoDiario) o;
        return Objects.equals(dataHora, that.dataHora) && Objects.equals(texto, that.texto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto, dataHora);
    }
}

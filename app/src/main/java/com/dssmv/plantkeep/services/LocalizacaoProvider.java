package com.dssmv.plantkeep.services;

public interface LocalizacaoProvider {
    interface Callback {
        void onLocalizacao(double latitude, double longitude);
        void onErro(String msg);
    }

    void obterLocalizacao(Callback cb);
}
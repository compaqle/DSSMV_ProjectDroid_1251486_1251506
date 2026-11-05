package com.dssmv.plantkeep.services;

public interface LocalizacaoProvider {
    interface Callback {
        void onLocalizacao(double latitude, double longitude);
        void onErro(String msg);
    }

    void obterLocalizacao(Callback cb);

    /** Para qualquer pedido ou escuta de localização ativo e liberta os recursos do sistema */
    void pararObterLocalizacao();
}

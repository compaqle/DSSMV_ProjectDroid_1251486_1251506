package com.dssmv.plantkeep.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AvisoTest {

    private Planta planta;
    private MeteorologiaContainer meteo;

    @Before
    public void setUp() {
        planta = new Planta("Monstera", "Monstera deliciosa");
        meteo = MeteorologiaContainer.getInstance();
        meteo.setPrevisaoHoraria(new ArrayList<>());
    }

    private CuidadosEspecie criarCuidados(Integer minTemp, Integer maxTemp) {
        return new CuidadosEspecie(
                minTemp, maxTemp,
                null, null,
                null, null,
                null, null,
                null, null,
                "Categoria", "Origem", ""
        );
    }

    private List<Meteorologia> criarPrevisao(double... temperaturas) {
        List<Meteorologia> lista = new ArrayList<>();
        long baseTime = System.currentTimeMillis() + 3600000L; // 1 hora no futuro
        for (int i = 0; i < temperaturas.length; i++) {
            Date dataHora = new Date(baseTime + (i * 3600000L));
            lista.add(new Meteorologia(temperaturas[i], 30.0, dataHora));
        }
        return lista;
    }

    @Test
    public void verificarTemperatura_temperaturaAbaixoDoMinimo_deveGerarAvisoFrio() {
        CuidadosEspecie cuidados = criarCuidados(12, 28);
        meteo.setPrevisaoHoraria(criarPrevisao(15.0, 6.5, 18.0));

        List<Aviso> avisos = Aviso.verificarTemperatura(planta, cuidados, meteo);

        assertNotNull(avisos);
        assertEquals(1, avisos.size());

        Aviso aviso = avisos.get(0);
        assertEquals(Aviso.TipoAviso.TEMPERATURA_BAIXA, aviso.getTipo());
        assertEquals("Monstera", aviso.getNomePlanta());
        assertEquals("Frio previsto (6.5 °C, Min: 12 °C)", aviso.getMensagem());
    }

    @Test
    public void verificarTemperatura_temperaturaAcimaDoMaximo_deveGerarAvisoCalor() {
        CuidadosEspecie cuidados = criarCuidados(15, 26);
        meteo.setPrevisaoHoraria(criarPrevisao(20.0, 32.0, 22.0));

        List<Aviso> avisos = Aviso.verificarTemperatura(planta, cuidados, meteo);

        assertNotNull(avisos);
        assertEquals(1, avisos.size());

        Aviso aviso = avisos.get(0);
        assertEquals(Aviso.TipoAviso.TEMPERATURA_ALTA, aviso.getTipo());
        assertEquals("Calor previsto (32.0 °C, Max: 26 °C).", aviso.getMensagem());
    }

    @Test
    public void verificarTemperatura_temperaturaDentroDosLimites_naoDeveGerarAvisos() {
        CuidadosEspecie cuidados = criarCuidados(10, 30);
        meteo.setPrevisaoHoraria(criarPrevisao(18.0, 22.0, 25.0));

        List<Aviso> avisos = Aviso.verificarTemperatura(planta, cuidados, meteo);

        assertNotNull(avisos);
        assertTrue(avisos.isEmpty());
    }

    @Test
    public void verificarTemperatura_extremosDeFrioECalor_deveGerarAmbosAvisos() {
        CuidadosEspecie cuidados = criarCuidados(10, 25);
        meteo.setPrevisaoHoraria(criarPrevisao(4.0, 18.0, 31.0));

        List<Aviso> avisos = Aviso.verificarTemperatura(planta, cuidados, meteo);

        assertNotNull(avisos);
        assertEquals(2, avisos.size());
        assertEquals(Aviso.TipoAviso.TEMPERATURA_BAIXA, avisos.get(0).getTipo());
        assertEquals(Aviso.TipoAviso.TEMPERATURA_ALTA, avisos.get(1).getTipo());
    }

    @Test
    public void verificarTemperatura_cuidadosSemLimites_naoDeveGerarAvisos() {
        CuidadosEspecie cuidados = criarCuidados(null, null);
        meteo.setPrevisaoHoraria(criarPrevisao(2.0, 38.0));

        List<Aviso> avisos = Aviso.verificarTemperatura(planta, cuidados, meteo);

        assertNotNull(avisos);
        assertTrue(avisos.isEmpty());
    }

    @Test
    public void verificarTemperatura_parametrosNulosOuSemPrevisao_deveRetornarListaVazia() {
        CuidadosEspecie cuidados = criarCuidados(10, 30);

        assertTrue(Aviso.verificarTemperatura(null, cuidados, meteo).isEmpty());
        assertTrue(Aviso.verificarTemperatura(planta, null, meteo).isEmpty());
        assertTrue(Aviso.verificarTemperatura(planta, cuidados, null).isEmpty());

        meteo.setPrevisaoHoraria(new ArrayList<>());
        assertTrue(Aviso.verificarTemperatura(planta, cuidados, meteo).isEmpty());
    }
}

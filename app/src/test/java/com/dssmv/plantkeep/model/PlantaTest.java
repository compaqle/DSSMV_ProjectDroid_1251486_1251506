package com.dssmv.plantkeep.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Date;

/**
 * Testes  para as regras de validação e integridade do modelo Planta.
 */
public class PlantaTest {

    @Test
    public void criarPlanta_comNomeValido_deveInstanciarEAplicarTrim() {
        Planta p = new Planta("   Monstera Deliciosa   ", "Monstera deliciosa");
        assertEquals("Monstera Deliciosa", p.getNome());
        assertEquals("Monstera deliciosa", p.getEspecie());
        assertNotNull(p.getDiario());
        assertTrue(p.getDiario().isEmpty());
    }

    @Test
    public void criarPlanta_comNomeNoLimiteDeTresCaracteres_deveCriarComSucesso() {
        Planta p = new Planta("Paz", "Spathiphyllum");
        assertEquals("Paz", p.getNome());
    }

    @Test(expected = IllegalArgumentException.class)
    public void criarPlanta_comNomeNulo_deveLancarIllegalArgumentException() {
        new Planta(null, "Especie Teste");
    }

    @Test(expected = IllegalArgumentException.class)
    public void criarPlanta_comNomeMenorQueTresCaracteres_deveLancarIllegalArgumentException() {
        new Planta("Ab", "Especie Teste");
    }

    @Test(expected = IllegalArgumentException.class)
    public void criarPlanta_comEspacosEmBrancoApenas_deveLancarIllegalArgumentException() {
        new Planta("     ", "Especie Teste");
    }

    @Test
    public void setNome_comNomeValido_deveAtualizarNome() {
        Planta p = new Planta("Planta Antiga", "Especie");
        p.setNome("  Novo Nome  ");
        assertEquals("Novo Nome", p.getNome());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNome_comNomeInvalido_deveLancarIllegalArgumentException() {
        Planta p = new Planta("Planta Valida", "Especie");
        p.setNome("Oi");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setUltimaRega_comTimestampNegativo_deveLancarIllegalArgumentException() {
        Planta p = new Planta("Planta Valida", "Especie");
        p.setUltimaRega(-1L);
    }

    @Test
    public void setUltimaRega_comTimestampValido_deveAtualizar() {
        Planta p = new Planta("Planta Valida", "Especie");
        p.setUltimaRega(1700000000000L);
        assertEquals(1700000000000L, p.getUltimaRega());
    }

    @Test(expected = IllegalArgumentException.class)
    public void adicionarRegistoDiario_comRegistoNulo_deveLancarIllegalArgumentException() {
        Planta p = new Planta("Planta Valida", "Especie");
        p.adicionarRegistoDiario(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getDiario_tentativaDeModificacaoDireta_deveLancarUnsupportedOperationException() {
        Planta p = new Planta("Planta Valida", "Especie");
        RegistoDiario r = new RegistoDiario("Registo", new Date(), "");
        p.getDiario().add(r);
    }
}

package com.faculdade.sensor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiretorSensoresTest {

    private DiretorSensores diretor;

    @BeforeEach
    void montar() {
        diretor = new DiretorSensores(new SensorBuilder());
    }

    @Test
    void termoparPadraoDeveSeguirAReceita() {
        SensorConfigurado s = diretor.termoparPadrao("T-02");

        assertEquals("T-02", s.id());
        assertEquals(Grandeza.TEMPERATURA, s.grandeza());
        assertEquals("°C", s.unidade());
        assertEquals(-50, s.minimo());
        assertEquals(1000, s.maximo());
        assertEquals(80, s.limiteAlerta());
        assertEquals(90, s.limiteCritico());
        assertEquals(1.0, s.ganho());
        assertEquals(-0.5, s.offset());
        assertEquals(500, s.intervaloMs());
        assertEquals(List.of("termopar", "processo"), s.tags());
        assertTrue(s.protocolo().isEmpty());
    }

    @Test
    void transdutorDePressaoDeveSeguirAReceitaComProtocolo() {
        SensorConfigurado s = diretor.transdutorPressao("P-01", "10.0.0.20:502");

        assertEquals(Grandeza.PRESSAO, s.grandeza());
        assertEquals("bar", s.unidade());
        assertEquals(0, s.minimo());
        assertEquals(16, s.maximo());
        assertEquals(8, s.limiteAlerta());
        assertEquals(10, s.limiteCritico());
        assertEquals(1000, s.intervaloMs());            // padrão do builder
        assertEquals(List.of("pressao"), s.tags());
        assertEquals(Protocolo.MODBUS_TCP, s.protocolo().orElseThrow());
        assertEquals("10.0.0.20:502", s.endereco().orElseThrow());
    }

    @Test
    void acelerometroDeveSeguirAReceita() {
        SensorConfigurado s = diretor.acelerometro("V-01");

        assertEquals(Grandeza.VIBRACAO, s.grandeza());
        assertEquals("mm/s", s.unidade());
        assertEquals(1.02, s.ganho());
        assertEquals(100, s.intervaloMs());
        assertEquals(List.of("vibracao"), s.tags());
        assertEquals(Protocolo.MQTT, s.protocolo().orElseThrow());
        assertEquals("planta/vibracao/V-01", s.endereco().orElseThrow());
    }

    @Test
    void receitaDepoisDeOutrasNaoDeveHerdarNadaDelas() {
        SensorConfigurado isolado = new DiretorSensores(new SensorBuilder()).termoparPadrao("T-09");

        diretor.transdutorPressao("P-01", "x:502");
        diretor.acelerometro("V-01");
        SensorConfigurado depois = diretor.termoparPadrao("T-09");

        assertEquals(isolado.tags(), depois.tags());
        assertEquals(isolado.ganho(), depois.ganho());
        assertEquals(isolado.offset(), depois.offset());
        assertEquals(isolado.intervaloMs(), depois.intervaloMs());
        assertEquals(isolado.protocolo(), depois.protocolo());
        assertEquals(isolado.endereco(), depois.endereco());
    }

    @Test
    void produtosDeUmMesmoDiretorDevemSerIndependentes() {
        SensorConfigurado a = diretor.termoparPadrao("T-01");
        SensorConfigurado b = diretor.termoparPadrao("T-02");

        assertNotSame(a, b);
        assertEquals("T-01", a.id());
        assertEquals("T-02", b.id());
        assertEquals(a.tags(), b.tags());
    }

    @Test
    void receitaDeveRepassarValidacoesDoBuilder() {
        assertThrows(IllegalArgumentException.class, () -> diretor.termoparPadrao(" "));
        assertThrows(IllegalArgumentException.class, () -> diretor.transdutorPressao("P", " "));
    }

    @Test
    void diretorDeveExigirBuilder() {
        assertThrows(NullPointerException.class, () -> new DiretorSensores(null));
    }
}

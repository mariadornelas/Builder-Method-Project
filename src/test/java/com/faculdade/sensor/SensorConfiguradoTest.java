package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SensorConfiguradoTest {

    private static SensorConfigurado sensor() {
        return new SensorBuilder().id("T-01").grandeza(Grandeza.TEMPERATURA).unidade("°C")
                .faixa(0, 100).limites(80, 90).calibracao(1.0, -0.5).tag("a").build();
    }

    @Test
    void calibrarDeveAplicarGanhoEOffset() {
        SensorConfigurado s = new SensorBuilder().id("V").grandeza(Grandeza.VIBRACAO).unidade("mm/s")
                .faixa(0, 100).limites(4.5, 7.1).calibracao(2.0, 1.0).build();

        assertEquals(21.0, s.calibrar(10.0));
        assertEquals(9.5, sensor().calibrar(10.0));
    }

    @Test
    void faixaDeveSerInclusiva() {
        SensorConfigurado s = sensor();

        assertTrue(s.dentroDaFaixa(0));
        assertTrue(s.dentroDaFaixa(100));
        assertFalse(s.dentroDaFaixa(-0.1));
        assertFalse(s.dentroDaFaixa(100.1));
        assertFalse(s.dentroDaFaixa(Double.NaN));
    }

    @Test
    void nivelDeveUsarLimitesInclusivos() {
        SensorConfigurado s = sensor();

        assertEquals(Nivel.NORMAL, s.nivel(79.9));
        assertEquals(Nivel.ALERTA, s.nivel(80.0));
        assertEquals(Nivel.ALERTA, s.nivel(89.9));
        assertEquals(Nivel.CRITICO, s.nivel(90.0));
    }

    @Test
    void tagsDoProdutoDevemSerSomenteLeitura() {
        assertThrows(UnsupportedOperationException.class, () -> sensor().tags().add("x"));
    }

    @Test
    void produtoDeveSerImutavel() {
        assertTrue(Modifier.isFinal(SensorConfigurado.class.getModifiers()));
        for (Field f : SensorConfigurado.class.getDeclaredFields()) {
            assertTrue(Modifier.isFinal(f.getModifiers()), f.getName());
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName());
        }
        boolean temSetter = Arrays.stream(SensorConfigurado.class.getMethods())
                .anyMatch(m -> m.getName().startsWith("set") || m.getName().startsWith("definir"));
        assertFalse(temSetter);
    }

    @Test
    void ninguemForaDoPacoteDeveConseguirConstruirOProdutoDiretamente() {
        long publicos = Arrays.stream(SensorConfigurado.class.getDeclaredConstructors())
                .filter(c -> Modifier.isPublic(c.getModifiers())).count();

        assertEquals(0, publicos);
    }
}

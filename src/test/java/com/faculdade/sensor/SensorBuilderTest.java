package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SensorBuilderTest {

    /** Builder com todos os obrigatórios e mais nada. */
    private static SensorBuilder minimo() {
        return new SensorBuilder().id("T-01").grandeza(Grandeza.TEMPERATURA).unidade("°C")
                .faixa(0, 100).limites(80, 90);
    }

    // ---- defaults e montagem completa ----

    @Test
    void soComObrigatoriosDeveAplicarOsPadroes() {
        SensorConfigurado s = minimo().build();

        assertEquals("T-01", s.id());
        assertEquals(Grandeza.TEMPERATURA, s.grandeza());
        assertEquals("°C", s.unidade());
        assertEquals(0, s.minimo());
        assertEquals(100, s.maximo());
        assertEquals(80, s.limiteAlerta());
        assertEquals(90, s.limiteCritico());
        assertEquals(1.0, s.ganho());
        assertEquals(0.0, s.offset());
        assertEquals(1000, s.intervaloMs());
        assertTrue(s.tags().isEmpty());
        assertTrue(s.protocolo().isEmpty());
        assertTrue(s.endereco().isEmpty());
    }

    @Test
    void montagemCompletaDeveCarregarTodosOsValores() {
        SensorConfigurado s = minimo().calibracao(1.02, -0.5).intervaloMs(250)
                .tag("forno").tag("critico").protocolo(Protocolo.OPC_UA, "opc.tcp://x:4840").build();

        assertEquals(1.02, s.ganho());
        assertEquals(-0.5, s.offset());
        assertEquals(250, s.intervaloMs());
        assertEquals(List.of("forno", "critico"), s.tags());
        assertEquals(Protocolo.OPC_UA, s.protocolo().orElseThrow());
        assertEquals("opc.tcp://x:4840", s.endereco().orElseThrow());
    }

    @Test
    void ordemDosPassosNaoDeveImportar() {
        SensorConfigurado a = new SensorBuilder().limites(80, 90).faixa(0, 100).unidade("°C")
                .grandeza(Grandeza.TEMPERATURA).id("T-01").build();

        assertEquals("T-01", a.id());
        assertEquals(80, a.limiteAlerta());
    }

    @Test
    void passosDevemDevolverOProprioBuilder() {
        SensorBuilder b = new SensorBuilder();

        assertSame(b, b.id("A"));
        assertSame(b, b.grandeza(Grandeza.PRESSAO));
        assertSame(b, b.unidade("bar"));
        assertSame(b, b.faixa(0, 1));
        assertSame(b, b.limites(0.5, 0.8));
        assertSame(b, b.calibracao(1, 0));
        assertSame(b, b.intervaloMs(100));
        assertSame(b, b.tag("x"));
        assertSame(b, b.protocolo(Protocolo.MQTT, "t"));
        assertSame(b, b.reiniciar());
    }

    // ---- obrigatórios ausentes ----

    @Test
    void buildSemNadaDeveListarTodosOsObrigatoriosAusentes() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new SensorBuilder().build());

        for (String campo : List.of("id", "grandeza", "unidade", "faixa", "limites")) {
            assertTrue(e.getMessage().contains(campo), campo + " em: " + e.getMessage());
        }
    }

    @Test
    void faltandoUmObrigatorioDeveNomearSomenteEle() {
        IllegalStateException semId = assertThrows(IllegalStateException.class,
                () -> new SensorBuilder().grandeza(Grandeza.PRESSAO).unidade("bar").faixa(0, 1).limites(0.5, 0.8).build());
        assertTrue(semId.getMessage().endsWith(": id"), semId.getMessage());

        assertThrows(IllegalStateException.class,
                () -> new SensorBuilder().id("A").unidade("bar").faixa(0, 1).limites(0.5, 0.8).build());   // grandeza
        assertThrows(IllegalStateException.class,
                () -> new SensorBuilder().id("A").grandeza(Grandeza.PRESSAO).faixa(0, 1).limites(0.5, 0.8).build());   // unidade
        assertThrows(IllegalStateException.class,
                () -> new SensorBuilder().id("A").grandeza(Grandeza.PRESSAO).unidade("bar").limites(0.5, 0.8).build());   // faixa
        assertThrows(IllegalStateException.class,
                () -> new SensorBuilder().id("A").grandeza(Grandeza.PRESSAO).unidade("bar").faixa(0, 1).build());   // limites
    }

    // ---- validação de cada passo ----

    @Test
    void idEUnidadeDevemSerValidados() {
        SensorBuilder b = new SensorBuilder();

        assertThrows(NullPointerException.class, () -> b.id(null));
        assertThrows(IllegalArgumentException.class, () -> b.id("  "));
        assertThrows(NullPointerException.class, () -> b.unidade(null));
        assertThrows(IllegalArgumentException.class, () -> b.unidade(""));
        assertThrows(NullPointerException.class, () -> b.grandeza(null));
    }

    @Test
    void faixaDeveExigirMinimoMenorQueMaximoEValoresFinitos() {
        SensorBuilder b = new SensorBuilder();

        assertThrows(IllegalArgumentException.class, () -> b.faixa(10, 10));
        assertThrows(IllegalArgumentException.class, () -> b.faixa(10, 0));
        assertThrows(IllegalArgumentException.class, () -> b.faixa(Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> b.faixa(0, Double.POSITIVE_INFINITY));
    }

    @Test
    void limitesDevemExigirAlertaMenorQueCriticoEValoresFinitos() {
        SensorBuilder b = new SensorBuilder();

        assertThrows(IllegalArgumentException.class, () -> b.limites(9, 9));
        assertThrows(IllegalArgumentException.class, () -> b.limites(9, 8));
        assertThrows(IllegalArgumentException.class, () -> b.limites(Double.NaN, 8));
    }

    @Test
    void calibracaoDeveExigirGanhoPositivoEValoresFinitos() {
        SensorBuilder b = new SensorBuilder();

        assertThrows(IllegalArgumentException.class, () -> b.calibracao(0, 0));
        assertThrows(IllegalArgumentException.class, () -> b.calibracao(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> b.calibracao(1, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> b.calibracao(Double.POSITIVE_INFINITY, 0));
    }

    @Test
    void intervaloDeveRespeitarOMinimoDe10Ms() {
        SensorBuilder b = new SensorBuilder();

        assertThrows(IllegalArgumentException.class, () -> b.intervaloMs(9));
        assertThrows(IllegalArgumentException.class, () -> b.intervaloMs(0));
        assertDoesNotThrow(() -> b.intervaloMs(10));
    }

    @Test
    void passoInvalidoNaoDeveAlterarOQueJaEstavaNoBuilder() {
        SensorBuilder b = minimo().calibracao(2.0, 1.0);

        assertThrows(IllegalArgumentException.class, () -> b.calibracao(-5, 0));

        SensorConfigurado s = b.build();
        assertEquals(2.0, s.ganho());
        assertEquals(1.0, s.offset());
    }

    // ---- tags ----

    @Test
    void tagsRepetidasDevemSerIgnoradasMantendoAOrdem() {
        SensorConfigurado s = minimo().tag("b").tag("a").tag("b").tag("c").tag("a").build();

        assertEquals(List.of("b", "a", "c"), s.tags());
    }

    @Test
    void tagDeveSerValidada() {
        SensorBuilder b = new SensorBuilder();

        assertThrows(NullPointerException.class, () -> b.tag(null));
        assertThrows(IllegalArgumentException.class, () -> b.tag(" "));
    }

    // ---- protocolo e endereço andam juntos ----

    @Test
    void protocoloEEnderecoDevemSerValidados() {
        SensorBuilder b = new SensorBuilder();

        assertThrows(NullPointerException.class, () -> b.protocolo(null, "x"));
        assertThrows(NullPointerException.class, () -> b.protocolo(Protocolo.MQTT, null));
        assertThrows(IllegalArgumentException.class, () -> b.protocolo(Protocolo.MQTT, " "));
    }

    // ---- coerência entre passos (só detectável no build) ----

    @Test
    void limitesForaDaFaixaDevemSerRecusadosNoBuild() {
        assertThrows(IllegalStateException.class,
                () -> new SensorBuilder().id("A").grandeza(Grandeza.PRESSAO).unidade("bar")
                        .faixa(0, 16).limites(8, 20).build());       // crítico acima do máximo
        assertThrows(IllegalStateException.class,
                () -> new SensorBuilder().id("A").grandeza(Grandeza.PRESSAO).unidade("bar")
                        .faixa(5, 16).limites(4, 10).build());       // alerta abaixo do mínimo
    }

    @Test
    void limitesNosExtremosDaFaixaDevemSerAceitos() {
        assertDoesNotThrow(() -> new SensorBuilder().id("A").grandeza(Grandeza.PRESSAO).unidade("bar")
                .faixa(0, 16).limites(0, 16).build());
    }

    @Test
    void coerenciaDeveValerQualquerQuesejaAOrdemDosPassos() {
        SensorBuilder b = new SensorBuilder().id("A").grandeza(Grandeza.PRESSAO).unidade("bar")
                .limites(8, 20).faixa(0, 16);

        assertThrows(IllegalStateException.class, b::build);
    }

    // ---- reuso e independência dos produtos ----

    @Test
    void builderDeveSerReutilizavelComProdutosIndependentes() {
        SensorBuilder b = minimo().tag("a");
        SensorConfigurado primeiro = b.build();

        b.id("T-02").tag("b").calibracao(2.0, 1.0);
        SensorConfigurado segundo = b.build();

        assertEquals("T-01", primeiro.id());
        assertEquals(List.of("a"), primeiro.tags());
        assertEquals(1.0, primeiro.ganho());
        assertEquals("T-02", segundo.id());
        assertEquals(List.of("a", "b"), segundo.tags());
        assertEquals(2.0, segundo.ganho());
    }

    @Test
    void reiniciarDeveLimparObrigatoriosOpcionaisETags() {
        SensorBuilder b = minimo().calibracao(2.0, 1.0).intervaloMs(50).tag("x")
                .protocolo(Protocolo.MQTT, "t");

        b.reiniciar();

        assertThrows(IllegalStateException.class, b::build);       // obrigatórios sumiram
        SensorConfigurado s = b.id("N").grandeza(Grandeza.PRESSAO).unidade("bar").faixa(0, 1).limites(0.5, 0.8).build();
        assertEquals(1.0, s.ganho());
        assertEquals(0.0, s.offset());
        assertEquals(1000, s.intervaloMs());
        assertTrue(s.tags().isEmpty());
        assertTrue(s.protocolo().isEmpty());
        assertTrue(s.endereco().isEmpty());
    }
}

package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** O que caracteriza o Builder: passos nomeados, estados impossíveis impedidos e produto separado. */
class BuilderDesignTest {

    @Test
    void passosDoBuilderDevemRetornarOProprioBuilderExcetoBuild() {
        for (Method m : SensorBuilder.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(m.getModifiers()) || m.getName().equals("build")) continue;
            assertEquals(SensorBuilder.class, m.getReturnType(), m.getName());
        }
    }

    @Test
    void buildDeveDevolverOProduto() throws Exception {
        assertEquals(SensorConfigurado.class, SensorBuilder.class.getMethod("build").getReturnType());
    }

    @Test
    void protocoloEEnderecoNaoDevemPoderSerDefinidosSeparadamente() {
        Set<String> publicos = Arrays.stream(SensorBuilder.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName).collect(Collectors.toSet());

        assertFalse(publicos.contains("endereco"));
        // o único passo de protocolo exige os dois argumentos de uma vez
        long sobrecargasDeProtocolo = Arrays.stream(SensorBuilder.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("protocolo")).count();
        assertEquals(1, sobrecargasDeProtocolo);
        assertEquals(2, Arrays.stream(SensorBuilder.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("protocolo")).findFirst().orElseThrow().getParameterCount());
    }

    @Test
    void estadoDoProdutoNaoDevePoderSerAlteradoPeloBuilderDepoisDoBuild() {
        SensorBuilder b = new SensorBuilder().id("A").grandeza(Grandeza.PRESSAO).unidade("bar")
                .faixa(0, 16).limites(8, 10).tag("x");
        SensorConfigurado s = b.build();

        b.tag("y").calibracao(3.0, 3.0).intervaloMs(20);

        assertEquals(List.of("x"), s.tags());
        assertEquals(1.0, s.ganho());
        assertEquals(1000, s.intervaloMs());
    }

    @Test
    void diretorDeveDependerSomenteDoBuilderENaoDoProdutoConcreto() {
        List<Class<?>> tipos = Arrays.stream(DiretorSensores.class.getDeclaredFields())
                .filter(f -> !f.isSynthetic()).map(Field::getType).collect(Collectors.toList());

        assertEquals(List.of(SensorBuilder.class), tipos);
    }

    @Test
    void produtoNaoDeveConhecerOBuilder() {
        for (Field f : SensorConfigurado.class.getDeclaredFields()) {
            assertNotEquals(SensorBuilder.class, f.getType(), f.getName());
        }
    }

    @Test
    void construirDoZeroDeveExigirOsCincoObrigatoriosEMaisNada() {
        // com apenas os obrigatórios o produto já é válido: opcionais nunca são necessários
        SensorConfigurado s = new SensorBuilder().id("A").grandeza(Grandeza.VIBRACAO).unidade("mm/s")
                .faixa(0, 100).limites(4.5, 7.1).build();

        assertNotNull(s);
    }
}

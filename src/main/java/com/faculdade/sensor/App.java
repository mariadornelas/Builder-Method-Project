package com.faculdade.sensor;

public class App {

    public static void main(String[] args) {
        System.out.println("=== Montagem manual: cada passo tem nome, a ordem não importa ===");
        SensorConfigurado t = new SensorBuilder()
                .id("T-01")
                .grandeza(Grandeza.TEMPERATURA)
                .unidade("°C")
                .faixa(-50, 1000)
                .limites(80, 90)
                .calibracao(1.0, -0.5)
                .intervaloMs(500)
                .tag("forno-2").tag("processo-critico")
                .protocolo(Protocolo.OPC_UA, "opc.tcp://10.0.0.5:4840")
                .build();
        imprimir(t);

        System.out.println();
        System.out.println("=== build() recusa objetos incompletos ou incoerentes ===");
        tentar(() -> new SensorBuilder().id("X-01").unidade("bar").build());
        tentar(() -> new SensorBuilder().id("X-02").grandeza(Grandeza.PRESSAO).unidade("bar")
                .faixa(0, 16).limites(8, 20).build());
        tentar(() -> new SensorBuilder().id("X-03").faixa(10, 0));

        System.out.println();
        System.out.println("=== Receitas prontas do Diretor (mesmo builder reaproveitado) ===");
        DiretorSensores diretor = new DiretorSensores(new SensorBuilder());
        imprimir(diretor.termoparPadrao("T-02"));
        imprimir(diretor.transdutorPressao("P-01", "10.0.0.20:502"));
        imprimir(diretor.acelerometro("V-01"));

        System.out.println();
        System.out.println("=== O objeto pronto trabalha ===");
        double valor = t.calibrar(85.0);
        System.out.printf(java.util.Locale.ROOT, "T-01: bruto 85.0 -> %.1f %s (%s)%n", valor, t.unidade(), t.nivel(valor));
    }

    private static void imprimir(SensorConfigurado s) {
        System.out.printf(java.util.Locale.ROOT,
                "  %-5s %-12s %-5s faixa [%.1f, %.1f] limites %.1f/%.1f ganho %.2f offset %.2f %d ms tags %s %s%n",
                s.id(), s.grandeza(), s.unidade(), s.minimo(), s.maximo(), s.limiteAlerta(), s.limiteCritico(),
                s.ganho(), s.offset(), s.intervaloMs(), s.tags(),
                s.protocolo().map(p -> p + " @ " + s.endereco().orElseThrow()).orElse("sem protocolo"));
    }

    private static void tentar(Runnable r) {
        try {
            r.run();
        } catch (RuntimeException e) {
            System.out.println("  " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}

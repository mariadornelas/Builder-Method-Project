package com.faculdade.sensor;

import java.util.Objects;

public class DiretorSensores {

    private final SensorBuilder builder;

    public DiretorSensores(SensorBuilder builder) {
        this.builder = Objects.requireNonNull(builder, "builder não pode ser nulo");
    }

    public SensorConfigurado termoparPadrao(String id) {
        return builder.reiniciar()
                .id(id).grandeza(Grandeza.TEMPERATURA).unidade("°C")
                .faixa(-50, 1000).limites(80, 90)
                .calibracao(1.0, -0.5).intervaloMs(500)
                .tag("termopar").tag("processo")
                .build();
    }

    public SensorConfigurado transdutorPressao(String id, String enderecoModbus) {
        return builder.reiniciar()
                .id(id).grandeza(Grandeza.PRESSAO).unidade("bar")
                .faixa(0, 16).limites(8, 10)
                .tag("pressao")
                .protocolo(Protocolo.MODBUS_TCP, enderecoModbus)
                .build();
    }

    public SensorConfigurado acelerometro(String id) {
        return builder.reiniciar()
                .id(id).grandeza(Grandeza.VIBRACAO).unidade("mm/s")
                .faixa(0, 100).limites(4.5, 7.1)
                .calibracao(1.02, 0.0).intervaloMs(100)
                .tag("vibracao")
                .protocolo(Protocolo.MQTT, "planta/vibracao/" + id)
                .build();
    }
}

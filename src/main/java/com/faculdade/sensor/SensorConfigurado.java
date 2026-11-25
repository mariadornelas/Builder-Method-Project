package com.faculdade.sensor;

import java.util.List;
import java.util.Optional;

public final class SensorConfigurado {

    private final String id;
    private final Grandeza grandeza;
    private final String unidade;
    private final double minimo;
    private final double maximo;
    private final double limiteAlerta;
    private final double limiteCritico;
    private final double ganho;
    private final double offset;
    private final int intervaloMs;
    private final List<String> tags;
    private final Protocolo protocolo;
    private final String endereco;

    SensorConfigurado(String id, Grandeza grandeza, String unidade, double minimo, double maximo,
                      double limiteAlerta, double limiteCritico, double ganho, double offset,
                      int intervaloMs, List<String> tags, Protocolo protocolo, String endereco) {
        this.id = id;
        this.grandeza = grandeza;
        this.unidade = unidade;
        this.minimo = minimo;
        this.maximo = maximo;
        this.limiteAlerta = limiteAlerta;
        this.limiteCritico = limiteCritico;
        this.ganho = ganho;
        this.offset = offset;
        this.intervaloMs = intervaloMs;
        this.tags = List.copyOf(tags);
        this.protocolo = protocolo;
        this.endereco = endereco;
    }

    public String id() { return id; }
    public Grandeza grandeza() { return grandeza; }
    public String unidade() { return unidade; }
    public double minimo() { return minimo; }
    public double maximo() { return maximo; }
    public double limiteAlerta() { return limiteAlerta; }
    public double limiteCritico() { return limiteCritico; }
    public double ganho() { return ganho; }
    public double offset() { return offset; }
    public int intervaloMs() { return intervaloMs; }
    public List<String> tags() { return tags; }
    public Optional<Protocolo> protocolo() { return Optional.ofNullable(protocolo); }
    public Optional<String> endereco() { return Optional.ofNullable(endereco); }

    public double calibrar(double bruto) {
        return bruto * ganho + offset;
    }

    public boolean dentroDaFaixa(double valorCalibrado) {
        return valorCalibrado >= minimo && valorCalibrado <= maximo;
    }

    public Nivel nivel(double valorCalibrado) {
        if (valorCalibrado >= limiteCritico) return Nivel.CRITICO;
        if (valorCalibrado >= limiteAlerta) return Nivel.ALERTA;
        return Nivel.NORMAL;
    }
}

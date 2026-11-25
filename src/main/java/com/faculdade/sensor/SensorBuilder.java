package com.faculdade.sensor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SensorBuilder {

    private static final double GANHO_PADRAO = 1.0;
    private static final double OFFSET_PADRAO = 0.0;
    private static final int INTERVALO_PADRAO_MS = 1000;
    private static final int INTERVALO_MINIMO_MS = 10;

    private String id;
    private Grandeza grandeza;
    private String unidade;
    private Double minimo;
    private Double maximo;
    private Double limiteAlerta;
    private Double limiteCritico;
    private double ganho = GANHO_PADRAO;
    private double offset = OFFSET_PADRAO;
    private int intervaloMs = INTERVALO_PADRAO_MS;
    private final Set<String> tags = new LinkedHashSet<>();
    private Protocolo protocolo;
    private String endereco;

    public SensorBuilder id(String id) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id não pode ser vazio");
        }
        this.id = id;
        return this;
    }

    public SensorBuilder grandeza(Grandeza grandeza) {
        this.grandeza = Objects.requireNonNull(grandeza, "grandeza não pode ser nula");
        return this;
    }

    public SensorBuilder unidade(String unidade) {
        Objects.requireNonNull(unidade, "unidade não pode ser nula");
        if (unidade.isBlank()) {
            throw new IllegalArgumentException("unidade não pode ser vazia");
        }
        this.unidade = unidade;
        return this;
    }

    public SensorBuilder faixa(double minimo, double maximo) {
        if (!Double.isFinite(minimo) || !Double.isFinite(maximo)) {
            throw new IllegalArgumentException("a faixa deve ser finita");
        }
        if (!(minimo < maximo)) {
            throw new IllegalArgumentException("o mínimo deve ser menor que o máximo");
        }
        this.minimo = minimo;
        this.maximo = maximo;
        return this;
    }

    public SensorBuilder limites(double alerta, double critico) {
        if (!Double.isFinite(alerta) || !Double.isFinite(critico)) {
            throw new IllegalArgumentException("os limites devem ser finitos");
        }
        if (!(alerta < critico)) {
            throw new IllegalArgumentException("o limite de alerta deve ser menor que o crítico");
        }
        this.limiteAlerta = alerta;
        this.limiteCritico = critico;
        return this;
    }

    public SensorBuilder calibracao(double ganho, double offset) {
        if (!Double.isFinite(ganho) || !Double.isFinite(offset)) {
            throw new IllegalArgumentException("ganho e offset devem ser finitos");
        }
        if (ganho <= 0) {
            throw new IllegalArgumentException("o ganho deve ser positivo");
        }
        this.ganho = ganho;
        this.offset = offset;
        return this;
    }

    public SensorBuilder intervaloMs(int intervaloMs) {
        if (intervaloMs < INTERVALO_MINIMO_MS) {
            throw new IllegalArgumentException("o intervalo mínimo é " + INTERVALO_MINIMO_MS + " ms");
        }
        this.intervaloMs = intervaloMs;
        return this;
    }

    public SensorBuilder tag(String tag) {
        Objects.requireNonNull(tag, "tag não pode ser nula");
        if (tag.isBlank()) {
            throw new IllegalArgumentException("tag não pode ser vazia");
        }
        this.tags.add(tag);
        return this;
    }

    public SensorBuilder protocolo(Protocolo protocolo, String endereco) {
        Objects.requireNonNull(protocolo, "protocolo não pode ser nulo");
        Objects.requireNonNull(endereco, "endereço não pode ser nulo");
        if (endereco.isBlank()) {
            throw new IllegalArgumentException("endereço não pode ser vazio");
        }
        this.protocolo = protocolo;
        this.endereco = endereco;
        return this;
    }

    public SensorBuilder reiniciar() {
        id = null;
        grandeza = null;
        unidade = null;
        minimo = null;
        maximo = null;
        limiteAlerta = null;
        limiteCritico = null;
        ganho = GANHO_PADRAO;
        offset = OFFSET_PADRAO;
        intervaloMs = INTERVALO_PADRAO_MS;
        tags.clear();
        protocolo = null;
        endereco = null;
        return this;
    }

    public SensorConfigurado build() {
        List<String> faltando = new ArrayList<>();
        if (id == null) faltando.add("id");
        if (grandeza == null) faltando.add("grandeza");
        if (unidade == null) faltando.add("unidade");
        if (minimo == null) faltando.add("faixa");
        if (limiteAlerta == null) faltando.add("limites");
        if (!faltando.isEmpty()) {
            throw new IllegalStateException("campos obrigatórios ausentes: " + String.join(", ", faltando));
        }
        if (limiteAlerta < minimo || limiteCritico > maximo) {
            throw new IllegalStateException("os limites [" + limiteAlerta + ", " + limiteCritico
                    + "] devem estar dentro da faixa [" + minimo + ", " + maximo + "]");
        }
        return new SensorConfigurado(id, grandeza, unidade, minimo, maximo, limiteAlerta, limiteCritico,
                ganho, offset, intervaloMs, new ArrayList<>(tags), protocolo, endereco);
    }
}

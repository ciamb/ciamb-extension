package com.ciamb.reactive.bool;

import com.ciamb.reactive.value.ReactiveValue;

import java.util.function.Supplier;

/**
 * <p>
 * {@code ReactiveBool} rappresenta un punto di ingresso per eseguire logica condizionale
 * in maniera fluida e reattiva.
 * </p>
 *
 * <p>
 * La classe consente di definire una condizione (booleana o lazily valutata tramite {@link Supplier}),
 * e di eseguire operazioni differenti a seconda che la condizione risulti vera o falsa.
 * </p>
 *
 * <p>
 * Uso tipico:
 * </p>
 *
 * <pre>{@code
 * var res = ReactiveBool.ofCondition(checkSomething())
 *     .ifTrue(() -> insertIntoDB())
 *     .map(entity -> enrichEntity(entity))
 *     .orElseThrow(() -> new RuntimeException("Errore durante l'inserimento"));
 * }</pre>
 */
public class ReactiveBool {

    private final Supplier<Boolean> condition;

    private ReactiveBool(Supplier<Boolean> condition) {
        this.condition = condition;
    }

    /**
     * Crea un {@code ReactiveBool} a partire da una condizione booleana immediata.
     *
     * @param condition valore booleano
     * @return istanza di {@code ReactiveBool}
     */

    public static ReactiveBool withCondition(boolean condition) {
        return new ReactiveBool(() -> condition);
    }

    /**
     * Crea un {@code ReactiveBool} a partire da una condizione booleana valutata in modo lazy.
     *
     * @param conditionSupplier {@link Supplier} che restituisce la condizione booleana
     * @return istanza di {@code ReactiveBool}
     */
    public static ReactiveBool withCondition(Supplier<Boolean> conditionSupplier) {
        return new ReactiveBool(conditionSupplier);
    }

    /**
     * ReactiveBool
     */

    /**
     * Esegue un'azione {@link Runnable} se la condizione è {@code true}.
     * L'azione non produce un valore di ritorno, quindi la pipeline rimane su {@code ReactiveBool}.
     *
     * @param action azione da eseguire se la condizione è vera
     * @return {@code this} per continuare la pipeline
     */
    public ReactiveBool ifTrue(Runnable action) {
        if (condition.get()) {
            action.run();
        }
        return this;
    }

    /**
     * Esegue un'azione {@link Runnable} se la condizione è {@code false}.
     * L'azione non produce un valore di ritorno, quindi la pipeline rimane su {@code ReactiveBool}.
     *
     * @param action azione da eseguire se la condizione è falsa
     * @return {@code this} per continuare la pipeline
     */
    public ReactiveBool ifFalse(Runnable action) {
        if (!condition.get()) {
            action.run();
        }
        return this;
    }

    /**
     * ReactiveValue
     */

    /**
     * Esegue un'azione che produce un valore ({@link Supplier}) se la condizione è {@code true}.
     * Se la condizione è soddisfatta, viene restituita un'istanza di {@link ReactiveValue} contenente il valore.
     * Se la condizione è falsa, viene restituita un'istanza vuota di {@link ReactiveValue}.
     *
     * @param <T>    tipo del valore prodotto
     * @param action supplier che genera il valore se la condizione è vera
     * @return {@link ReactiveValue} contenente il risultato o vuoto se la condizione è falsa
     */
    public <T> ReactiveValue<T> ifTrue(Supplier<T> action) {
        if (condition.get()) {
            return new ReactiveValue<>(action.get(), true);
        }
        return new ReactiveValue<>(null, false);
    }

    /**
     * Esegue un'azione che produce un valore ({@link Supplier}) se la condizione è {@code false}.
     * Se la condizione è soddisfatta (falsa), viene restituita un'istanza di {@link ReactiveValue} contenente il valore.
     * Se la condizione è vera, viene restituita un'istanza vuota di {@link ReactiveValue}.
     *
     * @param <T>    tipo del valore prodotto
     * @param action supplier che genera il valore se la condizione è falsa
     * @return {@link ReactiveValue} contenente il risultato o vuoto se la condizione è vera
     */
    public <T> ReactiveValue<T> ifFalse(Supplier<T> action) {
        if (!condition.get()) {
            return new ReactiveValue<>(action.get(), true);
        }
        return new ReactiveValue<>(null, false);
    }
}

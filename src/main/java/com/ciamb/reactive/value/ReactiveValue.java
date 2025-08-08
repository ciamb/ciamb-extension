package com.ciamb.reactive.value;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>
 * {@code ReactiveValue} rappresenta un contenitore opzionale per un valore generato
 * all'interno di una pipeline condizionale {@link com.ciamb.reactive.bool.ReactiveBool}.
 * </p>
 *
 * <p>
 * Questa classe fornisce metodi per trasformare il valore (come {@link #map(Function)}),
 * ottenere un valore di fallback ({@link #orElse(Object)}, {@link #orElseGet(Supplier)})
 * o lanciare eccezioni se il valore non è presente ({@link #orElseThrow(Supplier)}).
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
 *     .orElseThrow(() -> new RuntimeException("Entity non trovata"));
 * }</pre>
 *
 * @param <T> tipo del valore contenuto
 */
public class ReactiveValue<T> {

    private final T value;
    private final boolean present;

    public ReactiveValue(T value, boolean present) {
        this.value = value;
        this.present = present;
    }

    /**
     * Applica una funzione di trasformazione al valore, restituendo un nuovo {@link ReactiveValue}
     * contenente il risultato. Se il valore non è presente, viene restituita una nuova istanza vuota.
     *
     * @param <R>    tipo del valore risultante dalla trasformazione
     * @param mapper funzione di trasformazione
     * @return nuovo {@link ReactiveValue} contenente il risultato della trasformazione o vuoto
     */
    public <R> ReactiveValue<R> map(Function<? super T, ? extends R> mapper) {
        if (present && value != null) {
            return new ReactiveValue<>(mapper.apply(value), true);
        }
        return new ReactiveValue<>(null, false);
    }

    /**
     * Restituisce il valore contenuto se presente, altrimenti restituisce il valore di fallback specificato.
     *
     * @param other valore di fallback
     * @return valore contenuto o {@code other} se non presente
     */
    public T orElse(T other) {
        return present && value != null ? value : other;
    }

    /**
     * Restituisce il valore contenuto se presente, altrimenti invoca il {@link Supplier} fornito
     * per generare un valore di fallback.
     *
     * @param supplier supplier che fornisce il valore di fallback
     * @return valore contenuto o valore fornito dal supplier se non presente
     */
    public T orElseGet(Supplier<? extends T> supplier) {
        return present && value != null ? value : supplier.get();
    }

    /**
     * Restituisce il valore contenuto se presente, altrimenti lancia un'eccezione generata dal {@link Supplier}.
     *
     * @param exceptionSupplier supplier che fornisce l'eccezione da lanciare
     * @return valore contenuto
     * @throws RuntimeException eccezione fornita dal supplier se il valore non è presente
     */
    public T orElseThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
        if (present && value != null) {
            return value;
        }
        throw exceptionSupplier.get();
    }
}

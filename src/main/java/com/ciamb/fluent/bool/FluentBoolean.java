package com.ciamb.fluent.bool;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static java.lang.Boolean.FALSE;

public final class FluentBoolean {
    private final BooleanSupplier supplier;

    private FluentBoolean(BooleanSupplier supplier) {
        this.supplier = memoize(supplier);
    }

    private static BooleanSupplier memoize(BooleanSupplier supplier) {
        return new BooleanSupplier() {
            private volatile boolean done;
            private boolean value;
            @Override
            public boolean getAsBoolean() {
                if (!done) {
                    synchronized (this) {
                        if (!done) {
                            value = supplier.getAsBoolean();
                            done = true;
                        }
                    }
                }
                return value;
            }
        };
    }

    // eager
    public static FluentBoolean when(boolean condition) {
        return new FluentBoolean(() -> condition);
    }

    // lazy
    public static FluentBoolean when(BooleanSupplier supplier) {
        return new FluentBoolean(supplier);
    }

    // bridge Optional
    public static FluentBoolean when(Optional<?> o) {
        return when(o.isPresent());
    }

    // operatori logici
    // and
    public FluentBoolean and(boolean other) {
        return new FluentBoolean(() -> supplier.getAsBoolean() && other);
    }
    public FluentBoolean and(BooleanSupplier other) {
        return new FluentBoolean(() ->  supplier.getAsBoolean() && other.getAsBoolean());
    }

    // or
    public FluentBoolean or(boolean other) {
        return new FluentBoolean(() -> supplier.getAsBoolean() || other);
    }
    public FluentBoolean or(BooleanSupplier other) {
        return new FluentBoolean(() ->  supplier.getAsBoolean() || other.getAsBoolean());
    }

    // not
    public FluentBoolean not() {
        return new FluentBoolean(() -> !supplier.getAsBoolean());
    }

    // short-circuit
    public FluentBoolean isTrue() {
        return this;
    }
    public FluentBoolean isFalse() {
        return not();
    }

    // runnable
    public FluentBoolean thenRun(Runnable action) {
        if (supplier.getAsBoolean()) action.run();
        return this;
    }
    public FluentBoolean elseRun(Runnable action) {
        if (!supplier.getAsBoolean()) action.run();
        return this;
    }

    // ritorna Optional
    public <T> Optional<T> ifTrueSupply(Supplier<? extends T> ifTrue) {
        if (supplier.getAsBoolean())
            return Optional.ofNullable(ifTrue.get());
        else
            return Optional.empty();
    }

    // ternario
    public <T> Optional<T> chooseAndSupply(Supplier<? extends T> ifTrue, Supplier<? extends T> ifFalse) {
        if (supplier.getAsBoolean())
            return Optional.ofNullable(ifTrue.get());
        else
            return Optional.ofNullable(ifFalse.get());
    }

    // primitive value
    public boolean booleanValue() {
        return supplier.getAsBoolean();
    }
}

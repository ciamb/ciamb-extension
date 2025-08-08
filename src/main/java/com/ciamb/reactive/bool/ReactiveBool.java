package com.ciamb.reactive.bool;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class ReactiveBool {
    private final BooleanSupplier supplier;

    private ReactiveBool(BooleanSupplier supplier) {
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
    public static ReactiveBool when(boolean condition) {
        return new ReactiveBool(() -> condition);
    }

    // lazy
    public static ReactiveBool when(BooleanSupplier supplier) {
        return new ReactiveBool(supplier);
    }

    // bridge Optional
    public static ReactiveBool when(Optional<?> o) {
        return when(o.isPresent());
    }

    // operatori logici
    // and
    public ReactiveBool and(boolean other) {
        return new ReactiveBool(() -> supplier.getAsBoolean() && other);
    }
    public ReactiveBool and(BooleanSupplier other) {
        return new ReactiveBool(() ->  supplier.getAsBoolean() && other.getAsBoolean());
    }

    // or
    public ReactiveBool or(boolean other) {
        return new ReactiveBool(() -> supplier.getAsBoolean() || other);
    }
    public ReactiveBool or(BooleanSupplier other) {
        return new ReactiveBool(() ->  supplier.getAsBoolean() || other.getAsBoolean());
    }

    // not
    public ReactiveBool not() {
        return new ReactiveBool(() -> !supplier.getAsBoolean());
    }

    // short-circuit
    public ReactiveBool isTrue() {
        return this;
    }
    public ReactiveBool isFalse() {
        return not();
    }

    // runnable
    public void thenRun(Runnable action) {
        if (supplier.getAsBoolean()) action.run();
    }
    public void elseRun(Runnable action) {
        if (!supplier.getAsBoolean()) action.run();
    }

    // ritorna Optional
    public <T> Optional<T> thenGet(Supplier<? extends T> ifTrue) {
        if (supplier.getAsBoolean())
            return Optional.ofNullable(ifTrue.get());
        else
            return Optional.empty();
    }

    // ternario
    public <T> Optional<T> choose(Supplier<? extends T> ifTrue, Supplier<? extends T> ifFalse) {
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

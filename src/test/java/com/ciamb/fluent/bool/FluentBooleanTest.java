package com.ciamb.fluent.bool;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ciamb.fluent.bool.FluentBoolean.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class FluentBooleanTest {

    @Test
    @DisplayName("test if when work properly")
    void testInstance() {
        var isTrue = when(true);
        assertTrue(isTrue.booleanValue());
    }

    @Test
    @DisplayName("when true and false should be false")
    void testAndBoth() {
        var isFalse = when(true)
                .and(false);

        var isFalseLazy = when(() -> true)
                .and(() -> false);
        assertFalse(isFalse.booleanValue());
        assertFalse(isFalseLazy.booleanValue());
    }

    @Test
    @DisplayName("test memoization")
    void testMemoize() {
        var isTrue = when(() -> true);
        var stillTrue = isTrue.or(false);
        var isNowFalse = isTrue.and(false);
        assertTrue(stillTrue.booleanValue());
        assertFalse(isNowFalse.booleanValue());
    }

    @Test
    @DisplayName("test from Optional")
    void testFromOptional() {
        var isFalse = when(Optional.empty());
        assertFalse(isFalse.booleanValue());
    }

    @Test
    @DisplayName("test cache")
    void testCache() {
        var counter = new AtomicInteger(0);
        var fluentBoolean = when(() -> {
            var c = counter.incrementAndGet();
            if (c == 1) throw new RuntimeException("not done");
            return true;
        });

        // prima chiamata fallisce
        assertThatThrownBy(fluentBoolean::booleanValue)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not done");

        // seconda chiamata è ok e salva il valore e il contatore a due
        assertThat(fluentBoolean.booleanValue()).isTrue();
        assertThat(counter.get()).isEqualTo(2);

        // da adesso in poi anche se continuo a chiamare il contatore rimanre a due
        // perché ha il valore salvato
        assertThat(fluentBoolean.booleanValue()).isTrue();
        assertThat(fluentBoolean.booleanValue()).isTrue();
        assertThat(fluentBoolean.booleanValue()).isTrue();
        assertThat(fluentBoolean.booleanValue()).isTrue();
        assertThat(counter.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("test choose and supply with true condition")
    void testChooseAndSupplyWithTrueCondition() {
        var res = when(() -> true)
                .chooseAndSupply(
                        () -> new AtomicInteger(1),
                        ()-> new AtomicInteger(0));
        assertThat(res).isPresent();
        assertThat(res.get().get()).isEqualTo(1);
        assertNotEquals(0, res.get().get());
    }

    @Test
    @DisplayName("test choose and supply with false condition")
    void testChooseAndSupplyWithFalseCondition() {
        var res = when(() -> false)
                .chooseAndSupply(
                        () -> new AtomicInteger(1),
                        ()-> new AtomicInteger(0));
        assertThat(res).isPresent();
        assertThat(res.get().get()).isEqualTo(0);
        assertNotEquals(1, res.get().get());
    }

    @Test
    @DisplayName("test then supply something")
    void testIfTrueSupply() {
        var res = when(() -> true)
                .ifTrueSupply(() -> new AtomicInteger(1));
        assertThat(res).isPresent();
        assertThat(res.get().get()).isEqualTo(1);
    }

    @Test
    @DisplayName("test then supply when is false should return empty opt")
    void testIfFalseSupplyAnOptional() {
        var res = when(() -> false)
                .ifTrueSupply(() -> new AtomicInteger(1));
        assertThat(res).isInstanceOf(Optional.class)
                .isEmpty();
    }

    @Test
    @DisplayName("test or both eager and lazy")
    void testOrBoth() {
        var isTrueLazy = when(() -> true)
                .or(() -> false);
        var isFalse =  when(false)
                .or(false);
        assertTrue(isTrueLazy.booleanValue());
        assertFalse(isFalse.booleanValue());
    }

    @Test
    @DisplayName("Test all short-circuit")
    void testAllShortCircuit() {
        var isTrue = when(() -> true).isTrue();
        // isFalse su una condizione false torna true
        // perché è correttamente false
        var isFalseTrue = when(() -> false).isFalse()
                .chooseAndSupply(
                        () -> new AtomicInteger(1),
                        () -> new AtomicInteger(0)
                );
        assertTrue(isTrue.booleanValue());
        assertTrue(isFalseTrue.isPresent());
        assertThat(isFalseTrue.get().get()).isEqualTo(1);
    }

    @Test
    @DisplayName("test then run")
    void testThenRun() {
        var stringBuilder = new StringBuilder("FluentBoolean");
        when(() -> true)
                .thenRun(() -> stringBuilder.append(" worka alla grande!"));
        assertThat(stringBuilder.toString())
                .contains("worka alla grande!");
    }

    @Test
    @DisplayName("test else run something")
    void testElseRun() {
        var stringBuilder = new StringBuilder("FluentBoolean");
        var res = when(() -> false)
                .thenRun(() -> stringBuilder.append(" worka alla grande!"))
                .elseRun(() -> stringBuilder.append(" NON worka alla grande!"));
        assertThat(stringBuilder.toString())
                .contains("NON");
        assertThat(res.booleanValue()).isFalse();
        var counter = new AtomicInteger(0);
        var neverRun = when(true)
                .elseRun(counter::incrementAndGet);
        assertThat(neverRun.booleanValue()).isTrue();
        assertThat(counter.get()).isEqualTo(0);
    }
}
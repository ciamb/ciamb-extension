package com.ciamb.reactive.bool;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.ciamb.reactive.bool.ReactiveBool.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReactiveBoolTest {

    @Test
    @DisplayName("test if when work properly")
    void testInstance() {
        var bool = when(true);
        assertTrue(bool.booleanValue());
    }

    @Test
    @DisplayName("when true and false should be false")
    void testAnd() {
        var bool = when(true)
                .and(false);
        assertFalse(bool.booleanValue());
    }

    @Test
    @DisplayName("test memoization")
    void testMemoize() {
        var bool = when(() -> true);
        var stillTrue = bool.or(false);
        var isFalse = bool.and(false);
        assertTrue(stillTrue.booleanValue());
        assertFalse(isFalse.booleanValue());
    }

    @Test
    @DisplayName("test from Optional")
    void testFromOptional() {
        var bool = when(Optional.empty());
        assertFalse(bool.booleanValue());
    }

}
package io.geekya215.peco;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static io.geekya215.peco.Combinator.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberTest {
    private Parser<Double> number;

    @BeforeEach
    void init() {
        var optSign = opt(character('-'));
        var zero = string("0");
        var oneToNine = satisfy(c -> c >= '1' && c <= '9', "one to nine digit");
        var point = character('.');
        var exp = or(character('e'), character('E'));
        var optPlusMinus = opt(or(character('+'), character(('-'))));
        var nonZeroInt = map(
            t -> t.t1() + t.t2().stream().map(String::valueOf).collect(Collectors.joining()),
            then(oneToNine, many(digit())));
        var intPart = or(zero, nonZeroInt);
        var fractionPart = discardL(
            point,
            map(xs -> xs.stream().map(String::valueOf).collect(Collectors.joining()),
                many1(digit())));
        var exponentPart =
            then(
                discardL(exp, optPlusMinus),
                map(xs -> xs.stream().map(String::valueOf).collect(Collectors.joining()), many1(digit())));
        number =
            map(n -> {
                    var _optSign = n.t1().t1().t1();
                    var _intPart = n.t1().t1().t2();
                    var _fractionPart = n.t1().t2();
                    var _expPart = n.t2();

                    var signStr = _optSign.map(String::valueOf).orElse("");
                    var fractionPartStr = _fractionPart.map(s -> "." + s).orElse("");
                    var expPartStr = _expPart.map(e -> "e" + e.t1().map(String::valueOf).orElse("") + e.t2()).orElse("");
                    return Double.valueOf(signStr + _intPart + fractionPartStr + expPartStr);
                }
                , then(then(then(optSign, intPart), opt(fractionPart)), opt(exponentPart))
            );

    }

    @Test
    void testPositiveOne() {
        var expectedResult = 1.0;
        var actualResult = run(number, "1");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<Double, State>>) actualResult).getValue().t1());
    }

    @Test
    void testNegativeOne() {
        var expectedResult = -1.0;
        var actualResult = run(number, "-1");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<Double, State>>) actualResult).getValue().t1());
    }

    @Test
    void testOnePointTwoThree() {
        var expectedResult = 1.23;
        var actualResult = run(number, "1.23");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<Double, State>>) actualResult).getValue().t1());
    }

    @Test
    void testNegativeOnePointTwoThree() {
        var expectedResult = -1.23;
        var actualResult = run(number, "-1.23");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<Double, State>>) actualResult).getValue().t1());
    }

    @Test
    void testOnePointTwoThreeFourWithExponentThree() {
        var expectedResult = 1234;
        var actualResult = run(number, "1.234e3");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<Double, State>>) actualResult).getValue().t1());
    }

    @Test
    void testOnePointTwoThreeFourWithExponentNegativeThree() {
        var expectedResult = 0.001234;
        var actualResult = run(number, "1.234e-3");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<Double, State>>) actualResult).getValue().t1());
    }

    @Test
    void testNegativeOnePointTwoThreeFourWithExponentThree() {
        var expectedResult = -1234;
        var actualResult = run(number, "-1.234e3");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<Double, State>>) actualResult).getValue().t1());
    }

    @Test
    void testNegativeOnePointTwoThreeFourWithExponentNegativeThree() {
        var expectedResult = -0.001234;
        var actualResult = run(number, "-1.234e-3");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(expectedResult, ((Result.Success<Tuple<Double, State>>) actualResult).getValue().t1());
    }
}

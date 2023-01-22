package io.geekya215.peco;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static io.geekya215.peco.Combinator.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CombinatorTest {
    @Test
    void testPureCharA() {
        var parser = pure('a');
        var actualResult = run(parser, "abc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals('a', ((Result.Success<Tuple<Character, State>>) actualResult).getValue().t1());
    }

    @Test
    void testSatisfyCharA() {
        var parser = satisfy(c -> c == 'a', "'a'");
        var actualResult = run(parser, "abc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals('a', ((Result.Success<Tuple<Character, State>>) actualResult).getValue().t1());
    }

    @Test
    void testCharacterA() {
        var parser = character('a');
        var actualResult = run(parser, "abc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals('a', ((Result.Success<Tuple<Character, State>>) actualResult).getValue().t1());
    }

    @Test
    void testMapCharToInteger() {
        var parser = map(c -> Integer.parseInt(String.valueOf(c)), character('1'));
        var actualResult = run(parser, "1bc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(1, ((Result.Success<Tuple<Integer, State>>) actualResult).getValue().t1());
    }

    @Test
    void testApplyCharToListChar() {
        Parser<Function<Character, List<Character>>> parser = pure(List::of);
        var actualResult = run(apply(parser, character('a')), "abc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(List.of('a'), ((Result.Success<Tuple<List<Character>, State>>) actualResult).getValue().t1());
    }

    @Test
    void testStringHello() {
        var parser = string("hello");
        var actualResult = run(parser, "hello world!");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals("hello", ((Result.Success<Tuple<String, State>>) actualResult).getValue().t1());
    }

    @Test
    void testLift2CharToStrToInteger() {
        Function<Character, Function<String, Integer>> f = c -> s -> Integer.parseInt(c + s);
        var parser = lift2(f, character('4'), string("2"));
        var actualResult = run(parser, "42c");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(42, ((Result.Success<Tuple<Integer, State>>) actualResult).getValue().t1());
    }

    @Test
    void testCharacterAThenStringBC() {
        var parser = then(character('a'), string("bc"));
        var actualResult = run(parser, "abc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(Tuple.of('a', "bc"), ((Result.Success<Tuple<Tuple<Character, String>, State>>) actualResult).getValue().t1());
    }

    @Test
    void testCharacterAOrCharacterB() {
        var parser = or(character('a'), character('b'));
        var actualResult = run(parser, "abc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals('a', ((Result.Success<Tuple<Character, State>>) actualResult).getValue().t1());
    }

    @Test
    void testChoiceListCharacterACharacterBCharacterC() {
        var parser = choice(List.of(character('a'), character('b'), character('c')));
        var actualResult = run(parser, "bcd");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals('b', ((Result.Success<Tuple<Character, State>>) actualResult).getValue().t1());
    }

    @Test
    void testSequenceListCharacterACharacterBCharacterC() {
        var parser = sequence(List.of(character('a'), character('b'), character('c')));
        var actualResult = run(parser, "abc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(List.of('a', 'b', 'c'), ((Result.Success<Tuple<List<Character>, State>>) actualResult).getValue().t1());
    }

    @Test
    void testOptCharacterA() {
        var parser = opt(character('a'));
        var actualResult = run(parser, "abc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(Optional.of('a'), ((Result.Success<Tuple<Optional<Character>, State>>) actualResult).getValue().t1());
    }

    @Test
    void testDiscardLCharacterA() {
        var parser = discardL(character('a'), character('b'));
        var actualResult = run(parser, "abc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals('b', ((Result.Success<Tuple<Character, State>>) actualResult).getValue().t1());
    }

    @Test
    void testDiscardRCharacterB() {
        var parser = discardR(character('a'), character('b'));
        var actualResult = run(parser, "abc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals('a', ((Result.Success<Tuple<Character, State>>) actualResult).getValue().t1());
    }

    @Test
    void testCharacterABetweenParenthesis() {
        var parser = between(character('('), character('a'), character(')'));
        var actualResult = run(parser, "(a)bc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals('a', ((Result.Success<Tuple<Character, State>>) actualResult).getValue().t1());
    }

    @Test
    void testManyCharacterA() {
        var parser = many(character('a'));
        var actualResult = run(parser, "bc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(List.of(), ((Result.Success<Tuple<List<Character>, State>>) actualResult).getValue().t1());
    }

    @Test
    void testMany1CharacterA() {
        var parser = many1(character('a'));
        var actualResult = run(parser, "aaabc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(List.of('a', 'a', 'a'), ((Result.Success<Tuple<List<Character>, State>>) actualResult).getValue().t1());
    }

    @Test
    void testCharacterASepByCharacterComma() {
        var parser = sepBy(character('a'), character(','));
        var actualResult = run(parser, "b,c,d");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(List.of(), ((Result.Success<Tuple<List<Character>, State>>) actualResult).getValue().t1());
    }

    @Test
    void testCharacterASepBy1CharacterComma() {
        var parser = sepBy(character('a'), character(','));
        var actualResult = run(parser, "a,a,b,c,d");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(List.of('a', 'a'), ((Result.Success<Tuple<List<Character>, State>>) actualResult).getValue().t1());
    }

    @Test
    void testAnyOfListCharACharBCharC() {
        var parser = anyOf(List.of('a', 'b', 'c'));
        var actualResult = run(parser, "abc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals('a', ((Result.Success<Tuple<Character, State>>) actualResult).getValue().t1());
    }

    @Test
    void testCount3CharacterA() {
        var parser = count(3, character('a'));
        var actualResult = run(parser, "aaabc");
        assertEquals(Result.Success.class, actualResult.getClass());
        assertEquals(List.of('a', 'a', 'a'), ((Result.Success<Tuple<List<Character>, State>>) actualResult).getValue().t1());
    }
}

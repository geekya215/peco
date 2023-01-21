package io.geekya215.peco;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Combinator {
    public static <A> Result<Tuple<A, State>> runOnInput(Parser<A> parser, State state) {
        return parser.fn().apply(state);
    }

    public static <A> Result<Tuple<A, State>> run(Parser<A> parser, String input) {
        return runOnInput(parser, Input.fromString(input));
    }

    public static <A> Parser<A> setLabel(Parser<A> parser, String label) {
        return Parser.of(parser.fn(), label);
    }

    public static <A, B> Parser<B> bind(Function<A, Parser<B>> f, Parser<A> p) {
        var label = "unknown";
        Function<State, Result<Tuple<B, State>>> fn = input -> {
            var res = runOnInput(p, input);
            if (res instanceof Result.Success<Tuple<A, State>> s) {
                var t = s.getValue();
                var v = t.t1();
                var remaining = t.t2();
                var p2 = f.apply(v);
                return runOnInput(p2, remaining);
            } else {
                return (Result.Failure) res;
            }
        };
        return Parser.of(fn, label);
    }

    public static <A> Parser<A> pure(A a) {
        var label = a.toString();
        Function<State, Result<Tuple<A, State>>> fn = input -> Result.Success.of(Tuple.of(a, input));
        return Parser.of(fn, label);
    }

    public static Parser<Character> satisfy(Predicate<Character> predicate, String label) {
        Function<State, Result<Tuple<Character, State>>> fn = input -> {
            var t = Input.nextChar(input);
            var remaining = t.t1();
            var c = t.t2();
            if (c.isEmpty()) {
                var error = "No more input";
                var location = Input.getLocationFromState(input);
                return Result.Failure.of(label, error, location);
            } else {
                var first = c.get();
                if (predicate.test(first)) {
                    return Result.Success.of(Tuple.of(first, remaining));
                } else {
                    var error = String.format("Unexpected '%c'", first);
                    var location = Input.getLocationFromState(input);
                    return Result.Failure.of(label, error, location);
                }
            }
        };
        return Parser.of(fn, label);
    }

    public static <A, B> Parser<B> map(Function<A, B> f, Parser<A> p) {
        return bind(x -> pure(f.apply(x)), p);
    }

    public static <A, B> Parser<B> apply(Parser<Function<A, B>> fp, Parser<A> p) {
        return bind(f -> bind(x -> pure(f.apply(x)), p), fp);
    }

    public static <A, B, C> Parser<C> lift2(Function<A, Function<B, C>> f, Parser<A> x, Parser<B> y) {
        return apply(apply(pure(f), x), y);
    }

    public static <A, B> Parser<Tuple<A, B>> then(Parser<A> p1, Parser<B> p2) {
        var label = String.format("%s then %s", p1.label(), p2.label());
        return setLabel(bind(r1 -> bind(r2 -> pure(Tuple.of(r1, r2)), p2), p1), label);
    }

    public static <A> Parser<A> or(Parser<A> p1, Parser<A> p2) {
        var label = String.format("%s or %s", p1.label(), p2.label());
        Function<State, Result<Tuple<A, State>>> fn = input -> {
            var res = runOnInput(p1, input);
            if (res instanceof Result.Success<Tuple<A, State>>) {
                return res;
            } else {
                return runOnInput(p2, input);
            }
        };
        return Parser.of(fn, label);
    }

    public static <A> Parser<A> choice(List<Parser<A>> parsers) {
        Function<State, Result<Tuple<A, State>>> fn = __ ->
            Result.Failure.of("choice list", "empty choice list", new Location("", 0, 0));
        var identity = Parser.of(fn, "choice list");
        return parsers.stream().reduce(identity, Combinator::or);
    }

    public static <A> Parser<List<A>> sequence(List<Parser<A>> parsers) {
        Function<A, Function<List<A>, List<A>>> curryCons = head -> tail -> cons(head, tail);
        if (parsers.isEmpty()) {
            return pure(List.of());
        } else {
            return lift2(curryCons, parsers.get(0), sequence(parsers.subList(1, parsers.size())));
        }
    }

    public static <A> Tuple<List<A>, State> parseZeroOrMore(Parser<A> parser, State state) {
        var firstResult = runOnInput(parser, state);
        if (firstResult instanceof Result.Failure) {
            return Tuple.of(List.of(), state);
        } else {
            var t1 = ((Result.Success<Tuple<A, State>>) firstResult).getValue();
            var firstValue = t1.t1();
            var afterFirstParse = t1.t2();

            var t2 = parseZeroOrMore(parser, afterFirstParse);
            var subsequenceValue = t2.t1();
            var remaining = t2.t2();

            return Tuple.of(cons(firstValue, subsequenceValue), remaining);
        }
    }

    public static <A> Parser<Optional<A>> opt(Parser<A> p) {
        var label = String.format("opt %s", p.label());
        return setLabel(or(map(Optional::of, p), pure(Optional.empty())), label);
    }

    public static <A, B> Parser<B> discardL(Parser<A> p1, Parser<B> p2) {
        return map(Tuple::t2, then(p1, p2));
    }

    public static <A, B> Parser<A> discardR(Parser<A> p1, Parser<B> p2) {
        return map(Tuple::t1, then(p1, p2));
    }

    public static <A, B, C> Parser<B> between(Parser<A> p1, Parser<B> p2, Parser<C> p3) {
        return discardR(discardL(p1, p2), p3);
    }

    public static <A> Parser<List<A>> many(Parser<A> p) {
        var label = String.format("many %s", p.label());
        Function<State, Result<Tuple<List<A>, State>>> fn = input -> Result.Success.of(parseZeroOrMore(p, input));
        return Parser.of(fn, label);
    }

    public static <A> Parser<List<A>> many1(Parser<A> p) {
        var label = String.format("many1 %s", p.label());
        return setLabel(bind(head -> bind(tail -> pure(cons(head, tail)), many(p)), p), label);
    }

    public static <A, B> Parser<List<A>> sepBy(Parser<A> p, Parser<B> sep) {
        return or(sepBy1(p, sep), pure(List.of()));
    }

    public static <A, B> Parser<List<A>> sepBy1(Parser<A> p, Parser<B> sep) {
        var sepThen = discardL(sep, p);
        return map(t -> cons(t.t1(), t.t2()), then(p, many(sepThen)));
    }

    public static Parser<Character> anyOf(List<Character> chars) {
        var label = String.format("%s", chars.toString());
        return setLabel(choice(chars.stream().map(Combinator::character).toList()), label);
    }

    public static <A> Parser<List<A>> count(Integer n, Parser<A> p) {
        return n <= 0
            ? pure(List.of())
            : sequence(Stream.generate(() -> Parser.of(p.fn(), p.label())).limit(n).collect(Collectors.toList()));
    }

    public static Parser<Character> character(Character c) {
        var label = String.format("'%c'", c);
        return satisfy(x -> x == c, label);
    }

    public static Parser<String> string(String str) {
        return map(
            xs -> xs.stream().map(String::valueOf).collect(Collectors.joining()),
            sequence(str.chars().mapToObj(e -> (char) e).map(Combinator::character).toList())
        );
    }

    public static Parser<Character> space() {
        var label = "<whitespace>";
        return satisfy(Character::isWhitespace, label);
    }

    public static Parser<List<Character>> spaces() {
        var label = "<many whitespace>";
        return setLabel(many(space()), label);
    }

    public static Parser<Character> newline() {
        var label = "<newline>";
        return satisfy(c -> c == '\n', label);
    }

    public static Parser<Character> tab() {
        var label = "<tab>";
        return satisfy(c -> c == '\t', label);
    }

    public static Parser<Character> upper() {
        var label = "uppercase letter";
        return satisfy(Character::isUpperCase, label);
    }

    public static Parser<Character> lower() {
        var label = "lowercase letter";
        return satisfy(Character::isLowerCase, label);
    }

    public static Parser<Character> digit() {
        var label = "digit";
        return satisfy(Character::isDigit, label);
    }

    public static Parser<Character> letter() {
        var label = "letter";
        return satisfy(Character::isLetter, label);
    }

    public static Parser<Character> alphaNum() {
        var label = "letter or digit";
        return satisfy(Character::isLetterOrDigit, label);
    }

    public static <A> List<A> cons(A head, List<A> tail) {
        var lists = new ArrayList<A>();
        lists.add(head);
        lists.addAll(tail);
        return lists.stream().toList();
    }
}

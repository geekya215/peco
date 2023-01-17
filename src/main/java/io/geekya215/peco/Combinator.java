package io.geekya215.peco;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Combinator<A> {
    public static <A> Result<Tuple<A, State>> runOnInput(Parser<A> parser, State state) {
        return parser.fn().apply(state);
    }

    public static <A> Result<Tuple<A, State>> run(Parser<A> parser, String input) {
        return runOnInput(parser, Func.fromString(input));
    }

    public static <A> Parser<A> setLabel(Parser<A> parser, String label) {
        return Parser.of(parser.fn(), label);
    }

    public static Parser<Character> satisfy(Predicate<Character> predicate, String label) {
        Function<State, Result<Tuple<Character, State>>> fn = input -> {
            var t = Func.nextChar(input);
            var remaining = t.t1();
            var c = t.t2();
            if (c.isEmpty()) {
                var error = "No more input";
                var location = Func.getLocationFromState(input);
                return Result.Failure.of(label, error, location);
            } else {
                var first = c.get();
                if (predicate.test(first)) {
                    return Result.Success.of(Tuple.of(first, remaining));
                } else {
                    var error = String.format("Unexpected '%c'", first);
                    var location = Func.getLocationFromState(input);
                    return Result.Failure.of(label, error, location);
                }
            }
        };
        return Parser.of(fn, label);
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
                var r = runOnInput(p2, remaining);
                return r;
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

    public static <A, B> Parser<B> map(Function<A, B> f, Parser<A> p) {
        return bind(x -> pure(f.apply(x)), p);
    }

    public static <A, B> Parser<B> app(Parser<Function<A, B>> fp, Parser<A> xp) {
        return bind(f -> bind(x -> pure(f.apply(x)), xp), fp);
    }

    public static <A, B, C> Parser<C> lift2(Function<A, Function<B, C>> f, Parser<A> x, Parser<B> y) {
        return app(app(pure(f), x), y);
    }

    public static <A, B> Parser<Tuple<A, B>> then(Parser<A> x, Parser<B> y) {
        var label = String.format("%s then %s", x.label(), y.label());
        return setLabel(bind(r1 -> bind(r2 -> pure(Tuple.of(r1, r2)), y), x), label);
    }

    public static <A> Parser<A> or(Parser<A> p1, Parser<A> p2) {
        var label = String.format("%s or %s", p1.label(), p2.label());
        Function<State, Result<Tuple<A, State>>> fn = input -> {
            var r1 = runOnInput(p1, input);
            if (r1 instanceof Result.Success<Tuple<A, State>>) {
                return r1;
            } else {
                return runOnInput(p2, input);
            }
        };
        return Parser.of(fn, label);
    }

    public static <A> Parser<A> choice(List<Parser<A>> parsers) {
        Function<State, Result<Tuple<A, State>>> fn = __ ->
            Result.Failure.of("empty choice", "empty choice", new Location("", 0, 0));
        var identity = Parser.of(fn, "empty choice");
        return parsers.stream().reduce(identity, Combinator::or);
    }

    public static <A> Parser<List<A>> sequence(List<Parser<A>> parsers) {
        Function<A, Function<List<A>, List<A>>> cons = head -> tail -> {
            var arr = new ArrayList<A>();
            arr.add(head);
            arr.addAll(tail);
            return arr.stream().toList();
        };
        if (parsers.isEmpty()) {
            return pure(List.of());
        } else {
            return lift2(cons, parsers.get(0), sequence(parsers.subList(1, parsers.size())));
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

            var arr = new ArrayList<A>();
            arr.add(firstValue);
            arr.addAll(subsequenceValue);

            return Tuple.of(arr.stream().toList(), remaining);
        }
    }

    public static <A> Parser<List<A>> many(Parser<A> parser) {
        var label = String.format("many %s", parser.label());
        Function<State, Result<Tuple<List<A>, State>>> fn = input -> Result.Success.of(parseZeroOrMore(parser, input));
        return Parser.of(fn, label);
    }

    public static <A> Parser<List<A>> many1(Parser<A> parser) {
        var label = String.format("many1 %s", parser.label());
        Function<A, Function<List<A>, List<A>>> cons = head -> tail -> {
            var arr = new ArrayList<A>();
            arr.add(head);
            arr.addAll(tail);
            return arr.stream().toList();
        };
        return setLabel(bind(head -> bind(tail -> pure(cons.apply(head).apply(tail)), many(parser)), parser), label);
    }

    public static <A> Parser<Optional<A>> opt(Parser<A> parser) {
        var label = String.format("opt %s", parser.label());
        return setLabel(or(map(x -> Optional.of(x), parser), pure(Optional.empty())), label);
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

    public static <A, B> Parser<List<A>> sepBy1(Parser<A> p, Parser<B> sep) {
        Function<A, Function<List<A>, List<A>>> cons = head -> tail -> {
            var arr = new ArrayList<A>();
            arr.add(head);
            arr.addAll(tail);
            return arr.stream().toList();
        };
        var sepThen = discardL(sep, p);
        return map(t -> cons.apply(t.t1()).apply(t.t2()), then(p, many(sepThen)));
    }

    public static <A, B> Parser<List<A>> sepBy(Parser<A> p, Parser<B> sep) {
        return or(sepBy1(p, sep), pure(List.of()));
    }

    public static Parser<Character> pchar(Character c) {
        var label = String.format("%c", c);
        return satisfy(x -> x == c, label);
    }

    public static Parser<Character> anyOf(List<Character> chars) {
        var label = String.format("%s", chars.toString());
        return setLabel(choice(chars.stream().map(c -> pchar(c)).toList()), label);
    }
}

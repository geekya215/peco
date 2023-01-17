package io.geekya215.peco;

import java.util.function.Function;

public record Parser<A>(
    Function<State, Result<Tuple<A, State>>> fn,
    String label
) {
    public static <A> Parser<A> of(
        Function<State, Result<Tuple<A, State>>> fn,
        String label
    ) {
        return new Parser<>(fn, label);
    }
}

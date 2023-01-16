package io.geekya215.peco;

public record Tuple<A, B>(
        A t1, B t2
) {
    public static <A, B> Tuple of(A t1, B t2) {
        return new Tuple<>(t1, t2);
    }
}

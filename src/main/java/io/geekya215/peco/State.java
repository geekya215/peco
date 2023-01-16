package io.geekya215.peco;

import java.util.List;

public record State(
        List<String> lines, Position position
) {
}
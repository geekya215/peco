package io.geekya215.peco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class State {
    private List<String> lines;
    private Position position;

    private State() {
    }

    private State(List<String> lines, Position position) {
        lines = lines;
        position = position;
    }

    public static State fromString(String str) {
        if (str == null || str.isEmpty()) {
            return new State(new ArrayList<>(), Position.initialPosition());
        } else {
            var lines = new ArrayList<>(Arrays.stream(str.split("\\r?\\n")).toList());
            return new State(lines, Position.initialPosition());
        }
    }

    public String currentLine() {
        var linePos = position.getLine();
        if (linePos < lines.size()) {
            return lines.get(linePos);
        } else {
            return "end of file";
        }
    }

    public Optional<Character> nextChar() {
        var linePos = position.getLine();
        var colPos = position.getColumn();

        if (linePos >= lines.size()) {
            return Optional.empty();
        } else {
            var curLine = currentLine();
            if (colPos < curLine.length()) {
                var c = curLine.charAt(colPos);
                position.incrementColumn();
                return Optional.of(c);
            } else {
                position.incrementLine();
                return Optional.of('\n');
            }
        }
    }
}

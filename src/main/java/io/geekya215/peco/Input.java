package io.geekya215.peco;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class Input {
    public static Position initialPosition() {
        return new Position(0, 0);
    }

    public static Position incrementLine(Position position) {
        return new Position(position.line() + 1, position.column());
    }

    public static Position incrementColumn(Position position) {
        return new Position(position.line(), position.column() + 1);
    }

    public static String currentLine(State state) {
        var linePos = state.position().line();
        if (linePos < state.lines().size()) {
            return state.lines().get(linePos);
        } else {
            return "end of file";
        }
    }

    public static State fromString(String str) {
        if (str == null || str.isEmpty()) {
            return new State(List.of(), initialPosition());
        } else {
            return new State(Arrays.stream(str.split("\\r?\\n")).toList(), initialPosition());
        }
    }

    public static Tuple<State, Optional<Character>> nextChar(State state) {
        var linePos = state.position().line();
        var colPos = state.position().column();
        if (linePos >= state.lines().size()) {
            return Tuple.of(state, Optional.empty());
        } else {
            var curLine = currentLine(state);
            Character c;
            Position newPos;
            if (colPos < curLine.length()) {
                c = curLine.charAt(colPos);
                newPos = incrementColumn(state.position());
            } else {
                c = '\n';
                newPos = incrementLine(state.position());
            }
            var newState = new State(state.lines(), newPos);
            return Tuple.of(newState, Optional.of(c));
        }
    }

    public static Location getLocationFromState(State state) {
        return new Location(currentLine(state), state.position().line(), state.position().column());
    }
}

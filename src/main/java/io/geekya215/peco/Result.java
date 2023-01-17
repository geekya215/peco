package io.geekya215.peco;

public sealed interface Result<A> permits Result.Failure, Result.Success {
    final class Success<A> implements Result<A> {
        private final A value;

        public Success(A value) {
            this.value = value;
        }

        public static <A> Success<A> of(A value) {
            return new Success<>(value);
        }

        public A getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return "Success{" +
                "value=" + value +
                '}';
        }
    }

    final class Failure implements Result {
        private final String label;
        private final String error;
        private final Location location;

        public Failure(String label, String error, Location location) {
            this.label = label;
            this.error = error;
            this.location = location;
        }

        public static Failure of(String label, String error, Location location) {
            return new Failure(label, error, location);
        }

        @Override
        public String toString() {
            var errorLine = location.currentLine();
            var linePos = location.line();
            var colPos = location.column();
            var indentFormat = colPos == 0 ? "%s" : String.format("%%%ds", colPos);

            var failureCaret = String.format(indentFormat + "^%s", "", error);
            return String.format("Line:%d Col:%d Error parsing %s\n%s\n%s", linePos + 1, colPos + 1, label, errorLine, failureCaret);
        }
    }
}

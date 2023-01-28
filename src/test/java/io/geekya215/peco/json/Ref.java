package io.geekya215.peco.json;

public class Ref<T> {
    private T value;

    public Ref(T value) {
        this.value = value;
    }

    public static <T> Ref<T> of(T value) {
        return new Ref<>(value);
    }

    public T get() {
        return this.value;
    }

    public Ref<T> set(T value) {
        this.value = value;
        return this;
    }
}

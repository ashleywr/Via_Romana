package net.rasanovum.viaromana.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DelegatingConsumer<T> implements Consumer<T> {
    private final List<Consumer<T>> delegates = new ArrayList<>();

    public void add(Consumer<T> delegate) {
        delegates.add(delegate);
    }

    @Override
    public void accept(T value) {
        for (Consumer<T> delegate : delegates) {
            delegate.accept(value);
        }
    }
}

package net.rasanovum.viaromana.client.search;

import net.minecraft.core.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public record RegistryBackedList<T>(Registry<T> registry, Class<T> type) implements List<T> {
    @Override
    public int size() {
        return registry.size();
    }

    @Override
    public boolean isEmpty() {
        return registry.size() == 0;
    }

    @Override
    public boolean contains(Object object) {
        return type.isInstance(object) && registry.getResourceKey(type.cast(object)).isPresent();
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return registry.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return registry.stream().toArray();
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] target) {
        return registry.stream().toList().toArray(target);
    }

    @Override
    public boolean add(T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        return collection.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index) {
        return registry.byId(index);
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object object) {
        return type.isInstance(object) ? registry.getId(type.cast(object)) : -1;
    }

    @Override
    public int lastIndexOf(Object object) {
        return type.isInstance(object) ? registry.getId(type.cast(object)) : -1;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return registry.stream().toList().listIterator();
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return registry.stream().toList().listIterator(index);
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return registry.stream().toList().subList(fromIndex, toIndex);
    }
}

package net.rasanovum.viaromana.client.search;

import net.minecraft.client.searchtree.SearchTree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record MappedSearchTree<T, Z>(SearchTree<T> tree, Function<T, Z> mapper) implements SearchTree<Z> {
    @Override
    public List<Z> search(String query) {
        List<T> matches = tree.search(query);
        List<Z> mapped = new ArrayList<>(matches.size());
        for (T match : matches) {
            mapped.add(mapper.apply(match));
        }
        return mapped;
    }
}

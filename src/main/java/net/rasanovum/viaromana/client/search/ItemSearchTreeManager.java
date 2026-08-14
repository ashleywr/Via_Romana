package net.rasanovum.viaromana.client.search;

import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public final class ItemSearchTreeManager {
    private static SearchTree<ItemStack> itemSearch;

    private ItemSearchTreeManager() {
    }

    public static void reset() {
        itemSearch = null;
    }

    public static SearchTree<ItemStack> getSearchTree() {
        if (itemSearch == null) {
            itemSearch = new MappedSearchTree<>(
                    new IdSearchTree<>(
                            item -> item.builtInRegistryHolder()
                                    .unwrapKey()
                                    .map(key -> Stream.of(key.location()))
                                    .orElseGet(Stream::empty),
                            new RegistryBackedList<>(BuiltInRegistries.ITEM, Item.class)
                    ),
                    Item::getDefaultInstance
            );
        }
        return itemSearch;
    }
}

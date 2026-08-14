package net.rasanovum.viaromana.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.rasanovum.viaromana.ViaRomana;
import net.rasanovum.viaromana.path.Node;
import net.rasanovum.viaromana.util.VersionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public final class DestinationIconRegistry {
    private static final byte[] PNG_SIGNATURE = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final String CUSTOM_ICON_DIRECTORY = "textures/screens/destination_icons";
    private static final String ITEM_ICON_DIRECTORY = "via_romana/destination_icons";
    private static final String PNG_SUFFIX = ".png";
    private static final String JSON_SUFFIX = ".json";
    private static final int ICON_WIDTH = 16;
    private static final int ICON_HEIGHT = 16;
    private static final int MAX_ICON_BYTES = 16 * 1024;
    private static final int MAX_ICON_DEFINITION_BYTES = 4 * 1024;
    private static final int MAX_CUSTOM_ICONS = 64;
    private static final ResourceLocation FALLBACK_TEXTURE = VersionUtils.getLocation("via_romana:textures/screens/marker_signpost.png");

    private static List<Entry> icons = List.of();
    private static Map<ResourceLocation, Entry> iconsById = Map.of();
    private static final Map<ResourceLocation, Entry> dynamicItemIcons = new HashMap<>();
    private static boolean loaded = false;

    private DestinationIconRegistry() {
    }

    public enum Kind {
        TEXTURE,
        ITEM
    }

    public record Entry(ResourceLocation id, Kind kind, ResourceLocation texture, ItemStack itemStack) {
        public static Entry texture(ResourceLocation id, ResourceLocation texture) {
            return new Entry(id, Kind.TEXTURE, texture, ItemStack.EMPTY);
        }

        public static Entry item(ResourceLocation id, ItemStack itemStack) {
            return new Entry(id, Kind.ITEM, FALLBACK_TEXTURE, itemStack.copy());
        }
    }

    public static List<Entry> getIcons() {
        ensureLoaded();
        return icons;
    }

    public static ResourceLocation getTexture(ResourceLocation id) {
        Entry entry = getEntry(id);
        return entry.texture();
    }

    public static Entry getEntry(ResourceLocation id) {
        ensureLoaded();
        ResourceLocation resolvedId = id != null ? id : Node.DEFAULT_DESTINATION_ICON;
        Entry entry = iconsById.get(resolvedId);
        if (entry == null) entry = dynamicItemIcons.get(resolvedId);
        if (entry == null) {
            entry = createItemEntry(resolvedId);
            if (entry != null) dynamicItemIcons.put(resolvedId, entry);
        }
        if (entry == null) entry = iconsById.get(Node.DEFAULT_DESTINATION_ICON);
        return entry != null ? entry : Entry.texture(Node.DEFAULT_DESTINATION_ICON, FALLBACK_TEXTURE);
    }

    public static void reload() {
        dynamicItemIcons.clear();
        loaded = false;
    }

    private static void ensureLoaded() {
        if (loaded) return;

        LinkedHashMap<ResourceLocation, Entry> discovered = new LinkedHashMap<>();
        addBuiltInIcons(discovered);
        addResourcePackIcons(discovered);
        addItemIcons(discovered);

        iconsById = Map.copyOf(discovered);
        icons = List.copyOf(discovered.values());
        loaded = true;
    }

    private static void addBuiltInIcons(Map<ResourceLocation, Entry> destination) {
        for (ResourceLocation id : Node.BUILT_IN_DESTINATION_ICONS) {
            ResourceLocation texture = VersionUtils.getLocation(
                    "via_romana:textures/screens/marker_" + id.getPath() + ".png"
            );
            destination.put(id, Entry.texture(id, texture));
        }
    }

    private static void addResourcePackIcons(Map<ResourceLocation, Entry> destination) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) return;

        ResourceManager resourceManager = minecraft.getResourceManager();
        Map<ResourceLocation, Resource> resources = resourceManager
                .listResources(CUSTOM_ICON_DIRECTORY, id -> id.getPath().endsWith(PNG_SUFFIX));
        List<ResourceLocation> textures = new ArrayList<>(resources.keySet());
        textures.sort(Comparator.comparing(ResourceLocation::toString));

        int customIconCount = 0;
        for (ResourceLocation texture : textures) {
            if (customIconCount >= MAX_CUSTOM_ICONS) {
                ViaRomana.LOGGER.warn("Skipping destination icon {} because the custom icon limit ({}) was reached", texture, MAX_CUSTOM_ICONS);
                continue;
            }

            String path = texture.getPath();
            String iconPath = path.substring(CUSTOM_ICON_DIRECTORY.length() + 1, path.length() - PNG_SUFFIX.length());
            if (iconPath.isBlank()) continue;

            ResourceLocation id = VersionUtils.getLocation(texture.getNamespace(), iconPath);
            if (destination.containsKey(id)) {
                ViaRomana.LOGGER.warn("Skipping destination icon {} because icon id {} is already registered", texture, id);
                continue;
            }

            Resource resource = resources.get(texture);
            if (resource == null || !isValidIconResource(id, texture, resource)) continue;

            destination.put(id, Entry.texture(id, texture));
            customIconCount++;
        }
    }

    private static void addItemIcons(Map<ResourceLocation, Entry> destination) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) return;

        ResourceManager resourceManager = minecraft.getResourceManager();
        Map<ResourceLocation, Resource> resources = resourceManager
                .listResources(ITEM_ICON_DIRECTORY, id -> id.getPath().endsWith(JSON_SUFFIX));
        List<ResourceLocation> definitions = new ArrayList<>(resources.keySet());
        definitions.sort(Comparator.comparing(ResourceLocation::toString));

        int customIconCount = 0;
        for (ResourceLocation definition : definitions) {
            if (customIconCount >= MAX_CUSTOM_ICONS) {
                ViaRomana.LOGGER.warn("Skipping destination icon {} because the item icon limit ({}) was reached", definition, MAX_CUSTOM_ICONS);
                continue;
            }

            String path = definition.getPath();
            String iconPath = path.substring(ITEM_ICON_DIRECTORY.length() + 1, path.length() - JSON_SUFFIX.length());
            if (iconPath.isBlank()) continue;

            ResourceLocation id = VersionUtils.getLocation(definition.getNamespace(), iconPath);
            if (destination.containsKey(id)) {
                ViaRomana.LOGGER.warn("Skipping item destination icon {} because icon id {} is already registered", definition, id);
                continue;
            }

            Resource resource = resources.get(definition);
            Entry entry = resource != null ? readItemIconDefinition(id, definition, resource) : null;
            if (entry == null) continue;

            destination.put(id, entry);
            customIconCount++;
        }
    }

    private static boolean isValidIconResource(ResourceLocation id, ResourceLocation texture, Resource resource) {
        byte[] bytes;
        try (InputStream input = resource.open()) {
            bytes = input.readNBytes(MAX_ICON_BYTES + 1);
        } catch (IOException e) {
            ViaRomana.LOGGER.warn("Skipping destination icon {} ({}) because it could not be read: {}", id, texture, e.getMessage());
            return false;
        }

        if (bytes.length > MAX_ICON_BYTES) {
            ViaRomana.LOGGER.warn("Skipping destination icon {} ({}) because it is larger than {} bytes", id, texture, MAX_ICON_BYTES);
            return false;
        }

        int[] dimensions = readPngHeaderDimensions(bytes);
        if (dimensions == null) {
            ViaRomana.LOGGER.warn("Skipping destination icon {} ({}) because it does not have a valid PNG header", id, texture);
            return false;
        }

        if (dimensions[0] != ICON_WIDTH || dimensions[1] != ICON_HEIGHT) {
            ViaRomana.LOGGER.warn("Skipping destination icon {} ({}) because it declares {}x{} instead of {}x{}",
                    id, texture, dimensions[0], dimensions[1], ICON_WIDTH, ICON_HEIGHT);
            return false;
        }

        try (NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes))) {
            if (image.getWidth() != ICON_WIDTH || image.getHeight() != ICON_HEIGHT) {
                ViaRomana.LOGGER.warn("Skipping destination icon {} ({}) because it is {}x{} instead of {}x{}",
                        id, texture, image.getWidth(), image.getHeight(), ICON_WIDTH, ICON_HEIGHT);
                return false;
            }
        } catch (IOException | RuntimeException e) {
            ViaRomana.LOGGER.warn("Skipping destination icon {} ({}) because it is not a valid PNG: {}", id, texture, e.getMessage());
            return false;
        }

        return true;
    }

    private static int[] readPngHeaderDimensions(byte[] bytes) {
        if (bytes.length < 24) return null;
        for (int i = 0; i < PNG_SIGNATURE.length; i++) {
            if (bytes[i] != PNG_SIGNATURE[i]) return null;
        }
        if (bytes[12] != 0x49 || bytes[13] != 0x48 || bytes[14] != 0x44 || bytes[15] != 0x52) return null;

        int width = readBigEndianInt(bytes, 16);
        int height = readBigEndianInt(bytes, 20);
        if (width <= 0 || height <= 0) return null;
        return new int[] {width, height};
    }

    private static int readBigEndianInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24)
                | ((bytes[offset + 1] & 0xFF) << 16)
                | ((bytes[offset + 2] & 0xFF) << 8)
                | (bytes[offset + 3] & 0xFF);
    }

    private static Entry readItemIconDefinition(ResourceLocation id, ResourceLocation definition, Resource resource) {
        byte[] bytes;
        try (InputStream input = resource.open()) {
            bytes = input.readNBytes(MAX_ICON_DEFINITION_BYTES + 1);
        } catch (IOException e) {
            ViaRomana.LOGGER.warn("Skipping item destination icon {} ({}) because it could not be read: {}", id, definition, e.getMessage());
            return null;
        }

        if (bytes.length > MAX_ICON_DEFINITION_BYTES) {
            ViaRomana.LOGGER.warn("Skipping item destination icon {} ({}) because it is larger than {} bytes", id, definition, MAX_ICON_DEFINITION_BYTES);
            return null;
        }

        JsonObject json;
        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (RuntimeException | IOException e) {
            ViaRomana.LOGGER.warn("Skipping item destination icon {} ({}) because it is not valid JSON: {}", id, definition, e.getMessage());
            return null;
        }

        String type = getString(json, "type");
        if (!"item".equals(type)) {
            ViaRomana.LOGGER.warn("Skipping item destination icon {} ({}) because type must be \"item\"", id, definition);
            return null;
        }

        String itemName = getString(json, "item");
        if (itemName.isBlank()) {
            ViaRomana.LOGGER.warn("Skipping item destination icon {} ({}) because it is missing an item id", id, definition);
            return null;
        }

        ResourceLocation itemId = parseItemId(itemName);
        if (itemId == null) {
            ViaRomana.LOGGER.warn("Skipping item destination icon {} ({}) because item id {} is not a valid namespaced id", id, definition, itemName);
            return null;
        }

        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            ViaRomana.LOGGER.warn("Skipping item destination icon {} ({}) because item {} is not registered", id, definition, itemId);
            return null;
        }

        Item item = BuiltInRegistries.ITEM.get(itemId);
        ItemStack stack = item.getDefaultInstance();
        if (stack.isEmpty()) {
            ViaRomana.LOGGER.warn("Skipping item destination icon {} ({}) because item {} has an empty default stack", id, definition, itemId);
            return null;
        }

        return Entry.item(id, stack);
    }

    private static Entry createItemEntry(ResourceLocation id) {
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) return null;

        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) return null;

        ItemStack stack = item.getDefaultInstance();
        return stack.isEmpty() ? null : Entry.item(id, stack);
    }

    private static String getString(JsonObject json, String field) {
        try {
            return json.has(field) ? json.get(field).getAsString() : "";
        } catch (RuntimeException e) {
            return "";
        }
    }

    private static ResourceLocation parseItemId(String value) {
        if (value == null || value.isBlank() || value.indexOf(':') < 0) return null;
        try {
            return VersionUtils.getLocation(value);
        } catch (Exception e) {
            return null;
        }
    }
}

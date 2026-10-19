package com.kodari.hashiramcv2;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ItemFactory {
    public static final String HASHIRA_TOTEM = "hashira_totem";
    public static final String DEMON_TOTEM = "demon_totem";
    public static final String HUNTER_TOTEM = "hunter_totem";
    public static final String COPPER_CHARM = "copper_charm";
    public static final String GOLD_CHARM = "gold_charm";
    public static final String DIAMOND_CHARM = "diamond_charm";
    public static final String KATANA = "katana";
    public static final String KATANA_BLACK = "katana_black";
    public static final String KATANA_RED = "katana_red";
    public static final String KATANA_GOLD = "katana_gold";
    public static final String SPEED_1 = "speed_1";
    public static final String SPEED_2 = "speed_2";
    public static final String SPEED_3 = "speed_3";
    public static final String STRENGTH_1 = "strength_1";
    public static final String STRENGTH_2 = "strength_2";
    public static final String STRENGTH_3 = "strength_3";
    public static final String HASTE_SPEED_1 = "haste_speed_1";
    public static final String HASTE_SPEED_2 = "haste_speed_2";
    public static final String HASTE_SPEED_3 = "haste_speed_3";
    public static final String NIGHT_VISION = "night_vision";
    public static final String HEARTS_2 = "hearts_2";
    public static final String HEARTS_4 = "hearts_4";
    public static final String HEARTS_8 = "hearts_8";
    public static final String HEARTS_10 = "hearts_10";
    public static final String HEARTS_12 = "hearts_12";
    public static final String HEARTS_18 = "hearts_18";
    public static final String HEARTS_20 = "hearts_20";
    public static final String UNIFORM_BLACK_HELMET = "uniform_black_helmet";
    public static final String UNIFORM_BLACK_CHESTPLATE = "uniform_black_chestplate";
    public static final String UNIFORM_BLACK_LEGGINGS = "uniform_black_leggings";
    public static final String UNIFORM_BLACK_BOOTS = "uniform_black_boots";
    public static final String UNIFORM_RED_HELMET = "uniform_red_helmet";
    public static final String UNIFORM_RED_CHESTPLATE = "uniform_red_chestplate";
    public static final String UNIFORM_RED_LEGGINGS = "uniform_red_leggings";
    public static final String UNIFORM_RED_BOOTS = "uniform_red_boots";
    public static final String UNIFORM_GOLD_HELMET = "uniform_gold_helmet";
    public static final String UNIFORM_GOLD_CHESTPLATE = "uniform_gold_chestplate";
    public static final String UNIFORM_GOLD_LEGGINGS = "uniform_gold_leggings";
    public static final String UNIFORM_GOLD_BOOTS = "uniform_gold_boots";
    public static final String SAMURAI_PICKAXE = "samurai_pickaxe";
    public static final String SAMURAI_AXE = "samurai_axe";
    public static final String SAMURAI_SHOVEL = "samurai_shovel";
    public static final String SAMURAI_HOE = "samurai_hoe";
    public static final String DEMON_TROPHY = "demon_trophy";
    public static final String NICHIRIN_ORE = "nichirin_ore";
    public static final String NICHIRIN_INGOT = "nichirin_ingot";

    private final HashiramcV2Plugin plugin;
    private final NamespacedKey itemKey;

    public ItemFactory(HashiramcV2Plugin plugin) {
        this.plugin = plugin;
        itemKey = new NamespacedKey(plugin, "special_item");
    }

    public ItemStack create(String id) {
        return switch (id) {
            case HASHIRA_TOTEM -> createItem(id, "TOTEM_OF_UNDYING", "&6&lTótem Hashira",
                    Arrays.asList("&7Libera el poder de un Hashira.", "&eEfectos equilibrados durante 1 minuto."), false);
            case DEMON_TOTEM -> createItem(id, "TOTEM_OF_UNDYING", "&c&lTótem Demonio",
                    Arrays.asList("&7Despierta el poder de un demonio.", "&cReduce tu corazón máximo durante 1 minuto."), false);
            case HUNTER_TOTEM -> createItem(id, "TOTEM_OF_UNDYING", "&a&lTótem Cazador de Demonios",
                    Arrays.asList("&7Te da el poder de un cazador de demonios.", "&eUn impulso breve al salvarte de la muerte."), false);
            case COPPER_CHARM -> createItem(id, "COPPER_INGOT", "&6Amuleto Combinado I",
                    Arrays.asList("&7Sosténlo en la mano secundaria.", "&bVelocidad I &7| &cFuerza I"), false);
            case GOLD_CHARM -> createItem(id, "GOLD_INGOT", "&eAmuleto Combinado II",
                    Arrays.asList("&7Sosténlo en la mano secundaria.", "&bVelocidad II &7| &cFuerza II"), false);
            case DIAMOND_CHARM -> createItem(id, "DIAMOND", "&bAmuleto Combinado III",
                    Arrays.asList("&7Sosténlo en la mano secundaria.", "&bVelocidad III &7| &cFuerza III"), false);
            case SPEED_1 -> createItem(id, "FEATHER", "&bSello de Velocidad I", List.of("&7Mano secundaria", "&bVelocidad I"), false);
            case SPEED_2 -> createItem(id, "RABBIT_FOOT", "&bSello de Velocidad II", List.of("&7Mano secundaria", "&bVelocidad II"), false);
            case SPEED_3 -> createItem(id, "PHANTOM_MEMBRANE", "&bSello de Velocidad III", List.of("&7Mano secundaria", "&bVelocidad III"), false);
            case STRENGTH_1 -> createItem(id, "BLAZE_POWDER", "&cSello de Fuerza I", List.of("&7Mano secundaria", "&cFuerza I"), false);
            case STRENGTH_2 -> createItem(id, "FIRE_CHARGE", "&cSello de Fuerza II", List.of("&7Mano secundaria", "&cFuerza II"), false);
            case STRENGTH_3 -> createItem(id, "BLAZE_ROD", "&cSello de Fuerza III", List.of("&7Mano secundaria", "&cFuerza III"), false);
            case HASTE_SPEED_1 -> createItem(id, "PRISMARINE_CRYSTALS", "&3Sello de Marea I", List.of("&7Mano secundaria", "&3Prisa minera I &7| &bVelocidad I"), false);
            case HASTE_SPEED_2 -> createItem(id, "PRISMARINE_SHARD", "&3Sello de Marea II", List.of("&7Mano secundaria", "&3Prisa minera II &7| &bVelocidad II"), false);
            case HASTE_SPEED_3 -> createItem(id, "HEART_OF_THE_SEA", "&3Sello de Marea III", List.of("&7Mano secundaria", "&3Prisa minera III &7| &bVelocidad III"), false);
            case NIGHT_VISION -> createItem(id, "ENDER_EYE", "&5Amuleto de Visión Nocturna", List.of("&7Mano secundaria", "&5Visión nocturna"), false);
            case HEARTS_2 -> createItem(id, "GOLDEN_APPLE", "&cSello Vital II", List.of("&7Mano secundaria", "&c+2 corazones"), false);
            case HEARTS_4 -> createItem(id, "ENCHANTED_GOLDEN_APPLE", "&cSello Vital IV", List.of("&7Mano secundaria", "&c+4 corazones"), false);
            case HEARTS_8 -> createItem(id, "HEART_OF_THE_SEA", "&cSello Vital VIII", List.of("&7Mano secundaria", "&c+8 corazones"), false);
            case HEARTS_10 -> createItem(id, "NETHER_STAR", "&cSello Vital X", List.of("&7Mano secundaria", "&c+10 corazones"), false);
            case HEARTS_12 -> createItem(id, "NETHER_STAR", "&cSello Vital XII", List.of("&7Mano secundaria", "&c+12 corazones"), false);
            case HEARTS_18 -> createItem(id, "NETHER_STAR", "&cSello Vital XVIII", List.of("&7Mano secundaria", "&c+18 corazones"), false);
            case HEARTS_20 -> createItem(id, "NETHER_STAR", "&c&lSello Vital XX", List.of("&7Mano secundaria", "&c+20 corazones"), false);
            case UNIFORM_BLACK_HELMET -> createItem(id, "NETHERITE_HELMET", "&8&lUniforme de Cazador Negro · Casco", List.of("&7Uniforme reforzado de cazador.", "&8Protección V"), false);
            case UNIFORM_BLACK_CHESTPLATE -> createItem(id, "NETHERITE_CHESTPLATE", "&8&lUniforme de Cazador Negro · Peto", List.of("&7Uniforme reforzado de cazador.", "&8Protección V"), false);
            case UNIFORM_BLACK_LEGGINGS -> createItem(id, "NETHERITE_LEGGINGS", "&8&lUniforme de Cazador Negro · Pantalones", List.of("&7Uniforme reforzado de cazador.", "&8Protección V"), false);
            case UNIFORM_BLACK_BOOTS -> createItem(id, "NETHERITE_BOOTS", "&8&lUniforme de Cazador Negro · Botas", List.of("&7Uniforme reforzado de cazador.", "&8Protección V"), false);
            case UNIFORM_RED_HELMET -> createItem(id, "NETHERITE_HELMET", "&c&lUniforme de Cazador Rojo · Casco", List.of("&7Uniforme reforzado de cazador.", "&cProtección V"), false);
            case UNIFORM_RED_CHESTPLATE -> createItem(id, "NETHERITE_CHESTPLATE", "&c&lUniforme de Cazador Rojo · Peto", List.of("&7Uniforme reforzado de cazador.", "&cProtección V"), false);
            case UNIFORM_RED_LEGGINGS -> createItem(id, "NETHERITE_LEGGINGS", "&c&lUniforme de Cazador Rojo · Pantalones", List.of("&7Uniforme reforzado de cazador.", "&cProtección V"), false);
            case UNIFORM_RED_BOOTS -> createItem(id, "NETHERITE_BOOTS", "&c&lUniforme de Cazador Rojo · Botas", List.of("&7Uniforme reforzado de cazador.", "&cProtección V"), false);
            case UNIFORM_GOLD_HELMET -> createItem(id, "NETHERITE_HELMET", "&6&lUniforme de Cazador Dorado · Casco", List.of("&7Uniforme reforzado de cazador.", "&6Protección V"), false);
            case UNIFORM_GOLD_CHESTPLATE -> createItem(id, "NETHERITE_CHESTPLATE", "&6&lUniforme de Cazador Dorado · Peto", List.of("&7Uniforme reforzado de cazador.", "&6Protección V"), false);
            case UNIFORM_GOLD_LEGGINGS -> createItem(id, "NETHERITE_LEGGINGS", "&6&lUniforme de Cazador Dorado · Pantalones", List.of("&7Uniforme reforzado de cazador.", "&6Protección V"), false);
            case UNIFORM_GOLD_BOOTS -> createItem(id, "NETHERITE_BOOTS", "&6&lUniforme de Cazador Dorado · Botas", List.of("&7Uniforme reforzado de cazador.", "&6Protección V"), false);
            case SAMURAI_PICKAXE -> createItem(id, "NETHERITE_PICKAXE", "&8&lPico Samurai", List.of("&7Herramienta de acero Nichirin."), false);
            case SAMURAI_AXE -> createItem(id, "NETHERITE_AXE", "&8&lHacha Samurai", List.of("&7Herramienta de acero Nichirin."), false);
            case SAMURAI_SHOVEL -> createItem(id, "NETHERITE_SHOVEL", "&8&lPala Samurai", List.of("&7Herramienta de acero Nichirin."), false);
            case SAMURAI_HOE -> createItem(id, "NETHERITE_HOE", "&8&lAzada Samurai", List.of("&7Herramienta de acero Nichirin."), false);
            case DEMON_TROPHY -> createItem(id, "PLAYER_HEAD", "&4&lTrofeo de Demonio", List.of("&7Recuerdo de una cacería peligrosa."), false);
            case NICHIRIN_ORE -> createItem(id, "NETHER_GOLD_ORE", "&c&lMena de Nichirin", List.of("&7Mineral extraño de las montañas."), false);
            case NICHIRIN_INGOT -> createItem(id, "GOLD_INGOT", "&6&lLingote de Nichirin", List.of("&7Metal refinado para armas especiales."), false);
            case KATANA, KATANA_RED -> createItem(id, "IRON_SWORD", "&c&lKatana Nichirin Roja",
                    Arrays.asList("&7Hoja de acero de estilo tradicional.", "&7Algo más dañina que una espada de hierro."), true);
            case KATANA_BLACK -> createItem(id, "IRON_SWORD", "&8&lKatana Nichirin Negra",
                    Arrays.asList("&7Hoja oscura de acero forjado.", "&7Algo más dañina que una espada de hierro."), true);
            case KATANA_GOLD -> createItem(id, "IRON_SWORD", "&6&lKatana Nichirin Dorada",
                    Arrays.asList("&7Hoja dorada de acero forjado.", "&7Algo más dañina que una espada de hierro."), true);
            default -> null;
        };
    }

    public Map<String, Integer> getOffhandEffects(String id) {
        if (id == null) {
            return Map.of();
        }
        return switch (id) {
            case COPPER_CHARM -> Map.of("SPEED", 0, "STRENGTH", 0);
            case GOLD_CHARM -> Map.of("SPEED", 1, "STRENGTH", 1);
            case DIAMOND_CHARM -> Map.of("SPEED", 2, "STRENGTH", 2);
            case SPEED_1 -> Map.of("SPEED", 0);
            case SPEED_2 -> Map.of("SPEED", 1);
            case SPEED_3 -> Map.of("SPEED", 2);
            case STRENGTH_1 -> Map.of("STRENGTH", 0);
            case STRENGTH_2 -> Map.of("STRENGTH", 1);
            case STRENGTH_3 -> Map.of("STRENGTH", 2);
            case HASTE_SPEED_1 -> Map.of("HASTE", 0, "SPEED", 0);
            case HASTE_SPEED_2 -> Map.of("HASTE", 1, "SPEED", 1);
            case HASTE_SPEED_3 -> Map.of("HASTE", 2, "SPEED", 2);
            case NIGHT_VISION -> Map.of("NIGHT_VISION", 0);
            default -> Map.of();
        };
    }

    public int getOffhandHeartBonus(String id) {
        if (id == null) {
            return 0;
        }
        return switch (id) {
            case HEARTS_2 -> 2;
            case HEARTS_4 -> 4;
            case HEARTS_8 -> 8;
            case HEARTS_10 -> 10;
            case HEARTS_12 -> 12;
            case HEARTS_18 -> 18;
            case HEARTS_20 -> 20;
            default -> 0;
        };
    }

    public boolean isOffhandItem(String id) {
        return !getOffhandEffects(id).isEmpty() || getOffhandHeartBonus(id) > 0;
    }

    private ItemStack createItem(String id, String material, String name, List<String> lore, boolean katana) {
        ItemStack item = XMaterial.matchXMaterial(material).map(XMaterial::parseItem).orElse(null);
        if (item == null) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        meta.setDisplayName(color(name));
        meta.setLore(lore.stream().map(ItemFactory::color).toList());
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, id);
        String modelId = modelId(id);
        if (modelId != null) {
            meta.setItemModel(new NamespacedKey(plugin, modelId));
        }
        if (katana) {
            XEnchantment.matchXEnchantment("SHARPNESS")
                    .ifPresent(enchantment -> meta.addEnchant(enchantment.getEnchant(), 1, true));
        }
        if (id.startsWith("uniform_")) {
            XEnchantment.matchXEnchantment("PROTECTION")
                    .ifPresent(enchantment -> meta.addEnchant(enchantment.getEnchant(), 5, true));
            XEnchantment.matchXEnchantment("UNBREAKING")
                    .ifPresent(enchantment -> meta.addEnchant(enchantment.getEnchant(), 3, true));
        }
        if (id.startsWith("samurai_")) {
            XEnchantment.matchXEnchantment("EFFICIENCY")
                    .ifPresent(enchantment -> meta.addEnchant(enchantment.getEnchant(), 5, true));
            XEnchantment.matchXEnchantment("UNBREAKING")
                    .ifPresent(enchantment -> meta.addEnchant(enchantment.getEnchant(), 3, true));
        }
        if (id.startsWith("uniform_")) {
            XEnchantment.matchXEnchantment("PROTECTION")
                    .ifPresent(enchantment -> meta.addEnchant(enchantment.getEnchant(), 5, true));
            XEnchantment.matchXEnchantment("UNBREAKING")
                    .ifPresent(enchantment -> meta.addEnchant(enchantment.getEnchant(), 3, true));
        }
        if (id.startsWith("samurai_")) {
            XEnchantment.matchXEnchantment("EFFICIENCY")
                    .ifPresent(enchantment -> meta.addEnchant(enchantment.getEnchant(), 5, true));
            XEnchantment.matchXEnchantment("UNBREAKING")
                    .ifPresent(enchantment -> meta.addEnchant(enchantment.getEnchant(), 3, true));
        }
        item.setItemMeta(meta);
        return item;
    }

    private String modelId(String id) {
        return switch (id) {
            case HASHIRA_TOTEM -> "hashira_totem";
            case DEMON_TOTEM -> "demon_totem";
            case HUNTER_TOTEM -> "haste_token";
            case COPPER_CHARM, STRENGTH_1, STRENGTH_2, STRENGTH_3 -> "strength_token";
            case GOLD_CHARM, SPEED_1, SPEED_2, SPEED_3, HASTE_SPEED_1, HASTE_SPEED_2, HASTE_SPEED_3 -> "haste_token";
            case DIAMOND_CHARM, HEARTS_2, HEARTS_4, HEARTS_8, HEARTS_10, HEARTS_12, HEARTS_18, HEARTS_20 -> "heart_token";
            case NIGHT_VISION -> "night_vision";
            case KATANA, KATANA_RED -> "katana_red";
            case KATANA_BLACK -> "katana_black";
            case KATANA_GOLD -> "katana_gold";
            case SAMURAI_PICKAXE -> "samurai_pickaxe";
            case SAMURAI_AXE -> "samurai_axe";
            case SAMURAI_SHOVEL -> "samurai_shovel";
            case SAMURAI_HOE -> "samurai_hoe";
            case DEMON_TROPHY -> "demon_trophy";
            case NICHIRIN_ORE -> "nichirin_ore";
            default -> null;
        };
    }

    public String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
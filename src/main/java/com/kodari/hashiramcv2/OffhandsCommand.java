package com.kodari.hashiramcv2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public final class OffhandsCommand implements CommandExecutor {
    private final ItemFactory itemFactory;

    public OffhandsCommand(ItemFactory itemFactory) {
        this.itemFactory = itemFactory;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "Tótems: hashira, demonio, cazador. Katanas: negra, roja, dorada.");
            sender.sendMessage(ChatColor.GOLD + "Offhands: velocidad1-3, fuerza1-3, combinado1-3, prisa1-3, vision, corazones2/4/8/10/12/18/20.");
            sender.sendMessage(ChatColor.GOLD + "Equipo: uniforme_<color>_<pieza>, samurai_<herramienta>, trofeo_demonio, mena_nichirin, lingote_nichirin.");
            sender.sendMessage(ChatColor.YELLOW + "Uso: /offhands give <jugador> <objeto>");
            return true;
        }
        if (!args[0].equalsIgnoreCase("give") || args.length != 3) {
            sender.sendMessage(ChatColor.YELLOW + "Uso: /offhands give <jugador> <objeto>");
            return true;
        }
        if (!sender.hasPermission("hashiramcv2.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para entregar estos objetos.");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Ese jugador no está conectado.");
            return true;
        }
        String id = resolveId(args[2]);
        if (id == null) {
            sender.sendMessage(ChatColor.RED + "Objeto desconocido. Usa /offhands para ver la lista.");
            return true;
        }
        ItemStack item = itemFactory.create(id);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "No se pudo crear ese objeto en esta versión del servidor.");
            return true;
        }
        target.getInventory().addItem(item).values().forEach(remaining ->
                target.getWorld().dropItemNaturally(target.getLocation(), remaining));
        sender.sendMessage(ChatColor.GREEN + "Entregaste el objeto a " + target.getName() + ".");
        return true;
    }

    private String resolveId(String input) {
        return switch (input.toLowerCase(Locale.ROOT)) {
            case "hashira" -> ItemFactory.HASHIRA_TOTEM;
            case "demonio", "demon" -> ItemFactory.DEMON_TOTEM;
            case "cazador", "hunter" -> ItemFactory.HUNTER_TOTEM;
            case "cobre", "copper" -> ItemFactory.COPPER_CHARM;
            case "oro", "gold" -> ItemFactory.GOLD_CHARM;
            case "diamante", "diamond" -> ItemFactory.DIAMOND_CHARM;
            case "velocidad1", "speed1", "speed_1" -> ItemFactory.SPEED_1;
            case "velocidad2", "speed2", "speed_2" -> ItemFactory.SPEED_2;
            case "velocidad3", "speed3", "speed_3" -> ItemFactory.SPEED_3;
            case "fuerza1", "strength1", "strength_1" -> ItemFactory.STRENGTH_1;
            case "fuerza2", "strength2", "strength_2" -> ItemFactory.STRENGTH_2;
            case "fuerza3", "strength3", "strength_3" -> ItemFactory.STRENGTH_3;
            case "combinado1", "combo1" -> ItemFactory.COPPER_CHARM;
            case "combinado2", "combo2" -> ItemFactory.GOLD_CHARM;
            case "combinado3", "combo3" -> ItemFactory.DIAMOND_CHARM;
            case "prisa1", "haste1" -> ItemFactory.HASTE_SPEED_1;
            case "prisa2", "haste2" -> ItemFactory.HASTE_SPEED_2;
            case "prisa3", "haste3" -> ItemFactory.HASTE_SPEED_3;
            case "vision", "visionnocturna", "nightvision" -> ItemFactory.NIGHT_VISION;
            case "corazones2", "heart2" -> ItemFactory.HEARTS_2;
            case "corazones4", "heart4" -> ItemFactory.HEARTS_4;
            case "corazones8", "heart8" -> ItemFactory.HEARTS_8;
            case "corazones10", "heart10" -> ItemFactory.HEARTS_10;
            case "corazones12", "heart12" -> ItemFactory.HEARTS_12;
            case "corazones18", "heart18" -> ItemFactory.HEARTS_18;
            case "corazones20", "heart20" -> ItemFactory.HEARTS_20;
            case "katana", "katana_roja", "red_katana" -> ItemFactory.KATANA_RED;
            case "katana_negra", "black_katana" -> ItemFactory.KATANA_BLACK;
            case "katana_dorada", "gold_katana" -> ItemFactory.KATANA_GOLD;
            case "uniforme_negro_casco", "uniform_black_helmet" -> ItemFactory.UNIFORM_BLACK_HELMET;
            case "uniforme_negro_peto", "uniform_black_chestplate" -> ItemFactory.UNIFORM_BLACK_CHESTPLATE;
            case "uniforme_negro_pantalones", "uniform_black_leggings" -> ItemFactory.UNIFORM_BLACK_LEGGINGS;
            case "uniforme_negro_botas", "uniform_black_boots" -> ItemFactory.UNIFORM_BLACK_BOOTS;
            case "uniforme_rojo_casco", "uniform_red_helmet" -> ItemFactory.UNIFORM_RED_HELMET;
            case "uniforme_rojo_peto", "uniform_red_chestplate" -> ItemFactory.UNIFORM_RED_CHESTPLATE;
            case "uniforme_rojo_pantalones", "uniform_red_leggings" -> ItemFactory.UNIFORM_RED_LEGGINGS;
            case "uniforme_rojo_botas", "uniform_red_boots" -> ItemFactory.UNIFORM_RED_BOOTS;
            case "uniforme_dorado_casco", "uniform_gold_helmet" -> ItemFactory.UNIFORM_GOLD_HELMET;
            case "uniforme_dorado_peto", "uniform_gold_chestplate" -> ItemFactory.UNIFORM_GOLD_CHESTPLATE;
            case "uniforme_dorado_pantalones", "uniform_gold_leggings" -> ItemFactory.UNIFORM_GOLD_LEGGINGS;
            case "uniforme_dorado_botas", "uniform_gold_boots" -> ItemFactory.UNIFORM_GOLD_BOOTS;
            case "samurai_pico" -> ItemFactory.SAMURAI_PICKAXE;
            case "samurai_hacha" -> ItemFactory.SAMURAI_AXE;
            case "samurai_pala" -> ItemFactory.SAMURAI_SHOVEL;
            case "samurai_azada" -> ItemFactory.SAMURAI_HOE;
            case "trofeo_demonio", "demon_trophy" -> ItemFactory.DEMON_TROPHY;
            case "mena_nichirin", "nichirin_ore" -> ItemFactory.NICHIRIN_ORE;
            case "lingote_nichirin", "nichirin_ingot" -> ItemFactory.NICHIRIN_INGOT;
            default -> null;
        };
    }
}
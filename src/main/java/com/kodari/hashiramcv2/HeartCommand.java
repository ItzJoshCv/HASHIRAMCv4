package com.kodari.hashiramcv2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class HeartCommand implements CommandExecutor {
    private final HashiramcV2Plugin plugin;
    private final PlayerDataStore dataStore;
    private final HeartManager heartManager;

    public HeartCommand(HashiramcV2Plugin plugin, PlayerDataStore dataStore, HeartManager heartManager) {
        this.plugin = plugin;
        this.dataStore = dataStore;
        this.heartManager = heartManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Uso: /corazon <give|take|info> <jugador> [cantidad] [permanente|temporal] [minutos]");
            return true;
        }
        if (args[0].equalsIgnoreCase("info")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.YELLOW + "Uso: /corazon info <jugador>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            sendInfo(sender, target);
            return true;
        }
        if (!args[0].equalsIgnoreCase("give") && !args[0].equalsIgnoreCase("take")) {
            sender.sendMessage(ChatColor.YELLOW + "Uso: /corazon <give|take|info> <jugador> [cantidad] [permanente|temporal] [minutos]");
            return true;
        }
        if (!sender.hasPermission("hashiramcv2.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para modificar corazones.");
            return true;
        }
        if (args.length < 3 || args.length > 5) {
            sender.sendMessage(ChatColor.YELLOW + "Uso: /corazon " + args[0] + " <jugador> <cantidad> [permanente|temporal] [minutos]");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED + "La cantidad debe ser un número entero.");
            return true;
        }
        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "La cantidad debe ser mayor que cero.");
            return true;
        }

        String type = args.length >= 4 ? args[3].toLowerCase(Locale.ROOT) : "permanente";
        boolean temporary = type.equals("temporal") || type.equals("temporales");
        if (!temporary && !type.equals("permanente") && !type.equals("permanentes")) {
            sender.sendMessage(ChatColor.RED + "El tipo debe ser permanente o temporal.");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (temporary) {
            long minutes = plugin.getConfig().getLong("hearts.default-temporary-minutes", 60L);
            if (args.length == 5) {
                try {
                    minutes = Long.parseLong(args[4]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(ChatColor.RED + "Los minutos deben ser un número entero.");
                    return true;
                }
            }
            if (minutes <= 0) {
                sender.sendMessage(ChatColor.RED + "La duración debe ser mayor que cero.");
                return true;
            }
            int current = dataStore.getTemporaryHearts(target.getUniqueId());
            dataStore.setTemporaryHearts(target.getUniqueId(),
                    args[0].equalsIgnoreCase("give") ? current + amount : Math.max(0, current - amount),
                    System.currentTimeMillis() + minutes * 60_000L);
        } else {
            int current = dataStore.getPermanentHearts(target.getUniqueId());
            dataStore.setPermanentHearts(target.getUniqueId(),
                    args[0].equalsIgnoreCase("give") ? current + amount : Math.max(0, current - amount));
        }

        Player online = Bukkit.getPlayer(target.getUniqueId());
        if (online != null) {
            heartManager.apply(online);
        }
        sender.sendMessage(ChatColor.GREEN + "Actualizaste los corazones de " + target.getName() + ".");
        return true;
    }

    private void sendInfo(CommandSender sender, OfflinePlayer target) {
        int permanent = dataStore.getPermanentHearts(target.getUniqueId());
        int temporary = dataStore.getTemporaryHearts(target.getUniqueId());
        sender.sendMessage(ChatColor.GOLD + "Corazones de " + target.getName() + ": "
                + ChatColor.YELLOW + permanent + " permanentes, " + temporary + " temporales.");
    }
}
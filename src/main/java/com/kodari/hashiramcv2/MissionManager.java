package com.kodari.hashiramcv2;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Locale;

public final class MissionManager implements Listener, CommandExecutor {
    private final HashiramcV2Plugin plugin;
    private final PlayerDataStore dataStore;
    private final Economy economy;
    private final NamespacedKey demonKey;
    private final NamespacedKey demonTypeKey;

    public MissionManager(HashiramcV2Plugin plugin, PlayerDataStore dataStore, Economy economy) {
        this.plugin = plugin;
        this.dataStore = dataStore;
        this.economy = economy;
        demonKey = new NamespacedKey(plugin, "demon");
        demonTypeKey = new NamespacedKey(plugin, "demon_type");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onDemonKilled(EntityDeathEvent event) {
        Entity demon = event.getEntity();
        if (!demon.getPersistentDataContainer().has(demonKey, PersistentDataType.BYTE)) {
            return;
        }
        Player killer = demon instanceof org.bukkit.entity.LivingEntity living ? living.getKiller() : null;
        if (killer == null) {
            return;
        }
        String demonType = demon.getPersistentDataContainer().get(demonTypeKey, PersistentDataType.STRING);
        progress(killer, "DEMON_KILLS", null);
        if (demonType != null) {
            progress(killer, "DEMON_ID", demonType);
        }
    }

    @EventHandler
    public void onPlayerBounty(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            progress(killer, "PLAYER_KILL", event.getEntity().getName());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede usarlo un jugador.");
            return true;
        }
        ConfigurationSection missions = plugin.getConfig().getConfigurationSection("missions");
        if (missions == null || missions.getKeys(false).isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "No hay misiones configuradas.");
            return true;
        }
        if (economy == null) {
            player.sendMessage(ChatColor.RED + "Las recompensas están desactivadas: no hay economía de Vault.");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "Misiones de caza:");
        for (String id : missions.getKeys(false)) {
            ConfigurationSection mission = missions.getConfigurationSection(id);
            if (mission == null) {
                continue;
            }
            String name = ChatColor.translateAlternateColorCodes('&', mission.getString("name", id));
            int amount = Math.max(1, mission.getInt("amount", 1));
            int progress = Math.min(amount, dataStore.getMissionProgress(player.getUniqueId(), id));
            boolean completed = dataStore.isMissionCompleted(player.getUniqueId(), id);
            player.sendMessage(name + ChatColor.GRAY + " — " + progress + "/" + amount
                    + (completed ? ChatColor.GREEN + " ✓ completada" : ChatColor.YELLOW + " — "
                    + economy.format(Math.max(0.0, mission.getDouble("reward", 0.0)))));
        }
        return true;
    }

    private void progress(Player player, String type, String target) {
        if (economy == null) {
            return;
        }
        ConfigurationSection missions = plugin.getConfig().getConfigurationSection("missions");
        if (missions == null) {
            return;
        }

        for (String id : missions.getKeys(false)) {
            ConfigurationSection mission = missions.getConfigurationSection(id);
            if (mission == null || !type.equalsIgnoreCase(mission.getString("type", ""))
                    || dataStore.isMissionCompleted(player.getUniqueId(), id)) {
                continue;
            }
            if (type.equals("DEMON_ID") && !target.equalsIgnoreCase(mission.getString("target", ""))) {
                continue;
            }
            if (type.equals("PLAYER_KILL") && !target.equalsIgnoreCase(mission.getString("target", ""))) {
                continue;
            }

            int amount = Math.max(1, mission.getInt("amount", 1));
            int progress = Math.min(amount, dataStore.getMissionProgress(player.getUniqueId(), id) + 1);
            dataStore.setMissionProgress(player.getUniqueId(), id, progress);
            if (progress < amount) {
                continue;
            }

            double reward = Math.max(0.0, mission.getDouble("reward", 0.0));
            EconomyResponse response = economy.depositPlayer(player, reward);
            if (response.transactionSuccess()) {
                dataStore.setMissionCompleted(player.getUniqueId(), id, true);
                player.sendMessage(ChatColor.GOLD + "Misión completada: "
                        + ChatColor.translateAlternateColorCodes('&', mission.getString("name", id))
                        + ChatColor.GOLD + " — recibiste " + economy.format(reward) + ".");
            } else {
                player.sendMessage(ChatColor.RED + "La misión se completó, pero Vault no pudo entregar la recompensa.");
                plugin.getLogger().warning("Falló el pago de la misión " + id + " a " + player.getName()
                        + ": " + response.errorMessage);
            }
        }
    }
}
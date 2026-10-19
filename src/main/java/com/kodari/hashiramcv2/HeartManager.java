package com.kodari.hashiramcv2;

import com.cryptomorin.xseries.XAttribute;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public final class HeartManager implements Listener {
    private static final UUID PERMANENT_HEARTS_ID = UUID.fromString("b089b07a-5026-4b67-a923-2caed2512c07");
    private static final UUID TEMPORARY_HEARTS_ID = UUID.fromString("a1fc4e5b-73f0-41b7-814b-e6b2f077390f");
    private static final UUID OFFHAND_HEARTS_ID = UUID.fromString("d502c00b-93d8-4d57-b473-56e734e9a275");

    private final PlayerDataStore dataStore;
    private final Attribute maxHealthAttribute;
    private final BukkitTask expiryTask;

    public HeartManager(JavaPlugin plugin, PlayerDataStore dataStore) {
        this.dataStore = dataStore;
        maxHealthAttribute = XAttribute.of("MAX_HEALTH").map(XAttribute::get).orElse(null);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        expiryTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateOnlinePlayers, 20L, 400L);
        for (Player player : Bukkit.getOnlinePlayers()) {
            apply(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        apply(event.getPlayer());
    }

    public void apply(Player player) {
        if (maxHealthAttribute == null) {
            return;
        }
        AttributeInstance attribute = player.getAttribute(maxHealthAttribute);
        if (attribute == null) {
            return;
        }

        attribute.removeModifier(PERMANENT_HEARTS_ID);
        attribute.removeModifier(TEMPORARY_HEARTS_ID);
        int permanent = dataStore.getPermanentHearts(player.getUniqueId());
        int temporary = dataStore.getTemporaryHearts(player.getUniqueId());
        if (permanent > 0) {
            attribute.addModifier(new AttributeModifier(PERMANENT_HEARTS_ID, "hashiramcv2_permanent_hearts",
                    permanent * 2.0, AttributeModifier.Operation.ADD_NUMBER));
        }
        if (temporary > 0) {
            attribute.addModifier(new AttributeModifier(TEMPORARY_HEARTS_ID, "hashiramcv2_temporary_hearts",
                    temporary * 2.0, AttributeModifier.Operation.ADD_NUMBER));
        }
        player.setHealth(Math.min(player.getHealth(), attribute.getValue()));
    }

    public void updateOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            apply(player);
        }
    }

    public void setOffhandHearts(Player player, int hearts) {
        if (maxHealthAttribute == null) {
            return;
        }
        AttributeInstance attribute = player.getAttribute(maxHealthAttribute);
        if (attribute == null) {
            return;
        }
        attribute.removeModifier(OFFHAND_HEARTS_ID);
        if (hearts > 0) {
            attribute.addModifier(new AttributeModifier(OFFHAND_HEARTS_ID, "hashiramcv2_offhand_hearts",
                    hearts * 2.0, AttributeModifier.Operation.ADD_NUMBER));
        }
        player.setHealth(Math.min(player.getHealth(), attribute.getValue()));
    }

    public void shutdown() {
        expiryTask.cancel();
        for (Player player : Bukkit.getOnlinePlayers()) {
            AttributeInstance attribute = maxHealthAttribute == null ? null : player.getAttribute(maxHealthAttribute);
            if (attribute != null) {
                attribute.removeModifier(PERMANENT_HEARTS_ID);
                attribute.removeModifier(TEMPORARY_HEARTS_ID);
                attribute.removeModifier(OFFHAND_HEARTS_ID);
            }
        }
    }
}
package com.kodari.hashiramcv2;

import com.cryptomorin.xseries.XAttribute;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class GameplayListener implements Listener {
    private static final UUID DEMON_HEALTH_MODIFIER_ID = UUID.fromString("fa3a864c-89e0-4c9d-97c0-4b489d6e1238");

    private final HashiramcV2Plugin plugin;
    private final ItemFactory itemFactory;
    private final HeartManager heartManager;
    private final Random random = new Random();
    private final NamespacedKey demonKey;
    private final NamespacedKey demonTypeKey;
    private final Map<UUID, String> activeOffhandItems = new HashMap<>();
    private final Map<UUID, BukkitTask> demonDebuffTasks = new HashMap<>();
    private final Map<UUID, Long> processedMoonDays = new HashMap<>();
    private final Set<UUID> redMoonWorlds = new HashSet<>();
    private final Attribute maxHealthAttribute;
    private final BukkitTask offhandTask;
    private final BukkitTask redMoonTask;

    public GameplayListener(HashiramcV2Plugin plugin, ItemFactory itemFactory, HeartManager heartManager) {
        this.plugin = plugin;
        this.itemFactory = itemFactory;
        this.heartManager = heartManager;
        demonKey = new NamespacedKey(plugin, "demon");
        demonTypeKey = new NamespacedKey(plugin, "demon_type");
        maxHealthAttribute = XAttribute.of("MAX_HEALTH").map(XAttribute::get).orElse(null);
        offhandTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateOffhandEffects, 1L, 5L);
        redMoonTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateRedMoons, 20L, 20L);
    }

    @EventHandler
    public void onNaturalNetherSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL
                || event.getLocation().getWorld().getEnvironment() != World.Environment.NETHER
                || random.nextDouble() >= Math.max(0.0, Math.min(1.0,
                plugin.getConfig().getDouble("demons.nether-wither-skeleton-chance", 0.035)))) {
            return;
        }
        Entity demon = event.getLocation().getWorld().spawnEntity(event.getLocation(), EntityType.WITHER_SKELETON);
        demon.getPersistentDataContainer().set(demonKey, PersistentDataType.BYTE, (byte) 1);
        demon.getPersistentDataContainer().set(demonTypeKey, PersistentDataType.STRING, "nether_wither_skeleton");
    }

    @EventHandler
    public void onDemonDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity demon)
                || !demon.getPersistentDataContainer().has(demonKey, PersistentDataType.BYTE)) {
            return;
        }
        if (plugin.getConfig().getBoolean("demon-damage.only-during-red-moon", false)
                && !redMoonWorlds.contains(demon.getWorld().getUID())) {
            return;
        }
        double multiplier = Math.max(0.0, plugin.getConfig().getDouble("demon-damage.multiplier", 2.0));
        event.setDamage(event.getDamage() * multiplier);
    }

    @EventHandler
    public void onWitherSkeletonDeath(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.WITHER_SKELETON
                || !event.getEntity().getPersistentDataContainer().has(demonKey, PersistentDataType.BYTE)
                || random.nextDouble() >= 0.08) {
            return;
        }
        String[] totems = {ItemFactory.HASHIRA_TOTEM, ItemFactory.DEMON_TOTEM, ItemFactory.HUNTER_TOTEM};
        ItemStack drop = itemFactory.create(totems[random.nextInt(totems.length)]);
        if (drop != null) {
            event.getDrops().add(drop);
        }
    }

    @EventHandler
    public void onTotemResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        String itemId = itemFactory.getItemId(player.getInventory().getItemInOffHand());
        if (!isTotem(itemId)) {
            itemId = itemFactory.getItemId(player.getInventory().getItemInMainHand());
        }
        if (!isTotem(itemId)) {
            return;
        }

        switch (itemId) {
            case ItemFactory.HASHIRA_TOTEM -> {
                applyEffect(player, "SPEED", 1200, 0);
                applyEffect(player, "STRENGTH", 1200, 0);
                applyEffect(player, "RESISTANCE", 1200, 0);
                Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + " consumió un Tótem Hashira.");
            }
            case ItemFactory.DEMON_TOTEM -> {
                applyEffect(player, "SPEED", 1200, 0);
                applyEffect(player, "STRENGTH", 1200, 1);
                applyDemonHealthDebuff(player);
                Bukkit.broadcastMessage(ChatColor.RED + player.getName() + " consumió un Tótem Demonio.");
            }
            case ItemFactory.HUNTER_TOTEM -> {
                applyEffect(player, "SPEED", 160, 0);
                applyEffect(player, "RESISTANCE", 100, 0);
                applyEffect(player, "REGENERATION", 100, 0);
                Bukkit.broadcastMessage(ChatColor.GREEN + player.getName() + " consumió un Tótem Cazador de Demonios.");
            }
            default -> {
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String activeItem = activeOffhandItems.remove(player.getUniqueId());
        if (activeItem != null) {
            removeOffhandEffects(player, activeItem);
            heartManager.setOffhandHearts(player, 0);
        }
        removeDemonHealthDebuff(player);
    }

    public void cleanup() {
        offhandTask.cancel();
        redMoonTask.cancel();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String activeItem = activeOffhandItems.get(player.getUniqueId());
            if (activeItem != null) {
                removeOffhandEffects(player, activeItem);
                heartManager.setOffhandHearts(player, 0);
            }
            removeDemonHealthDebuff(player);
        }
        activeOffhandItems.clear();
    }

    private void updateRedMoons() {
        if (!plugin.getConfig().getBoolean("red-moon.enabled", true)) {
            for (World world : Bukkit.getWorlds()) {
                if (redMoonWorlds.remove(world.getUID())) {
                    broadcastMoonMessage("red-moon.end-message", world);
                }
            }
            return;
        }

        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }

            UUID worldId = world.getUID();
            long gameDay = world.getFullTime() / 24000L;
            long time = world.getTime();
            boolean isNight = time >= 13000L && time < 23000L;

            if (!isNight) {
                if (redMoonWorlds.remove(worldId)) {
                    broadcastMoonMessage("red-moon.end-message", world);
                }
                continue;
            }

            if (processedMoonDays.getOrDefault(worldId, Long.MIN_VALUE) == gameDay) {
                continue;
            }
            processedMoonDays.put(worldId, gameDay);
            if (shouldStartRedMoon(gameDay)) {
                redMoonWorlds.add(worldId);
                broadcastMoonMessage("red-moon.start-message", world);
            }
        }
    }

    private boolean shouldStartRedMoon(long gameDay) {
        String trigger = plugin.getConfig().getString("red-moon.trigger", "CHANCE");
        if ("EVERY_N_DAYS".equalsIgnoreCase(trigger)) {
            int interval = Math.max(1, plugin.getConfig().getInt("red-moon.every-game-days", 7));
            return (gameDay + 1) % interval == 0;
        }
        double chance = Math.max(0.0, Math.min(1.0,
                plugin.getConfig().getDouble("red-moon.chance-per-night", 0.20)));
        return random.nextDouble() < chance;
    }

    private void broadcastMoonMessage(String path, World world) {
        String message = plugin.getConfig().getString(path, "")
                .replace("{world}", world.getName());
        if (!message.isBlank()) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    private void updateOffhandEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            String itemId = itemFactory.getItemId(player.getInventory().getItemInOffHand());
            if (!itemFactory.isOffhandItem(itemId)) {
                itemId = null;
            }

            String previousId = activeOffhandItems.get(playerId);
            if (!Objects.equals(previousId, itemId)) {
                if (previousId != null) {
                    removeOffhandEffects(player, previousId);
                }
                heartManager.setOffhandHearts(player, itemFactory.getOffhandHeartBonus(itemId));
            }
            if (itemId == null) {
                activeOffhandItems.remove(playerId);
                continue;
            }

            activeOffhandItems.put(playerId, itemId);
            for (Map.Entry<String, Integer> effect : itemFactory.getOffhandEffects(itemId).entrySet()) {
                applyEffect(player, effect.getKey(), 40, effect.getValue());
            }
        }
    }

    private void applyDemonHealthDebuff(Player player) {
        if (maxHealthAttribute == null) {
            return;
        }
        AttributeInstance attribute = player.getAttribute(maxHealthAttribute);
        if (attribute == null) {
            return;
        }
        removeDemonHealthDebuff(player);
        attribute.addModifier(new AttributeModifier(
                DEMON_HEALTH_MODIFIER_ID,
                "hashiramcv2_demon_heart_debuff",
                -2.0,
                AttributeModifier.Operation.ADD_NUMBER
        ));
        player.setHealth(Math.min(player.getHealth(), attribute.getValue()));

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            AttributeInstance currentAttribute = player.getAttribute(maxHealthAttribute);
            if (currentAttribute != null) {
                currentAttribute.removeModifier(DEMON_HEALTH_MODIFIER_ID);
            }
            demonDebuffTasks.remove(player.getUniqueId());
        }, 1200L);
        demonDebuffTasks.put(player.getUniqueId(), task);
    }

    private void removeDemonHealthDebuff(Player player) {
        BukkitTask task = demonDebuffTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        if (maxHealthAttribute != null) {
            AttributeInstance attribute = player.getAttribute(maxHealthAttribute);
            if (attribute != null) {
                attribute.removeModifier(DEMON_HEALTH_MODIFIER_ID);
            }
        }
    }

    private void removeOffhandEffects(Player player, String itemId) {
        for (String effect : itemFactory.getOffhandEffects(itemId).keySet()) {
            XPotion.matchXPotion(effect).map(potion -> potion.buildPotionEffect(1, 0).getType())
                    .ifPresent(player::removePotionEffect);
        }
    }

    private void applyEffect(Player player, String effect, int duration, int amplifier) {
        XPotion.matchXPotion(effect)
                .map(potion -> potion.buildPotionEffect(duration, amplifier))
                .ifPresent(potionEffect -> player.addPotionEffect(potionEffect, true));
    }

    private boolean isTotem(String itemId) {
        return ItemFactory.HASHIRA_TOTEM.equals(itemId)
                || ItemFactory.DEMON_TOTEM.equals(itemId)
                || ItemFactory.HUNTER_TOTEM.equals(itemId);
    }

}
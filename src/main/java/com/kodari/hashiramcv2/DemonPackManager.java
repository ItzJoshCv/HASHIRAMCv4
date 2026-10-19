package com.kodari.hashiramcv2;

import com.cryptomorin.xseries.XAttribute;
import com.cryptomorin.xseries.XPotion;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.bukkit.NamespacedKey;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class DemonPackManager implements Listener {
    private final HashiramcV2Plugin plugin;
    private final Economy economy;
    private final Random random = new Random();
    private final NamespacedKey demonKey;
    private final NamespacedKey demonTypeKey;
    private final NamespacedKey packKey;
    private final Map<UUID, Integer> remainingPackMembers = new HashMap<>();
    private final Map<UUID, Long> lastPackSpawnTicks = new HashMap<>();
    private final Map<UUID, Long> lastSpitTicks = new HashMap<>();
    private final BukkitTask task;

    public DemonPackManager(HashiramcV2Plugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
        demonKey = new NamespacedKey(plugin, "demon");
        demonTypeKey = new NamespacedKey(plugin, "demon_type");
        packKey = new NamespacedKey(plugin, "demon_pack");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        long interval = Math.max(20L, plugin.getConfig().getLong("demons.night-packs.check-interval-ticks", 100L));
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::update, interval, interval);
    }

    public void shutdown() {
        task.cancel();
        remainingPackMembers.clear();
        lastPackSpawnTicks.clear();
        lastSpitTicks.clear();
    }

    private void update() {
        if (!plugin.getConfig().getBoolean("demons.night-packs.enabled", true)) {
            return;
        }
        for (World world : Bukkit.getWorlds()) {
            updatePoisonSpits(world);
            if (world.getEnvironment() != World.Environment.NORMAL || !isNight(world)) {
                continue;
            }
            if (countActivePacks(world) >= Math.max(1,
                    plugin.getConfig().getInt("demons.night-packs.max-active-packs-per-world", 3))) {
                continue;
            }

            long now = world.getFullTime();
            long cooldown = Math.max(0L, plugin.getConfig().getLong("demons.night-packs.cooldown-ticks", 1200L));
            if (now - lastPackSpawnTicks.getOrDefault(world.getUID(), Long.MIN_VALUE / 2) < cooldown) {
                continue;
            }
            List<Player> candidates = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().equals(world)) {
                    candidates.add(player);
                }
            }
            if (candidates.isEmpty()) {
                continue;
            }
            Player player = candidates.get(random.nextInt(candidates.size()));
            double chance = Math.max(0.0, Math.min(1.0,
                    plugin.getConfig().getDouble("demons.night-packs.chance-per-player-check", 0.06)));
            if (random.nextDouble() >= chance) {
                continue;
            }
            Location spawn = findPackLocation(player);
            if (spawn != null && spawnPack(spawn)) {
                lastPackSpawnTicks.put(world.getUID(), now);
            }
        }
    }

    @EventHandler
    public void onPackMemberDeath(EntityDeathEvent event) {
        String packId = event.getEntity().getPersistentDataContainer().get(packKey, PersistentDataType.STRING);
        if (packId == null) {
            return;
        }
        UUID id;
        try {
            id = UUID.fromString(packId);
        } catch (IllegalArgumentException exception) {
            return;
        }
        Integer remaining = remainingPackMembers.get(id);
        if (remaining == null) {
            return;
        }
        remaining--;
        if (remaining > 0) {
            remainingPackMembers.put(id, remaining);
            return;
        }

        remainingPackMembers.remove(id);
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            rewardFullPack(killer);
        }
    }

    @EventHandler
    public void onDemonMelee(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity demon)
                || !(event.getEntity() instanceof Player player)
                || !demon.getPersistentDataContainer().has(demonKey, PersistentDataType.BYTE)) {
            return;
        }
        if (isPoisonDemon(demon)) {
            applyPoison(player);
        }
    }

    @EventHandler
    public void onPoisonSpitHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)
                || !(snowball.getShooter() instanceof LivingEntity shooter)
                || !shooter.getPersistentDataContainer().has(demonKey, PersistentDataType.BYTE)
                || !(event.getHitEntity() instanceof Player player)) {
            return;
        }
        applyPoison(player);
    }

    private boolean spawnPack(Location center) {
        World world = center.getWorld();
        if (world == null) {
            return false;
        }
        int packSize = Math.max(2, plugin.getConfig().getInt("demons.night-packs.pack-size", 4));
        List<Location> locations = new ArrayList<>();
        locations.add(center);
        for (int i = 1; i < packSize; i++) {
            Location location = findSafeLocation(world, center, 4);
            if (location == null) {
                return false;
            }
            locations.add(location);
        }

        UUID packId = UUID.randomUUID();
        List<LivingEntity> spawned = new ArrayList<>();
        EntityType commonType = readEntityType("demons.night-packs.common-entity", EntityType.ZOMBIE);
        EntityType bossType = readEntityType("demons.night-packs.subboss-entity", EntityType.HUSK);
        for (int i = 0; i < locations.size(); i++) {
            boolean subboss = i == locations.size() - 1;
            Entity entity;
            try {
                entity = world.spawnEntity(locations.get(i), subboss ? bossType : commonType);
            } catch (IllegalArgumentException exception) {
                removeSpawned(spawned);
                plugin.getLogger().warning("No se pudo crear el mob configurado para una manada: " + exception.getMessage());
                return false;
            }
            if (!(entity instanceof LivingEntity living)) {
                entity.remove();
                removeSpawned(spawned);
                return false;
            }

            spawned.add(living);
            String typeId = subboss ? "wolf_pack_subboss" : "poison_demon";
            tagDemon(living, typeId, packId);
            configureMob(living, subboss);
        }
        remainingPackMembers.put(packId, spawned.size());
        return true;
    }

    private void configureMob(LivingEntity entity, boolean subboss) {
        String path = subboss ? "demons.night-packs.subboss" : "demons.night-packs.common";
        String name = plugin.getConfig().getString(path + "-name",
                subboss ? "&4Subjefe de la Manada &c✦" : "&2Demonio Venenoso");
        entity.setCustomName(ChatColor.translateAlternateColorCodes('&', name));
        entity.setCustomNameVisible(true);
        if (subboss) {
            entity.setGlowing(true);
        }
        if (entity instanceof Mob mob) {
            mob.setRemoveWhenFarAway(false);
        }
        setAttribute(entity, "MAX_HEALTH",
                plugin.getConfig().getDouble(path + "-health", subboss ? 80.0 : 28.0), true);
        setAttribute(entity, "ATTACK_DAMAGE",
                plugin.getConfig().getDouble(path + "-damage", subboss ? 12.0 : 6.0), false);
    }

    private void tagDemon(LivingEntity entity, String typeId, UUID packId) {
        entity.getPersistentDataContainer().set(demonKey, PersistentDataType.BYTE, (byte) 1);
        entity.getPersistentDataContainer().set(demonTypeKey, PersistentDataType.STRING, typeId);
        entity.getPersistentDataContainer().set(packKey, PersistentDataType.STRING, packId.toString());
    }

    private void setAttribute(LivingEntity entity, String name, double value, boolean fillHealth) {
        Attribute attributeType = XAttribute.of(name).map(XAttribute::get).orElse(null);
        AttributeInstance attribute = attributeType == null ? null : entity.getAttribute(attributeType);
        if (attribute == null) {
            return;
        }
        attribute.setBaseValue(Math.max(1.0, value));
        if (fillHealth) {
            entity.setHealth(attribute.getValue());
        }
    }

    private void updatePoisonSpits(World world) {
        long now = world.getFullTime();
        long cooldown = Math.max(20L,
                plugin.getConfig().getLong("demons.night-packs.poison-spit-cooldown-ticks", 100L));
        double range = Math.max(1.0, plugin.getConfig().getDouble("demons.night-packs.spit-range", 12.0));
        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof LivingEntity demon)
                    || !demon.getPersistentDataContainer().has(demonKey, PersistentDataType.BYTE)
                    || !isPoisonDemon(demon)
                    || now - lastSpitTicks.getOrDefault(demon.getUniqueId(), Long.MIN_VALUE / 2) < cooldown) {
                continue;
            }
            Player target = nearestPlayer(demon, range);
            if (target == null) {
                continue;
            }
            Snowball spit = world.spawn(demon.getEyeLocation(), Snowball.class);
            spit.setShooter(demon);
            Vector direction = target.getEyeLocation().toVector().subtract(spit.getLocation().toVector());
            if (direction.lengthSquared() > 0) {
                spit.setVelocity(direction.normalize().multiply(1.15));
            }
            lastSpitTicks.put(demon.getUniqueId(), now);
        }
    }

    private Player nearestPlayer(LivingEntity demon, double range) {
        Player nearest = null;
        double nearestDistance = range * range;
        for (Entity nearby : demon.getNearbyEntities(range, range, range)) {
            if (!(nearby instanceof Player player)) {
                continue;
            }
            double distance = demon.getLocation().distanceSquared(player.getLocation());
            if (distance <= nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private void applyPoison(Player player) {
        int duration = Math.max(1, plugin.getConfig().getInt("demons.night-packs.poison-duration-ticks", 60));
        int amplifier = Math.max(0, plugin.getConfig().getInt("demons.night-packs.poison-amplifier", 0));
        XPotion.matchXPotion("POISON")
                .map(potion -> potion.buildPotionEffect(duration, amplifier))
                .ifPresent(effect -> player.addPotionEffect(effect, true));
    }

    private boolean isPoisonDemon(LivingEntity entity) {
        String type = entity.getPersistentDataContainer().get(demonTypeKey, PersistentDataType.STRING);
        return "poison_demon".equals(type) || "wolf_pack_subboss".equals(type);
    }

    private Location findPackLocation(Player player) {
        int radius = Math.max(16, plugin.getConfig().getInt("demons.night-packs.spawn-radius", 24));
        double angle = random.nextDouble() * Math.PI * 2.0;
        int distance = radius + random.nextInt(Math.max(1, radius / 3));
        int x = player.getLocation().getBlockX() + (int) Math.round(Math.cos(angle) * distance);
        int z = player.getLocation().getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
        return findSafeLocation(player.getWorld(), x, z);
    }

    private Location findSafeLocation(World world, Location center, int radius) {
        for (int attempt = 0; attempt < 12; attempt++) {
            int x = center.getBlockX() + random.nextInt(radius * 2 + 1) - radius;
            int z = center.getBlockZ() + random.nextInt(radius * 2 + 1) - radius;
            Location location = findSafeLocation(world, x, z);
            if (location != null) {
                return location;
            }
        }
        return null;
    }

    private Location findSafeLocation(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z) + 1;
        Location location = new Location(world, x + 0.5, y, z + 0.5);
        if (location.getBlock().isPassable()
                && location.clone().add(0, 1, 0).getBlock().isPassable()
                && location.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
            return location;
        }
        return null;
    }

    private int countActivePacks(World world) {
        Set<String> packIds = new HashSet<>();
        for (Entity entity : world.getEntities()) {
            String id = entity.getPersistentDataContainer().get(packKey, PersistentDataType.STRING);
            if (id != null && !entity.isDead()) {
                packIds.add(id);
            }
        }
        return packIds.size();
    }

    private boolean isNight(World world) {
        long time = world.getTime();
        return time >= 13000L && time < 23000L;
    }

    private EntityType readEntityType(String path, EntityType fallback) {
        String configured = plugin.getConfig().getString(path, fallback.name());
        try {
            return EntityType.valueOf(configured.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning("Tipo de entidad inválido en " + path + ": " + configured);
            return fallback;
        }
    }

    private void rewardFullPack(Player player) {
        ConfigurationSection rewards = plugin.getConfig().getConfigurationSection("demons.night-packs.rewards");
        double money = rewards == null ? 0.0 : Math.max(0.0, rewards.getDouble("money", 0.0));
        int experience = rewards == null ? 0 : Math.max(0, rewards.getInt("experience", 0));
        player.giveExp(experience);
        String message = "&a¡Eliminaste toda la manada!";
        if (economy != null && money > 0.0) {
            EconomyResponse response = economy.depositPlayer(player, money);
            if (response.transactionSuccess()) {
                message += " &6+" + economy.format(money);
            } else {
                plugin.getLogger().warning("Falló el pago de recompensa de manada a " + player.getName()
                        + ": " + response.errorMessage);
            }
        }
        if (experience > 0) {
            message += " &b+" + experience + " XP";
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private void removeSpawned(List<LivingEntity> spawned) {
        for (LivingEntity entity : spawned) {
            entity.remove();
        }
    }
}
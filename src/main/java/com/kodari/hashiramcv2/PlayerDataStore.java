package com.kodari.hashiramcv2;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class PlayerDataStore {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration data;

    public PlayerDataStore(JavaPlugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        file = new File(plugin.getDataFolder(), "player-data.yml");
        data = YamlConfiguration.loadConfiguration(file);
    }

    public int getPermanentHearts(UUID playerId) {
        return data.getInt(heartsPath(playerId) + ".permanent", 0);
    }

    public void setPermanentHearts(UUID playerId, int hearts) {
        data.set(heartsPath(playerId) + ".permanent", Math.max(0, hearts));
        save();
    }

    public int getTemporaryHearts(UUID playerId) {
        String path = heartsPath(playerId);
        long expiry = data.getLong(path + ".temporary-expires-at", 0L);
        if (expiry != 0L && expiry <= System.currentTimeMillis()) {
            data.set(path + ".temporary", 0);
            data.set(path + ".temporary-expires-at", 0L);
            save();
            return 0;
        }
        return data.getInt(path + ".temporary", 0);
    }

    public long getTemporaryHeartExpiry(UUID playerId) {
        getTemporaryHearts(playerId);
        return data.getLong(heartsPath(playerId) + ".temporary-expires-at", 0L);
    }

    public void setTemporaryHearts(UUID playerId, int hearts, long expiry) {
        String path = heartsPath(playerId);
        int safeHearts = Math.max(0, hearts);
        data.set(path + ".temporary", safeHearts);
        data.set(path + ".temporary-expires-at", safeHearts == 0 ? 0L : expiry);
        save();
    }

    public int getMissionProgress(UUID playerId, String missionId) {
        return data.getInt(missionPath(playerId, missionId) + ".progress", 0);
    }

    public void setMissionProgress(UUID playerId, String missionId, int progress) {
        data.set(missionPath(playerId, missionId) + ".progress", Math.max(0, progress));
        save();
    }

    public boolean isMissionCompleted(UUID playerId, String missionId) {
        return data.getBoolean(missionPath(playerId, missionId) + ".completed", false);
    }

    public void setMissionCompleted(UUID playerId, String missionId, boolean completed) {
        data.set(missionPath(playerId, missionId) + ".completed", completed);
        save();
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("No se pudo guardar player-data.yml: " + exception.getMessage());
        }
    }

    private String heartsPath(UUID playerId) {
        return "players." + playerId + ".hearts";
    }

    private String missionPath(UUID playerId, String missionId) {
        return "players." + playerId + ".missions." + missionId;
    }
}
package com.kodari.hashiramcv2;

import org.bukkit.Bukkit;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class HashiramcV2Plugin extends JavaPlugin {
    private ItemFactory itemFactory;
    private GameplayListener gameplayListener;
    private PlayerDataStore playerDataStore;
    private HeartManager heartManager;
    private DemonPackManager demonPackManager;
    private Economy economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        hookEconomy();
        playerDataStore = new PlayerDataStore(this);
        itemFactory = new ItemFactory(this);
        heartManager = new HeartManager(this, playerDataStore);
        gameplayListener = new GameplayListener(this, itemFactory, heartManager);
        Bukkit.getPluginManager().registerEvents(gameplayListener, this);
        getCommand("offhands").setExecutor(new OffhandsCommand(itemFactory));
        getCommand("corazon").setExecutor(new HeartCommand(this, playerDataStore, heartManager));
        getCommand("misiones").setExecutor(new MissionManager(this, playerDataStore, economy));
        demonPackManager = new DemonPackManager(this, economy);
    }

    private void hookEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault no está instalado; las misiones de dinero quedan inactivas.");
            return;
        }
        RegisteredServiceProvider<Economy> registration = getServer().getServicesManager().getRegistration(Economy.class);
        if (registration != null) {
            economy = registration.getProvider();
        }
        if (economy == null) {
            getLogger().warning("Vault no tiene una economía registrada; las misiones de dinero quedan inactivas.");
        }
    }

    @Override
    public void onDisable() {
        if (gameplayListener != null) {
            gameplayListener.cleanup();
        }
        if (demonPackManager != null) {
            demonPackManager.shutdown();
        }
        if (heartManager != null) {
            heartManager.shutdown();
        }
        if (playerDataStore != null) {
            playerDataStore.save();
        }
    }
}
package me.Scusemua.BuyableWorldGuardRegions.Core;

import me.Scusemua.BuyableWorldGuardRegions.Commands.BaseCommand;
import me.Scusemua.BuyableWorldGuardRegions.Commands.CommandCancelSelection;
import me.Scusemua.BuyableWorldGuardRegions.Commands.CommandConfirmSelection;
import me.Scusemua.BuyableWorldGuardRegions.Listeners.GeneratedBlocksBrokenListener;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Created by Benjamin on 8/19/2016.
 */
public class Main extends JavaPlugin {
    private static final Logger log = Logger.getLogger("Minecraft");
    public static Economy econ = null;
    public static Permission perms = null;
    public static Chat chat = null;
    public static GeneratedBlocksBrokenListener generatedBlocksBrokenListener;

    /**
     * Called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        getLogger().info("BuyableWorldGuardRegions Enabled.");

        this.getCommand("protection").setExecutor(new BaseCommand(this));

        // generatedBlocksBrokenListener = new GeneratedBlocksBrokenListener(this);
        // getServer().getPluginManager().registerEvents(generatedBlocksBrokenListener, this);

        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getLogger().info("WARNING: No economy plugin found. BuyableWorldGuardRegions disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupPermissions();
        setupChat();
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        getLogger().info("BuyableWorldGuardRegions disabled");
        // GeneratedBlocksBrokenListener.resetGeneratedBlocks();
    }

    /**
     * Responsible for setting up the economy integration with Vault.
     * @return a boolean indicating whether or not the economy was successfully integrated.
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    /**
     * Set up the permissions integration with Vault.
     * @return boolean indicating successful/unsuccessful integration with Vault.
     */
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    /**
     * Set up the chat integration with Vault.
     * @return boolean indicating successful/unsuccessful integration with Vault.
     */
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }
}

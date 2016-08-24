package me.Scusemua.BuyableWorldGuardRegions.Commands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import me.Scusemua.BuyableWorldGuardRegions.Core.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

/**
 * Created by Benjamin on 8/20/2016.
 */
public class CommandCancelSelection implements CommandExecutor {
    private final Plugin myPlugin;

    /**
     * Constructor for the BuyRegion command.
     * @param plugin Reference to the plugin object.
     */
    public CommandCancelSelection(Plugin plugin) {
        myPlugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] remainder) {
        if (sender instanceof ConsoleCommandSender) {
            getLogger().info("ERROR: Cannot call /selectiondetails from the console, bitch.");
        }

        // If a player executed the command...
        if (sender instanceof Player) {
            Player player = (Player)sender;

            if (!Main.perms.has(player, "BuyableWorldGuardRegions.cancelselection")) {
                sender.sendMessage(ChatColor.RED + "ERROR: You do not have permission to do this lol. Tell somebody.");
                return false;
            }

            // Create a reference to the WorldEdit plugin and the selection (which may be null if it does not exist).
            WorldEditPlugin plugin = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
            Selection selection = plugin.getSelection(player);

            // If the player has not made a selection yet, inform them and then return false.
            if (selection == null) {
                player.sendMessage(ChatColor.RED + "ERROR: Please select a region first.");
                return false;
            }

            return Main.generatedBlocksBrokenListener.removeSelection(selection);
        }

        return false;
    }
}

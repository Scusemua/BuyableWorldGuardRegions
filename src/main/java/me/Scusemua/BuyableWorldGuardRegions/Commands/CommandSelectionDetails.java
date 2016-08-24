package me.Scusemua.BuyableWorldGuardRegions.Commands;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import me.Scusemua.BuyableWorldGuardRegions.Core.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

/**
 * Created by Benjamin on 8/19/2016.
 */
public class CommandSelectionDetails implements CommandExecutor {
    private final Plugin myPlugin;

    /**
     * Constructor for the CommandSelectionDetails command.
     * @param plugin Reference to the plugin object.
     */
    public CommandSelectionDetails(Plugin plugin) {
        myPlugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] remainder) {
        if (sender instanceof ConsoleCommandSender) {
            getLogger().info("ERROR: Cannot call /selectiondetails from the console, bitch.");
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            // Ensure the player has permission to execute the command.
            if (!Main.perms.has(player, "BuyableWorldGuardRegions.currentselection")) {
                sender.sendMessage(ChatColor.RED + "ERROR: You do not have permission to do this.");
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

            // Create a reference to the RegionSelector for the player's region.
            RegionSelector regionSelector = selection.getRegionSelector();

            try {
                if (regionSelector.getRegion() == null) {
                    player.sendMessage(ChatColor.RED + "ERROR: Please select a region first.");
                    return false;
                }

                // Expand the user's selection to all blocks on the y-axis.
                BlockVector min = selection.getNativeMinimumPoint().toBlockVector();
                BlockVector max = selection.getNativeMaximumPoint().toBlockVector();

                // Adjust the region such that it is expanded vertically all.
                // BlockVector minAdjusted = new BlockVector(0, -255, 0);
                // BlockVector maxAdjusted = new BlockVector(0, 255, 0);

                // Region region = regionSelector.getRegion();
                // region.expand(minAdjusted, maxAdjusted);
                // regionSelector.learnChanges();

                // List all the details of the selection to the player.
                player.sendMessage(ChatColor.AQUA + "Selection Details: " + regionSelector.getRegion().toString());
                player.sendMessage(ChatColor.AQUA + "Length: " + regionSelector.getRegion().getLength() + ". Width: " +
                    regionSelector.getRegion().getWidth() + ". Height: " + regionSelector.getRegion().getHeight() + ".");
                player.sendMessage(ChatColor.GREEN + "COST: $" + (regionSelector.getRegion().getLength() *
                        regionSelector.getRegion().getWidth() * 10));
                player.sendMessage(ChatColor.DARK_GREEN + "Please note that, once bought, the region " +
                        "will expand all the way up and down automatically.");

            } catch (IncompleteRegionException e) {
                player.sendMessage(ChatColor.RED + "ERROR: An unexpected error as occurred. Please inform a staff member.");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}

package me.Scusemua.BuyableWorldGuardRegions.Commands;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.RegionSelector;
import me.Scusemua.BuyableWorldGuardRegions.Core.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
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
public class CommandConfirmSelection implements CommandExecutor {
    private final Plugin myPlugin;

    /**
     * Constructor for the BuyRegion command.
     * @param plugin Reference to the plugin object.
     */
    public CommandConfirmSelection(Plugin plugin) {
        myPlugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] remainder) {
        if (sender instanceof ConsoleCommandSender) {
            getLogger().info("ERROR: Cannot call /selectiondetails from the console, bitch.");
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            // Ensure the player has permission to execute the command.
            if (!Main.perms.has(player, "BuyableWorldGuardRegions.confirmselection")) {
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

                // Create a reference to the current World of the selection.
                World world = selection.getWorld();

                // Set glowstone blocks at the defining blocks for the region.
                // Notice that we get the y-values from different BlockVector objects. This is because the
                // block vector objects do not have the y-values you expect them to have. They're basically flipped.
                Location L1 = new Location(world, min.getBlockX(), min.getBlockY(), min.getBlockZ());
                Location L2 = new Location(world, max.getBlockX(), max.getBlockY(), max.getBlockZ());

                Main.generatedBlocksBrokenListener.addSelection(selection);

                Main.generatedBlocksBrokenListener.generatedBlocks.add(L1);
                Main.generatedBlocksBrokenListener.generatedBlocks.add(L2);

                Main.generatedBlocksBrokenListener.previousBlocks.put(L1, L1.getBlock());
                Main.generatedBlocksBrokenListener.previousBlocks.put(L2, L2.getBlock());

                if (L1.getBlock() instanceof Chest) {
                    Main.generatedBlocksBrokenListener.chestInventories.put(L1.getBlock(), ((Chest) L1.getBlock()).getBlockInventory());
                }

                if (L2.getBlock() instanceof Chest) {
                    Main.generatedBlocksBrokenListener.chestInventories.put(L2.getBlock(), ((Chest) L2.getBlock()).getBlockInventory());
                }

                L1.getBlock().setType(Material.GLOWSTONE);
                L2.getBlock().setType(Material.GLOWSTONE);

                // List all the details of the selection to the player.
                player.sendMessage(ChatColor.AQUA + "To view the following information again, simply type /CurrentSelection");
                player.sendMessage(ChatColor.AQUA + "Selection Details: " + regionSelector.getRegion().toString());
                player.sendMessage(ChatColor.AQUA + "Length: " + regionSelector.getRegion().getLength() + ". Width: " +
                        regionSelector.getRegion().getWidth() + ". Height: " + regionSelector.getRegion().getHeight() + ".");
                player.sendMessage(ChatColor.GREEN + "COST: $" + (regionSelector.getRegion().getLength() *
                        regionSelector.getRegion().getWidth() * 10));
            } catch (IncompleteRegionException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}


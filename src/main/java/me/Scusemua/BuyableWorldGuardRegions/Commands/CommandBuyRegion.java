package me.Scusemua.BuyableWorldGuardRegions.Commands;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.Scusemua.BuyableWorldGuardRegions.Core.Main;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.awt.*;

import static me.Scusemua.BuyableWorldGuardRegions.Core.Main.econ;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

/**
 * Created by Benjamin on 8/19/2016.
 */
public class CommandBuyRegion implements CommandExecutor {
    private final Plugin myPlugin;

    /**
     * Constructor for the BuyRegion command.
     * @param plugin Reference to the plugin object.
     */
    public CommandBuyRegion(Plugin plugin) {
        myPlugin = plugin;
    }

    /**
     * This method is called when somebody uses the Buy Region command.
     * @param sender Represents whatever sent the command (Player, Console, etc.)
     * @param cmd Represents the command being called (generally known ahead of time).
     * @param label Exact first word of the command entered by sender (excluding arguments).
     * @param remainder The remainder of the command statement, excluding the label.
     */
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] remainder) {
        if (sender instanceof ConsoleCommandSender) {
            getLogger().info("ERROR: Cannot call /buyregion from the console, bitch.");
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Ensure the player has permission to execute the command.
            if (!Main.perms.has(player, "BuyableWorldGuardRegions.buyregion")) {
                sender.sendMessage(ChatColor.RED + "ERROR: You do not have permission to do this.");
                return false;
            }

            // Create instances to both the WorldEdit and WorldGuard plugins.
            WorldEditPlugin worldEditPlugin = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
            WorldGuardPlugin worldGuardPlugin = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");

            // Create a reference to the selection created by the current player.
            Selection selection = worldEditPlugin.getSelection(player);

            // Create a reference to the current World of the selection.
            World world = selection.getWorld();

            // Create a reference to the RegionContainer.
            RegionContainer container = worldGuardPlugin.getRegionContainer();

            // Create a reference to the RegionManager for the current World (manages regionManager for current World).
            RegionManager regionManager = container.get(world);

            // If the player has not made a selection, inform the player and return false.
            if (selection == null) {
                player.sendMessage(ChatColor.RED + "ERROR: Please select a region first.");
                return false;
            }

            // Create a reference to the RegionSelector for the current region.
            RegionSelector regionSelector = selection.getRegionSelector();

            try {
                // If the current region is null, inform the player and return false.
                if (regionSelector.getRegion() == null) {
                    player.sendMessage(ChatColor.RED + "ERROR: Please select a region first.");
                    return false;
                }
                // Create a reference to the currently selected region.
                Region region = regionSelector.getRegion();
                // Calculate the price (L * W * 10)
                double price = region.getLength() * region.getWidth() * 10;
                // Attempt to withdrawal the money from the player.
                EconomyResponse r = econ.withdrawPlayer(player, price);
                if(r.transactionSuccess()) {
                    // Adjust the region such that it is expanded vertically all.
                    BlockVector minAdjusted = new BlockVector(0, -255, 0);
                    BlockVector maxAdjusted = new BlockVector(0, 255, 0);

                    // Expand the region vertically and inform the RegionSelector that we modified the region.
                    region.expand(minAdjusted, maxAdjusted);
                    regionSelector.learnChanges();

                    // Create a name for the new ProtectedRegion. According to some quick Google
                    // research, Minecraft usernames can only be between 3 and 18 characters long.
                    String regionName = player.getName() +
                            regionManager.getRegionCountOfPlayer(worldGuardPlugin.wrapPlayer(player)) + "Region";

                    // Log to the console that we're creating a new WorldGuard region for
                    // the proper player who is connected from the proper IP address.
                    getLogger().info("INFO: Creating new WorldGuard region: " + regionName + " for player " +
                    player.getName() + " " + player.getAddress());

                    // Expand the user's selection to all blocks on the y-axis.
                    BlockVector min = selection.getNativeMinimumPoint().toBlockVector();
                    BlockVector max = selection.getNativeMaximumPoint().toBlockVector();

                    // Create the new region and add it to the World's region list, basically.
                    ProtectedRegion protectedRegion = new ProtectedCuboidRegion(regionName, min, max);

                    ApplicableRegionSet regionSet = regionManager.getApplicableRegions(protectedRegion);

                    if (regionSet.size() > 0) {
                        player.sendMessage(ChatColor.RED + "Error: Region is overlapping another region!");
                        return true;
                    }

                    DefaultDomain owners = protectedRegion.getOwners();
                    owners.addPlayer(worldGuardPlugin.wrapPlayer(player));
                    // protectedRegion.setFlag(DefaultFlag.DAMAGE_ANIMALS, StateFlag.State.DENY);
                    // protectedRegion.setFlag(DefaultFlag.DAMAGE_ANIMALS, StateFlag.State.DENY);
                    protectedRegion.setFlag(DefaultFlag.MOB_DAMAGE, StateFlag.State.ALLOW);
                    protectedRegion.setFlag(DefaultFlag.MOB_SPAWNING, StateFlag.State.ALLOW);
                    protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, "Welcome to " + player.getDisplayName() +
                            "\'s Region.");
                    protectedRegion.setFlag(DefaultFlag.FAREWELL_MESSAGE, "You have left " + player.getDisplayName() +
                            "\'s Region.");

                    // Log the toString() of the newly-created Protected Region to the console.
                    getLogger().info("Player " + player.getDisplayName() + " has bought and created a new region!");
                    getLogger().info(protectedRegion.toString());
                    regionManager.getRegionCountOfPlayer(worldGuardPlugin.wrapPlayer(player));
                    regionManager.addRegion(protectedRegion);

                    // Notify the player of the success.
                    sender.sendMessage(ChatColor.GREEN + "Congratulations. You purchased the land for "
                            + econ.format(r.amount) +"!");
                    sender.sendMessage(String.format("New Balance: %s",
                            econ.format(r.balance)));

                    // Since the selection was bought, remove everything.
                    Main.generatedBlocksBrokenListener.removeSelection(selection);
                } else {    // If the player did not have enough money, notify them and return.
                    sender.sendMessage(String.format("An error occurred: %s", r.errorMessage));
                    return false;
                }
                return true;

            } catch (IncompleteRegionException e) {
                player.sendMessage(ChatColor.DARK_RED + "ERROR: An unexpected error as occurred. Please inform a staff member.");
                e.printStackTrace();
                return false;
            } catch (RegionOperationException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}

package me.Scusemua.BuyableWorldGuardRegions.Listeners;

import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Created by Benjamin on 8/20/2016.
 */
public class GeneratedBlocksBrokenListener implements Listener {
    private static Plugin myPlugin;

    public static Set<Location> generatedBlocks;        // HashSet of all the generated Glowstone Blocks
    public static Map<Location, Block> previousBlocks;  // HashMap of all the blocks before Glowstone was placed.
    public static Map<Block, Inventory> chestInventories;   // Chest inventories of chests with glowstone placed on them.

    private static Set<Selection> temporarySelectionRegistry;

    /**
     * Constructor for the listener.
     */
    public GeneratedBlocksBrokenListener(Plugin plugin) {
        generatedBlocks = new HashSet<Location>();
        temporarySelectionRegistry = new HashSet<Selection>();

        previousBlocks = new HashMap<Location, Block>();
        chestInventories = new HashMap<Block, Inventory>();

        myPlugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Block block = event.getBlock();

        myPlugin.getLogger().info("Removing selection.");

        // If the broken block is one of the glowstone blocks in the generatedBlocks hashset, do not let it get broken.
        if (generatedBlocks.contains(block.getLocation())) {
            event.setCancelled(true);
            block.setType(Material.GLOWSTONE);
        }
    }

    /**
     * Clears the generatedBlocks HashSet<Location> and sets them to their previous values.
     */
    public static void resetGeneratedBlocks() {
        generatedBlocks.clear();

        Iterator it = previousBlocks.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Location loc = (Location)pair.getKey();
            Block block = (Block)pair.getValue();
            loc.getBlock().setType(block.getType());
        }
    }

    /**
     * Add a selection to the temporary selection registry.
     */
    public boolean addSelection(Selection selection) {
        temporarySelectionRegistry.add(selection);
        return true;
    }

    /**
     * Remove a selection from the temporary selection registry and properly handle restoring the blocks and all.
     */
    public boolean removeSelection(Selection selection) {
        // If we're able to remove the given selection from the registry, take care of putting everything back.
        if(temporarySelectionRegistry.remove(selection)) {
            Location L1 = selection.getMinimumPoint();
            Location L2 = selection.getMinimumPoint();

            // If we had generated blocks at Location L1, put the correct block back. If that block was a chest,
            // given the chest the proper contents back so the player(s) do not lose any items.
            if (generatedBlocks.remove(L1)) {
                L1.getBlock().setType(previousBlocks.get(L1).getType());
                if (previousBlocks.get(L1).getType() == Material.CHEST) {
                    ((Chest)L1.getBlock()).getBlockInventory().setContents
                            (chestInventories.get(previousBlocks.get(L1)).getContents());
                }
            }

            // If we had generated blocks at Location L2, put the correct block back. If that block was a chest,
            // given the chest the proper contents back so the player(s) do not lose any items.
            if (generatedBlocks.remove(L2)) {
                L2.getBlock().setType(previousBlocks.get(L2).getType());
                if (previousBlocks.get(L2).getType() == Material.CHEST) {
                    ((Chest)L1.getBlock()).getBlockInventory().setContents
                            (chestInventories.get(previousBlocks.get(L2)).getContents());
                }
            }

            // Remove the chest inventories from the HashMap.
            chestInventories.remove(previousBlocks.get(L1));
            chestInventories.remove(previousBlocks.get(L2));

            // Now that we're completely done with the previous blocks, remove those too.
            previousBlocks.remove(L1);
            previousBlocks.remove(L2);
        }
        return false;
    }
}

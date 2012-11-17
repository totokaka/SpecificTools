package me.totokaka.specifictools;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class Listeners implements Listener
{
    private final SpecificTools plugin;

    public Listeners(SpecificTools plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK)
        {
            Map<String, Replacements.ToolMeta> tools = plugin.replacements.getToolsByBlock(event.getClickedBlock().getType().toString(),
                    event.getClickedBlock().getWorld());
            if (tools == null) return;
            if (tools.isEmpty()) return;
            Replacements.ToolMeta meta = tools.get(event.getPlayer().getItemInHand().getType().toString());
            if (meta == null) return;

            if (meta.time > -1)
            {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BlockBreaker(event.getPlayer(),
                        event.getClickedBlock()), meta.time);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(final BlockBreakEvent event)
    {
        final Map<String, Replacements.ToolMeta> tools = plugin.replacements.getToolsByBlock(event
                .getBlock().getType().toString(), event.getPlayer().getWorld());
        if (tools != null)
        {
            Material itemInHand = event.getPlayer().getItemInHand().getType();
            String tool = itemInHand.toString();
            if (tool.equals("AIR"))
            {
                tool = "HAND";
            }

            List<String> actions = tools.get(tool).actions;
            if (actions != null)
            {
                event.setCancelled(true);
                if (actions.contains("destroy"))
                {
                    event.getBlock().setType(Material.AIR);
                }
                if (actions.contains("drop"))
                {
                    for (ItemStack stack : event.getBlock().getDrops())
                    {
                        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), stack);
                    }
                }
                if (actions.contains("explode"))
                {
                    event.getBlock().getWorld().createExplosion(event.getBlock().getLocation(), (float) event.getPlayer().getItemInHand().getAmount());
                }
                if (actions.contains("lightning"))
                {
                    event.getBlock().getWorld().strikeLightning(event.getBlock().getLocation());
                }
            }
        }
    }

    private class BlockBreaker implements Runnable
    {
        private final Player player;
        private final Block block;

        public BlockBreaker(Player p, Block b)
        {
            this.player = p;
            this.block = b;
        }

        @Override
        public void run()
        {
            Block targetBlock = player.getTargetBlock(null, 5);
            if (block.equals(targetBlock))
            { // The player is still digging the block
                BlockBreakEvent event = new BlockBreakEvent(block, player);
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
        }
    }

}

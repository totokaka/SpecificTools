package me.totokaka.specifictools;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SpecificTools extends JavaPlugin implements Listener{
	
	Replacements replacements = new Replacements();
	private boolean noDrop = true;
	
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		this.saveDefaultConfig();
		this.reloadConfig();
		this.noDrop = this.getConfig().getBoolean("NoDrop");
		this.getConfig().options().header("------------------SpecificTools Config------------------\n"
										 +"Here is an example of the format in this config:\n" +
										 "DIRT:\n" +
										 "   - HAND\n" +
										 "   - DIAMOND_SHOVEL\n" +
										 "This exaple makes the players hand and Diamond shovel as the only \"tools\" " +
										 "capable of destroying Dirt\n\n" +
										 "If you set NoDrop to false the block will not break if the player tries to destroy it with a tool that is not valid\n" +
										 "If you set it to true the block will not drop anything.."
										 +"");
		this.saveConfig();
		replacements.load(this);
		
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event){
		Set<Material> tools = replacements.getToolsByBlock(event.getBlock().getType());
		if(!event.getPlayer().hasPermission("SpecificTools.default") && tools != null){
			Material inHand = event.getPlayer().getItemInHand().getType();
			if(!tools.contains(inHand)){
				event.setCancelled(true);
				if(noDrop){
					event.getBlock().setTypeId(0);
				}
			}
		}
	}
	
}

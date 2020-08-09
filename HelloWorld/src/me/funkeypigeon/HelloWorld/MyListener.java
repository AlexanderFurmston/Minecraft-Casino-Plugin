package me.funkeypigeon.HelloWorld;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class MyListener implements Listener {
	//@EventHandler
	//public boolean PlayerInteractEvent(Player who, Action action, ItemStack item, Block clickedBlock, BlockFace clickedFace ) {
	//	Bukkit.broadcastMessage("Interaction occured, OwO");
	//	return false;
	//}
	
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Bukkit.broadcastMessage("Interaction occured, OwO");
	}
}
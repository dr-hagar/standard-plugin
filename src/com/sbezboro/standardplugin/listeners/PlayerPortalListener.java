package com.sbezboro.standardplugin.listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.model.StandardPlayer;

public class PlayerPortalListener extends EventListener implements Listener {

	public PlayerPortalListener(StandardPlugin plugin) {
		super(plugin);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPortalEvent(PlayerPortalEvent event) {
		World fromWorld = event.getFrom().getWorld();
		
		if (event.getCause() == TeleportCause.END_PORTAL) {
			// Going from the end
			if (fromWorld.getEnvironment() == Environment.THE_END) {
				StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());
				
				// Set event location to the player's bed if one exists
				Location to = player.getBedLocationIfValid();
				
				if (to == null) {
					to = plugin.getServer().getWorld(StandardPlugin.OVERWORLD_NAME).getSpawnLocation();
				}
				
				event.setTo(to);
				
				plugin.getLogger().info(event.getPlayer().getName() + " leaving the end.");
			// Going to the end
			} else {
				World newEnd = plugin.getEndResetManager().getNewEndWorld();
				if (newEnd != null && !plugin.getEndResetStorage().getActivePortals().isEmpty()) {
					event.setTo(new Location(newEnd, 100, 50, 0));
				}
				
				plugin.getLogger().info(event.getPlayer().getName() + " going to the end.");
			}
		}
	}

}

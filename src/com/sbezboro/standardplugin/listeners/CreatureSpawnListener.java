package com.sbezboro.standardplugin.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.sbezboro.standardplugin.StandardPlugin;

public class CreatureSpawnListener extends EventListener implements Listener {

	public CreatureSpawnListener(StandardPlugin plugin) {
		super(plugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		Entity entity = event.getEntity();
		
		if (entity.getType() == EntityType.WITHER) {
			event.setCancelled(true);
		}
	}

}

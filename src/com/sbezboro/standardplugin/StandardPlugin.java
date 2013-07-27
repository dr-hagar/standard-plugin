package com.sbezboro.standardplugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.sbezboro.standardplugin.commands.ForumMuteCommand;
import com.sbezboro.standardplugin.commands.GateCommand;
import com.sbezboro.standardplugin.commands.ICommand;
import com.sbezboro.standardplugin.commands.PvpProtectionCommand;
import com.sbezboro.standardplugin.commands.RankCommand;
import com.sbezboro.standardplugin.commands.RegisterCommand;
import com.sbezboro.standardplugin.commands.SetSpawnCommand;
import com.sbezboro.standardplugin.commands.SpawnCommand;
import com.sbezboro.standardplugin.commands.StandardCommand;
import com.sbezboro.standardplugin.commands.TitleCommand;
import com.sbezboro.standardplugin.commands.TitlesCommand;
import com.sbezboro.standardplugin.commands.UnfreezeCommand;
import com.sbezboro.standardplugin.integrations.EssentialsIntegration;
import com.sbezboro.standardplugin.integrations.SimplyVanishIntegration;
import com.sbezboro.standardplugin.jsonapi.ForumPostAPICallHandler;
import com.sbezboro.standardplugin.jsonapi.PlayerStatsAPICallHandler;
import com.sbezboro.standardplugin.jsonapi.ServerStatusAPICallHandler;
import com.sbezboro.standardplugin.jsonapi.WebChatAPICallHandler;
import com.sbezboro.standardplugin.listeners.CreatureSpawnListener;
import com.sbezboro.standardplugin.listeners.DeathListener;
import com.sbezboro.standardplugin.listeners.DispenseListener;
import com.sbezboro.standardplugin.listeners.EntityDamageListener;
import com.sbezboro.standardplugin.listeners.HungerListener;
import com.sbezboro.standardplugin.listeners.PlayerInteractListener;
import com.sbezboro.standardplugin.listeners.PlayerJoinListener;
import com.sbezboro.standardplugin.listeners.PlayerLeaveListener;
import com.sbezboro.standardplugin.listeners.PlayerMoveListener;
import com.sbezboro.standardplugin.listeners.RespawnListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.persistence.GateStorage;
import com.sbezboro.standardplugin.persistence.IStorage;
import com.sbezboro.standardplugin.persistence.LogWriter;
import com.sbezboro.standardplugin.persistence.PlayerStorage;
import com.sbezboro.standardplugin.persistence.TitleStorage;
import com.sbezboro.standardplugin.util.PlayerSaver;

public class StandardPlugin extends JavaPlugin {
	private static StandardPlugin instance;

	private List<IStorage> storages;
	private List<LogWriter> logs;

	private StandardConfig config;

	private GateStorage gateStorage;
	private TitleStorage titleStorage;
	private PlayerStorage playerStorage;

	public StandardPlugin() {
		instance = this;
	}

	public static StandardPlugin getPlugin() {
		return instance;
	}

	@Override
	public void onLoad() {
		super.onLoad();
	}

	@Override
	public void onEnable() {
		super.onEnable();

		saveDefaultConfig();

		getConfig().options().copyDefaults(true);
		saveConfig();

		storages = new ArrayList<IStorage>();
		gateStorage = new GateStorage(this);
		titleStorage = new TitleStorage(this);
		playerStorage = new PlayerStorage(this);
		storages.add(gateStorage);
		storages.add(titleStorage);
		storages.add(playerStorage);

		logs = new ArrayList<LogWriter>();

		config = new StandardConfig(this);

		reloadPlugin();

		registerCommands();
		registerEvents();

		EssentialsIntegration.init(this);
		SimplyVanishIntegration.init(this);

		registerJSONAPIHandlers();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new PlayerSaver(), 1200, 1200);
	}

	@Override
	public void onDisable() {
		super.onDisable();

		for (LogWriter writer : logs) {
			writer.unload();
		}

		for (IStorage storage : storages) {
			storage.unload();
		}

		Bukkit.getScheduler().cancelTasks(this);
	}

	public void reloadPlugin() {
		reloadConfig();

		config.reload();

		for (IStorage storage : storages) {
			storage.reload();
		}
	}

	private void registerCommands() {
		List<ICommand> commands = new ArrayList<ICommand>();
		commands.add(new RegisterCommand(this));
		commands.add(new RankCommand(this));
		commands.add(new UnfreezeCommand(this));
		commands.add(new GateCommand(this));
		commands.add(new SetSpawnCommand(this));
		commands.add(new SpawnCommand(this));
		commands.add(new ForumMuteCommand(this));
		commands.add(new StandardCommand(this));
		commands.add(new PvpProtectionCommand(this));
		commands.add(new TitleCommand(this));
		commands.add(new TitlesCommand(this));

		for (ICommand command : commands) {
			command.register();
		}
	}

	private void registerEvents() {
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new DeathListener(this), this);
		pluginManager.registerEvents(new PlayerJoinListener(this), this);
		pluginManager.registerEvents(new PlayerLeaveListener(this), this);
		pluginManager.registerEvents(new PlayerInteractListener(this), this);
		pluginManager.registerEvents(new EntityDamageListener(this), this);
		pluginManager.registerEvents(new RespawnListener(this), this);
		pluginManager.registerEvents(new PlayerMoveListener(this), this);
		pluginManager.registerEvents(new CreatureSpawnListener(this), this);
		pluginManager.registerEvents(new HungerListener(this), this);
		pluginManager.registerEvents(new DispenseListener(this), this);
	}

	private void registerJSONAPIHandlers() {
		Plugin plugin = this.getServer().getPluginManager().getPlugin("JSONAPI");
		if (plugin != null) {
			JSONAPI jsonapi = (JSONAPI) plugin;

			jsonapi.registerAPICallHandler(new ForumPostAPICallHandler(this));
			jsonapi.registerAPICallHandler(new ServerStatusAPICallHandler(this));
			jsonapi.registerAPICallHandler(new PlayerStatsAPICallHandler(this));
			jsonapi.registerAPICallHandler(new WebChatAPICallHandler(this));
		}
	}

	// ------
	// Miscellaneous Player related utilty funcitions mirroring the respective
	// Bukkit versions with a StandardPlayer wrapper
	// ------
	public static void playerBroadcast(Player sender, final String message) {
		for (StandardPlayer player : instance.getOnlinePlayers()) {
			if (player != sender) {
				player.sendMessage(message);
			}
		}
	}

	public static void playerBroadcast(final String message) {
		playerBroadcast(null, message);
	}

	public StandardPlayer[] getOnlinePlayers() {
		Player[] onlinePlayers = Bukkit.getOnlinePlayers();
		StandardPlayer[] result = new StandardPlayer[onlinePlayers.length];

		for (int i = 0; i < onlinePlayers.length; ++i) {
			result[i] = getStandardPlayer(onlinePlayers[i]);
		}

		return result;
	}
	
	/**
	 * Matches the given username to a currently online player's display name
	 * or an offline player's username that has played before
	 * @param username The username to query
	 * @return StandardPlayer instance if online or has played before, null otherwise
	 */
	public StandardPlayer matchPlayer(String username) {
		StandardPlayer player = null;
		
		StandardPlayer[] onlinePlayers = getOnlinePlayers();
		for (StandardPlayer onlinePlayer : onlinePlayers) {
			if (onlinePlayer.getDisplayName(false).toLowerCase().startsWith(username.toLowerCase())) {
				player = onlinePlayer;
				break;
			}
		}
		
		if (player == null) {
			player = getStandardPlayer(username);
			if (!player.hasPlayedBefore()) {
				player = null;
			}
		}

		return player;
	}

	// ------
	// Configuration getters
	// ------
	public int getServerId() {
		return config.getServerId();
	}

	public String getSecretKey() {
		return config.getSecretKey();
	}

	public boolean isDebug() {
		return config.isDebug();
	}

	public String getEndpoint() {
		return config.getEndpoint();
	}

	public int getPvpProtectionTime() {
		return config.getPvpProtectionTime();
	}

	public boolean isPvpProtectionEnabled() {
		return config.getPvpProtectionTime() > 0;
	}

	public int getHungerProtectionTime() {
		return config.getHungerProtectionTime();
	}

	public boolean isHungerProtectionEnabled() {
		return config.getHungerProtectionTime() > 0;
	}

	public int getNewbieStalkerThreshold() {
		return config.getNewbieStalkerThreshold();
	}

	public GateStorage getGateStorage() {
		return gateStorage;
	}

	public TitleStorage getTitleStorage() {
		return titleStorage;
	}

	public StandardPlayer getStandardPlayer(String username) {
		return playerStorage.getPlayer(username);
	}

	public StandardPlayer getStandardPlayer(Object object) {
		if (!(object instanceof Player)) {
			return null;
		}

		return getStandardPlayer(((Player) object).getName());
	}
}

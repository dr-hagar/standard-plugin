package com.sbezboro.standardplugin.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.integrations.EssentialsIntegration;
import com.sbezboro.standardplugin.persistence.PersistedList;
import com.sbezboro.standardplugin.persistence.PersistedProperty;
import com.sbezboro.standardplugin.persistence.PlayerStorage;
import com.sbezboro.standardplugin.persistence.TitleStorage;
import com.sbezboro.standardplugin.persistence.persistables.PersistableLocation;
import com.sbezboro.standardplugin.util.AnsiConverter;
import com.sbezboro.standardplugin.util.MiscUtil;

public class StandardPlayer extends PlayerDelegate {
	private static final String FORUM_MUTED_PROPERTY = "forum-muted";
	private static final String PVP_PROTECTION_PROPERTY = "pvp-protection";
	private static final String BED_LOCATION_PROPERTY = "bed";
	private static final String TITLES_PROPERTY = "titles";

	private PersistedProperty<Boolean> forumMuted;
	private PersistedProperty<Boolean> pvpProtection;
	private PersistedProperty<PersistableLocation> bedLocation;
	private PersistedList<String> titleNames;

	private ArrayList<Title> titles;

	private int timeSpent;
	private int rank;

	private int newbieAttacks;

	public StandardPlayer(final Player player, final PlayerStorage storage) {
		super(player, storage);
	}

	public StandardPlayer(final OfflinePlayer player, final PlayerStorage storage) {
		super(player, storage);
	}

	@Override
	public void loadProperties() {
		titles = new ArrayList<Title>();

		forumMuted = loadProperty(Boolean.class, FORUM_MUTED_PROPERTY);
		pvpProtection = loadProperty(Boolean.class, PVP_PROTECTION_PROPERTY);
		bedLocation = loadProperty(PersistableLocation.class, BED_LOCATION_PROPERTY);
		titleNames = loadList(String.class, TITLES_PROPERTY);

		TitleStorage titleStorage = StandardPlugin.getPlugin().getTitleStorage();
		for (String name : titleNames) {
			Title title = titleStorage.getTitle(name);

			if (title == null) {
				StandardPlugin.getPlugin().getLogger().severe("Player has title non-existant title \"" + name + "\"!");
			} else {
				titles.add(title);
			}
		}
	}

	// ------
	// Persisted property mutators
	// ------
	public Boolean isForumMuted() {
		return forumMuted.getValue();
	}

	public void setForumMuted(boolean forumMuted) {
		this.forumMuted.setValue(forumMuted);
	}

	public boolean toggleForumMute() {
		setForumMuted(!isForumMuted());
		return isForumMuted();
	}

	public boolean isHungerProtected() {
		return timeSpent <= StandardPlugin.getPlugin().getHungerProtectionTime();
	}

	public Boolean isPvpProtected() {
		return pvpProtection.getValue();
	}

	public void setPvpProtection(boolean pvpProtection) {
		this.pvpProtection.setValue(pvpProtection);
	}

	public int getPvpProtectionTimeRemaining() {
		return Math.max(StandardPlugin.getPlugin().getPvpProtectionTime() - timeSpent, 0);
	}

	public void saveBedLocation(Location location) {
		bedLocation.setValue(new PersistableLocation(location));
	}

	public Location getBedLocation() {
		return bedLocation.getValue().getLocation();
	}

	public void addTitle(Title title) {
		if (!titles.contains(title)) {
			titles.add(title);
			titleNames.add(title.getName());
		}
	}

	public Title addTitle(String name) {
		Title title = StandardPlugin.getPlugin().getTitleStorage().getTitle(name);

		addTitle(title);

		return title;
	}

	public void removeTitle(String name) {
		Title title = StandardPlugin.getPlugin().getTitleStorage().getTitle(name);

		titles.remove(title);
		titleNames.remove(name);
	}

	// ------
	// Non-persisted property mutators
	// ------
	public int getTimeSpent() {
		return timeSpent;
	}

	public void setTimeSpent(int timeSpent) {
		this.timeSpent = timeSpent;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getNewbieAttacks() {
		return newbieAttacks;
	}

	public int incrementNewbieAttacks() {
		return newbieAttacks++;
	}

	public ArrayList<Title> getTitles() {
		return titles;
	}

	public boolean hasTitle(String name) {
		for (Title title : titles) {
			if (title.getName().equals(name)) {
				return true;
			}
		}

		return false;
	}

	public boolean isNewbieStalker() {
		return hasTitle(Title.NEWBIE_STALKER);
	}

	public boolean isTop10Veteran() {
		return hasTitle(Title.TOP10_VETERAN);
	}

	public boolean isTop40Veteran() {
		return hasTitle(Title.TOP40_VETERAN);
	}

	public boolean isVeteran() {
		return hasTitle(Title.VETERAN);
	}

	public void onLeaveServer() {
		player = null;
	}

	public String getRankDescription(boolean self, int rank) {
		String message = "";
		
		Title broadcastedTitle = null;
		for (Title title : getTitles()) {
			if (title.isBroadcast()) {
				broadcastedTitle = title;
				break;
			}
		}
		
		if (broadcastedTitle == null) {
			for (Title title : getTitles()) {
				if (title.getName().equals(Title.TOP10_VETERAN) ||
						title.getName().equals(Title.TOP40_VETERAN) ||
						title.getName().equals(Title.VETERAN)) {
					broadcastedTitle = title;
					break;
				}
			}
		}
		
		if (self) {
			message = "You ";

			if (broadcastedTitle != null) {
				message += "are a " + ChatColor.GOLD + broadcastedTitle.getDisplayName() + ChatColor.WHITE + " and ";
			}

			message += "are ranked " + ChatColor.AQUA + MiscUtil.getRankString(rank) + ChatColor.WHITE + " on the server!";
		} else {
			if (broadcastedTitle != null) {
				message += ChatColor.GOLD + broadcastedTitle.getDisplayName() + ChatColor.WHITE + " and ";
			}
			
			message += ChatColor.AQUA + getDisplayName() + ChatColor.WHITE + " is ranked " + ChatColor.AQUA
					+ MiscUtil.getRankString(rank) + ChatColor.WHITE + " on the server!";
		}
		
		return message;
	}

	public String getTimePlayedDescription(boolean self, String time) {
		if (self) {
			return "You have played here " + ChatColor.AQUA + time + ChatColor.WHITE + ".";
		} else {
			return getName() + " has played here " + ChatColor.AQUA + time + ChatColor.WHITE + ".";
		}
	}

	public String[] getTitleDescription(boolean self) {
		ArrayList<String> messages = new ArrayList<String>();

		String name = self ? "You" + ChatColor.RESET + " have" : getDisplayName(false) + ChatColor.RESET + " has";

		if (titles.size() == 0) {
			messages.add(ChatColor.AQUA + name + " no titles.");
		} else {
			messages.add(ChatColor.AQUA + name + " the following titles:");
			for (Title title : titles) {
				messages.add(ChatColor.DARK_AQUA + title.getDisplayName());
			}
		}

		return messages.toArray(new String[messages.size()]);
	}

	public HashMap<String, Object> getInfo() {
		HashMap<String, Object> info = new HashMap<String, Object>();

		info.put("username", getName());
		info.put("address", getAddress().getAddress().getHostAddress());

		if (hasNickname()) {
			String nicknameAnsi = AnsiConverter.toAnsi(player.getDisplayName());
			String nickname = getDisplayName(false);

			info.put("nickname_ansi", nicknameAnsi);
			info.put("nickname", nickname);
		}

		info.put("rank", getRank());
		info.put("time_spent", getTimeSpent());

		info.put("is_pvp_protected", isPvpProtected());
		info.put("is_hunger_protected", isHungerProtected());

		info.put("world", getLocation().getWorld().getName());
		info.put("x", getLocation().getBlockX());
		info.put("y", getLocation().getBlockY());
		info.put("z", getLocation().getBlockZ());

		info.put("health", getHealth());

		ArrayList<HashMap<String, String>> titleInfos = new ArrayList<HashMap<String, String>>();
		for (Title title : titles) {
			HashMap<String, String> titleInfo = new HashMap<String, String>();
			titleInfo.put("name", title.getName());
			titleInfo.put("display_name", title.getDisplayName());
			titleInfos.add(titleInfo);
		}

		info.put("titles", titleInfos);

		return info;
	}

	public boolean hasNickname() {
		return EssentialsIntegration.hasNickname(getName());
	}

	public String getDisplayName(boolean colored) {
		if (hasNickname()) {
			String name = EssentialsIntegration.getNickname(getName());

			if (!colored) {
				return ChatColor.stripColor(name);
			}

			return name;
		}

		return getName();
	}

	@Override
	public String getDisplayName() {
		return getDisplayName(true);
	}
}

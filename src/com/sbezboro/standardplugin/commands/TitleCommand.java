package com.sbezboro.standardplugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.model.Title;

public class TitleCommand extends BaseCommand {

	public TitleCommand(StandardPlugin plugin) {
		super(plugin, "sstitle");
	}

	@Override
	public boolean handle(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			showUsageInfo(sender);
			return false;
		}

		if (args[0].equalsIgnoreCase("create")) {
			if (args.length <= 2) {
				showUsageInfo(sender);
				return false;
			}

			String displayName = getRemainingString(args, 2);

			plugin.getTitleStorage().createTitle(args[1], displayName);

			sender.sendMessage("Title " + args[1] + " created!");
		} else if (args[0].equalsIgnoreCase("rename")) {
			if (args.length <= 2) {
				showUsageInfo(sender);
				return false;
			}

			String displayName = getRemainingString(args, 2);
			
			String titleName = args[1];
			
			Title title = plugin.getTitleStorage().getTitle(titleName);
			if (title == null) {
				sender.sendMessage("Title doesn't exist!");
				return true;
			}

			title.setDisplayName(displayName);

			sender.sendMessage("Title " + args[1] + " renamed!");
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("list")) {
				sender.sendMessage("The following titles exist:");

				for (Title title : plugin.getTitleStorage().getTitles()) {
					sender.sendMessage(title.getDescription());
				}
			} else {
				showUsageInfo(sender);
				return false;
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("broadcast")) {
				String titleName = args[1];
				Title title = plugin.getTitleStorage().getTitle(titleName);
				if (title == null) {
					sender.sendMessage("Title doesn't exist!");
					return true;
				}
				
				title.setBroadcast(!title.isBroadcast());
				
				sender.sendMessage(String.format("%s%s%s is %s", ChatColor.AQUA, title.getDisplayName(), 
						ChatColor.RESET, title.isBroadcast() ? "now broadcasted" : "no longer broadcasted"));
			} else if (args[0].equalsIgnoreCase("delete")) {
				String titleName = args[1];
				Title title = plugin.getTitleStorage().getTitle(titleName);
				if (title == null) {
					sender.sendMessage("Title doesn't exist!");
					return true;
				}

				plugin.getTitleStorage().removeTitle(title);
				
				sender.sendMessage(String.format("%s%s%s removed!", ChatColor.AQUA, title.getDisplayName(),
						ChatColor.RESET));
			}
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("add")) {
				StandardPlayer player = plugin.matchPlayer(args[1]);
				if (player == null) {
					sender.sendMessage("Player " + args[1] + " not found!");
					return true;
				}
				
				String titleName = args[2];

				Title title = plugin.getTitleStorage().getTitle(titleName);
				if (title == null) {
					sender.sendMessage("Title doesn't exist!");
					return true;
				}

				if (player.hasTitle(titleName)) {
					sender.sendMessage(player.getDisplayName(false) + " already has the title " + titleName + "!");
				} else {
					player.addTitle(title);

					StandardPlugin.broadcast(ChatColor.AQUA + player.getDisplayName() + ChatColor.RESET + " now has the title " + ChatColor.AQUA
							+ title.getDisplayName());
				}

				return true;
			} else if (args[0].equalsIgnoreCase("remove")) {
				StandardPlayer player = plugin.matchPlayer(args[1]);
				if (player == null) {
					sender.sendMessage("Player " + args[1] + " not found!");
					return true;
				}
				
				String titleName = args[2];

				if (player.hasTitle(titleName)) {
					Title title = plugin.getTitleStorage().getTitle(titleName);

					player.removeTitle(title);

					StandardPlugin.broadcast(ChatColor.AQUA + player.getDisplayName() + ChatColor.RESET + " no longer has the title " + ChatColor.AQUA
							+ title.getDisplayName());
				} else {
					sender.sendMessage(player.getDisplayName(false) + " doesn't have the title " + titleName + "!");
				}

				return true;
			} else {
				showUsageInfo(sender);
				return false;
			}
		} else {
			showUsageInfo(sender);
			return false;
		}

		return false;
	}

	@Override
	public void showUsageInfo(CommandSender sender) {
		sender.sendMessage("Usage: /" + name + " list");
		sender.sendMessage("Usage: /" + name + " broadcast <name>");
		sender.sendMessage("Usage: /" + name + " create <name> <displayName>");
		sender.sendMessage("Usage: /" + name + " delete <name>");
		sender.sendMessage("Usage: /" + name + " add <player> <title>");
		sender.sendMessage("Usage: /" + name + " remove <player> <title>");
	}

	@Override
	public boolean isPlayerOnly(int numArgs) {
		return false;
	}

}

package com.antibrainrot;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiBrainrotPlugin extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        if (getCommand("antibrainrot") != null) {
            getCommand("antibrainrot").setExecutor(this);
        }

        getLogger().info("Anti-Brainrot включен! Слова 'six seven' / '67' / 'сикс севен' будут блокироваться.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Anti-Brainrot выключен.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("antibrainrot.admin")) {
                sender.sendMessage("§cУ вас нет прав для использования этой команды.");
                return true;
            }
            reloadConfig();
            sender.sendMessage("§a[Anti-Brainrot] Конфигурация перезагружена.");
            return true;
        }

        sender.sendMessage("§eИспользование: /antibrainrot reload");
        return true;
    }
}

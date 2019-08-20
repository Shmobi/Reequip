package com.litts.reequip;

import org.bukkit.plugin.java.JavaPlugin;

public class ReequipPlugin extends JavaPlugin {

    private Configuration config;

    @Override
    public void onEnable() {
        config = new Configuration();
        config.load();
        getServer().getPluginManager().registerEvents(new EventListener(config, this), this);
        getServer().getPluginCommand(CommandListener.COMMAND_TOGGLE).setExecutor(new CommandListener(config));
    }

    @Override
    public void onDisable() {
        config.save();
    }
}

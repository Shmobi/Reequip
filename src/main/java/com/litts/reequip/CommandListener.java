package com.litts.reequip;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CommandListener implements CommandExecutor {

    public static final String COMMAND_TOGGLE = "reequip";
    private static final String MESSAGE_TOGGLE_ENABLED = "Reequip is now enabled";
    private static final String MESSAGE_TOGGLE_DISABLED = "Reequip is now disabled";
    private static final String MESSAGE_TOGGLE_CREATIVE = "Reequip is always disabled in creative mode";

    private final Configuration config;

    public CommandListener(Configuration config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        if (commandSender instanceof Player && COMMAND_TOGGLE.equals(command.getName()) && args.length == 0) {
            Player player = (Player) commandSender;
            if (player.getGameMode() == GameMode.CREATIVE) {
                player.sendMessage(MESSAGE_TOGGLE_CREATIVE);
            } else {
                UUID senderUUID = player.getUniqueId();
                config.setEnabled(senderUUID, !config.isEnabled(senderUUID));
                String message = config.isEnabled(senderUUID) ? MESSAGE_TOGGLE_ENABLED : MESSAGE_TOGGLE_DISABLED;
                player.sendMessage(message);
            }
            return true;
        }
        return false;
    }
}

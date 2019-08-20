package com.litts.reequip;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class Configuration {

    private static final String PERMISSION_USE = "reequip.use";
    private static final String CONFIG_FILE_NAME = "save.txt";
    private static final String PLUGIN_NAME = "Reequip";
    private final Set<UUID> enabledUUIDs = new HashSet<>();

    public void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (Scanner scanner = new Scanner(configFile)) {
                while (scanner.hasNextLine()) {
                    enabledUUIDs.add(UUID.fromString(scanner.nextLine()));
                }
            } catch (FileNotFoundException fnf) {
                logFileError(fnf);
            }
        } else {
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                enabledUUIDs.add(player.getUniqueId());
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                enabledUUIDs.add(player.getUniqueId());
            }
        }
    }

    public void save() {
        File configFile = getConfigFile();
        configFile.delete();
        try (PrintWriter writer = new PrintWriter(configFile)) {
            for (UUID uuid : enabledUUIDs) {
                writer.println(uuid);
            }
        } catch (FileNotFoundException fnf) {
            logFileError(fnf);
        }
    }

    public boolean isEnabled(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }
        return enabledUUIDs.contains(playerUUID);
    }

    public void setEnabled(UUID playerUUID, boolean enabled) {
        if (enabled) {
            enabledUUIDs.add(playerUUID);
        } else {
            enabledUUIDs.remove(playerUUID);
        }
    }

    public boolean hasUsePermission(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            return player.hasPermission(PERMISSION_USE);
        }
        return false;
    }

    private void logFileError(FileNotFoundException fnf) {
        Bukkit.getLogger().log(Level.SEVERE, "Save file couldn't be loaded. Is access to the folder granted?", fnf);
    }

    private File getConfigFile() {
        File jarFile = new File(Configuration.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        File configFolder = new File(jarFile.getParentFile(), PLUGIN_NAME);
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }
        return new File(configFolder, CONFIG_FILE_NAME);
    }
}

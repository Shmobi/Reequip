package com.litts.reequip;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class EventListener implements Listener {

    private final Configuration config;
    private final ReequipPlugin plugin;

    public EventListener(Configuration config, ReequipPlugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoined(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore() && config.hasUsePermission(player.getUniqueId())) {
            config.setEnabled(player.getUniqueId(), true);
        }
    }

    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player != null && config.hasUsePermission(player.getUniqueId()) && config.isEnabled(player.getUniqueId())) {
            ItemStack originalStack = event.getItemInHand();
            if (originalStack != null) {
                Material originalMaterial = originalStack.getType();
                if (originalStack.getAmount() == 1 && originalMaterial == event.getBlock().getType()) {
                    new ReequipRunnable(originalMaterial, event.getHand(), player.getInventory()).runTask(plugin);
                }
            }
        }
    }

    @EventHandler
    public void onItemBroken(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        if (player != null && config.hasUsePermission(player.getUniqueId()) && config.isEnabled(player.getUniqueId())) {
            ItemStack originalStack = event.getBrokenItem();
            PlayerInventory inventory = player.getInventory();
            new ReequipRunnable(originalStack.getType(), getItemSlot(originalStack, inventory), inventory).runTask(plugin);
        }
    }

    private EquipmentSlot getItemSlot(ItemStack originalStack, PlayerInventory inventory) {
        MaterialItem materialItem = MaterialItem.fromType(originalStack.getType());
        if (materialItem != null) {
            switch (materialItem) {
                case HELMET:
                    return EquipmentSlot.HEAD;
                case CHESTPLATE:
                    return EquipmentSlot.CHEST;
                case LEGGINGS:
                    return EquipmentSlot.LEGS;
                case BOOTS:
                    return EquipmentSlot.FEET;
                default:
                    // handled like all other unmapped types
                    break;
            }
        }
        return originalStack.equals(inventory.getItemInMainHand()) ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;
    }

    private enum MaterialItem {
        PICKAXE(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.STONE_PICKAXE, Material.WOODEN_PICKAXE),
        AXE(Material.DIAMOND_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.STONE_AXE, Material.WOODEN_AXE),
        SHOVEL(Material.DIAMOND_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.STONE_SHOVEL, Material.WOODEN_SHOVEL),
        HOE(Material.DIAMOND_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.STONE_HOE, Material.WOODEN_HOE),
        SWORD(Material.DIAMOND_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.STONE_SWORD, Material.WOODEN_SWORD),
        HELMET(Material.DIAMOND_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.TURTLE_HELMET),
        CHESTPLATE(Material.DIAMOND_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE),
        LEGGINGS(Material.DIAMOND_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS),
        BOOTS(Material.DIAMOND_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS);

        private final Material[] sortedMaterials;

        MaterialItem(Material... sortedMaterials) {
            this.sortedMaterials = sortedMaterials;
        }

        private static MaterialItem fromType(Material type) {
            for (MaterialItem materialItem : values()) {
                for (Material material : materialItem.sortedMaterials) {
                    if (material == type) {
                        return materialItem;
                    }
                }
            }
            return null;
        }
    }

    private class ReequipRunnable extends BukkitRunnable {

        private final Material originalMaterial;
        private final EquipmentSlot equipmentSlot;
        private final PlayerInventory inventory;

        private ReequipRunnable(Material originalMaterial, EquipmentSlot equipmentSlot, PlayerInventory inventory) {
            this.originalMaterial = originalMaterial;
            this.equipmentSlot = equipmentSlot;
            this.inventory = inventory;
        }

        @Override
        public void run() {
            List<Material> suitableMaterials = new ArrayList<>();
            MaterialItem materialItem = MaterialItem.fromType(originalMaterial);
            suitableMaterials.add(originalMaterial);
            if (materialItem != null) {
                for (Material material : materialItem.sortedMaterials) {
                    if (material != originalMaterial) {
                        suitableMaterials.add(material);
                    }
                }
            }
            for (Material suitableMaterial : suitableMaterials) {
                int suitableItemPosition = inventory.first(suitableMaterial);
                if (suitableItemPosition != -1) {
                    ItemStack suitableItem = inventory.getItem(suitableItemPosition);
                    inventory.setItem(suitableItemPosition, null);
                    equip(suitableItem);
                    return;
                }
            }
        }

        private void equip(ItemStack item) {
            switch (equipmentSlot) {
                case HAND:
                    inventory.setItemInMainHand(item);
                    break;
                case OFF_HAND:
                    inventory.setItemInOffHand(item);
                    break;
                case HEAD:
                    inventory.setHelmet(item);
                    break;
                case CHEST:
                    inventory.setChestplate(item);
                    break;
                case LEGS:
                    inventory.setLeggings(item);
                    break;
                case FEET:
                    inventory.setBoots(item);
                    break;
                default:
                    // all EquiptmentSlots are handled
                    break;
            }
        }
    }

}

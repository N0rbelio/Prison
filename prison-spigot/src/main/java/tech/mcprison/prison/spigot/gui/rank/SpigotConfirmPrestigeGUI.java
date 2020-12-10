package tech.mcprison.prison.spigot.gui.rank;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import tech.mcprison.prison.spigot.SpigotPrison;
import tech.mcprison.prison.spigot.gui.SpigotGUIComponents;

import java.util.List;

public class SpigotConfirmPrestigeGUI extends SpigotGUIComponents {

    private final Player p;
    private final Configuration messages = messages();

    public SpigotConfirmPrestigeGUI(Player p) {
        this.p = p;
    }

    public void open(){

        // Create the inventory
        int dimension = 9;
        Inventory inv = Bukkit.createInventory(null, dimension, SpigotPrison.format("&3Prestige -> Confirmation"));

        if (guiBuilder(inv)) return;

        // Open the inventory
        openGUI(p, inv);
    }

    private boolean guiBuilder(Inventory inv) {
        try {
            buttonsSetup(inv);
        } catch (NullPointerException ex){
            p.sendMessage(SpigotPrison.format("&cThere's a null value in the GuiConfig.yml [broken]"));
            ex.printStackTrace();
            return true;
        }
        return false;
    }

    private void buttonsSetup(Inventory inv) {

        // Blocks of the mine
        List<String> confirmLore = createLore(
                messages.getString("Lore.ClickToConfirm"),
                messages.getString("Lore.PrestigeWarning"),
                messages.getString("Lore.PrestigeWarning2"),
                messages.getString("Lore.PrestigeWarning3")
        );

        // Blocks of the mine
        List<String> cancelLore = createLore(
                messages.getString("Lore.ClickToCancel"));

        // Create the button, set up the material, amount, lore and name
        ItemStack confirm = createButton(Material.EMERALD_BLOCK, 1, confirmLore, SpigotPrison.format("&3" + "Confirm: Prestige"));
        ItemStack cancel = createButton(Material.REDSTONE_BLOCK, 1, cancelLore, SpigotPrison.format("&3" + "Cancel: Don't Prestige"));

        // Position of the button
        inv.setItem(2, confirm);
        inv.setItem(6, cancel);
    }

}

package tech.seife.moderation.commands.spy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import tech.seife.moderation.datamanager.spiedtext.SpiedText;
import tech.seife.moderation.datamanager.spiedtext.SpiedTextManager;

import java.util.Set;

public class ViewSpiedText implements CommandExecutor {

    private final SpiedTextManager spiedTextManager;

    public ViewSpiedText(SpiedTextManager spiedTextManager) {
        this.spiedTextManager = spiedTextManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            if (Bukkit.getPlayer(args[0]) != null) {
                ((Player) sender).getInventory().addItem(createBook(Bukkit.getPlayer(args[0]).getDisplayName()));
            }
        }

        return true;
    }

    private ItemStack createBook(String playerName) {
        ItemStack itemStack = new ItemStack(Material.WRITABLE_BOOK, 1);

        Set<SpiedText> pastMessages = spiedTextManager.retrieveSpiedText(playerName);

        BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();

        for (SpiedText spiedText : pastMessages) {
            bookMeta.addPage("Date: " + spiedText.getDate() + "        Message: " + spiedText.getText());
        }
        bookMeta.setAuthor(playerName);
        bookMeta.setTitle("Spied text of: " + playerName);

        itemStack.setItemMeta(bookMeta);

        return itemStack;
    }
}

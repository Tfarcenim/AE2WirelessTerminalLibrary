package tfar.ae2wtlib.wut;

import appeng.container.ContainerLocator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WUTHandler {
    public static boolean hasTerminal(ItemStack itemStack, String terminal) {
        if(!terminalNames.contains(terminal)) return false;
        if(itemStack.getTag() == null) return false;
        return itemStack.getTag().getBoolean(terminal);
    }

    public static String getCurrentTerminal(ItemStack wirelessUniversalTerminal) {
        if(!(wirelessUniversalTerminal.getItem() instanceof ItemWUT) || wirelessUniversalTerminal.getTag() == null)
            return "noTerminal";
        String currentTerminal = wirelessUniversalTerminal.getTag().getString("currentTerminal");

        if(!wirelessTerminals.containsKey(currentTerminal)) for(String terminal : terminalNames)
            if(wirelessUniversalTerminal.getTag().getBoolean(terminal)) {
                currentTerminal = terminal;
                wirelessUniversalTerminal.getTag().putString("currentTerminal", currentTerminal);
                break;
            }
        return currentTerminal;
    }

    public static void cycle(ItemStack itemStack) {
        if(itemStack.getTag() == null) return;
        String nextTerminal = itemStack.getTag().getString("currentTerminal");
        do {
            int i = terminalNames.indexOf(nextTerminal) + 1;
            if(i == terminalNames.size()) i = 0;
            nextTerminal = terminalNames.get(i);
        } while(!itemStack.getTag().getBoolean(nextTerminal));
        itemStack.getTag().putString("currentTerminal", nextTerminal);
    }

    public static void open(final PlayerEntity player, final ContainerLocator locator) {
        ItemStack is = player.inventory.getStackInSlot(locator.getItemIndex());
        if(is.getTag() == null) return;
        String currentTerminal = is.getTag().getString("currentTerminal");

        if(!wirelessTerminals.containsKey(currentTerminal)) for(String terminal : terminalNames)
            if(is.getTag().getBoolean(terminal)) {
                currentTerminal = terminal;
                is.getTag().putString("currentTerminal", currentTerminal);
                break;
            }
        if(!wirelessTerminals.containsKey(currentTerminal)) {
            player.sendStatusMessage(new StringTextComponent("This terminal does not contain any other Terminals"), false);
            return;
        }
        wirelessTerminals.get(currentTerminal).open(player, locator);
    }

    private static final HashMap<String, containerOpener> wirelessTerminals = new HashMap<>();
    private static final List<String> terminalNames = new ArrayList<>();

    public static void addTerminal(String Name, containerOpener open) {
        if(terminalNames.contains(Name)) return;
        wirelessTerminals.put(Name, open);
        terminalNames.add(Name);
    }

    @FunctionalInterface
    public interface containerOpener {
        void open(final PlayerEntity player, final ContainerLocator locator);
    }
}
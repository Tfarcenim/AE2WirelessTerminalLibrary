package tfar.ae2wt.wut.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import tfar.ae2wt.AE2WirelessTerminals;

public class InputHelper {

    public static ItemStack getInputStack(CraftingInventory inventory, Ingredient ingredient) {
        for(int i = 0; i < inventory.size(); i++)
            if(ingredient.test(inventory.getStack(i))) return inventory.getStack(i);
        return ItemStack.EMPTY;
    }

    public static int getInputCount(CraftingInventory inventory) {
        int count = 0;
        for(int i = 0; i < inventory.size(); i++) if(!inventory.getStack(i).isEmpty()) count++;
        return count;
    }

    public static Ingredient wut = Ingredient.ofItems(AE2WirelessTerminals.UNIVERSAL_TERMINAL);
}
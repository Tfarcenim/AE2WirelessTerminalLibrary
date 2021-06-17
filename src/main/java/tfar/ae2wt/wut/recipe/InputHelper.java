package tfar.ae2wt.wut.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import tfar.ae2wt.init.ModItems;

public class InputHelper {

    public static ItemStack getInputStack(CraftingInventory inventory, Ingredient ingredient) {
        for(int i = 0; i < inventory.getSizeInventory(); i++)
            if(ingredient.test(inventory.getStackInSlot(i))) return inventory.getStackInSlot(i);
        return ItemStack.EMPTY;
    }

    public static int getInputCount(CraftingInventory inventory) {
        int count = 0;
        for(int i = 0; i < inventory.getSizeInventory(); i++) if(!inventory.getStackInSlot(i).isEmpty()) count++;
        return count;
    }

    public static Ingredient wut = Ingredient.fromItems(ModItems.UNIVERSAL_TERMINAL);
}
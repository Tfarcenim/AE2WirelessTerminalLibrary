package tfar.ae2wt.wut.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.util.ResourceLocation;
import tfar.ae2wt.init.ModItems;

public abstract class Common implements ICraftingRecipe {

    protected final ItemStack outputStack = new ItemStack(ModItems.UNIVERSAL_TERMINAL);
    protected final ResourceLocation id;

    protected Common(ResourceLocation id) {
        this.id = id;
    }


    @Override
    public boolean canFit(int width, int height) {
        return width > 1 || height > 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return outputStack;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }
}
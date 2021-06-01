package tfar.ae2wt.wut.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.util.Identifier;
import tfar.ae2wt.AE2WirelessTerminals;

public abstract class Common implements CraftingRecipe {

    protected final ItemStack outputStack = new ItemStack(AE2WirelessTerminals.UNIVERSAL_TERMINAL);
    protected final Identifier id;

    protected Common(Identifier id) {
        this.id = id;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean fits(int width, int height) {
        return width > 1 || height > 1;
    }

    @Override
    public ItemStack getOutput() {
        return outputStack;
    }

    @Override
    public Identifier getId() {
        return id;
    }
}
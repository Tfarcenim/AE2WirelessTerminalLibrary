package tfar.ae2wt.wut.recipe;

import net.minecraft.item.crafting.IRecipeSerializer;
import tfar.ae2wt.wut.WUTHandler;

public abstract class Serializer<T extends Common> implements IRecipeSerializer<T> {
    protected boolean validateOutput(String s) {
        if(s == null) return true;
        return !WUTHandler.terminalNames.contains(s);
    }
}
package tfar.ae2wt.wut.recipe;

import net.minecraft.recipe.RecipeSerializer;
import tfar.ae2wt.wut.WUTHandler;

public abstract class Serializer<T extends Common> implements RecipeSerializer<T> {
    protected boolean validateOutput(String s) {
        if(s == null) return true;
        return !WUTHandler.terminalNames.contains(s);
    }
}
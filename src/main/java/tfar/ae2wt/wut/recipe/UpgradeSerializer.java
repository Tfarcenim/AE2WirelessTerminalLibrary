package tfar.ae2wt.wut.recipe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class UpgradeSerializer extends Serializer<Upgrade> {
    public static final UpgradeSerializer INSTANCE = new UpgradeSerializer();

    public static final ResourceLocation ID = new ResourceLocation("ae2wtlib", "upgrade");

    @Override
    public Upgrade read(ResourceLocation id, JsonObject json) {
        UpgradeJsonFormat recipeJson = new Gson().fromJson(json, UpgradeJsonFormat.class);
        if(recipeJson.Terminal == null || validateOutput(recipeJson.TerminalName))
            throw new JsonSyntaxException("A required attribute is missing or invalid!");

        Ingredient Terminal = Ingredient.deserialize(recipeJson.Terminal);

        return new Upgrade(Terminal, recipeJson.TerminalName, id);
    }

    @Override
    public void write(PacketBuffer packetData, Upgrade recipe) {
        recipe.getTerminal().write(packetData);
        packetData.writeString(recipe.getTerminalName());
    }

    @Override
    public Upgrade read(ResourceLocation id, PacketBuffer packetData) {
        Ingredient TerminalA = Ingredient.read(packetData);
        String TerminalAName = packetData.readString(32767);
        return new Upgrade(TerminalA, TerminalAName, id);
    }

    @Override
    public IRecipeSerializer<?> setRegistryName(ResourceLocation name) {
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public Class<IRecipeSerializer<?>> getRegistryType() {
        return null;
    }
}
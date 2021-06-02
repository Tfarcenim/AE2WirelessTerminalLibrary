package tfar.ae2wt.wut.recipe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class CombineSerializer extends Serializer<Combine> {
    public static final CombineSerializer INSTANCE = new CombineSerializer();

    public static final ResourceLocation ID = new ResourceLocation("ae2wtlib", "combine");

    @Override
    public Combine read(ResourceLocation id, JsonObject json) {
        CombineJsonFormat recipeJson = new Gson().fromJson(json, CombineJsonFormat.class);
        if(recipeJson.TerminalA == null || recipeJson.TerminalB == null || validateOutput(recipeJson.TerminalAName) || validateOutput(recipeJson.TerminalBName))
            throw new JsonSyntaxException("A required attribute is missing or invalid!");

        Ingredient TerminalA = Ingredient.deserialize(recipeJson.TerminalA);
        Ingredient TerminalB = Ingredient.deserialize(recipeJson.TerminalB);

        return new Combine(TerminalA, TerminalB, recipeJson.TerminalAName, recipeJson.TerminalBName, id);
    }

    @Override
    public void write(PacketBuffer packetData, Combine recipe) {
        recipe.getTerminalA().write(packetData);
        recipe.getTerminalB().write(packetData);
        packetData.writeString(recipe.getTerminalAName());
        packetData.writeString(recipe.getTerminalBName());
    }

    @Override
    public Combine read(ResourceLocation id, PacketBuffer packetData) {
        Ingredient TerminalA = Ingredient.read(packetData);
        Ingredient TerminalB = Ingredient.read(packetData);
        String TerminalAName = packetData.readString(32767);
        String TerminalBName = packetData.readString(32767);
        return new Combine(TerminalA, TerminalB, TerminalAName, TerminalBName, id);
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
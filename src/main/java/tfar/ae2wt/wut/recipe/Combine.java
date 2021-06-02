package tfar.ae2wt.wut.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class Combine extends Common {
    private final Ingredient TerminalA;
    private final Ingredient TerminalB;
    private final String TerminalAName;
    private final String TerminalBName;

    public Combine(Ingredient TerminalA, Ingredient TerminalB, String TerminalAName, String TerminalBName, ResourceLocation id) {
        super(id);
        this.TerminalA = TerminalA;
        this.TerminalB = TerminalB;
        this.TerminalAName = TerminalAName;
        this.TerminalBName = TerminalBName;
        if(!outputStack.hasTag()) outputStack.setTag(new CompoundNBT());
        outputStack.getTag().putBoolean(TerminalAName, true);
        outputStack.getTag().putBoolean(TerminalBName, true);
    }

    public Ingredient getTerminalA() {
        return TerminalA;
    }

    public Ingredient getTerminalB() {
        return TerminalB;
    }

    public String getTerminalAName() {
        return TerminalAName;
    }

    public String getTerminalBName() {
        return TerminalBName;
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        return !InputHelper.getInputStack(inv, TerminalA).isEmpty() && !InputHelper.getInputStack(inv, TerminalB).isEmpty() && InputHelper.getInputCount(inv) == 2;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        CompoundNBT terminalA = InputHelper.getInputStack(inv, TerminalA).getTag();
        if(terminalA == null) terminalA = new CompoundNBT();
        else terminalA = terminalA.copy();

        CompoundNBT terminalB = InputHelper.getInputStack(inv, TerminalB).getTag();
        if(terminalB == null) terminalB = new CompoundNBT();
        else terminalB = terminalB.copy();

        ItemStack wut = outputStack.copy();
        wut.getTag().merge(terminalB).merge(terminalA);
        return wut;
    }

    @Override
    public Serializer<Combine> getSerializer() {
        return CombineSerializer.INSTANCE;
    }

    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> inputs = NonNullList.create();
        inputs.add(TerminalA);
        inputs.add(TerminalB);
        return inputs;
    }
}
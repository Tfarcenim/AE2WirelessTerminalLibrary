package tfar.ae2wt.wut.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import tfar.ae2wt.wut.WUTHandler;

public class Upgrade extends Common {
    private final Ingredient Terminal;
    private final String TerminalName;

    public Upgrade(Ingredient Terminal, String TerminalName, ResourceLocation id) {
        super(id);
        this.Terminal = Terminal;
        this.TerminalName = TerminalName;
        if(!outputStack.hasTag()) outputStack.setTag(new CompoundNBT());
        outputStack.getTag().putBoolean(TerminalName, true);
    }

    public Ingredient getTerminal() {
        return Terminal;
    }

    public String getTerminalName() {
        return TerminalName;
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        ItemStack wut = InputHelper.getInputStack(inv, InputHelper.wut);
        return !InputHelper.getInputStack(inv, Terminal).isEmpty() && !wut.isEmpty()
                && InputHelper.getInputCount(inv) == 2 && !WUTHandler.hasTerminal(wut, TerminalName);
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack wut = InputHelper.getInputStack(inv, InputHelper.wut).copy();
        CompoundNBT terminal = InputHelper.getInputStack(inv, Terminal).getTag().copy();
        wut.getTag().putBoolean(TerminalName, true);
        terminal.merge(wut.getTag());
        wut.setTag(terminal);

        return wut;
    }

    @Override
    public Serializer<Upgrade> getSerializer() {
        return UpgradeSerializer.INSTANCE;
    }

    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> inputs = NonNullList.create();
        inputs.add(Terminal);
        inputs.add(InputHelper.wut);
        return inputs;
    }
}
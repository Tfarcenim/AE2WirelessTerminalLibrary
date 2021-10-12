package tfar.ae2wt.wirelesscraftingterminal;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageMonitorable;
import appeng.container.slot.CraftingTermSlot;
import appeng.helpers.IContainerCraftingPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public class WirelessCraftingTermSlot extends CraftingTermSlot {
  private final IContainerCraftingPacket container;

  public WirelessCraftingTermSlot(PlayerEntity player, IActionSource mySrc, IEnergySource energySrc, IStorageMonitorable storage, IItemHandler cMatrix, IItemHandler secondMatrix, IContainerCraftingPacket container) {
    super(player, mySrc, energySrc, storage, cMatrix, secondMatrix, container);
    this.container = container;
  }

  // TODO: This is really hacky and NEEDS to be solved with a full container/gui
  // refactoring.
  @Override
  protected IRecipe<CraftingInventory> findRecipe(CraftingInventory ic, World world) {
    if (this.container instanceof WirelessCraftingTerminalContainer) {
      final WirelessCraftingTerminalContainer containerTerminal = (WirelessCraftingTerminalContainer) this.container;
      final IRecipe<CraftingInventory> recipe = containerTerminal.getCurrentRecipe();

      if (recipe != null && recipe.matches(ic, world)) {
        return containerTerminal.getCurrentRecipe();
      }
    }

    return world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, ic, world).orElse(null);
  }

  // TODO: This is really hacky and NEEDS to be solved with a full container/gui
  // refactoring.
  @Override
  protected NonNullList<ItemStack> getRemainingItems(CraftingInventory ic, World world) {
    if (this.container instanceof WirelessCraftingTerminalContainer) {
      final WirelessCraftingTerminalContainer containerTerminal = (WirelessCraftingTerminalContainer) this.container;
      final IRecipe<CraftingInventory> recipe = containerTerminal.getCurrentRecipe();

      if (recipe != null && recipe.matches(ic, world)) {
        return containerTerminal.getCurrentRecipe().getRemainingItems(ic);
      }
    }

    return super.getRemainingItems(ic, world);
  }
}

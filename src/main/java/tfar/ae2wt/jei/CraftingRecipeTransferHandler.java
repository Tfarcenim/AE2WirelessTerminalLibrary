package tfar.ae2wt.jei;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tfar.ae2wt.wirelesscraftingterminal.WirelessCraftingTerminalContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftingRecipeTransferHandler extends RecipeTransferHandler<WirelessCraftingTerminalContainer> {
  CraftingRecipeTransferHandler(Class<WirelessCraftingTerminalContainer> containerClass, IRecipeTransferHandlerHelper helper) {
    super(containerClass, helper);
  }

  @Override
  protected IRecipeTransferError doTransferRecipe(WirelessCraftingTerminalContainer container, IRecipe<?> recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer) {
    // Try to figure out if any slots have missing ingredients
    // Find every "slot" (in JEI parlance) that has no equivalent item in the item repo or player inventory
    List<Integer> missingSlots = new ArrayList<>();

    // We need to track how many of a given item stack we've already used for other slots in the recipe.
    // Otherwise recipes that need 4x<item> will not correctly show missing items if at least 1 of <item> is in
    // the grid.
    Map<IAEItemStack, Integer> reservedGridAmounts = new HashMap<>();

    for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : recipeLayout.getItemStacks()
        .getGuiIngredients().entrySet()) {
      IGuiIngredient<ItemStack> ingredient = entry.getValue();
      List<ItemStack> ingredients = ingredient.getAllIngredients();
      if (!ingredient.isInput() || ingredients.isEmpty()) {
        continue;
      }
      boolean found = false;
      // Player inventory is cheaper to check
      for (ItemStack itemStack : ingredients) {
        if (itemStack != null && player.inventory.getSlotFor(itemStack) != -1) {
          found = true;
          break;
        }
      }
      // Then check the terminal screen's repository of network items
      if (!found) {
        for (ItemStack itemStack : ingredients) {
          if (itemStack != null) {
            // We use AE stacks to get an easily comparable item type key that ignores stack size
            IAEItemStack aeStack = AEItemStack.fromItemStack(itemStack);
            int reservedAmount = reservedGridAmounts.getOrDefault(aeStack, 0) + 1;
            if (container.hasItemType(itemStack, reservedAmount)) {
              reservedGridAmounts.put(aeStack, reservedAmount);
              found = true;
              break;
            }
          }
        }
      }

      if (!found) {
        missingSlots.add(entry.getKey());
      }
    }

    if (!missingSlots.isEmpty()) {
      ITextComponent message = new TranslationTextComponent("jei.appliedenergistics2.missing_items");
      return new TransferWarning(helper.createUserErrorForSlots(message, missingSlots));
    }

    return null;
  }

  protected boolean isCrafting() {
    return true;
  }

  private static class TransferWarning implements IRecipeTransferError {

    private final IRecipeTransferError parent;

    public TransferWarning(IRecipeTransferError parent) {
      this.parent = parent;
    }

    @Override
    public Type getType() {
      return Type.COSMETIC;
    }

    @Override
    public void showError(MatrixStack matrixStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX,
                          int recipeY) {
      this.parent.showError(matrixStack, mouseX, mouseY, recipeLayout, recipeX, recipeY);
    }

  }
}

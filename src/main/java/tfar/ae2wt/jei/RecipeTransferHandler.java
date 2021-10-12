package tfar.ae2wt.jei;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.JEIRecipePacket;
import appeng.helpers.IContainerCraftingPacket;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Iterator;
import java.util.Map;

public abstract class RecipeTransferHandler<T extends Container & IContainerCraftingPacket> implements IRecipeTransferHandler<T> {
    private final Class<T> containerClass;
    protected final IRecipeTransferHandlerHelper helper;

    RecipeTransferHandler(Class<T> containerClass, IRecipeTransferHandlerHelper helper) {
        this.containerClass = containerClass;
        this.helper = helper;
    }

    public final Class<T> getContainerClass() {
        return this.containerClass;
    }

    public final IRecipeTransferError transferRecipe(T container, Object recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        if (!(recipe instanceof IRecipe)) {
            return this.helper.createInternalError();
        }
        IRecipe<?> irecipe = (IRecipe<?>) recipe;
        ResourceLocation recipeId = irecipe.getId();
        if (recipeId == null) {
            return this.helper
                .createUserErrorWithTooltip(new TranslationTextComponent("jei.appliedenergistics2.missing_id"));
        }

        boolean canSendReference = true;
        if (!player.getEntityWorld().getRecipeManager().getRecipe(recipeId).isPresent()) {
            if (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe)) {
                return this.helper.createUserErrorWithTooltip(new TranslationTextComponent("jei.appliedenergistics2.missing_id"));
            }

            canSendReference = false;
        }

        if (!irecipe.canFit(3, 3)) {
            return this.helper.createUserErrorWithTooltip(
                new TranslationTextComponent("jei.appliedenergistics2.recipe_too_large"));
        }
        final IRecipeTransferError error = doTransferRecipe(container, irecipe, recipeLayout, player, maxTransfer);
        if (doTransfer && this.canTransfer(error)) {
            if (canSendReference) {
                NetworkHandler.instance().sendToServer(new JEIRecipePacket(recipeId, isCrafting()));
            } else {
                // To avoid earlier problems of too large packets being sent that crashed the client, as a fallback when
                // the recipe ID could not be resolved, we'll just send the displayed items.
                NonNullList<Ingredient> flatIngredients = NonNullList.withSize(9, Ingredient.EMPTY);
                ItemStack output = ItemStack.EMPTY;

                // Determine the first JEI slot that has an actual input, we'll use this to offset the crafting grid
                // target slot
                int firstInputSlot = recipeLayout.getItemStacks().getGuiIngredients().entrySet().stream().filter((e) -> e.getValue().isInput()).mapToInt(Map.Entry::getKey).min().orElse(0);

                // Now map the actual ingredients into the output/input
                for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : recipeLayout.getItemStacks().getGuiIngredients().entrySet()) {
                    IGuiIngredient<ItemStack> item = entry.getValue();
                    if (item.getDisplayedIngredient() == null) {
                        continue;
                    }

                    int inputIndex = entry.getKey() - firstInputSlot;
                    if (item.isInput() && inputIndex < flatIngredients.size()) {
                        ItemStack displayedIngredient = item.getDisplayedIngredient();
                        if (displayedIngredient != null) {
                            flatIngredients.set(inputIndex, Ingredient.fromStacks(displayedIngredient));
                        }
                    } else if (!item.isInput() && output.isEmpty()) {
                        output = item.getDisplayedIngredient();
                    }
                }

                ShapedRecipe fallbackRecipe = new ShapedRecipe(recipeId, "", 3, 3, flatIngredients, output);
                NetworkHandler.instance().sendToServer(new JEIRecipePacket(fallbackRecipe, isCrafting()));
            }
        }

        return error;
    }

    protected abstract IRecipeTransferError doTransferRecipe(T container, IRecipe<?> recipe, IRecipeLayout recipeLayout,
                                                             PlayerEntity player, boolean maxTransfer);

    protected abstract boolean isCrafting();

    private boolean canTransfer(IRecipeTransferError error) {
        return error == null || error.getType() == IRecipeTransferError.Type.COSMETIC;
    }
}

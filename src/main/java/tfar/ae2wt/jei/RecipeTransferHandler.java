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
        } else {
            IRecipe<?> irecipe = (IRecipe) recipe;
            ResourceLocation recipeId = irecipe.getId();
            if (recipeId == null) {
                return this.helper.createUserErrorWithTooltip(I18n.format("jei.appliedenergistics2.missing_id"));
            } else {
                boolean canSendReference = true;
                if (!player.getEntityWorld().getRecipeManager().getRecipe(recipeId).isPresent()) {
                    if (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe)) {
                        return this.helper.createUserErrorWithTooltip(I18n.format("jei.appliedenergistics2.missing_id"));
                    }

                    canSendReference = false;
                }

                if (!irecipe.canFit(3, 3)) {
                    return this.helper.createUserErrorWithTooltip(I18n.format("jei.appliedenergistics2.recipe_too_large"));
                } else {
                    IRecipeTransferError error = this.doTransferRecipe(container, irecipe, recipeLayout, player, maxTransfer);
                    if (error != null) {
                        return error;
                    } else {
                        if (doTransfer) {
                            if (canSendReference) {
                                NetworkHandler.instance().sendToServer(new JEIRecipePacket(recipeId, this.isCrafting()));
                            } else {
                                NonNullList<Ingredient> flatIngredients = NonNullList.withSize(9, Ingredient.EMPTY);
                                ItemStack output = ItemStack.EMPTY;
                                int firstInputSlot = recipeLayout.getItemStacks().getGuiIngredients().entrySet().stream().filter((e) -> {
                                    return e.getValue().isInput();
                                }).mapToInt(Map.Entry::getKey).min().orElse(0);
                                Iterator var14 = recipeLayout.getItemStacks().getGuiIngredients().entrySet().iterator();

                                while (true) {
                                    while (true) {
                                        Map.Entry entry;
                                        IGuiIngredient item;
                                        do {
                                            if (!var14.hasNext()) {
                                                ShapedRecipe fallbackRecipe = new ShapedRecipe(recipeId, "", 3, 3, flatIngredients, output);
                                                NetworkHandler.instance().sendToServer(new JEIRecipePacket(fallbackRecipe, this.isCrafting()));
                                                return null;
                                            }

                                            entry = (Map.Entry) var14.next();
                                            item = (IGuiIngredient) entry.getValue();
                                        } while (item.getDisplayedIngredient() == null);

                                        int inputIndex = (Integer) entry.getKey() - firstInputSlot;
                                        if (item.isInput() && inputIndex < flatIngredients.size()) {
                                            ItemStack displayedIngredient = (ItemStack) item.getDisplayedIngredient();
                                            if (displayedIngredient != null) {
                                                flatIngredients.set(inputIndex, Ingredient.fromStacks(displayedIngredient));
                                            }
                                        } else if (!item.isInput() && output.isEmpty()) {
                                            output = (ItemStack) item.getDisplayedIngredient();
                                        }
                                    }
                                }
                            }
                        }

                        return null;
                    }
                }
            }
        }
    }

    protected abstract IRecipeTransferError doTransferRecipe(T var1, IRecipe<?> var2, IRecipeLayout var3, PlayerEntity var4, boolean var5);

    protected abstract boolean isCrafting();
}

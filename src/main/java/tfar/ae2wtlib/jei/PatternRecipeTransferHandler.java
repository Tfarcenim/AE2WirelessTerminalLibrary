package tfar.ae2wtlib.jei;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import tfar.ae2wtlib.wpt.WPatternTContainer;

public class PatternRecipeTransferHandler extends RecipeTransferHandler<WPatternTContainer> {
    PatternRecipeTransferHandler(Class<WPatternTContainer> containerClass, IRecipeTransferHandlerHelper helper) {
        super(containerClass, helper);
    }

    @Override
    protected IRecipeTransferError doTransferRecipe(WPatternTContainer container, IRecipe<?> recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer) {
        if (container.isCraftingMode() && recipeLayout.getRecipeCategory().getUid() != VanillaRecipeCategoryUid.CRAFTING) {
            return this.helper.createUserErrorWithTooltip(I18n.format("jei.appliedenergistics2.requires_processing_mode"));
        } else {
            return recipe.getRecipeOutput().isEmpty() ? this.helper.createUserErrorWithTooltip(I18n.format("jei.appliedenergistics2.no_output")) : null;
        }
    }

    protected boolean isCrafting() {
        return false;
    }
}

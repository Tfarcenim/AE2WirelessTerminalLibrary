package tfar.ae2wt.jei;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import tfar.ae2wt.wpt.WirelessPatternTerminalContainer;

public class PatternRecipeTransferHandler extends RecipeTransferHandler<WirelessPatternTerminalContainer> {
    PatternRecipeTransferHandler(Class<WirelessPatternTerminalContainer> containerClass, IRecipeTransferHandlerHelper helper) {
        super(containerClass, helper);
    }

    @Override
    protected IRecipeTransferError doTransferRecipe(WirelessPatternTerminalContainer container, IRecipe<?> recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer) {
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

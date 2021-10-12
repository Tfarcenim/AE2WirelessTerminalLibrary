package tfar.ae2wt.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.util.ResourceLocation;
import tfar.ae2wt.AE2WirelessTerminals;
import tfar.ae2wt.wirelesscraftingterminal.WirelessCraftingTerminalContainer;
import tfar.ae2wt.wpt.WirelessPatternTerminalContainer;

@JeiPlugin
public class JeiPlug implements IModPlugin {

    public static final ResourceLocation UNIVERSAL_RECIPE_TRANSFER_UID = new ResourceLocation("jei", "universal_recipe_transfer_handler");
    public static final ResourceLocation CRAFTING = new ResourceLocation("minecraft", "crafting");


    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(AE2WirelessTerminals.MODID, AE2WirelessTerminals.MODID);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new CraftingRecipeTransferHandler(WirelessCraftingTerminalContainer.class, registration.getTransferHelper()), this.CRAFTING);
        registration.addUniversalRecipeTransferHandler(new PatternRecipeTransferHandler(WirelessPatternTerminalContainer.class, registration.getTransferHelper()));
    }

    public static class RecipeTransferHandler implements IRecipeTransferHandler<WirelessCraftingTerminalContainer> {
        @Override
        public Class<WirelessCraftingTerminalContainer> getContainerClass() {
            return WirelessCraftingTerminalContainer.class;
        }
    }
}

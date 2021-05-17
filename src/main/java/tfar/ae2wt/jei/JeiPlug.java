package tfar.ae2wt.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.util.ResourceLocation;
import tfar.ae2wt.AE2WirelessTerminals;
import tfar.ae2wt.wirelesscraftingterminal.WCTContainer;
import tfar.ae2wt.wpt.WPatternTContainer;

@JeiPlugin
public class JeiPlug implements IModPlugin {

    public static final ResourceLocation UNIVERSAL_RECIPE_TRANSFER_UID = new ResourceLocation("jei", "universal_recipe_transfer_handler");


    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(AE2WirelessTerminals.MODID, AE2WirelessTerminals.MODID);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new RecipeTransferHandler(), UNIVERSAL_RECIPE_TRANSFER_UID);
        registration.addUniversalRecipeTransferHandler(new PatternRecipeTransferHandler(WPatternTContainer.class, registration.getTransferHelper()));
    }


    public static class RecipeTransferHandler implements IRecipeTransferHandler<WCTContainer> {
        @Override
        public Class<WCTContainer> getContainerClass() {
            return WCTContainer.class;
        }
    }
}

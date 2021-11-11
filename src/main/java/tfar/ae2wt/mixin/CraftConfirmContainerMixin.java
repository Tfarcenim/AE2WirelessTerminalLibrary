package tfar.ae2wt.mixin;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpener;
import appeng.container.me.crafting.CraftConfirmContainer;
import net.minecraft.inventory.container.ContainerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfar.ae2wt.init.Menus;
import tfar.ae2wt.wirelesscraftingterminal.WCTGuiObject;

@Mixin(value = CraftConfirmContainer.class,remap = false)
abstract class CraftConfirmContainerMixin {

    @Shadow
    private ICraftingCPU selectedCpu;
    @Shadow
    private ICraftingJob result;

    @Shadow
    protected abstract IGrid getGrid();

    @Shadow
    protected abstract IActionSource getActionSrc();

    @Shadow
    public abstract void setAutoStart(boolean autoStart);

    @Inject(method = "startJob", at = @At(value = "HEAD"), cancellable = true)
    public void serverPacketData(CallbackInfo ci) {
        IActionHost ah = ((AEBaseContainerAccess) this).invokeGetActionHost();
        if(ah instanceof WCTGuiObject) {
            ContainerType<?> originalGui = Menus.WCT;
            if(result == null || result.isSimulation()) return;
            ICraftingLink g = ((ICraftingGrid) getGrid().getCache(ICraftingGrid.class)).submitJob(result, null, selectedCpu, true, getActionSrc());
            setAutoStart(false);
            if(g != null && originalGui != null && ((AEBaseContainer) (Object) this).getLocator() != null)
                ContainerOpener.openContainer(originalGui, ((AEBaseContainer) (Object) this).getPlayerInventory().player, ((AEBaseContainer) (Object) this).getLocator());
            ci.cancel();
        }
    }
}

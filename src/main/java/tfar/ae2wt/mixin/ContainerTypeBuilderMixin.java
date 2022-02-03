package tfar.ae2wt.mixin;

import appeng.container.ContainerLocator;
import appeng.container.implementations.ContainerTypeBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;
import tfar.ae2wt.wirelesscraftingterminal.WCTGuiObject;
import tfar.ae2wt.wirelesscraftingterminal.WCTItem;
import tfar.ae2wt.wirelessinterfaceterminal.WITGuiObject;
import tfar.ae2wt.wirelessinterfaceterminal.WITItem;
import tfar.ae2wt.wpt.WPTGuiObject;
import tfar.ae2wt.wpt.WPTItem;

@Mixin(value = ContainerTypeBuilder.class, remap = false)
public class ContainerTypeBuilderMixin<I> {
    @Shadow
    @Final
    private Class<I> hostInterface;

    @Inject(method = "getHostFromPlayerInventory", at = @At(value = "HEAD"), cancellable = true)
    private void getWirelessHostFromPlayerInventory(PlayerEntity player, ContainerLocator locator, CallbackInfoReturnable<I> cir) {
        ItemStack it = player.inventory.getStackInSlot(locator.getItemIndex());

        if (it.isEmpty()) {
            return;
        }

        // FIXME: this shouldn't be hardcoded
        if (it.getItem() instanceof AbstractWirelessTerminalItem) {
            AbstractWirelessTerminalItem awti = (AbstractWirelessTerminalItem)it.getItem();
            if (awti instanceof WCTItem) {
                cir.setReturnValue(hostInterface.cast(new WCTGuiObject(awti, it, player, locator.getItemIndex())));
            } else if (awti instanceof WPTItem) {
                cir.setReturnValue(hostInterface.cast(new WPTGuiObject(awti, it, player, locator.getItemIndex())));
            } else if (awti instanceof WITItem) {
                cir.setReturnValue(hostInterface.cast(new WITGuiObject(awti, it, player, locator.getItemIndex())));
            }
        }
    }
}

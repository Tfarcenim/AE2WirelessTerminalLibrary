package tfar.ae2wt.mixin;

import appeng.client.gui.me.common.MEMonitorableScreen;
import tfar.ae2wt.init.Menus;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfar.ae2wt.net.C2SSwitchGuiPacket;
import tfar.ae2wt.net.PacketHandler;
import tfar.ae2wt.wirelesscraftingterminal.WirelessCraftingTerminalScreen;
import tfar.ae2wt.wpt.WirelessPatternTerminalScreen;

@Mixin(MEMonitorableScreen.class)
public class MeMonitorableScreenWireless {

    @Inject(method = "showCraftingStatus", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void showWirelessCraftingStatus(CallbackInfo ci) {
        if(!((Object) this instanceof WirelessCraftingTerminalScreen) && !((Object) this instanceof WirelessPatternTerminalScreen)) return;
        PacketHandler.INSTANCE.sendToServer(new C2SSwitchGuiPacket(Registry.MENU.getKey(Menus.WIRELESS_FLUID_TERMINAL).getPath()));
        ci.cancel();
    }
}
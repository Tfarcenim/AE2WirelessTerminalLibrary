package tfar.ae2wtlib.mixin;

import appeng.client.gui.implementations.MEMonitorableScreen;
import appeng.container.implementations.WirelessCraftingStatusContainer;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfar.ae2wtlib.net.C2SSwitchGuiPacket;
import tfar.ae2wtlib.net.PacketHandler;
import tfar.ae2wtlib.wct.WCTScreen;
import tfar.ae2wtlib.wpt.WPTScreen;

@Mixin(MEMonitorableScreen.class)
public class MeMonitorableScreenWireless {

    @Inject(method = "showCraftingStatus", at = @At(value = "INVOKE"), cancellable = true, remap = false)
    private void showWirelessCraftingStatus(CallbackInfo ci) {
        if(!((Object) this instanceof WCTScreen) && !((Object) this instanceof WPTScreen)) return;
        PacketHandler.INSTANCE.sendToServer(new C2SSwitchGuiPacket(Registry.MENU.getKey(WirelessCraftingStatusContainer.TYPE).getPath()));
    }
}
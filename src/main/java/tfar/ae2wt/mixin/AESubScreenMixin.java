package tfar.ae2wt.mixin;

import appeng.client.gui.implementations.AESubScreen;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfar.ae2wt.terminal.WTGuiObject;

@Mixin(value = AESubScreen.class, remap = false)
public class AESubScreenMixin {

    @Mutable
    @Shadow
    @Final
    private ContainerType<?> previousContainerType;

    @Mutable
    @Shadow
    @Final
    private ItemStack previousContainerIcon;

    @Inject(method = "<init>(Ljava/lang/Object;)V", at = @At(value = "TAIL"))
    public void serverPacketData(Object containerHost, CallbackInfo ci) {
        if (containerHost instanceof WTGuiObject) {
            previousContainerType = ((WTGuiObject) containerHost).getType();
            previousContainerIcon = ((WTGuiObject) containerHost).getItemStack();
        }
    }
}

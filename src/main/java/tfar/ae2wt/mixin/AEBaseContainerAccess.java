package tfar.ae2wt.mixin;

import appeng.api.networking.security.IActionHost;
import appeng.container.AEBaseContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = AEBaseContainer.class,remap =  false)
public interface AEBaseContainerAccess {
        @Invoker("getActionHost")
        IActionHost invokeGetActionHost();
}

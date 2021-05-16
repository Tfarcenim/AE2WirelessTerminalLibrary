package tfar.ae2wtlib.mixin;

import net.minecraft.inventory.container.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccess {
    @Accessor void setXPos(int x);
}

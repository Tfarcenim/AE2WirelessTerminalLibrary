package tfar.ae2wt.wirelesscraftingterminal.magnet_card;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class MagnetSettings {

    public MagnetMode magnetMode;

    private MagnetSettings(MagnetMode mode) {
        this.magnetMode = mode;
    }

    public static MagnetSettings from(ItemStack magnetCard) {
        if (magnetCard.isEmpty()) {
            return new MagnetSettings(MagnetMode.NO_CARD);
        } else {
            CompoundNBT nbt = magnetCard.getTag() != null ? magnetCard.getTag().getCompound("magnet_settings") : null;
            if (nbt != null) {
                int ordinal = nbt.getInt("magnetMode");
                MagnetMode mode = MagnetMode.modes[ordinal];
                return new MagnetSettings(mode);
            } else {
                return new MagnetSettings(MagnetMode.NO_CARD);
            }
        }
    }

    public void saveTo(ItemStack magnetCard) {
        magnetCard.getOrCreateTag().put("magnet_settings", toTag());
    }

    public CompoundNBT toTag() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("magnetMode", magnetMode.ordinal());
        return tag;
    }
}
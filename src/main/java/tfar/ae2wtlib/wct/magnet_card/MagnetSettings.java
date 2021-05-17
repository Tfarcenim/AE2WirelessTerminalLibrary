package tfar.ae2wtlib.wct.magnet_card;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class MagnetSettings {

    public MagnetMode magnetMode;

    /**
     * loads {@link MagnetSettings} from a tag.
     * @param tag tag to load the settings from.
     *            An empty tag will result in Default {@link MagnetSettings}
     */
    public MagnetSettings(CompoundNBT tag) {
        if(tag == null) {
            magnetMode = MagnetMode.DEFAULT;
        } else {
            magnetMode = MagnetMode.modes[(tag.getInt("magnetMode"))];
        }
    }

    /**
     * creates {@link MagnetSettings} for an empty {@link ItemStack}
     */
    public MagnetSettings() {
        magnetMode = MagnetMode.INVALID;
    }

    public CompoundNBT toTag() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("magnetMode", magnetMode.ordinal());
        return tag;
    }
}
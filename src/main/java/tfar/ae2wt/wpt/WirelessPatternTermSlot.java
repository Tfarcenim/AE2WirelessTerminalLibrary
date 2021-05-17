package tfar.ae2wt.wpt;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.PatternTermSlot;
import appeng.core.Api;
import appeng.core.sync.BasePacket;
import appeng.core.sync.packets.PatternSlotPacket;
import appeng.helpers.IContainerCraftingPacket;
import tfar.ae2wt.net.C2SPatternSlotPacket;
import tfar.ae2wt.net.PacketHandler;
import tfar.ae2wt.util.FixedEmptyInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.Field;

public class WirelessPatternTermSlot extends PatternTermSlot {
    public WirelessPatternTermSlot(PlayerEntity player, IActionSource mySrc, IEnergySource energySrc, IStorageMonitorable storage, IItemHandler cMatrix, IItemHandler secondMatrix, IItemHandler output, int x, int y, IOptionalSlotHost h, int groupNumber, IContainerCraftingPacket c) {
        super(player, mySrc, energySrc, storage, cMatrix, secondMatrix, output, x, y, h, groupNumber, c);
    }

    @Override
    public BasePacket getRequest(final boolean shift) {
        IItemHandler pattern = null;
        try {
            Field f = getClass().getSuperclass().getSuperclass().getDeclaredField("pattern");
            f.setAccessible(true);
            pattern = (IItemHandler) f.get(this);
            f.setAccessible(false);
        } catch(IllegalAccessException | NoSuchFieldException ignored) {}

        if(pattern == null)
            return new PatternSlotPacket(new FixedEmptyInventory(9), Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(getStack()), shift);

        //todo this seems like an awful idea
        IAEItemStack[] stacks = new IAEItemStack[9];
        for (int i = 0; i < 9; i++) {
            stacks[i] = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(pattern.getStackInSlot(i));
        }
        PacketHandler.INSTANCE.sendToServer(new C2SPatternSlotPacket(Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(getStack()),shift,stacks));

        //This is just a stub. we don't actually use it, but this is the easiest (tho dirty) way to hack our own solution in
        return new PatternSlotPacket(pattern, Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(getStack()), shift);
    }

    private void writeItem(final IAEItemStack slotItem, final PacketBuffer data) {
        if(slotItem == null) data.writeBoolean(false);
        else {
            data.writeBoolean(true);
            slotItem.writeToPacket(data);
        }
    }
}
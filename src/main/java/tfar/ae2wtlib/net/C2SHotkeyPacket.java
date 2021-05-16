package tfar.ae2wtlib.net;

import appeng.container.ContainerLocator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.ae2wtlib.util.ContainerHelper;
import tfar.ae2wtlib.wct.ItemWCT;
import tfar.ae2wtlib.wit.ItemWIT;
import tfar.ae2wtlib.wpt.ItemWPT;
import tfar.ae2wtlib.wut.ItemWUT;
import tfar.ae2wtlib.wut.WUTHandler;

import java.util.function.Supplier;

import static tfar.ae2wtlib.AE2WirelessTerminals.*;

public class C2SHotkeyPacket {

    private String terminalName;

    public C2SHotkeyPacket(String terminalName) {
        this.terminalName = terminalName;
    }

    public C2SHotkeyPacket(PacketBuffer buf) {
        terminalName = buf.readString(32767);
    }

    public void encode(PacketBuffer buf) {
        buf.writeString(terminalName);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PlayerEntity player = ctx.get().getSender();

        if (player == null) return;

        ctx.get().enqueueWork(  ()->  {
                    MinecraftServer server = player.getServer();
            server.execute(() -> {
                if (terminalName.equalsIgnoreCase("crafting")) {
                    PlayerInventory inv = player.inventory;
                    int slot = -1;
                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        ItemStack terminal = inv.getStackInSlot(i);
                        if (terminal.getItem() instanceof ItemWCT || (terminal.getItem() instanceof ItemWUT && WUTHandler.hasTerminal(terminal, "crafting"))) {
                            slot = i;
                            break;
                        }
                    }
                    if (slot == -1) return;
                    ContainerLocator locator = ContainerHelper.getContainerLocatorForSlot(slot);
                    CRAFTING_TERMINAL.open(player, locator);
                } else if (terminalName.equalsIgnoreCase("pattern")) {
                    PlayerInventory inv = player.inventory;
                    int slot = -1;
                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        ItemStack terminal = inv.getStackInSlot(i);
                        if (terminal.getItem() instanceof ItemWPT || (terminal.getItem() instanceof ItemWUT && WUTHandler.hasTerminal(terminal, "pattern"))) {
                            slot = i;
                            break;
                        }
                    }
                    if (slot == -1) return;
                    ContainerLocator locator = ContainerHelper.getContainerLocatorForSlot(slot);
                    PATTERN_TERMINAL.open(player, locator);
                } else if (terminalName.equalsIgnoreCase("interface")) {
                    PlayerInventory inv = player.inventory;
                    int slot = -1;
                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        ItemStack terminal = inv.getStackInSlot(i);
                        if (terminal.getItem() instanceof ItemWIT || (terminal.getItem() instanceof ItemWUT && WUTHandler.hasTerminal(terminal, "interface"))) {
                            slot = i;
                            break;
                        }
                    }
                    if (slot == -1) return;
                    ContainerLocator locator = ContainerHelper.getContainerLocatorForSlot(slot);
                    INTERFACE_TERMINAL.open(player, locator);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

}

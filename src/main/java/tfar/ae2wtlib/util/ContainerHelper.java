package tfar.ae2wtlib.util;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.AEPartLocation;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.util.Platform;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkHooks;
import tfar.ae2wtlib.terminal.ItemWT;
import tfar.ae2wtlib.terminal.WTGuiObject;
import tfar.ae2wtlib.wpt.WPTGuiObject;
import tfar.ae2wtlib.wct.WCTGuiObject;
import tfar.ae2wtlib.wit.WITGuiObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import java.lang.reflect.Constructor;

public final class ContainerHelper<C extends AEBaseContainer, I> {

    private final Class<I> interfaceClass;

    private final ContainerFactory<C, I> factory;

    private final SecurityPermissions requiredPermission;

    public ContainerHelper(ContainerFactory<C, I> factory, Class<I> interfaceClass) {
        this(factory, interfaceClass, null);
    }

    public ContainerHelper(ContainerFactory<C, I> factory, Class<I> interfaceClass, SecurityPermissions requiredPermission) {
        this.requiredPermission = requiredPermission;
        this.interfaceClass = interfaceClass;
        this.factory = factory;
    }

    /**
     * Same as {@link #open}, but allows or additional data to be read from the packet, and passed onto the container.
     */
    public C fromNetwork(int windowId, PlayerInventory inv, PacketBuffer packetBuf) {
        //I host = getHostFromLocator(inv.player, ContainerLocator.read(packetBuf));
        I host = getHostFromLocator(inv.player, ContainerLocator.forHand(inv.player, Hand.MAIN_HAND));
        if(host != null) {
            return factory.create(windowId, inv, host);
        }
        return null;
    }

    public boolean open(PlayerEntity player, ContainerLocator locator) {
        if(!(player instanceof ServerPlayerEntity)) return false;

        I accessInterface = getHostFromLocator(player, locator);

        if(accessInterface == null) return false;

        if(!checkPermission(player, accessInterface)) return false;

        //NetworkHooks.openGui(player,new HandlerFactory(locator, GuiText.Terminal.text(), accessInterface),);

        player.openContainer(new HandlerFactory(locator, GuiText.Terminal.text(), accessInterface));

        return true;
    }

    private class HandlerFactory implements INamedContainerProvider {
        private final ContainerLocator locator;

        private final I accessInterface;

        private final ITextComponent title;


        public HandlerFactory(ContainerLocator locator, ITextComponent title, I accessInterface) {
            this.locator = locator;
            this.title = title;
            this.accessInterface = accessInterface;
        }

        @Override
        public ITextComponent getDisplayName() {
            return title;
        }

        @Override
        public Container createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
            C c = factory.create(syncId, inv, accessInterface);
            // Set the original locator on the opened server-side container for it to more
            // easily remember how to re-open after being closed.
            c.setLocator(locator);
            return c;
        }
    }

    private I getHostFromLocator(PlayerEntity player, ContainerLocator locator) {
        if(locator.hasItemIndex()) return getHostFromPlayerInventory(player, locator);
        return null;
    }

    private I getHostFromPlayerInventory(PlayerEntity player, ContainerLocator locator) {

        ItemStack it = player.inventory.getStackInSlot(locator.getItemIndex());

        if(it.isEmpty()) {
            AELog.debug("Cannot open container for player %s since they no longer hold the item in slot %d", player, locator.hasItemIndex());
            return null;
        }
        
        if (it.getItem() instanceof ItemWT) {
            if (interfaceClass.isAssignableFrom(WCTGuiObject.class))//TODO do something generic, I don't want to hardcode everything
                return interfaceClass.cast(new WCTGuiObject((ItemWT) it.getItem(), it, player, locator.getItemIndex()));

            if (interfaceClass.isAssignableFrom(WPTGuiObject.class))
                return interfaceClass.cast(new WPTGuiObject((ItemWT) it.getItem(), it, player, locator.getItemIndex()));

            if (interfaceClass.isAssignableFrom(WITGuiObject.class))
                return interfaceClass.cast(new WITGuiObject((ItemWT) it.getItem(), it, player, locator.getItemIndex()));
        }
        return null;
    }

    private boolean checkPermission(PlayerEntity player, Object accessInterface) {
        return requiredPermission == null || Platform.checkPermissions(player, accessInterface, requiredPermission, true);
    }

    /**
     * creates a @link ContainerLocator} for any Inventory Slot since it's constructor is private and there is no static method which directly allows this
     * @param slot the slot the container is in
     * @return The new {@link ContainerLocator}
     */
    public static ContainerLocator getContainerLocatorForSlot(int slot) {
        try {
            Object containerLocatorTypePLAYER_INVENTORY = null;
            Class<?> containerLocatorTypeClass = Class.forName("appeng.container.ContainerLocator$Type");
            for (Object obj : containerLocatorTypeClass.getEnumConstants()) {
                if(obj.toString().equals("PLAYER_INVENTORY")) {
                    containerLocatorTypePLAYER_INVENTORY = obj;
                    break;
                }
            }

            Constructor<ContainerLocator> constructor = ContainerLocator.class.getDeclaredConstructor(containerLocatorTypeClass, int.class, ResourceLocation.class, BlockPos.class, AEPartLocation.class);
            constructor.setAccessible(true);
            ContainerLocator containerLocator = constructor.newInstance(containerLocatorTypePLAYER_INVENTORY, slot, null, null, null);
            constructor.setAccessible(false);
            return containerLocator;
        } catch(Exception ignored) {}
        return null;
    }

    @FunctionalInterface
    public interface ContainerFactory<C, I> {
        C create(int windowId, PlayerInventory playerInv, I accessObj);
    }

}
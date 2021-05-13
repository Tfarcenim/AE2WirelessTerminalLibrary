package tfar.ae2wtlib;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionHost;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.implementations.WirelessCraftConfirmContainer;
import appeng.container.implementations.WirelessCraftingStatusContainer;
import appeng.core.AELog;
import appeng.core.Api;
import tfar.ae2wtlib.terminal.ItemInfinityBooster;
import tfar.ae2wtlib.rei.REIRecipePacket;
import tfar.ae2wtlib.util.ContainerHelper;
import tfar.ae2wtlib.util.WirelessCraftAmountContainer;
import tfar.ae2wtlib.wct.ItemWCT;
import tfar.ae2wtlib.wct.WCTContainer;
import tfar.ae2wtlib.wct.magnet_card.ItemMagnetCard;
import tfar.ae2wtlib.wit.ItemWIT;
import tfar.ae2wtlib.wit.WITContainer;
import tfar.ae2wtlib.wpt.ItemWPT;
import tfar.ae2wtlib.wpt.WPTContainer;
import tfar.ae2wtlib.wut.ItemWUT;
import tfar.ae2wtlib.wut.WUTHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.concurrent.Future;

@Mod(value = AE2WirelessCraftingTerminals.MODID)
public class AE2WirelessCraftingTerminals {

    public static final ItemGroup ITEM_GROUP = new ItemGroup(MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(AE2WirelessCraftingTerminals.CRAFTING_TERMINAL);
        }
    };

    public static final ItemWCT CRAFTING_TERMINAL = new ItemWCT();
    public static final ItemWPT PATTERN_TERMINAL = new ItemWPT();
    public static final ItemWIT INTERFACE_TERMINAL = new ItemWIT();

    public static final ItemWUT UNIVERSAL_TERMINAL = new ItemWUT();

    public static final ItemInfinityBooster INFINITY_BOOSTER_CARD = new ItemInfinityBooster();
    public static final ItemMagnetCard MAGNET_CARD = new ItemMagnetCard();
    public static final String MODID = "ae2wtlib";

    public AE2WirelessCraftingTerminals() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addGenericListener(Item.class,this::items);
    }


    public void items(RegistryEvent.Register<Item> e) {
        register("infinity_booster_card", INFINITY_BOOSTER_CARD, e.getRegistry());
        register("magnet_card", MAGNET_CARD, e.getRegistry());
        register("wireless_crafting_terminal", CRAFTING_TERMINAL, e.getRegistry());
        register("wireless_pattern_terminal", PATTERN_TERMINAL, e.getRegistry());
        register("wireless_interface_terminal", INTERFACE_TERMINAL, e.getRegistry());
        register("wireless_universal_terminal", UNIVERSAL_TERMINAL, e.getRegistry());

    }

    public void menus(RegistryEvent.Register<ContainerType<?>> e) {
        WCTContainer.TYPE = (ContainerType<WCTContainer>) register("wireless_crafting_terminal", IForgeContainerType.create(WCTContainer::fromNetwork),e.getRegistry());
        WPTContainer.TYPE = register( "wireless_pattern_terminal", IForgeContainerType.create(WPTContainer::fromNetwork),e.getRegistry());
        WITContainer.TYPE = (ContainerType<WITContainer>) register( "wireless_interface_terminal",IForgeContainerType.create(WITContainer::fromNetwork),e.getRegistry());

        WirelessCraftingStatusContainer.TYPE = (ContainerType<WirelessCraftingStatusContainer>) register( "wireless_crafting_status",IForgeContainerType.create(WirelessCraftingStatusContainer::fromNetwork),e.getRegistry());
        WirelessCraftAmountContainer.TYPE = (ContainerType<WirelessCraftAmountContainer>) register( "wireless_craft_amount", IForgeContainerType.create(WirelessCraftAmountContainer::fromNetwork),e.getRegistry());
        WirelessCraftConfirmContainer.TYPE = (ContainerType<WirelessCraftConfirmContainer>) register( "wireless_craft_confirm",IForgeContainerType.create(WirelessCraftConfirmContainer::fromNetwork),e.getRegistry());



        WUTHandler.addTerminal("crafting", CRAFTING_TERMINAL::open);
        WUTHandler.addTerminal("pattern", PATTERN_TERMINAL::open);
        WUTHandler.addTerminal("interface", INTERFACE_TERMINAL::open);

        Api.instance().registries().charger().addChargeRate(CRAFTING_TERMINAL, Config.getChargeRate());
        Api.instance().registries().charger().addChargeRate(PATTERN_TERMINAL, Config.getChargeRate());
        Api.instance().registries().charger().addChargeRate(INTERFACE_TERMINAL, Config.getChargeRate());
        Api.instance().registries().charger().addChargeRate(UNIVERSAL_TERMINAL, Config.getChargeRate() * Config.WUTChargeRateMultiplier());
    }

       /* ServerPlayNetworking.registerGlobalReceiver(new Identifier("ae2wtlib", "patternslotpacket"), (server, player, handler, buf, sender) -> {
            buf.retain();
            server.execute(() -> {
                if(player.openContainer instanceof WPTContainer) {
                    final WPTContainer patternTerminal = (WPTContainer) player.openContainer;
                    patternTerminal.craftOrGetItem(buf);
                }
                buf.release();
            });
        });*/
        ServerPlayNetworking.registerGlobalReceiver(new Identifier("ae2wtlib", "switch_gui"), (server, player, handler, buf, sender) -> {
            buf.retain();
            server.execute(() -> {
                Identifier id = buf.readIdentifier();
                final Container c = player.openContainer;
                if(!(c instanceof AEBaseContainer)) {
                    buf.release();
                    return;
                }
                AEBaseContainer container = (AEBaseContainer) c;
                final ContainerLocator locator = container.getLocator();
                if(locator == null) {
                    buf.release();
                    return;
                }
                switch(id.getPath()) {
                    case "wireless_crafting_terminal":
                        WCTContainer.open(player, locator);
                        break;
                    case "wireless_pattern_terminal":
                        WPTContainer.open(player, locator);
                        break;
                    case "wireless_interface_terminal":
                        WITContainer.open(player, locator);
                        break;
                    case "wireless_crafting_status":
                        WirelessCraftingStatusContainer.open(player, locator);
                }
                buf.release();
            });
        });
       /* ServerPlayNetworking.registerGlobalReceiver(new Identifier("ae2wtlib", "rei_recipe"), (server, player, handler, buf, sender) -> {
            buf.retain();
            server.execute(() -> {
                new REIRecipePacket(buf, player);
                buf.release();
            });
        });*/
        ServerPlayNetworking.registerGlobalReceiver(new Identifier("ae2wtlib", "craft_request"), (server, player, handler, buf, sender) -> {
            buf.retain();
            server.execute(() -> {
                int amount = buf.readInt();
                boolean heldShift = buf.readBoolean();
                if(player.openContainer instanceof WirelessCraftAmountContainer) {
                    final WirelessCraftAmountContainer cca = (WirelessCraftAmountContainer) player.openContainer;
                    final Object target = cca.getTarget();
                    if(target instanceof IActionHost) {
                        final IActionHost ah = (IActionHost) target;
                        final IGridNode gn = ah.getActionableNode();
                        if(gn == null) return;

                        final IGrid g = gn.getGrid();
                        if(cca.getItemToCraft() == null) return;

                        cca.getItemToCraft().setStackSize(amount);

                        Future<ICraftingJob> futureJob = null;
                        try {
                            final ICraftingGrid cg = g.getCache(ICraftingGrid.class);
                            futureJob = cg.beginCraftingJob(cca.getWorld(), cca.getGrid(), cca.getActionSrc(), cca.getItemToCraft(), null);

                            final ContainerLocator locator = cca.getLocator();
                            if(locator != null) {
                                WirelessCraftConfirmContainer.open(player, locator);

                                if(player.openContainer instanceof WirelessCraftConfirmContainer) {
                                    final WirelessCraftConfirmContainer ccc = (WirelessCraftConfirmContainer) player.openContainer;
                                    ccc.setAutoStart(heldShift);
                                    ccc.setJob(futureJob);
                                    cca.detectAndSendChanges();
                                }
                            }
                        } catch(final Throwable e) {
                            if(futureJob != null) futureJob.cancel(true);
                            AELog.info(e);
                        }
                    }
                    buf.release();
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(new Identifier("ae2wtlib", "cycle_terminal"), (server, player, handler, buf, sender) -> server.execute(() -> {
            final Container screenHandler = player.openContainer;

            if(!(screenHandler instanceof AEBaseContainer)) return;

            final AEBaseContainer container = (AEBaseContainer) screenHandler;
            final ContainerLocator locator = container.getLocator();
            ItemStack item = player.inventory.getStack(locator.getItemIndex());

            if(!(item.getItem() instanceof ItemWUT)) return;
            WUTHandler.cycle(item);

            WUTHandler.open(player, locator);
        }));
        ServerPlayNetworking.registerGlobalReceiver(new Identifier("ae2wtlib", "hotkey"), (server, player, handler, buf, sender) -> {
            buf.retain();
            server.execute(() -> {
                String terminalName = buf.readString(32767);
                if(terminalName.equalsIgnoreCase("crafting")) {
                    PlayerInventory inv = player.inventory;
                    int slot = -1;
                    for(int i = 0; i < inv.size(); i++) {
                        ItemStack terminal = inv.getStack(i);
                        if(terminal.getItem() instanceof ItemWCT || (terminal.getItem() instanceof ItemWUT && WUTHandler.hasTerminal(terminal, "crafting"))) {
                            slot = i;
                            break;
                        }
                    }
                    if(slot == -1) return;
                    ContainerLocator locator = ContainerHelper.getContainerLocatorForSlot(slot);
                    CRAFTING_TERMINAL.open(player, locator);
                } else if(terminalName.equalsIgnoreCase("pattern")) {
                    PlayerInventory inv = player.inventory;
                    int slot = -1;
                    for(int i = 0; i < inv.size(); i++) {
                        ItemStack terminal = inv.getStack(i);
                        if(terminal.getItem() instanceof ItemWPT || (terminal.getItem() instanceof ItemWUT && WUTHandler.hasTerminal(terminal, "pattern"))) {
                            slot = i;
                            break;
                        }
                    }
                    if(slot == -1) return;
                    ContainerLocator locator = ContainerHelper.getContainerLocatorForSlot(slot);
                    PATTERN_TERMINAL.open(player, locator);
                } else if(terminalName.equalsIgnoreCase("interface")) {
                    PlayerInventory inv = player.inventory;
                    int slot = -1;
                    for(int i = 0; i < inv.size(); i++) {
                        ItemStack terminal = inv.getStack(i);
                        if(terminal.getItem() instanceof ItemWIT || (terminal.getItem() instanceof ItemWUT && WUTHandler.hasTerminal(terminal, "interface"))) {
                            slot = i;
                            break;
                        }
                    }
                    if(slot == -1) return;
                    ContainerLocator locator = ContainerHelper.getContainerLocatorForSlot(slot);
                    INTERFACE_TERMINAL.open(player, locator);
                }
                buf.release();
            });
        });

        ServerTickEvents.START_SERVER_TICK.register(minecraftServer -> new MagnetHandler().doMagnet(minecraftServer));

    private static <T extends IForgeRegistryEntry<T>> T register(String name,T obj,IForgeRegistry<T> registry) {
        registry.register(obj.setRegistryName(new ResourceLocation(MODID,name)));
        return obj;
    }
}
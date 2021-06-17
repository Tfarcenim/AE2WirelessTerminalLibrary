package tfar.ae2wt;

import appeng.api.features.IRegistryContainer;
import appeng.api.features.IWirelessTermRegistry;
import appeng.core.Api;
import tfar.ae2wt.init.Menus;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import tfar.ae2wt.client.ae2wtlibclient;
import tfar.ae2wt.init.ModItems;
import tfar.ae2wt.net.PacketHandler;

@Mod(value = AE2WirelessTerminals.MODID)
public class AE2WirelessTerminals {
    public static final String MODID = "ae2wtlib";

    public static final ItemGroup ITEM_GROUP = new ItemGroup(MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModItems.CRAFTING_TERMINAL);
        }
    };

    public AE2WirelessTerminals() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addGenericListener(Item.class,this::items);
        bus.addGenericListener(ContainerType.class, Menus::menus);
        MinecraftForge.EVENT_BUS.addListener(Events::serverTick);
        bus.addListener(this::common);
        if (FMLEnvironment.dist.isClient()) {
            MinecraftForge.EVENT_BUS.addListener(ae2wtlibclient::clientTick);
            bus.addListener(ae2wtlibclient::setup);
        }
    }

    private void common(FMLCommonSetupEvent e) {
        PacketHandler.registerPackets();
        IRegistryContainer iRegistryContainer = Api.instance().registries();

        IWirelessTermRegistry iWirelessTermRegistry = iRegistryContainer.wireless();

        iWirelessTermRegistry.registerWirelessHandler(ModItems.CRAFTING_TERMINAL);
        iWirelessTermRegistry.registerWirelessHandler(ModItems.PATTERN_TERMINAL);
        iWirelessTermRegistry.registerWirelessHandler(ModItems.INTERFACE_TERMINAL);
        iWirelessTermRegistry.registerWirelessHandler(ModItems.UNIVERSAL_TERMINAL);
        iWirelessTermRegistry.registerWirelessHandler(ModItems.WIRELESS_FLUID_TERMINAL);

    }


    public void items(RegistryEvent.Register<Item> e) {
        register("infinity_booster_card", ModItems.INFINITY_BOOSTER_CARD, e.getRegistry());
        register("magnet_card", ModItems.MAGNET_CARD, e.getRegistry());
        register("wireless_crafting_terminal", ModItems.CRAFTING_TERMINAL, e.getRegistry());
        register("wireless_pattern_terminal", ModItems.PATTERN_TERMINAL, e.getRegistry());
        register("wireless_interface_terminal", ModItems.INTERFACE_TERMINAL, e.getRegistry());
        register("wireless_universal_terminal", ModItems.UNIVERSAL_TERMINAL, e.getRegistry());
        register("wireless_fluid_terminal",ModItems.WIRELESS_FLUID_TERMINAL,e.getRegistry());
    }

    public static <T extends IForgeRegistryEntry<T>> T register(String name,T obj,IForgeRegistry<T> registry) {
        registry.register(obj.setRegistryName(new ResourceLocation(MODID,name)));
        return obj;
    }
}
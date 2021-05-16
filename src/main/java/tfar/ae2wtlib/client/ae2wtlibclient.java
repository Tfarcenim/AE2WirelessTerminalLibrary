package tfar.ae2wtlib.client;

import appeng.container.implementations.WirelessCraftConfirmContainer;
import appeng.container.implementations.WirelessCraftingStatusContainer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tfar.ae2wtlib.net.C2SHotkeyPacket;
import tfar.ae2wtlib.net.PacketHandler;
import tfar.ae2wtlib.util.WirelessCraftAmountContainer;
import tfar.ae2wtlib.util.WirelessCraftAmountScreen;
import tfar.ae2wtlib.util.WirelessCraftConfirmScreen;
import tfar.ae2wtlib.util.WirelessCraftingStatusScreen;
import tfar.ae2wtlib.wct.WCTContainer;
import tfar.ae2wtlib.wct.WCTScreen;
import tfar.ae2wtlib.wit.WITContainer;
import tfar.ae2wtlib.wit.WITScreen;
import tfar.ae2wtlib.wpt.WPatternTContainer;
import tfar.ae2wtlib.wpt.WPTScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class ae2wtlibclient {

    public static void setup(FMLClientSetupEvent e) {
        ScreenManager.registerFactory(WCTContainer.TYPE,
                WCTScreen::new);
        ScreenManager.registerFactory(WPatternTContainer.TYPE, WPTScreen::new);
        ScreenManager.registerFactory(WITContainer.TYPE, WITScreen::new);
        ScreenManager.registerFactory(WirelessCraftingStatusContainer.TYPE, WirelessCraftingStatusScreen::new);
        ScreenManager.registerFactory(WirelessCraftAmountContainer.TYPE, WirelessCraftAmountScreen::new);
        ScreenManager.registerFactory(WirelessCraftConfirmContainer.TYPE, WirelessCraftConfirmScreen::new);

  /*      ClientPlayNetworking.registerGlobalReceiver(new Identifier("ae2wtlib", "interface_terminal"), (client, handler, buf, responseSender) -> {
            buf.retain();
            client.execute(() -> {
                if (client.player == null) return;

                final Screen screen = MinecraftClient.getInstance().currentScreen;
                if (screen instanceof WITScreen) {
                    WITScreen s = (WITScreen) screen;
                    CompoundNBT tag = buf.readCompoundTag();
                    if (tag != null) s.postUpdate(tag);
                }
                buf.release();
            });
        });*/
        registerKeybindings();
    }

    static KeyBinding wct = new KeyBinding("key.ae2wtlib.wct", GLFW.GLFW_KEY_UNKNOWN, "key.category.ae2wtlib");
    static KeyBinding wpt = new KeyBinding("key.ae2wtlib.wpt", GLFW.GLFW_KEY_UNKNOWN, "key.category.ae2wtlib");
    static KeyBinding wit = new KeyBinding("key.ae2wtlib.wit", GLFW.GLFW_KEY_UNKNOWN, "key.category.ae2wtlib");

    public static void registerKeybindings() {
        ClientRegistry.registerKeyBinding(wct);
        ClientRegistry.registerKeyBinding(wpt);
        ClientRegistry.registerKeyBinding(wit);
    }

    public static void clientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            while (wct.isPressed()) {
                PacketHandler.INSTANCE.sendToServer(new C2SHotkeyPacket("crafting"));
            }
            while (wpt.isPressed()) {
                PacketHandler.INSTANCE.sendToServer(new C2SHotkeyPacket("pattern"));

            }
            while (wit.isPressed()) {
                PacketHandler.INSTANCE.sendToServer(new C2SHotkeyPacket("interface"));
            }
        }
    }
}
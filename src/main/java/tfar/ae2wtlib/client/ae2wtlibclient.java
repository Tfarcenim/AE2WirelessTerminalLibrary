package tfar.ae2wtlib.client;

import appeng.container.implementations.WirelessCraftConfirmContainer;
import appeng.container.implementations.WirelessCraftingStatusContainer;
import tfar.ae2wtlib.util.WirelessCraftAmountContainer;
import tfar.ae2wtlib.util.WirelessCraftAmountScreen;
import tfar.ae2wtlib.util.WirelessCraftConfirmScreen;
import tfar.ae2wtlib.util.WirelessCraftingStatusScreen;
import tfar.ae2wtlib.wct.WCTContainer;
import tfar.ae2wtlib.wct.WCTScreen;
import tfar.ae2wtlib.wit.WITContainer;
import tfar.ae2wtlib.wit.WITScreen;
import tfar.ae2wtlib.wpt.WPTContainer;
import tfar.ae2wtlib.wpt.WPTScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class ae2wtlibclient {
    @Override
    public void onInitializeClient() {
        ScreenManager.registerFactory(WCTContainer.TYPE,
                WCTScreen::new);
        ScreenManager.registerFactory(WPTContainer.TYPE, WPTScreen::new);
        ScreenManager.registerFactory(WITContainer.TYPE, WITScreen::new);
        ScreenManager.registerFactory(WirelessCraftingStatusContainer.TYPE, WirelessCraftingStatusScreen::new);
        ScreenManager.registerFactory(WirelessCraftAmountContainer.TYPE, WirelessCraftAmountScreen::new);
        ScreenManager.registerFactory(WirelessCraftConfirmContainer.TYPE, WirelessCraftConfirmScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(new Identifier("ae2wtlib", "interface_terminal"), (client, handler, buf, responseSender) -> {
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
        });
        registerKeybindings();
    }

    static KeyBinding wct = new KeyBinding("key.ae2wtlib.wct", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.ae2wtlib");
    static KeyBinding wpt = new KeyBinding("key.ae2wtlib.wpt", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.ae2wtlib");
    static KeyBinding wit = new KeyBinding("key.ae2wtlib.wit", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.ae2wtlib");

    public static void registerKeybindings() {
        ClientRegistry.registerKeyBinding(wct);
        ClientRegistry.registerKeyBinding(wpt);
        ClientRegistry.registerKeyBinding(wit);
    }

    public static void clientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            while (wct.wasPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString("crafting");
                ClientPlayNetworking.send(new Identifier("ae2wtlib", "hotkey"), buf);
            }
            while (wpt.wasPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString("pattern");
                ClientPlayNetworking.send(new Identifier("ae2wtlib", "hotkey"), buf);
            }
            while (wit.wasPressed()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString("interface");
                ClientPlayNetworking.send(new Identifier("ae2wtlib", "hotkey"), buf);
            }
        }
    }
}
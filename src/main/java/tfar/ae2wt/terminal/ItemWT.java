package tfar.ae2wt.terminal;

import appeng.api.config.Actionable;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.container.ContainerLocator;
import appeng.core.Api;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.DoubleSupplier;

public abstract class ItemWT extends AEBasePoweredItem implements IWirelessTermHandler {

    public ItemWT(DoubleSupplier powerCapacity, Properties props) {
        super(powerCapacity, props);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final PlayerEntity player, final Hand hand) {
        openWirelessTerminalGui(player.getHeldItem(hand), player, hand);
        return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
    }

    private void openWirelessTerminalGui(ItemStack item, PlayerEntity player, Hand hand) {
        if(Platform.isClient()) {
            return;
        }

        final String unparsedKey = getEncryptionKey(item);
        if(unparsedKey.isEmpty()) {
            player.sendMessage(PlayerMessages.DeviceNotLinked.get(), Util.DUMMY_UUID);
            return;
        }

        final long parsedKey = Long.parseLong(unparsedKey);
        final ILocatable securityStation = Api.instance().registries().locatable().getLocatableBy(parsedKey);
        if(securityStation == null) {
            player.sendMessage(PlayerMessages.StationCanNotBeLocated.get(), Util.DUMMY_UUID);
            return;
        }

        if(hasPower(player, 0.5, item)) {
            open(player, ContainerLocator.forHand(player, hand));
        } else {
            player.sendMessage(PlayerMessages.DeviceNotPowered.get(), Util.DUMMY_UUID);
        }
    }

    public abstract void open(final PlayerEntity player, final ContainerLocator locator);

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines, final ITooltipFlag advancedTooltips) {
        super.addInformation(stack, world, lines, advancedTooltips);

        if(stack.hasTag()) {
            final CompoundNBT tag = stack.getTag();
            if(tag != null) {
                final String encKey = tag.getString("encryptionKey");

                if(encKey.isEmpty()) {
                    lines.add(GuiText.Unlinked.text());
                } else {
                    lines.add(GuiText.Linked.text());
                }
            }
        } else {
            lines.add(new TranslationTextComponent("AppEng.GuiITooltip.Unlinked"));
        }
    }

    @Override
    public boolean canHandle(ItemStack is) {
        return is.getItem() instanceof ItemWT;
    }

    @Override
    public boolean usePower(PlayerEntity player, double amount, ItemStack is) {
        if (player.abilities.isCreativeMode) {
            return false;
        }
        return extractAEPower(is, amount, Actionable.MODULATE) >= amount - 0.5;
    }

    @Override
    public boolean hasPower(PlayerEntity player, double amount, ItemStack is) {
        return getAECurrentPower(is) >= amount;
    }

    @Override
    public IConfigManager getConfigManager(ItemStack is) {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
            final CompoundNBT data = is.getOrCreateTag();
            manager.writeToNBT(data);
        });

        out.registerSetting(appeng.api.config.Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(appeng.api.config.Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(appeng.api.config.Settings.SORT_DIRECTION, SortDir.ASCENDING);

        out.readFromNBT(is.getOrCreateTag().copy());
        return out;
    }

    @Override
    public String getEncryptionKey(ItemStack item) {
        final CompoundNBT tag = item.getOrCreateTag();
        return tag.getString("encryptionKey");
    }

    @Override
    public void setEncryptionKey(ItemStack item, String encKey, String name) {
        final CompoundNBT tag = item.getOrCreateTag();
        tag.putString("encryptionKey", encKey);
        tag.putString("name", name);
    }

    /**
     * get a previously stored {@link ItemStack} from a WirelessTerminal
     *
     * @param hostItem the Terminal to load from
     * @param slot     the location where the item is stored
     * @return the stored Item or {@link ItemStack}.EMPTY if it wasn't found
     */
    public static ItemStack getSavedSlot(ItemStack hostItem, String slot) {
        if(!(hostItem.getItem() instanceof ItemWT)) return ItemStack.EMPTY;
        return ItemStack.read(hostItem.getOrCreateTag().getCompound(slot));
    }

    /**
     * store an {@link ItemStack} in a WirelessTerminal
     * this will overwrite any previously existing tags in slot
     *
     * @param hostItem  the Terminal to store in
     * @param savedItem the item to store
     * @param slot      the location where the stored item will be
     */
    public static void setSavedSlot(ItemStack hostItem, ItemStack savedItem, String slot) {
        if(!(hostItem.getItem() instanceof ItemWT)) return;
        CompoundNBT wctTag = hostItem.getOrCreateTag();
        if(savedItem.isEmpty()) {
            wctTag.remove(slot);
        } else {
            wctTag.put(slot, savedItem.write(new CompoundNBT()));
        }
    }

    /**
     * get a previously stored boolean from a WirelessTerminal
     *
     * @param hostItem the Terminal to load from
     * @return the boolean or false if it wasn't found
     */
    public static boolean getBoolean(ItemStack hostItem, String key) {
        if(!(hostItem.getItem() instanceof ItemWT)) return false;
        return hostItem.getOrCreateTag().getBoolean(key);
    }

    /**
     * store a boolean in a WirelessTerminal
     * this will overwrite any previously existing tags in slot
     *
     * @param hostItem the Terminal to store in
     * @param b        the boolean to store
     * @param key      the location where the stored item will be
     */
    public static void setBoolean(ItemStack hostItem, boolean b, String key) {
        if(!(hostItem.getItem() instanceof ItemWT)) return;
        CompoundNBT wctTag = hostItem.getOrCreateTag();
        wctTag.putBoolean(key, b);
    }

    public boolean hasBoosterCard(ItemStack hostItem) {
        return getBoosterCard(hostItem).getItem() instanceof ItemInfinityBooster;
    }

    public void setBoosterCard(ItemStack hostItem, ItemStack boosterCard) {
        if(hostItem.getItem() instanceof IInfinityBoosterCardHolder) {
            setSavedSlot(hostItem, boosterCard, "boosterCard");
        }
    }

    public ItemStack getBoosterCard(ItemStack hostItem) {
        if(hostItem.getItem() instanceof IInfinityBoosterCardHolder) {
            return getSavedSlot(hostItem, "boosterCard");
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.areItemStacksEqual(oldStack, newStack);
    }
}
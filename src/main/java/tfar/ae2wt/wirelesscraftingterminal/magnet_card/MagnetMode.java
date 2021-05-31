package tfar.ae2wt.wirelesscraftingterminal.magnet_card;

public enum MagnetMode {
    NO_CARD,OFF,PICKUP_INVENTORY,PICKUP_ME;
    public static final MagnetMode DEFAULT = OFF;
    public static final MagnetMode[] modes = values();

    public boolean isActive() {
        return this == PICKUP_INVENTORY || this == PICKUP_ME;
    }
}
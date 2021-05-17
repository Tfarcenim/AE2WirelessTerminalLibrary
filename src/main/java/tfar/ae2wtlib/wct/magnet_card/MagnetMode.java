package tfar.ae2wtlib.wct.magnet_card;

public enum MagnetMode {
    INVALID,NO_CARD, OFF, PICKUP_INVENTORY,PICKUP_ME;
    public static final MagnetMode DEFAULT = OFF;
    public static final MagnetMode[] modes = values();
}
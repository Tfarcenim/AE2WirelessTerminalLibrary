package tfar.ae2wt;

import appeng.core.AEConfig;

public class WTConfig {

    public static double getPowerMultiplier(double range, boolean isOutOfRange) {
        if(!isOutOfRange) return AEConfig.instance().wireless_getDrainRate(range);
        return AEConfig.instance().wireless_getDrainRate(528 * getOutOfRangePowerMultiplier());//the drain rate while in range is dependent on the distance, and calculated by ae2. since 528 blocks is the max distance a terminal can normally work, out of range has to calculate the power with max range, or otherwise it uses less energy when out of range than at the edge of range
    }

    public static double getChargeRate() {
        return 32000;//8000 is ae2's default charge rate, so I chose to use it too
    }

    public static double WUTChargeRateMultiplier() {
        return 1;
    }

    private static int getOutOfRangePowerMultiplier() {
        return 2;//has to be > 1 for terminals to use more energy outside of range than in range
    }
}
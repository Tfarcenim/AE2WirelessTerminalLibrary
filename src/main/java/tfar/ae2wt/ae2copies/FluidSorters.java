package tfar.ae2wt.ae2copies;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.util.Platform;

import java.util.Comparator;
import java.util.function.Function;

public class FluidSorters {
    public static final Comparator<IAEFluidStack> NAME_ASC = Comparator.comparing((fs) -> Platform.getFluidDisplayName(fs).getString(), String::compareToIgnoreCase);
    public static final Comparator<IAEFluidStack> NAME_DESC = NAME_ASC.reversed();
    public static final Comparator<IAEFluidStack> MOD_ASC = Comparator.comparing((Function<IAEFluidStack, String>) Platform::getModId, String::compareToIgnoreCase).thenComparing(NAME_ASC);
    public static final Comparator<IAEFluidStack> MOD_DESC = MOD_ASC.reversed();
    public static final Comparator<IAEFluidStack> SIZE_ASC = Comparator.comparingLong(IAEStack::getStackSize);
    public static final Comparator<IAEFluidStack> SIZE_DESC = SIZE_ASC.reversed();

    private FluidSorters() {
    }

    public static Comparator<IAEFluidStack> getComparator(SortOrder order, SortDir dir) {
        switch (order) {
            case NAME:
            default:
                return dir == SortDir.ASCENDING ? NAME_ASC : NAME_DESC;
            case AMOUNT:
                return dir == SortDir.ASCENDING ? SIZE_ASC : SIZE_DESC;
            case MOD:
                return dir == SortDir.ASCENDING ? MOD_ASC : MOD_DESC;
        }
    }
}

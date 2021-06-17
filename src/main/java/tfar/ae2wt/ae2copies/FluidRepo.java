package tfar.ae2wt.ae2copies;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.util.Platform;

import java.util.Comparator;
import java.util.regex.Pattern;

public class FluidRepo extends Repo<IAEFluidStack> {
    public FluidRepo(IScrollSource src, ISortSource sortSrc) {
        super(src, sortSrc);
    }

    protected boolean matchesSearch(SearchMode searchMode, Pattern searchPattern, IAEFluidStack stack) {
        String displayName;
        if (searchMode == SearchMode.MOD) {
            displayName = Platform.getModId(stack);
            return searchPattern.matcher(displayName).find();
        } else {
            displayName = Platform.getFluidDisplayName(stack).getString();
            return searchPattern.matcher(displayName).find();
        }
    }

    protected Comparator<? super IAEFluidStack> getComparator(SortOrder sortBy, SortDir sortDir) {
        return FluidSorters.getComparator(sortBy, sortDir);
    }
}

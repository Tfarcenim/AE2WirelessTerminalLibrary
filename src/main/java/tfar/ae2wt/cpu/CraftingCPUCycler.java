package tfar.ae2wt.cpu;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.text.StringTextComponent;

public class CraftingCPUCycler {
    private final Predicate<ICraftingCPU> cpuFilter;
    private final CraftingCPUCycler.ChangeListener changeListener;
    private final List<CraftingCPURecord> cpus = new ArrayList<>();
    private int selectedCpu = -1;
    private boolean initialDataSent = false;
    private boolean allowNoSelection;

    public CraftingCPUCycler(Predicate<ICraftingCPU> cpuFilter,CraftingCPUCycler.ChangeListener changeListener) {
        this.cpuFilter = cpuFilter;
        this.changeListener = changeListener;
    }

    public void detectAndSendChanges(IGrid network) {
        ICraftingGrid cc = network.getCache(ICraftingGrid.class);
        ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();
        int matches = 0;
        boolean changed = !this.initialDataSent;
        this.initialDataSent = true;
        UnmodifiableIterator<ICraftingCPU> var6 = cpuSet.iterator();

        ICraftingCPU c;
        while(var6.hasNext()) {
            c = var6.next();
            boolean found = false;

            for (CraftingCPURecord ccr : this.cpus) {
                if (ccr.getCpu() == c) {
                    found = true;
                    break;
                }
            }

            boolean matched = this.cpuFilter.test(c);
            if (matched) {
                ++matches;
            }

            if (found == !matched) {
                changed = true;
            }
        }

        if (changed || this.cpus.size() != matches) {
            this.cpus.clear();
            var6 = cpuSet.iterator();

            while(var6.hasNext()) {
                c = var6.next();
                if (this.cpuFilter.test(c)) {
                    this.cpus.add(new CraftingCPURecord(c.getAvailableStorage(), c.getCoProcessors(), c));
                }
            }

            Collections.sort(this.cpus);

            for(int i = 0; i < this.cpus.size(); ++i) {
                CraftingCPURecord cpu = this.cpus.get(i);
                if (cpu.getName() == null) {
                    cpu.setName(new StringTextComponent("#" + (i + 1)));
                }
            }

            this.notifyListener();
        }

    }

    public void cycleCpu(boolean next) {
        if (next) {
            ++this.selectedCpu;
        } else {
            --this.selectedCpu;
        }

        int lowerLimit = this.allowNoSelection ? -1 : 0;
        if (this.selectedCpu < lowerLimit) {
            this.selectedCpu = this.cpus.size() - 1;
        } else if (this.selectedCpu >= this.cpus.size()) {
            this.selectedCpu = lowerLimit;
        }

        this.notifyListener();
    }

    public boolean isAllowNoSelection() {
        return this.allowNoSelection;
    }

    public void setAllowNoSelection(boolean allowNoSelection) {
        this.allowNoSelection = allowNoSelection;
    }

    private void notifyListener() {
        if (this.selectedCpu >= this.cpus.size()) {
            this.selectedCpu = -1;
        }

        if (!this.allowNoSelection && this.selectedCpu == -1 && !this.cpus.isEmpty()) {
            this.selectedCpu = 0;
        }

        if (this.selectedCpu != -1) {
            this.changeListener.onChange(this.cpus.get(this.selectedCpu), true);
        } else {
            this.changeListener.onChange(null, !this.cpus.isEmpty());
        }

    }

    @FunctionalInterface
    public interface ChangeListener {
        void onChange(CraftingCPURecord var1, boolean var2);
    }
}

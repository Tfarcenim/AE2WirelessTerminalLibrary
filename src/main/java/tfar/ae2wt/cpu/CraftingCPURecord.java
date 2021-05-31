package tfar.ae2wt.cpu;

import appeng.api.networking.crafting.ICraftingCPU;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class CraftingCPURecord implements Comparable<CraftingCPURecord> {
    private final ICraftingCPU cpu;
    private final long size;
    private final int processors;
    private ITextComponent name;

    public CraftingCPURecord(long size, int coProcessors, ICraftingCPU server) {
        this.size = size;
        this.processors = coProcessors;
        this.cpu = server;
        this.name = server.getName();
    }

    public int compareTo(@Nonnull CraftingCPURecord o) {
        int a = Long.compare(o.getProcessors(), this.getProcessors());
        return a != 0 ? a : Long.compare(o.getSize(), this.getSize());
    }

    public ICraftingCPU getCpu() {
        return this.cpu;
    }

    public int getProcessors() {
        return this.processors;
    }

    public long getSize() {
        return this.size;
    }

    public ITextComponent getName() {
        return this.name;
    }

    public void setName(ITextComponent name) {
        this.name = name;
    }
}
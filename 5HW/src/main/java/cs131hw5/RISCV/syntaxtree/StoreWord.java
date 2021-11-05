package cs131hw5.RISCV.syntaxtree;

public class StoreWord extends Opcode {
    public RVRegister source;
    public RVRegister dest;
    public int offset;

    public StoreWord(RVRegister source, RVRegister dest, int offset) {
        this.source = source;
        this.dest = dest;
        this.offset = offset;
    }

    @Override
    public String fmt() {
        return "sw " + source + ", " + offset + "(" + dest + ")";
    }
}

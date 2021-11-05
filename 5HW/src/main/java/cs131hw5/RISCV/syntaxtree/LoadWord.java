package cs131hw5.RISCV.syntaxtree;

public class LoadWord extends Opcode {
    public RVRegister dest;
    public RVRegister source;
    public int offset;

    public LoadWord(RVRegister dest, RVRegister source, int offset) {
        this.dest = dest;
        this.source = source;
        this.offset = offset;
    }

    @Override
    public String fmt() {
        return "lw " + dest + ", " + offset + "(" + source + ")";
    }
}

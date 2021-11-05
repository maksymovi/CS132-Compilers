package cs131hw5.RISCV.syntaxtree;

public class SubI extends Opcode {
    public RVRegister dest;
    public RVRegister source;
    public int num;

    public SubI(RVRegister dest, RVRegister source, int num) {
        this.dest = dest;
        this.source = source;
        this.num = num;
    }

    @Override
    public String fmt() {
        return "subi " + dest + ", " + source + ", " + num;
    }
}

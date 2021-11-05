package cs131hw5.RISCV.syntaxtree;

public class Move extends Opcode {
    public RVRegister dest;
    public RVRegister source;

    public Move(RVRegister dest, RVRegister source) {
        this.dest = dest;
        this.source = source;
    }

    @Override
    public String fmt() {
        return "mv " + dest + ", " + source;
    }
}

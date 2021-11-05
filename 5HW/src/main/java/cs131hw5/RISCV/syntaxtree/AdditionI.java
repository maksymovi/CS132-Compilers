package cs131hw5.RISCV.syntaxtree;

/**
 * Add Immediate
 */
public class AdditionI extends Opcode {
    public RVRegister dest;
    public RVRegister source;
    public int num;

    public AdditionI(RVRegister dest, RVRegister source, int num) {
        this.dest = dest;
        this.source = source;
        this.num = num;
    }

    @Override
    public String fmt() {
        return "addi " + dest + ", " + source + ", " + num;
    }
}

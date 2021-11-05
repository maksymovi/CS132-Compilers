package cs131hw5.RISCV.syntaxtree;

public class LoadAddress extends Opcode {
    public RVRegister dest;
    public String function;

    public LoadAddress(RVRegister dest, String function) {
        this.dest = dest;
        this.function = function;
    }

    @Override
    public String fmt() {
        return "la " + dest + ", " + function;
    }
}

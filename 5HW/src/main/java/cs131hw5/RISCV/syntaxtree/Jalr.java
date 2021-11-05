package cs131hw5.RISCV.syntaxtree;

public class Jalr extends Opcode {
    RVRegister loc;

    public Jalr(RVRegister loc) {
        this.loc = loc;
    }

    @Override
    public String fmt() {
        return "jalr " + loc.toString();
    }
}

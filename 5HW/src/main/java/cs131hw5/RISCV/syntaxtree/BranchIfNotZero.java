package cs131hw5.RISCV.syntaxtree;

public class BranchIfNotZero extends Opcode {
    public RVRegister r;
    public String l;

    public BranchIfNotZero(RVRegister r, String l) {
        this.r = r;
        this.l = l;
    }

    @Override
    public String fmt() {
        return "bnez " + r + ", " + l;
    }
}

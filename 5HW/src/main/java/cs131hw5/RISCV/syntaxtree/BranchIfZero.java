package cs131hw5.RISCV.syntaxtree;

public class BranchIfZero extends Opcode {
    public RVRegister r;
    public String l;

    public BranchIfZero(RVRegister r, String l) {
        this.r = r;
        this.l = l;
    }

    @Override
    public String fmt() {
        return "beqz " + r + ", " + l;
    }
}

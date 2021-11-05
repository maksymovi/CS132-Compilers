package cs131hw5.RISCV.syntaxtree;

public class Sub extends Opcode {
    public RVRegister dest;
    public RVRegister rs1;
    public RVRegister rs2;

    public Sub(RVRegister dest, RVRegister rs1, RVRegister rs2) {
        this.dest = dest;
        this.rs1 = rs1;
        this.rs2 = rs2;
    }

    @Override
    public String fmt() {
        return "sub " + dest + ", " + rs1 + ", " + rs2;
    }
}

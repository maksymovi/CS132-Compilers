package cs131hw5.RISCV.syntaxtree;

public class Mul extends Opcode {
    public RVRegister rd;
    public RVRegister rs1;
    public RVRegister rs2;

    public Mul(RVRegister rd, RVRegister rs1, RVRegister rs2) {
        this.rd = rd;
        this.rs1 = rs1;
        this.rs2 = rs2;
    }

    @Override
    public String fmt() {
        return "mul " + rd + ", " + rs1 + ", " + rs2;
    }
}

package cs131hw5.RISCV.syntaxtree;

public class ECall extends Opcode {
    @Override
    public String fmt() {
        return "ecall";
    }
}

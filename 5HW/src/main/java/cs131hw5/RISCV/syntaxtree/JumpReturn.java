package cs131hw5.RISCV.syntaxtree;

public class JumpReturn extends Opcode {
    RVRegister ret;

    public JumpReturn(RVRegister ret) {
        this.ret = ret;
    }

    @Override
    public String fmt() {
        return "jr " + ret.toString();
    }
}

package cs131hw5.RISCV.syntaxtree;

public class LoadImmediate extends Opcode {
    public RVRegister r;
    public String num;

    public LoadImmediate(RVRegister r, String num) {
        this.r = r;
        this.num = num;
    }

    @Override
    public String fmt() {
        return "li " + r + ", " + num;
    }
}

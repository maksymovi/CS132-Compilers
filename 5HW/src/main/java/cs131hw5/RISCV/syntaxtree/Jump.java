package cs131hw5.RISCV.syntaxtree;

public class Jump extends Opcode {
    String l;

    public Jump(String l) {
        this.l = l;
    }

    @Override
    public String fmt() {
        return "j " + l;
    }
}

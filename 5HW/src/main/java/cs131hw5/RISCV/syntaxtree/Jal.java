package cs131hw5.RISCV.syntaxtree;

/**
 * JAL instruction, like jump but stores return register into RA first
 */
public class Jal extends Opcode {
    public String loc;

    public Jal(String loc) {
        this.loc = loc;
    }
    @Override
    public String fmt() {
        return "jal " + loc;
    }
}

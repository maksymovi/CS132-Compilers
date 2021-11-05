package cs131hw5.RISCV.syntaxtree;

public abstract class Opcode extends OpcodeOrLabel {

    /**
     * Format instruction into a string.
     * @return The string of the instruction as it would be found in assembly, without intentation.
     */
    public abstract String fmt();
    public String format() {
        return "  " + fmt() + super.format();
    }
}

package cs131hw5.RISCV.syntaxtree;

/**
 * The section class can contain one or more lines of RISCV code.
 */

public abstract class Section {
    /**
     * Formats RISCV section
     * @return RISCV section as formatted in an assembly file
     */
    public abstract String format();
}

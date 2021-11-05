package cs131hw5.RISCV.syntaxtree;

public class DataString extends Section {
    public String label;
    public String str;

    public DataString(String label, String str) {
        this.label = label;
        this.str = str;
    }

    @Override
    public String format() {
        return ".globl " + label + "\n" + label + ":\n  .asciiz " + str + "\n  .align 2\n\n";
    }
}

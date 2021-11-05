package cs131hw5.RISCV.syntaxtree;


/**
 * Jump and branch label in RISCV.
 */
public class Label extends OpcodeOrLabel {
    public String label;
    public Label(String label) {
        this.label = label;
    }
    @Override
    public String format() {
        return label + ":" + super.format();
    }
}

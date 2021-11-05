package cs131hw5.RISCV.syntaxtree;


/**
 * To differentiate between instructions and labels without them being the same.
 */
public abstract class OpcodeOrLabel extends Section {
    public String comment;

    @Override
    public String format() {
        if(comment == null)
            return "";
        return "\t\t\t\t# " + comment;
    }
}
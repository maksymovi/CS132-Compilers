package cs131hw5.RISCV.syntaxtree;

import java.util.*;


/**
 * Text section of a program.
 */
public class TextSection extends Section {

    private final List<OpcodeOrLabel> preamble = Arrays.asList(
            new Jal("Main"),
            new LoadImmediate(RVRegister.a0, "@exit"),
            new ECall());
    public List<Function> functionList;


    @Override
    public String format() {
        StringBuilder b = new StringBuilder();
        b.append(".text\n\n");
        for(OpcodeOrLabel p : preamble) {
            b.append(p.format() + "\n");
        }
        b.append("\n");

        for(Function f : functionList) {
            b.append(f.format() + "\n");
        }
        return b.toString();
    }
}

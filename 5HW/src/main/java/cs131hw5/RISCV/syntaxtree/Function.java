package cs131hw5.RISCV.syntaxtree;

import java.util.*;


/**
 * General layout of a function, has a name and instructions
 */
public class Function extends Section {
    public String functionComment;
    public String name;

    public List<OpcodeOrLabel> instructions;

    public Function() {
    }

    public Function(String functionComment, String name, List<OpcodeOrLabel> instructions) {
        this.functionComment = functionComment;
        this.name = name;
        this.instructions = instructions;
    }

    @Override
    public String format() {
        StringBuilder b = new StringBuilder();
        if(functionComment != null)
            b.append("# " + functionComment + "\n");
        b.append(".globl " + name + "\n" + name + ":\n");
        for(OpcodeOrLabel i : instructions) {
            b.append(i.format() + "\n");
        }
        return b.toString();
    }
}

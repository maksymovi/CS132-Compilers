package cs131hw5.RISCV.syntaxtree;


import java.util.*;

/**
 * Data section of the program.
 *
 * Currently all boilerplate code, will replace with something better if necessary.
 */
public class DataSection extends Section {
    public List<DataString> dataStringList;

    public DataSection(List<DataString> dataStringList) {
        this.dataStringList = dataStringList;
    }

    @Override
    public String format() {
        StringBuilder b = new StringBuilder();
        b.append(".data\n\n");
        for(DataString d : dataStringList) {
            b.append(d.format());
        }
        return b.toString();
    }
}

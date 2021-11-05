package cs131hw5.RISCV.syntaxtree;

import java.util.*;

/**
 * Overall program class for the RISCV package, can be printed by the toString class
 *
 */
public class RISCVProgram {
    public List<Section> sectionList;


    /**
     *
     * @return Complete RISCV assembly program in string.
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for(Section s : sectionList) {
            b.append(s.format()).append("\n\n");
        }
        return b.toString();
    }
}

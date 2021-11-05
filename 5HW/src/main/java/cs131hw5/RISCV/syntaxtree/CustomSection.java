package cs131hw5.RISCV.syntaxtree;

public class CustomSection extends Section{
    private final String section;

    public CustomSection(String section) {
        this.section = section;
    }

    @Override
    public String format() {
        return section;
    }
}

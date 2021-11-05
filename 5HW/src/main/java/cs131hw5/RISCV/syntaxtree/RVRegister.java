package cs131hw5.RISCV.syntaxtree;

import cs132.IR.token.*;
public class RVRegister {
    private final String name;
    public RVRegister(String name)  {
        this.name = name;
        if(!name.matches("^(a[0-7]|t[0-6]|s([0-9]|1[0-1]|p)|zero|ra|fp)$")) {
            //throw new RISCVException("Register name \"" + name + "\" invalid.");
            System.err.println("Error: Invalid register: " + name);
            System.exit(-1);
        }
    }


    @Override
    public String toString() {
        return name;
    }
    //some baseline registers for convenience
    public static final RVRegister sp = new RVRegister("sp");
    public static final RVRegister fp = new RVRegister("fp");
    public static final RVRegister ra = new RVRegister("ra");
    public static final RVRegister t6 = new RVRegister("t6");
    public static final RVRegister a0 = new RVRegister("a0");
}

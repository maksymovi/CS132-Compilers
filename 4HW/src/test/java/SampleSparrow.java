import cs132.IR.token.FunctionName;
import cs132.IR.token.Identifier;

import java.util.ArrayList;

public class SampleSparrow
{
    // Here we construct a small sparrow program that has
    // a function that adds two integers together
    // The purpose is to show you how to construct a sparrow tree
    // and print it out

    // The program tree we are constructing


    public static void main(String[] args) {
        SampleSparrow n = new SampleSparrow();
        return;
    }
    cs132.IR.sparrow.Program sparrowProgram;
    SampleSparrow() {
        buildProgram();
    }

    void buildProgram() {
        ArrayList<cs132.IR.sparrow.FunctionDecl> functionDecls = new ArrayList<>();
        functionDecls.add(buildAddFunction());
        this.sparrowProgram = new cs132.IR.sparrow.Program(functionDecls);
    }

    cs132.IR.sparrow.FunctionDecl buildAddFunction() {
        // To construct a function, we need:
        // 1. Function Name
        cs132.IR.token.FunctionName functionName = new FunctionName("Add");

        // 2. a list of parameters
        ArrayList<Identifier> parameters = new ArrayList<>();
        parameters.add(new Identifier("a1"));
        parameters.add(new Identifier("a2"));

        // 3. a block filled with a list of instructions
        ArrayList<cs132.IR.sparrow.Instruction> instructions = buildInstructions();
        cs132.IR.sparrow.Block block =
                new cs132.IR.sparrow.Block(instructions, new Identifier("t3"));

        // setting back pointer
        for(cs132.IR.sparrow.Instruction i : instructions) {
            i.parent = block;
        }

        // Finally, we call the constructor of functionDecl
        cs132.IR.sparrow.FunctionDecl func =
                new cs132.IR.sparrow.FunctionDecl(functionName,parameters, block);

        // setting back pointer
        block.parent = func;

        return func;
    }

    ArrayList<cs132.IR.sparrow.Instruction> buildInstructions() {
        ArrayList<cs132.IR.sparrow.Instruction> instructions = new ArrayList<>();

        Identifier t1 = new Identifier("t1");
        Identifier t2 = new Identifier("t2");
        Identifier t3 = new Identifier("t3");
        Identifier a1 = new Identifier("a1");
        Identifier a2 = new Identifier("a2");
        Identifier test2 = new Identifier("a2");
        if(a2.equals(test2))
            System.out.println("True");
        else {
            System.out.println("False");
        }

        // t1 = a1
        cs132.IR.sparrow.Move_Id_Id i1 =
                new cs132.IR.sparrow.Move_Id_Id(t1, a1);

        // t2 = a2
        cs132.IR.sparrow.Move_Id_Id i2 =
                new cs132.IR.sparrow.Move_Id_Id(t2, a2);

        // t3 = t1 + t2
        cs132.IR.sparrow.Add i3 =
                new cs132.IR.sparrow.Add(t3, t1, t2);

        instructions.add(i1);
        instructions.add(i2);
        instructions.add(i3);

        return instructions;
    }

    void print(){
        // To print the program, simple call toString() and print it out
        System.out.println(this.sparrowProgram.toString());
    }
}
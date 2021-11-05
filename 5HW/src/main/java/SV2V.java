//package cs131hw5;


import cs131hw5.RISCV.syntaxtree.*;
import cs131hw5.RISCV.syntaxtree.Label;
import cs132.IR.*;
import cs132.IR.sparrowv.*;
import cs132.IR.sparrowv.Add;
import cs132.IR.sparrowv.visitor.*;
import cs132.IR.token.*;

import java.util.*;

public class SV2V{
    public static void main(String [] args) {
        /* This is a placeholder file, 
            please create a new class with required name for the homework
            and remove this file */
        cs132.IR.registers.Registers.SetRiscVregs();
        try {
            cs132.IR.syntaxtree.Node root = new SparrowParser(System.in).Program();
            cs132.IR.visitor.SparrowVConstructor sc = new cs132.IR.visitor.SparrowVConstructor();
            root.accept(sc);

            Program p = sc.getProgram();
            SV2V sv2V = new SV2V();
            sv2V.printProgram(p);


        }
        catch (ParseException e) {
            System.out.println(e.toString());
        }

    }
    public void printProgram(Program p) {
        RISCVBuilder riscvBuilder = new RISCVBuilder();
        RISCVProgram riscvProgram = riscvBuilder.buildProgram(p);
        System.out.println(riscvProgram.toString());

    }

    public static void error(String e) {
            System.err.println(e);
            System.exit(-1);
        }

    private class RISCVFunctionBuilder implements Visitor {
        public Function getFunction() {
            return function;
        }

        private Function function; //function to be returned
        private HashMap<String, Integer> variableStack; //stack of variables
        private HashMap<String, Integer> argumentStack; //stack of arguments
        private final ArrayList<OpcodeOrLabel> l = new ArrayList<>(); //opcode list to be used to generate function
        private OpcodeOrLabel last; //last opcode used to set comments

        public LinkedHashMap<String, String> getFunctionStrings() {
            return functionStrings;
        }

        Random rand; //random int for label purposes

        private final LinkedHashMap<String, String> functionStrings = new LinkedHashMap<>(); //maps from string labels to strings for data section

        //im too lazy to type out stuff
        private final RVRegister sp = RVRegister.sp;
        private final RVRegister fp = RVRegister.fp;
        private final RVRegister ra = RVRegister.ra;
        private final RVRegister t6 = RVRegister.t6;
        private final RVRegister a0 = RVRegister.a0;

        /**
         * Gets FP offset of a Sparrow-V variable
         * @param var variable to find offset of
         * @return FP offset, returns 0 if error
         */
        private int getOffset(String var) {
            Integer temp;
            temp = variableStack.get(var);
            if(temp != null) {
                //-8 because ra and fp
                return -12 - temp;
            }
            temp = argumentStack.get(var);
            if(temp == null) {
                return 0;
            }
            //recalculation for FP, list needs to be reversed because FP is after arguments.
            //System.err.println("Using argument stack for: " + var + " with value of " + temp.toString());
            return argumentStack.size()*4 - temp - 4;
        }

        /**
         * Converts sparrow-v register to riscv register
         * @param r Sparrow-V register to convert
         * @return RISCV register to return
         */
        private RVRegister regConv(Register r) {
            return new RVRegister(r.toString());
        }



        /**
         * Pushes instruction onto the instruction list.
         * @param o OpcodeOrLabel or label to be pushed.
         */
        private void push(OpcodeOrLabel o) {
            l.add(o);
            last = o;
        }


        @Override
        public void visit(Program program) {
            //shouldn't happen
        }

        @Override
        public void visit(FunctionDecl functionDecl) {
            MemoryCounter m = new MemoryCounter();
            functionDecl.accept(m);
            variableStack = m.getVariableStack();
            argumentStack = m.getArgumentStack();
            rand = new Random();
            //System.err.println(functionDecl.functionName.name);
            //System.err.println(argumentStack.toString());
            //Beginning function prologue
            //Going to do this slightly differently from the doc to make sure its all good
            //Store frame pointer
            push(new StoreWord(fp, sp, -8));
            last.comment = "Store frame pointer";
            //Store return address
            push(new StoreWord(ra, sp, -4));
            last.comment = "Store return address";
            //We set a new frame pointer
            push(new Move(fp, sp));
            last.comment = "Set a new frame pointer";
            //Establish new stack frame with size of variableStack, -8 because of the old return and frame pointers
            push(new LoadImmediate(t6, Integer.toString((variableStack.size() *4) + 8)));
            last.comment = "New stack frame";
            push(new Sub(sp, sp, t6));
            last.comment = "Prologue complete, rest is instructions";
            //prologue complete, the rest is handled by the instructions
            functionDecl.block.accept(this);
            //now we epilogue
            //restore stack frame
            push(new Move(sp, fp));
            last.comment = "Restore stack frame";
            //restore old frame pointer
            push(new LoadWord(fp, sp, -8));
            last.comment = "Restore old frame pointer";
            //reload return address
            push(new LoadWord(ra, sp, -4));
            last.comment = "Reload return address";
            //return
            push(new JumpReturn(ra));
            last.comment = "Return";
            //At this point we finalize our function
            function = new Function();
            function.name = functionDecl.functionName.name;
            function.instructions = l;

        }

        @Override
        public void visit(Block block) {
            for(Instruction i: block.instructions) {
                i.accept(this);
                last.comment = i.toString(); //will overwrite comment of any last instruction
            }
            //return load
            push(new LoadWord(a0,fp,getOffset(block.return_id.toString())));
        }

        @Override
        public void visit(LabelInstr labelInstr) {
            push(new Label(labelInstr.label.toString() + "@" + labelInstr.parent.parent.functionName.name));
        }

        @Override
        public void visit(Move_Reg_Integer move_reg_integer) {
            push(new LoadImmediate(regConv(move_reg_integer.lhs), Integer.toString(move_reg_integer.rhs)));
        }

        @Override
        public void visit(Move_Reg_FuncName move_reg_funcName) {
            push(new LoadAddress(regConv(move_reg_funcName.lhs), move_reg_funcName.rhs.name));
        }

        @Override
        public void visit(Add add) {
            push(new Addition(regConv(add.lhs), regConv(add.arg1), regConv(add.arg2)));
        }

        @Override
        public void visit(Subtract subtract) {
            push(new Sub(regConv(subtract.lhs), regConv(subtract.arg1), regConv(subtract.arg2)));
        }

        @Override
        public void visit(Multiply multiply) {
            push(new Mul(regConv(multiply.lhs), regConv(multiply.arg1), regConv(multiply.arg2)));
        }

        @Override
        public void visit(LessThan lessThan) {
            push(new SetLessThan(regConv(lessThan.lhs), regConv(lessThan.arg1), regConv(lessThan.arg2)));
        }

        @Override
        public void visit(Load load) {
            push(new LoadWord(regConv(load.lhs), regConv(load.base), load.offset));
        }

        @Override
        public void visit(Store store) {
            push(new StoreWord(regConv(store.rhs), regConv(store.base), store.offset));
        }

        @Override
        public void visit(Move_Reg_Reg move_reg_reg) {
            push(new Move(regConv(move_reg_reg.lhs), regConv(move_reg_reg.rhs)));
        }

        @Override
        public void visit(Move_Id_Reg move_id_reg) {
            push(new StoreWord(regConv(move_id_reg.rhs), fp, getOffset(move_id_reg.lhs.toString())));
        }

        @Override
        public void visit(Move_Reg_Id move_reg_id) {
            push(new LoadWord(regConv(move_reg_id.lhs), fp, getOffset(move_reg_id.rhs.toString())));
        }

        @Override
        public void visit(Alloc alloc) {
            //move into a0 to allocate
            push(new Move(a0, regConv(alloc.size)));
            last.comment = "Memory to allocate in a0";
            push(new Jal("alloc"));
            push(new Move(regConv(alloc.lhs), a0));
        }

        @Override
        public void visit(Print print)  {

            //probably save the old register
            push(new Move(new RVRegister("a1") ,regConv(print.content)));
            push(new LoadImmediate(a0, "@print_int"));
            push(new ECall());
            push(new LoadImmediate(new RVRegister("a1"), "10"));
            push(new LoadImmediate(RVRegister.a0, "@print_char"));
            push(new ECall());


        }

        @Override
        public void visit(ErrorMessage errorMessage) {
            //TODO Fix error message
            String key = errorMessage.parent.parent.functionName.name + "_msg" + functionStrings.size();
            functionStrings.put(key, errorMessage.msg);
            push(new LoadAddress(a0, key));
            push(new Jump("error"));

        }

        @Override
        public void visit(Goto aGoto) {
            push(new Jump(aGoto.label.toString() + "@" + aGoto.parent.parent.functionName.name));
        }

        @Override
        public void visit(IfGoto ifGoto) {
            //here due to the creation of a new label to the end, we have to append a random string because otherwise we get collisions when jumping to the same label
            //kinda jank bit it works
            String endAppend = "_end" + Integer.toHexString(rand.nextInt());
            String label = ifGoto.label.toString() + "@" + ifGoto.parent.parent.functionName.name;
            push(new BranchIfNotZero(regConv(ifGoto.condition), label + endAppend));
            push(new Jal(label));
            push(new Label(label+endAppend));
        }

        @Override
        public void visit(Call call) {
            //allocate stack space for arguments
            //we allocate stack space for arguments
            if(!call.args.isEmpty()) {
                push(new LoadImmediate(t6, Integer.toString(call.args.size() * 4)));
                push(new Sub(sp, sp, t6));
                for(int i = 0; i < call.args.size(); i++) {
                    push(new LoadWord(t6, fp, getOffset(call.args.get(i).toString()))); //load argument into t6
                    push(new StoreWord(t6, sp, (call.args.size() - i - 1) * 4)); //wacky calculation for arg space
                }
            }
            //should be ready for call at this point so we call
            push(new Jalr(regConv(call.callee)));
            //restore old stack pointer
            if(!call.args.isEmpty()) {
                push(new LoadImmediate(t6, Integer.toString(call.args.size() * 4)));
                push(new Addition(sp, sp, t6));
            }

        }
    }

    private class RISCVBuilder {

        private final RISCVProgram p = new RISCVProgram();
        private final TextSection textSection = new TextSection();
        private final ArrayList<Function> funcList = new ArrayList<>();
        private final LinkedHashMap<String, String> stringList = new LinkedHashMap<>();

        public RISCVProgram buildProgram(Program program) {
            for (FunctionDecl f : program.funDecls) {
                RISCVFunctionBuilder builder = new RISCVFunctionBuilder();
                f.accept(builder);
                funcList.add(builder.getFunction());
                stringList.putAll(builder.getFunctionStrings());
            }
            funcList.add(errorFunc);
            funcList.add(allocFunc);
            //Build DataSection
            ArrayList<DataString> dataStrings = new ArrayList<>();
            for(Map.Entry<String, String> e : stringList.entrySet()) {
                dataStrings.add(new DataString(e.getKey(), e.getValue()));
            }
            DataSection dataSection = new DataSection(dataStrings);

            //We have to build the error function real quick
            //all functions built, now we do stuff.
            textSection.functionList = funcList;

            //Assemble the program

            p.sectionList = Arrays.asList(programPreamble, textSection, dataSection);
            return p;
        }
        private final CustomSection programPreamble = new CustomSection("  .equiv @sbrk, 9\n" +
                "  .equiv @print_string, 4\n" +
                "  .equiv @print_char, 11\n" +
                "  .equiv @print_int, 1\n" +
                "  .equiv @exit 10\n" +
                "  .equiv @exit2, 17\n\n");
        private final Function errorFunc = new Function(null, "error", Arrays.asList(
                    new Move(new RVRegister("a1"), RVRegister.a0),
                    new LoadImmediate(RVRegister.a0, "@print_string"),
                    new ECall(),
                    new LoadImmediate(new RVRegister("a1"), "10"),
                    new LoadImmediate(RVRegister.a0, "@print_char"),
                    new ECall(),
                    new LoadImmediate(RVRegister.a0, "@exit"),
                    new ECall(),
                    new Label("abort_17"),
                    new Jump("abort_17")
        ));

        private final Function allocFunc = new Function(null, "alloc", Arrays.asList(
                new Move(new RVRegister("a1"), RVRegister.a0),
                new LoadImmediate(RVRegister.a0, "@sbrk"),
                new ECall(),
                new JumpReturn(RVRegister.ra)
        ));
    }

    /**
     * Counts the amount of stack the function uses, also decides where to allocate variables on the stack.
     */
    private class MemoryCounter implements Visitor {


        public HashMap<String, Integer> getVariableStack() {
            return variableStack;
        }

        private final HashMap<String, Integer> variableStack = new HashMap<>();

        public HashMap<String, Integer> getArgumentStack() {
            return argumentStack;
        }

        private final HashMap<String, Integer> argumentStack = new HashMap<>();

        private void putIfAbsent(String s) {
            //System.err.println("Putting " + s + ", stack size is " + variableStack.size()*4);
            if(argumentStack.containsKey(s))
                return; //don't put arguments into the variable stack
            variableStack.putIfAbsent(s, variableStack.size() * 4); //we add everything in arguments to the stack, in order of its argument.
        }



        @Override
        public void visit(Program program) {
            //shouldn't happen.
            SV2V.error("Error: Entered Program visitor in Memory counter, should not happen.");
        }

        @Override
        public void visit(FunctionDecl functionDecl) {
            for(Identifier i : functionDecl.formalParameters) {
                argumentStack.put(i.toString(), argumentStack.size() * 4);
            }
            functionDecl.block.accept(this);
            //return identifier should already exist in function block, it not being there is a sparrow-v error.
        }

        @Override
        public void visit(Block block) {
            for(Instruction i : block.instructions)
                i.accept(this);
        }

        @Override
        public void visit(LabelInstr labelInstr) {
            //nothing
        }

        @Override
        public void visit(Move_Reg_Integer move_reg_integer) {
            //nothing
        }

        @Override
        public void visit(Move_Reg_FuncName move_reg_funcName) {
            //nothing
        }

        @Override
        public void visit(Add add) {
            //nothing
        }

        @Override
        public void visit(Subtract subtract) {
            //nothing
        }

        @Override
        public void visit(Multiply multiply) {
            //nothing
        }

        @Override
        public void visit(LessThan lessThan) {
            //nothing
        }

        @Override
        public void visit(Load load) {
            //nothing
        }

        @Override
        public void visit(Store store) {
            //nothing
        }

        @Override
        public void visit(Move_Reg_Reg move_reg_reg) {
            //nothing
        }

        @Override
        public void visit(Move_Id_Reg move_id_reg) {
            putIfAbsent(move_id_reg.lhs.toString());
        }

        @Override
        public void visit(Move_Reg_Id move_reg_id) {
            putIfAbsent(move_reg_id.rhs.toString());
        }

        @Override
        public void visit(Alloc alloc) {

        }

        @Override
        public void visit(Print print) {

        }

        @Override
        public void visit(ErrorMessage errorMessage) {

        }

        @Override
        public void visit(Goto aGoto) {

        }

        @Override
        public void visit(IfGoto ifGoto) {

        }

        @Override
        public void visit(Call call) {

        }
    }

}
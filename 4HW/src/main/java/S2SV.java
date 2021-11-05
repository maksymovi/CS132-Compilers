import cs132.IR.sparrow.*;
import cs132.IR.sparrow.Add;
import cs132.IR.sparrow.Alloc;
import cs132.IR.sparrow.Block;
import cs132.IR.sparrow.Call;
import cs132.IR.sparrow.ErrorMessage;
import cs132.IR.sparrow.FunctionDecl;
import cs132.IR.sparrow.Goto;
import cs132.IR.sparrow.IfGoto;
import cs132.IR.sparrow.Instruction;
import cs132.IR.sparrow.LabelInstr;
import cs132.IR.sparrow.LessThan;
import cs132.IR.sparrow.Load;
import cs132.IR.sparrow.Multiply;
import cs132.IR.sparrow.Print;
import cs132.IR.sparrow.Program;
import cs132.IR.sparrow.Store;
import cs132.IR.sparrow.Subtract;

import cs132.IR.sparrow.visitor.Visitor;
import cs132.IR.token.Identifier;
import cs132.IR.token.Register;
import cs132.IR.visitor.*;

import java.util.*;


public class S2SV {
    public static void main(String[] args) {
        cs132.IR.registers.Registers.SetRiscVregs();
        try {
            cs132.IR.syntaxtree.Node root = new cs132.IR.SparrowParser(System.in).Program();
            cs132.IR.visitor.SparrowConstructor sc = new cs132.IR.visitor.SparrowConstructor();
            root.accept(sc);
            cs132.IR.sparrow.Program p = sc.getProgram();
            S2SV compiler = new S2SV();
            compiler.buildSparrowVProgram(p);
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }
    }

    private void buildSparrowVProgram(cs132.IR.sparrow.Program p) {
        ArrayList<cs132.IR.sparrowv.FunctionDecl> functionDecls = new ArrayList<>();
        for(FunctionDecl f : p.funDecls) {
            LiveAnalysis l = new LiveAnalysis();
            RegisterAllocation r;
            f.accept(l);
            r = new RegisterAllocation(l.getSortedRanges(), l.getArgumentRegisters());
            f.accept(r);
            functionDecls.add(r.getNewFunctionDecl());


        }
        //all functions gathered, now we can assemble

        cs132.IR.sparrowv.Program newP = new cs132.IR.sparrowv.Program(functionDecls);
        for(cs132.IR.sparrowv.FunctionDecl f : newP.funDecls) {
            f.parent = newP;
        }
        System.out.print(newP.toString());


    }

    private class RegisterAllocation implements Visitor {

        private final ArrayList<IdLiveRange> sortedRanges;
        private final ArrayList<IdLiveRange> argumentRegisters;

        private final HashMap<String, String> usedRegisters = new HashMap<>(); //register allocation, maps from variable to register, null if on stack
        private final Stack<String> freeRegisters = new Stack<>();
        private HashSet<String> freeRegistersFinal;
        private final PriorityQueue<IdLiveRange> activeIntervals;

        private final ArrayList<String> registerList;
        private final List<String> callerSaved = Arrays.asList("t0", "t1", "t2", "t3", "t4", "t5");
        private final List<String> calleeSaved = Arrays.asList("s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9");
        private final List<String> argumentRegisterList = Arrays.asList("a2", "a3", "a4", "a5", "a6", "a6");

        private final Register s10 = new Register("s10");
        private final Register s11 = new Register("s11");
        private final ArrayList<cs132.IR.sparrowv.Instruction> sparrowVList = new ArrayList<>();

        private final ArrayList<cs132.IR.sparrowv.Instruction> queuedInstructions = new ArrayList<>();

        private cs132.IR.sparrowv.Block newBlock;

        public cs132.IR.sparrowv.FunctionDecl getNewFunctionDecl() {
            return newFunctionDecl;
        }

        private cs132.IR.sparrowv.FunctionDecl newFunctionDecl;

        public RegisterAllocation(ArrayList<IdLiveRange> sortedRanges, ArrayList<IdLiveRange> argumentRegisters) {
            this.sortedRanges = sortedRanges;
            this.argumentRegisters = argumentRegisters;
            activeIntervals = new PriorityQueue<>(sortedRanges.size()/2, new endSort());
            registerList = new ArrayList<>(calleeSaved);
            registerList.addAll(callerSaved);
            loadRegisters();


        }

        private void queueI(cs132.IR.sparrowv.Instruction i) {
            queuedInstructions.add(i);
        }

        private void dumpQueuedI() {
            sparrowVList.addAll(queuedInstructions);
            queuedInstructions.clear();
        }


        private void loadRegisters() {
            for(String s : registerList) {
                freeRegisters.push(s);
            }
        }

        private void allocateRegisters() {
            for(IdLiveRange i : sortedRanges) {
                expireOldRegisters(i);
                if(freeRegisters.isEmpty()) {
                    splitAtInterval(i);
                }
                else {
                    usedRegisters.put(i.getName(), freeRegisters.pop());
                }
            }

            //add argument registers
            for(int a = 0; a < argumentRegisters.size() && a < 6; a++) {
                if(argumentRegisters.get(a) != null) {
                    usedRegisters.put(argumentRegisters.get(a).getName(), argumentRegisterList.get(a));
                }
            }


        }
        private void expireOldRegisters(IdLiveRange i) {
            while(activeIntervals.peek() != null && activeIntervals.peek().getEnd() < i.getStart()) {
                IdLiveRange j = activeIntervals.poll(); //remove from active interval
                assert j != null;
                String register = usedRegisters.get(j.getName());
                freeRegisters.push(register);
            }
        }

        private void splitAtInterval(IdLiveRange i) {
            //priority queues are hard to look from the back apparently, am going to just going to put i on the stack
            //ArrayList<IdLiveRange> list = new ArrayList<>(activeIntervals);

            //IdLiveRange spill = Collections.max(list, new IdLiveRange.endSort());
            //activeIntervals.add(i);

        }


        //VARIOUS HELPER FUNCTIONS
        private String find(Identifier s) {
            return usedRegisters.get(s.toString());
        }

        private void loadToS10(Identifier id) {
            sparrowVList.add(new cs132.IR.sparrowv.Move_Id_Reg(id, s10));
        }
        private void loadToS11(Identifier id) {
            sparrowVList.add(new cs132.IR.sparrowv.Move_Id_Reg(id, s11));
        }
        private void storeFromS10(Identifier id) {
            sparrowVList.add(new cs132.IR.sparrowv.Move_Reg_Id(s10, id));
        }
        private void storeFromS11(Identifier id) {
            sparrowVList.add(new cs132.IR.sparrowv.Move_Reg_Id(s11, id));
        }
        private void queuedStoreFromS10(Identifier id) {
            queueI(new cs132.IR.sparrowv.Move_Reg_Id(s10, id));
        }
        private void queuedStoreFromS11(Identifier id) {
            queueI(new cs132.IR.sparrowv.Move_Reg_Id(s11, id));
        }



        private void addI(cs132.IR.sparrowv.Instruction i) {
            sparrowVList.add(i);
        }



        @Override
        public void visit(Program program) {
            //should not happen
        }

        @Override
        public void visit(FunctionDecl functionDecl) {
            allocateRegisters();
            ArrayList<String> temp = new ArrayList<>();
            while(!freeRegisters.empty()) {
                temp.add(freeRegisters.pop());
            }
            freeRegistersFinal = new HashSet<>(temp);


            functionDecl.block.accept(this);

            for(cs132.IR.sparrowv.Instruction i : newBlock.instructions) {
                i.parent = newBlock;
            }
            List<Identifier> idl;
            if(functionDecl.formalParameters.size() > 6)
                idl = functionDecl.formalParameters.subList(6, functionDecl.formalParameters.size());
            else {
                idl = new ArrayList<>(); //empty list
            }
            newFunctionDecl = new cs132.IR.sparrowv.FunctionDecl(functionDecl.functionName, idl,newBlock);
            newFunctionDecl.block.parent = newFunctionDecl;



        }

        @Override
        public void visit(Block block) {
            //start off with callee saving
            for(String s : calleeSaved) {
                if(!freeRegistersFinal.contains(s))
                    addI(new cs132.IR.sparrowv.Move_Id_Reg(new Identifier("callee_saved_".concat(s)), new Register(s)));
            }
            for(Instruction i : block.instructions) {
                i.accept(this);
                dumpQueuedI(); //this way I can assign instructions to be added at the end, not having to check multiple times for some cases.
            }
            //callee restore
            for(String s : calleeSaved) {
                if(!freeRegistersFinal.contains(s))
                    addI(new cs132.IR.sparrowv.Move_Reg_Id(new Register(s), new Identifier("callee_saved_".concat(s))));
            }
            //return identifier should be saved to stack
            String retReg = find(block.return_id);
            if(retReg != null) {
                addI(new cs132.IR.sparrowv.Move_Id_Reg(block.return_id, new Register(retReg)));
            }
            newBlock = new cs132.IR.sparrowv.Block(sparrowVList, block.return_id);
        }

        @Override
        public void visit(LabelInstr labelInstr) {
            addI(new cs132.IR.sparrowv.LabelInstr(labelInstr.label));
        }

        @Override
        public void visit(Move_Id_Integer move_id_integer) {
            String lhsReg = find(move_id_integer.lhs);
            if(lhsReg == null) {
                lhsReg = "s10";
                queuedStoreFromS10(move_id_integer.lhs);
            }
            addI(new cs132.IR.sparrowv.Move_Reg_Integer(new Register(lhsReg), move_id_integer.rhs));
        }

        @Override
        public void visit(Move_Id_FuncName move_id_funcName) {
            String lhsReg = find(move_id_funcName.lhs);
            if(lhsReg == null) {
                lhsReg = "s10";
                queuedStoreFromS10(move_id_funcName.lhs);
            }
            addI(new cs132.IR.sparrowv.Move_Reg_FuncName(new Register(lhsReg), move_id_funcName.rhs));
        }

        @Override
        public void visit(Add add) {
            String arg1Reg = find(add.arg1);
            if(arg1Reg == null) {
                arg1Reg = "s10";
                loadToS10(add.arg1);
            }
            String arg2Reg = find(add.arg2);
            if(arg2Reg == null) {
                arg2Reg = "s11";
                loadToS11(add.arg1);
            }
            String lhsReg = find(add.lhs);
            if(lhsReg == null) {
                lhsReg = "s11";
                queuedStoreFromS11(add.lhs);
            }
            addI(new cs132.IR.sparrowv.Add(new Register(lhsReg), new Register(arg1Reg), new Register(arg2Reg)));
        }

        @Override
        public void visit(Subtract subtract) {
            String arg1Reg = find(subtract.arg1);
            if(arg1Reg == null) {
                arg1Reg = "s10";
                loadToS10(subtract.arg1);
            }
            String arg2Reg = find(subtract.arg2);
            if(arg2Reg == null) {
                arg2Reg = "s11";
                loadToS11(subtract.arg1);
            }
            String lhsReg = find(subtract.lhs);
            if(lhsReg == null) {
                lhsReg = "s11";
                queuedStoreFromS11(subtract.lhs);
            }
            addI(new cs132.IR.sparrowv.Subtract(new Register(lhsReg), new Register(arg1Reg), new Register(arg2Reg)));
        }

        @Override
        public void visit(Multiply multiply) {
            String arg1Reg = find(multiply.arg1);
            if(arg1Reg == null) {
                arg1Reg = "s10";
                loadToS10(multiply.arg1);
            }
            String arg2Reg = find(multiply.arg2);
            if(arg2Reg == null) {
                arg2Reg = "s11";
                loadToS11(multiply.arg1);
            }
            String lhsReg = find(multiply.lhs);
            if(lhsReg == null) {
                lhsReg = "s11";
                queuedStoreFromS11(multiply.lhs);
            }
            addI(new cs132.IR.sparrowv.Multiply(new Register(lhsReg), new Register(arg1Reg), new Register(arg2Reg)));
        }

        @Override
        public void visit(LessThan lessThan) {
            String arg1Reg = find(lessThan.arg1);
            if(arg1Reg == null) {
                arg1Reg = "s10";
                loadToS10(lessThan.arg1);
            }
            String arg2Reg = find(lessThan.arg2);
            if(arg2Reg == null) {
                arg2Reg = "s11";
                loadToS11(lessThan.arg1);
            }
            String lhsReg = find(lessThan.lhs);
            if(lhsReg == null) {
                lhsReg = "s11";
                queuedStoreFromS11(lessThan.lhs);
            }
            addI(new cs132.IR.sparrowv.LessThan(new Register(lhsReg), new Register(arg1Reg), new Register(arg2Reg)));
        }

        @Override
        public void visit(Load load) {
            String baseReg = find(load.base);
            if(baseReg == null)
            {
                baseReg = "s10";
                loadToS10(load.base);
            }
            String lhsReg = find(load.lhs);
            if(lhsReg == null) {
                lhsReg = "s11";
                queuedStoreFromS11(load.lhs);
            }
            addI(new cs132.IR.sparrowv.Load(new Register(lhsReg), new Register(baseReg), load.offset));
        }

        @Override
        public void visit(Store store) {
            String baseReg = find(store.base);
            if(baseReg == null)
            {
                baseReg = "s10";
                loadToS10(store.base);
            }
            String rhsReg = find(store.rhs);
            if(rhsReg == null) {
                rhsReg = "s11";
                loadToS11(store.rhs);
            }
            addI(new cs132.IR.sparrowv.Store(new Register(baseReg), store.offset, new Register(rhsReg)));
        }

        @Override
        public void visit(Move_Id_Id move_id_id) {
            String rhsReg = find(move_id_id.rhs);
            if(rhsReg == null)
            {
                rhsReg = "s10";
                loadToS10(move_id_id.rhs);
            }
            String lhsReg = find(move_id_id.lhs);

            if(lhsReg == null)
            {
                lhsReg = "s11";
                queuedStoreFromS11(move_id_id.lhs);
            }
            addI(new cs132.IR.sparrowv.Move_Reg_Reg(new Register(lhsReg), new Register(rhsReg)));
        }

        @Override
        public void visit(Alloc alloc) {
            String lhsReg = find(alloc.lhs);
            if(lhsReg == null) {
                lhsReg = "s10";
                queuedStoreFromS10(alloc.lhs);
            }
            String sizeReg = find(alloc.size);
            if(sizeReg == null) {
                sizeReg = "s11";
                loadToS11(alloc.size);
            }
            addI(new cs132.IR.sparrowv.Alloc(new Register(lhsReg), new Register(sizeReg)));
        }

        @Override
        public void visit(Print print) {
            String printReg = find(print.content);
            if(printReg == null) {
                printReg = "s10";
                loadToS10(print.content);
            }
            addI( new cs132.IR.sparrowv.Print(new Register(printReg)));

        }

        @Override
        public void visit(ErrorMessage errorMessage) {
            addI(new cs132.IR.sparrowv.ErrorMessage(errorMessage.msg));
        }

        @Override
        public void visit(Goto aGoto) {
            addI(new cs132.IR.sparrowv.Goto(aGoto.label));
        }

        @Override
        public void visit(IfGoto ifGoto) {
            String conReg = find(ifGoto.condition);
            if(conReg == null) {
                conReg = "s10";
                loadToS10(ifGoto.condition);
            }
            addI(new cs132.IR.sparrowv.IfGoto(new Register(conReg), ifGoto.label));

        }

        @Override
        public void visit(Call call) {
            String lhsReg = find(call.lhs);
            if(lhsReg == null) {
                lhsReg = "s10";
                queuedStoreFromS10(call.lhs);
            }
            String calleeReg = find(call.callee);
            if(calleeReg == null) {
                calleeReg = "s11";
                loadToS11(call.callee);
            }

            //save our argument registers
            for(int i = 0; i < argumentRegisters.size(); i++) {
                if(!argumentRegisterList.get(i).equals(lhsReg) /*&& !freeRegistersFinal.contains(argumentRegisterList.get(i))*/) {
                    addI(new cs132.IR.sparrowv.Move_Id_Reg(new Identifier("caller_saved_".concat(argumentRegisterList.get(i))), new Register(argumentRegisterList.get(i))));
                    queueI(new cs132.IR.sparrowv.Move_Reg_Id(new Register(argumentRegisterList.get(i)), new Identifier("caller_saved_".concat(argumentRegisterList.get(i)))));
                }
            }

            //caller saving now
            for(String s : callerSaved) {
                if(!s.equals(lhsReg) && !freeRegistersFinal.contains(s)) {
                    addI(new cs132.IR.sparrowv.Move_Id_Reg(new Identifier("caller_saved_".concat(s)), new Register(s)));
                    queueI(new cs132.IR.sparrowv.Move_Reg_Id(new Register(s), new Identifier("caller_saved_".concat(s))));
                }
            }

            //load function's argument registers
            for(int i = 0; i < 6 && i < call.args.size(); i++) {
                String argReg = find(call.args.get(i));
                if (argReg == null) {
                    addI(new cs132.IR.sparrowv.Move_Reg_Id(new Register(argumentRegisterList.get(i)), call.args.get(i)));
                } else {
                    addI(new cs132.IR.sparrowv.Move_Reg_Reg(new Register(argumentRegisterList.get(i)), new Register(argReg)));
                }
            }
            //stack arguments
            for(int i = 6; i < call.args.size(); i++) {
                String argReg = find(call.args.get(i));
                //in this case of null/no register allocation, we want stuff to be on the stack, we do not touch already stacked registers, we restore afterwards
                if(argReg != null) {
                    addI(new cs132.IR.sparrowv.Move_Id_Reg(call.args.get(i), new Register(argReg)));
                    if(!argReg.equals(lhsReg))
                        queueI(new cs132.IR.sparrowv.Move_Reg_Id(new Register(argReg), call.args.get(i)));
                }
            }
            List<Identifier> idl;
            if(call.args.size() > 6) {
                idl = call.args.subList(6, call.args.size());
            }
            else {
                idl = new ArrayList<>();
            }

            addI(new cs132.IR.sparrowv.Call(new Register(lhsReg), new Register(calleeReg), idl));

            //restoring the state should be handled by the queued items

        }
    }


    private class LoopSearch implements Visitor {
        private int CurrentLocation;
        private final HashMap<Integer, Integer> LoopMap = new HashMap<>(); //key is start of loop, value is end of loop
        private final HashMap<String, Integer> labelToLine = new HashMap<>();

        //returns end of biggest loop this line is in, 0 if no loop

        public int getBiggestLoopEnd(int currentMax) { //the end range passed in effectively is what starts as currentmax
            for (Map.Entry<Integer, Integer> i : LoopMap.entrySet()) {
                if (i.getKey() < currentMax && i.getValue() > currentMax)
                    currentMax = i.getValue();
            }
            return currentMax;
        }
        //realized this was unnecessary.
        /*public int getBiggestLoopStart(int LineLoc) {
            int currentMin = 0;
            for (Map.Entry<Integer, Integer> i : LoopMap.entrySet()) {
                if (i.getKey() < LineLoc && i.getValue() > LineLoc)
                    if (currentMin == 0)
                        currentMin = i.getKey();
                    else if (i.getKey() < currentMin)
                        currentMin = i.getKey();
            }
            return currentMin;
        }*/


        @Override
        public void visit(cs132.IR.sparrow.Program program) {
            //shouldn't happen
            throw new RuntimeException("Entered, LoopSearch program visitor, should not happen.");
        }

        @Override
        public void visit(FunctionDecl functionDecl) {
            functionDecl.block.accept(this);
        }

        @Override
        public void visit(Block block) {
            CurrentLocation = 1;
            for (Instruction i : block.instructions) {
                i.accept(this);
                CurrentLocation++;//we keep a count of all instructions in the current loop, so that we know where our loops start and end
            }
        }

        @Override
        public void visit(LabelInstr labelInstr) {
            labelToLine.put(labelInstr.label.toString(), CurrentLocation);
        }

        @Override
        public void visit(Move_Id_Integer move_id_integer) {

        }

        @Override
        public void visit(Move_Id_FuncName move_id_funcName) {

        }

        @Override
        public void visit(Add add) {

        }

        @Override
        public void visit(Subtract subtract) {

        }

        @Override
        public void visit(Multiply multiply) {

        }

        @Override
        public void visit(LessThan lessThan) {

        }

        @Override
        public void visit(Load load) {

        }

        @Override
        public void visit(Store store) {

        }

        @Override
        public void visit(Move_Id_Id move_id_id) {

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
            Integer LabelLine;
            if((LabelLine = labelToLine.get(aGoto.label.toString())) != null) {
                //This means we loop, so we add this into out loop record
                LoopMap.put(LabelLine, CurrentLocation);
            }
        }

        @Override
        public void visit(IfGoto ifGoto) {
            Integer LabelLine;
            if((LabelLine = labelToLine.get(ifGoto.label.toString())) != null) {
                //This means we loop, so we add this into out loop record
                LoopMap.put(LabelLine, CurrentLocation);
            }
        }

        @Override
        public void visit(Call call) {

        }
    }
    //To return argument names


    private class IdLiveRange implements Comparable<IdLiveRange> { //Identifier live range
        IdLiveRange(String name, int start, int end) {
            this.start = start;
            this.end = end;
            this.name = name;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }
        public void setStart(int start) {
            this.start = start;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private int start;
        private int end;
        private String name;

        @Override
        public int compareTo(IdLiveRange o) {
            if (getStart() < o.getStart())
                return -1;
            if (getStart() > o.getStart())
                return 1;
            return Integer.compare(getEnd(), o.getEnd());
        }



    }
    private class endSort implements Comparator<IdLiveRange> {

        @Override
        public int compare(IdLiveRange o1, IdLiveRange o2) {
            return Integer.compare(o1.getEnd(), o2.getEnd());
        }
    }

    private class LiveAnalysis implements Visitor {
        HashMap<String, IdLiveRange> rangeMap  = new HashMap<>();
        private int currentLocation = 1;
        private LoopSearch ls;

        public ArrayList<IdLiveRange> getSortedRanges() {
            return sortedRanges;
        }

        public ArrayList<IdLiveRange> getArgumentRegisters() {
            return argumentRegisters;
        }

        private ArrayList<IdLiveRange> sortedRanges;

        private final ArrayList<IdLiveRange> argumentRegisters = new ArrayList<>();

        private void functionActions(FunctionDecl f) {
            this.ls = new LoopSearch();
            f.accept(ls); //find all loops in function
            handleArguments(f); //add stack arguments to argument listings. MAKE SURE TO REMOVE FIRST SIX ARGUMENTS FROM LISTING
            f.block.accept(this);
            removeRegisterArguments(f); //removes first six register arguments
            processLoops();


            sortedRanges = new ArrayList<>(rangeMap.values()); //putting everything into a list into a store
            Collections.sort(sortedRanges);
            //making sorted list for register allocation
            //at this point we should have everything we need for register allocation

        }

        private void removeRegisterArguments(FunctionDecl f) { //we remove the 6 initial register arguments from our hashmap because these will be used for the a register set

            for(int i = 0; i < f.formalParameters.size() && i < 6; i++)
                argumentRegisters.add(rangeMap.remove(f.formalParameters.get(i).toString())); //removes from rangemap, places in argument registers
            //might add null key, be careful
        }

        private void updateEntry(Identifier id) {

            if(rangeMap.containsKey(id.toString())) {
                rangeMap.get(id.toString()).setEnd(currentLocation);
            }
            else {
                rangeMap.put(id.toString(), new IdLiveRange(id.toString(), currentLocation, currentLocation));
            }
        }
        private void handleArguments(FunctionDecl fun) {
            int tempCurrentLoc = currentLocation;
            currentLocation = 0; //we handle arguments by setting current location to 0, which means they have precedent over all else

            for(int i = 6; i < fun.formalParameters.size(); i++) { //first 6 args in registers, we use the 'a' block of registers for only arguments
                updateEntry(fun.formalParameters.get(i));
            }
            currentLocation = tempCurrentLoc;
        }

        private void processLoops() {
            for(IdLiveRange i : rangeMap.values()) {
                i.setEnd(ls.getBiggestLoopEnd(i.getEnd()));
            }
        }



        @Override
        public void visit(cs132.IR.sparrow.Program program) {
            //this should not happen
            throw new RuntimeException("Entered LiveAnalysis program visitor, no control flow should lead here.");
        }

        @Override
        public void visit(FunctionDecl functionDecl) {
            functionActions(functionDecl);
        }

        @Override
        public void visit(Block block) {
            for(Instruction i : block.instructions) {
                i.accept(this);
                currentLocation++;
            }
        }

        @Override
        public void visit(LabelInstr labelInstr) {
            //do nothing
        }

        @Override
        public void visit(Move_Id_Integer move_id_integer) {
            updateEntry(move_id_integer.lhs);
        }

        @Override
        public void visit(Move_Id_FuncName move_id_funcName) {
            updateEntry(move_id_funcName.lhs);
        }

        @Override
        public void visit(Add add) {
            updateEntry(add.arg1);
            updateEntry(add.arg2);
            updateEntry(add.lhs);
        }

        @Override
        public void visit(Subtract subtract) {
            updateEntry(subtract.arg1);
            updateEntry(subtract.arg2);
            updateEntry(subtract.lhs);
        }

        @Override
        public void visit(Multiply multiply) {
            updateEntry(multiply.arg1);
            updateEntry(multiply.arg2);
            updateEntry(multiply.lhs);
        }

        @Override
        public void visit(LessThan lessThan) {
            updateEntry(lessThan.arg1);
            updateEntry(lessThan.arg2);
            updateEntry(lessThan.lhs);
        }

        @Override
        public void visit(Load load) {
            updateEntry(load.base);
            updateEntry(load.lhs);
        }

        @Override
        public void visit(Store store) {
            updateEntry(store.base);
            updateEntry(store.rhs);
        }

        @Override
        public void visit(Move_Id_Id move_id_id) {
            updateEntry(move_id_id.lhs);
            updateEntry(move_id_id.rhs);
        }

        @Override
        public void visit(Alloc alloc) {
            updateEntry(alloc.lhs);
            updateEntry(alloc.size);
        }

        @Override
        public void visit(Print print) {
            updateEntry(print.content);
        }

        @Override
        public void visit(ErrorMessage errorMessage) {
        }

        @Override
        public void visit(Goto aGoto) {
        }

        @Override
        public void visit(IfGoto ifGoto) {
            updateEntry(ifGoto.condition);
        }

        @Override
        public void visit(Call call) {
            updateEntry(call.lhs);
            updateEntry(call.callee);
            for(Identifier id : call.args)
                updateEntry(id);
        }
    }
}
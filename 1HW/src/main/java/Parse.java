import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.Collections;



//	public enum ValidTokens {leftBrace, rightBrace, SOP, openParen, closeParen, semicolon, ifStatement, elseStatement, whileStatement, trueStatement, falseStatement, exclamation, S, L, E, nullable}

public class Parse {

	public static class TPair {		//pair class for hashtable purposes
		private Tok.N N;
		private Tok.T T;
		public TPair(Tok.N NArg, Tok.T TArg) {
			N = NArg;
			T = TArg;
		}
		@Override
		public boolean equals(Object t) {
			if (this == t)
				return true;
			if (!(t instanceof TPair))
				return false;
			TPair temp = (TPair) t;
			return T == temp.T && N == temp.N;
		}
		@Override
		public int hashCode() {
			return 53 * N.val + T.val;
		}
		
	}

	public static void main(String[] args) {
		
		List<Tok.T> programList = Lexer.lex();
		if(programList.isEmpty()) {	//lexer failed to lex, grammar invalid
			System.out.println("Parse error");
			return;
		}
		//System.err.println("Lexed successfully");
		Stack<Tok.T> programStack = new Stack<Tok.T>();
		Parse p = new Parse();
		
		p.InitializeGrammar();
		Collections.reverse(programList);	//list to stack conversion is wrong way, need to reverse
		programStack.addAll(programList);
		
		if(p.parse(programStack, Tok.N.S) && programStack.isEmpty())
			System.out.println("Program parsed successfully");
		else
			System.out.println("Parse error");
	}
	
	HashMap<TPair, List<Tok>> grammar;

	public void InitializeGrammar() {
		grammar = new HashMap<TPair, List<Tok>>();
		

		//S GRAMMARS
		grammar.put(new TPair(Tok.N.S, Tok.T.leftBrace), Arrays.asList(
						Tok.T.leftBrace,
						Tok.N.L,
						Tok.T.rightBrace));
		grammar.put(new TPair(Tok.N.S, Tok.T.SOP), Arrays.asList(
						Tok.T.SOP,
						Tok.T.openParen,
						Tok.N.E,
						Tok.T.closeParen,
						Tok.T.semicolon));
		grammar.put(new TPair(Tok.N.S, Tok.T.ifStatement), Arrays.asList(
						Tok.T.ifStatement,
						Tok.T.openParen,
						Tok.N.E,
						Tok.T.closeParen,
						Tok.N.S,
						Tok.T.elseStatement,
						Tok.N.S));
		grammar.put(new TPair(Tok.N.S, Tok.T.whileStatement), Arrays.asList(
						Tok.T.whileStatement,
						Tok.T.openParen,
						Tok.N.E,
						Tok.T.closeParen,
						Tok.N.S));
		//L GRAMMARS
		grammar.put(new TPair(Tok.N.L, Tok.T.leftBrace), Arrays.asList(
						Tok.N.S,
						Tok.N.L));
		grammar.put(new TPair(Tok.N.L, Tok.T.rightBrace), Arrays.asList(
						Tok.Nullable.nullable));
		grammar.put(new TPair(Tok.N.L, Tok.T.SOP), Arrays.asList(
						Tok.N.S,
						Tok.N.L));
		grammar.put(new TPair(Tok.N.L, Tok.T.ifStatement), Arrays.asList(
						Tok.N.S,
						Tok.N.L));
		grammar.put(new TPair(Tok.N.L, Tok.T.whileStatement), Arrays.asList(
						Tok.N.S,
						Tok.N.L));
		//E GRAMMARS
		grammar.put(new TPair(Tok.N.E, Tok.T.trueStatement), Arrays.asList(
						Tok.T.trueStatement));
		grammar.put(new TPair(Tok.N.E, Tok.T.falseStatement), Arrays.asList(
						Tok.T.falseStatement));
		grammar.put(new TPair(Tok.N.E, Tok.T.exclamation), Arrays.asList(
						Tok.T.exclamation,
						Tok.N.E));
		//System.err.println("Grammar loaded");
	}

	public Boolean parse(Stack<Tok.T> program, Tok.N symbol) {
		//System.err.println("Parsing");
		Tok.T curTerm = program.peek();	//get first token
		if(curTerm == null) {
			//System.err.println("Program returned null");
			return false;
		}
		////System.err.println(curTerm.val);
		List<Tok> rule = grammar.get(new TPair(symbol, curTerm));	//get corresponding rule from parsetable
		if(rule == null) {	//no rule, grammar is invalid, return false
			//System.err.println("hashmap returned null");
			return false;
		}
		for(Tok current : rule) {
			if(program.isEmpty())
				return false;
			curTerm = program.peek(); //get element from program
			switch(current.type()) {
			case Nullable:
				return true; //we dont need to go further down this path, nullable case
			case Terminal:
				if(!(current == curTerm)) {
					//System.err.println("Failed to match terminal");
					return false;
				}
				//System.err.println("Passed check");
				//token matches, we advance through program
				program.pop();	//we want to keep the token on top of the stack, so we peek to get and pop when necessary
				break;
			case Nonterminal:	//check maybe isnt needed
				//System.err.println("Recursing");
				if(!parse(program, (Tok.N) current))	//casting for compiler purposes, checked type above
					return false;	//parse didn't work, dont advance
				//if worked, we have to grab the top of the stack again
			}
		}
		//if we got this far, it is successful. If it is the top level of recursion, we also have to check that the program got fully consumed by checking if the stack is empty
		//System.err.println("correct return");
		return true;
	}
}

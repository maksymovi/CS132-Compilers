import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.Collections;



//	public enum ValidTokens {leftBrace, rightBrace, SOP, openParen, closeParen, semicolon, ifStatement, elseStatement, whileStatement, trueStatement, falseStatement, exclamation, S, L, E, nullable}

public class Parse {

	public static class TPair {		//pair class for hashtable purposes
		private Token.N N;
		private Token.T T;
		public TPair(Token.N NArg, Token.T TArg) {
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
		
		List<Token.T> programList = Lexer.lex();
		if(programList.isEmpty()) {	//lexer failed to lex, grammar invalid
			System.out.println("Parse error");
			return;
		}
		//System.err.println("Lexed successfully");
		Stack<Token.T> programStack = new Stack<Token.T>();
		Parse p = new Parse();
		
		p.InitializeGrammar();
		Collections.reverse(programList);	//list to stack conversion is wrong way, need to reverse
		programStack.addAll(programList);
		
		if(p.parse(programStack, Token.N.S) && programStack.isEmpty())
			System.out.println("Program parsed successfully");
		else
			System.out.println("Parse error");
	}
	
	HashMap<TPair, List<Token>> grammar;

	public void InitializeGrammar() {
		grammar = new HashMap<TPair, List<Token>>();
		

		//S GRAMMARS
		grammar.put(new TPair(Token.N.S, Token.T.leftBrace), Arrays.asList(
						Token.T.leftBrace,
						Token.N.L,
						Token.T.rightBrace));
		grammar.put(new TPair(Token.N.S, Token.T.SOP), Arrays.asList(
						Token.T.SOP,
						Token.T.openParen,
						Token.N.E,
						Token.T.closeParen,
						Token.T.semicolon));
		grammar.put(new TPair(Token.N.S, Token.T.ifStatement), Arrays.asList(
						Token.T.ifStatement,
						Token.T.openParen,
						Token.N.E,
						Token.T.closeParen,
						Token.N.S,
						Token.T.elseStatement,
						Token.N.S));
		grammar.put(new TPair(Token.N.S, Token.T.whileStatement), Arrays.asList(
						Token.T.whileStatement,
						Token.T.openParen,
						Token.N.E,
						Token.T.closeParen,
						Token.N.S));
		//L GRAMMARS
		grammar.put(new TPair(Token.N.L, Token.T.leftBrace), Arrays.asList(
						Token.N.S,
						Token.N.L));
		grammar.put(new TPair(Token.N.L, Token.T.rightBrace), Arrays.asList(
						Token.Nullable.nullable));
		grammar.put(new TPair(Token.N.L, Token.T.SOP), Arrays.asList(
						Token.N.S,
						Token.N.L));
		grammar.put(new TPair(Token.N.L, Token.T.ifStatement), Arrays.asList(
						Token.N.S,
						Token.N.L));
		grammar.put(new TPair(Token.N.L, Token.T.whileStatement), Arrays.asList(
						Token.N.S,
						Token.N.L));
		//E GRAMMARS
		grammar.put(new TPair(Token.N.E, Token.T.trueStatement), Arrays.asList(
						Token.T.trueStatement));
		grammar.put(new TPair(Token.N.E, Token.T.falseStatement), Arrays.asList(
						Token.T.falseStatement));
		grammar.put(new TPair(Token.N.E, Token.T.exclamation), Arrays.asList(
						Token.T.exclamation,
						Token.N.E));
		//System.err.println("Grammar loaded");
	}

	public Boolean parse(Stack<Token.T> program, Token.N symbol) {
		//System.err.println("Parsing");
		Token.T curTerm = program.peek();	//get first token
		if(curTerm == null) {
			//System.err.println("Program returned null");
			return false;
		}
		////System.err.println(curTerm.val);
		List<Token> rule = grammar.get(new TPair(symbol, curTerm));	//get corresponding rule from parsetable
		if(rule == null) {	//no rule, grammar is invalid, return false
			//System.err.println("hashmap returned null");
			return false;
		}
		for(Token current : rule) {
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
				if(!parse(program, (Token.N) current))	//casting for compiler purposes, checked type above
					return false;	//parse didn't work, dont advance
				//if worked, we have to grab the top of the stack again
			}
		}
		//if we got this far, it is successful. If it is the top level of recursion, we also have to check that the program got fully consumed by checking if the stack is empty
		//System.err.println("correct return");
		return true;
	}
}

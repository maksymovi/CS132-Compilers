import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.Collections;


public class Main {
	public static void main(String [] args) {
		Parser.main();
	}

}

//Making my own enum-like class for simplicity for tokens, in hindsight this is not simple but eh.
//	public enum ValidTokens {leftBrace, rightBrace, SOP, openParen, closeParen, semicolon, ifStatement, elseStatement, whileStatement, trueStatement, falseStatement, exclamation, S, L, E, nullable}
public abstract class Token {

	public enum Type {Terminal, Nonterminal, Nullable}
	
	//private static int counter = 0; //global counter for enum discrimination purposes

	//private final int value;
	/*
	public Token() {
		value = counter;
		counter++;
	}
	*/
	private static int count = 0;
	public final int val;
	private Token() {
		val = count;
		count++;
	}

	
	
	public abstract Type type();
	
	public static class T extends Token {
		public Type type() {
			return Type.Terminal;
		}//a little boilerplatey
		public final static T leftBrace = new T();
		public final static T rightBrace = new T();
		public final static T SOP = new T();
		public final static T openParen = new T();
		public final static T closeParen = new T();
		public final static T semicolon = new T();
		public final static T ifStatement = new T();
		public final static T elseStatement = new T();
		public final static T whileStatement = new T();
		public final static T trueStatement = new T();
		public final static T falseStatement = new T();
		public final static T exclamation = new T();

	}
	public static class N extends Token {
		public Type type() {
			return Type.Nonterminal;
		}
		public final static N S = new N();
		public final static N L = new N();
		public final static N E = new N();
	}
	public static class Nullable extends Token {
		public Type type() {
			return Type.Nullable;
		}
		public final static Nullable nullable = new Nullable();
	}

}

public class Parser {

	public static class TPair {
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

	public static void main() {
		
		List<Token.T> programList = Lexer.lex();
		if(programList.isEmpty()) {	//lexer failed to lex, grammar invalid
			System.out.println("Parse error");
			return;
		}
		//System.err.println("Lexed successfully");
		Stack<Token.T> programStack = new Stack<Token.T>();
		Parser p = new Parser();
		
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
		System.err.println("Grammar loaded");
	}

	public Boolean parse(Stack<Token.T> program, Token.N symbol) {
		System.err.println("Parsing");
		Token.T curTerm = program.peek();	//get first token
		if(curTerm == null) {
			System.err.println("Program returned null");
			return false;
		}
		//System.err.println(curTerm.val);
		List<Token> rule = grammar.get(new TPair(symbol, curTerm));	//get corresponding rule from parsetable
		if(rule == null) {	//no rule, grammar is invalid, return false
			System.err.println("hashmap returned null");
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
					System.err.println("Failed to match terminal");
					return false;
				}
				System.err.println("Passed check");
				//token matches, we advance through program
				program.pop();	//we want to keep the token on top of the stack, so we peek to get and pop when necessary
				break;
			case Nonterminal:	//check maybe isnt needed
				System.err.println("Recursing");
				if(!parse(program, (Token.N) current))	//casting for compiler purposes, checked type above
					return false;	//parse didn't work, dont advance
				//if worked, we have to grab the top of the stack again
			}
		}
		//if we got this far, it is successful. If it is the top level of recursion, we also have to check that the program got fully consumed by checking if the stack is empty
		System.err.println("correct return");
		return true;
	}
}


public class Lexer {
	public static void main(String [ ] args) {
		ArrayList<Token.T> terms = lex();
		if(terms.isEmpty())
		{
			System.out.println("Error");
		}
		else
			System.out.println("Valid");
			/*
			for(T t : terms)
			{
				System.out.println(t.name());
				}*/
		return;
		}
	public static ArrayList<Token.T> lex () { //returns empty arraylist if error

		ArrayList<Token.T> tList = new ArrayList<Token.T>();
		//Scanner s = new Scanner(program); //scanner removes whitespace, making lexing a lot easier
		Scanner s = new Scanner(System.in); //POSSIBLE TESTING LINE THAT TAKES IN STDIN DIRECTLY
		while(s.hasNext()) {
			
			String line = s.next(); //now we scan this line
			Boolean hadletters = false; //lettered terminals cannot be next to each other, this makes sure this does not happen
			while(!line.isEmpty()) { //whole line analysis
				
				if(!Character.isLetter(line.charAt(0))) {
						switch(line.charAt(0)) {//easy cases first
						case '{':
							System.err.println("leftBrace");
							tList.add(Token.T.leftBrace);
							break;
						case '}':
							System.err.println("rightBrace");
							tList.add(Token.T.rightBrace);
							break;
						case '(':
							System.err.println("openParen");
							tList.add(Token.T.openParen);
							break;
						case ')':
							System.err.println("closeParen");
							tList.add(Token.T.closeParen);
							break;
						case ';':
							System.err.println("semicolon");
							tList.add(Token.T.semicolon);
							break;
						case '!':
							System.err.println("exclamation");
							tList.add(Token.T.exclamation);
							break;
						default:
							//starts without letter but not a character here, error, grammar is invalid
							tList.clear();
							return tList;
						}
						hadletters = false;
						line = line.substring(1); //strip the first character and carry on
					}
					else {
						if(hadletters) { //two lettered terminals in sequence, not allowed
							tList.clear(); //error
							return tList;
						}
						hadletters = true;
						if(line.startsWith("if")) { //there is probably a better way to do this than chaining if statements, might fix this later, though the code is simple this way
							tList.add(Token.T.ifStatement);
							line = line.substring("if".length());
							System.err.println("ifStatement");
						}
						else if(line.startsWith("else")) {
							tList.add(Token.T.elseStatement);
							line = line.substring("else".length());
							System.err.println("elseStatement");
						}
						else if(line.startsWith("while")) {
							tList.add(Token.T.whileStatement);
							line = line.substring("while".length());
							System.err.println("whileStatement");
						}
						else if(line.startsWith("true")) {
							tList.add(Token.T.trueStatement);
							line = line.substring("true".length());
							System.err.println("trueStatement");
						}
						else if(line.startsWith("false")) {
							tList.add(Token.T.falseStatement);
							line = line.substring("false".length());
							System.err.println("falseStatement");
						}
						else if(line.startsWith("System.out.println")) {
							tList.add(Token.T.SOP);
							line = line.substring("System.out.println".length());
							System.err.println("SOP");
						}
						else {
							tList.clear(); //error, terminal not found
							return tList;
						}
					}
			}
		}
		return tList;
	}
}




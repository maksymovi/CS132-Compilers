import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Main {
	public static void main(String [] args) {
		
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
	
	public abstract Type type();
	
	public static class Terminal extends Token {
		public Type type() {
			return Type.Terminal;
		}//a little boilerplatey
		public final static Terminal leftBrace = new Terminal();
		public final static Terminal rightBrace = new Terminal();
		public final static Terminal SOP = new Terminal();
		public final static Terminal openParen = new Terminal();
		public final static Terminal closeParen = new Terminal();
		public final static Terminal semicolon = new Terminal();
		public final static Terminal ifStatement = new Terminal();
		public final static Terminal elseStatement = new Terminal();
		public final static Terminal whileStatement = new Terminal();
		public final static Terminal trueStatement = new Terminal();
		public final static Terminal falseStatement = new Terminal();
		public final static Terminal exclamation = new Terminal();

	}
	public static class Nonterminal extends Token {
		public Type type() {
			return Type.Nonterminal;
		}
		public final static Nonterminal S = new Nonterminal();
		public final static Nonterminal L = new Nonterminal();
		public final static Nonterminal E = new Nonterminal();
	}
	public static class Nullable extends Token {
		public Type type() {
			return Type.Nullable;
		}
		public final static Nullable nullable = new Nullable();
	}

}

public class Parser {
	/* private class TClass { //used for individual rules, there may be a better way to sort between terminals and nonterminals but eh
		private TType type;
		private T token;
		public T getToken() {
			return token;
		}
		public T getType() {
			return type;
		}
		TClass(TType argType, T argToken) {
			token = argToken;
			type = argType;
		}
		
	}
	private class TPair { //used in parse table locations, dont even need getters for comparison purposes
		private T n;
		private T t;
		TPair(T argN, T argT) {
			n = argN;
			t = argT;
		}
		}*/
	
}


public class Lexer {
	/*public static void main(String [ ] args) {
		ArrayList<T> terms = lex("");
		if(terms.isEmpty())
		{
			System.out.println("Error");
		}
		else
			for(T t : terms)
			{
				System.out.println(t.name());
			}
		return;
		}*/
	public static ArrayList<Token.Terminal> lex (String program) { //returns empty arraylist if error

		ArrayList<Token.Terminal> tList = new ArrayList<Token.Terminal>();
		//Scanner s = new Scanner(program); //scanner removes whitespace, making lexing a lot easier
		Scanner s = new Scanner(System.in); //POSSIBLE TESTING LINE THAT TAKES IN STDIN DIRECTLY
		while(s.hasNext()) {
			
			String line = s.next(); //now we scan this line
			Boolean hadletters = false; //lettered terminals cannot be next to each other, this makes sure this does not happen
			while(!line.isEmpty()) { //whole line analysis
				
				if(!Character.isLetter(line.charAt(0))) {
						switch(line.charAt(0)) {//easy cases first
						case '{':
							tList.add(Token.Terminal.leftBrace);
							break;
						case '}':
							tList.add(Token.Terminal.rightBrace);
							break;
						case '(':
							tList.add(Token.Terminal.openParen);
							break;
						case ')':
							tList.add(Token.Terminal.closeParen);
							break;
						case ';':
							tList.add(Token.Terminal.semicolon);
							break;
						case '!':
							tList.add(Token.Terminal.exclamation);
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
							tList.add(Token.Terminal.ifStatement);
							line = line.substring("if".length());
						}
						else if(line.startsWith("else")) {
							tList.add(Token.Terminal.elseStatement);
							line = line.substring("else".length());
						}
						else if(line.startsWith("while")) {
							tList.add(Token.Terminal.whileStatement);
							line = line.substring("while".length());
						}
						else if(line.startsWith("true")) {
							tList.add(Token.Terminal.trueStatement);
							line = line.substring("true".length());
						}
						else if(line.startsWith("false")) {
							tList.add(Token.Terminal.falseStatement);
							line = line.substring("false".length());
						}
						else if(line.startsWith("System.out.println")) {
							tList.add(Token.Terminal.SOP);
							line = line.substring("System.out.println".length());
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




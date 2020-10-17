import java.util.ArrayList;
import java.util.Scanner;

public class Lexer {
	/*
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
				}*
		return;
		}*/
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
							//System.err.println("leftBrace");
							tList.add(Token.T.leftBrace);
							break;
						case '}':
							//System.err.println("rightBrace");
							tList.add(Token.T.rightBrace);
							break;
						case '(':
							//System.err.println("openParen");
							tList.add(Token.T.openParen);
							break;
						case ')':
							//System.err.println("closeParen");
							tList.add(Token.T.closeParen);
							break;
						case ';':
							//System.err.println("semicolon");
							tList.add(Token.T.semicolon);
							break;
						case '!':
							//System.err.println("exclamation");
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
							//System.err.println("ifStatement");
						}
						else if(line.startsWith("else")) {
							tList.add(Token.T.elseStatement);
							line = line.substring("else".length());
							//System.err.println("elseStatement");
						}
						else if(line.startsWith("while")) {
							tList.add(Token.T.whileStatement);
							line = line.substring("while".length());
							//System.err.println("whileStatement");
						}
						else if(line.startsWith("true")) {
							tList.add(Token.T.trueStatement);
							line = line.substring("true".length());
							//System.err.println("trueStatement");
						}
						else if(line.startsWith("false")) {
							tList.add(Token.T.falseStatement);
							line = line.substring("false".length());
							//System.err.println("falseStatement");
						}
						else if(line.startsWith("System.out.println")) {
							tList.add(Token.T.SOP);
							line = line.substring("System.out.println".length());
							//System.err.println("SOP");
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

import java.util.ArrayList;
import java.util.Scanner;


public class Lexer {
	public static enum Terminals {leftBrace, rightBrace, SOP, openParen, closeParen, semicolon, ifStatement, elseStatement, whileStatement, trueStatement, falseStatement, exclamation}
	public static void main(String [ ] args) {
		ArrayList<Terminals> terms = lex("");
		if(terms.isEmpty())
		{
			System.out.println("Error");
		}
		else
			for(Terminals t : terms)
			{
				System.out.println(t.name());
			}
		return;
	}
	public static ArrayList<Terminals> lex (String program) { //returns empty arraylist if error

		ArrayList<Terminals> tList = new ArrayList<Terminals>();
		//Scanner s = new Scanner(program); //scanner removes whitespace, making lexing a lot easier
		Scanner s = new Scanner(System.in); //POSSIBLE TESTING LINE THAT TAKES IN STDIN DIRECTLY
		while(s.hasNext()) {
			
			String line = s.next(); //now we scan this line
			Boolean hadletters = false; //lettered terminals cannot be next to each other, this makes sure this does not happen
			while(!line.isEmpty()) { //whole line analysis
				
				if(!Character.isLetter(line.charAt(0))) {
						switch(line.charAt(0)) {//easy cases first
						case '{':
							tList.add(Terminals.leftBrace);
							break;
						case '}':
							tList.add(Terminals.rightBrace);
							break;
						case '(':
							tList.add(Terminals.openParen);
							break;
						case ')':
							tList.add(Terminals.closeParen);
							break;
						case ';':
							tList.add(Terminals.semicolon);
							break;
						case '!':
							tList.add(Terminals.exclamation);
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
							tList.add(Terminals.ifStatement);
							line = line.substring("if".length());
						}
						else if(line.startsWith("else")) {
							tList.add(Terminals.elseStatement);
							line = line.substring("else".length());
						}
						else if(line.startsWith("while")) {
							tList.add(Terminals.whileStatement);
							line = line.substring("while".length());
						}
						else if(line.startsWith("true")) {
							tList.add(Terminals.trueStatement);
							line = line.substring("true".length());
						}
						else if(line.startsWith("false")) {
							tList.add(Terminals.falseStatement);
							line = line.substring("false".length());
						}
						else if(line.startsWith("System.out.println")) {
							tList.add(Terminals.SOP);
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

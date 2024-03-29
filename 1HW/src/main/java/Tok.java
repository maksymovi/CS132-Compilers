
//Making my own enum-like class for simplicity for tokens, in hindsight this is not simple but eh.

//Apparently I cant name this class Token.java so I have to do Tok instead
public abstract class Tok {

	public enum Type {Terminal, Nonterminal, Nullable}
	
	//private static int counter = 0; //global counter for enum discrimination purposes

	//private final int value;
	/*
	public Tok() {
		value = counter;
		counter++;
	}
	*/
	private static int count = 0;
	public final int val;
	private Tok() {
		val = count;
		count++;
	}

	
	
	public abstract Type type();
	
	public static class T extends Tok {
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
	public static class N extends Tok {
		public Type type() {
			return Type.Nonterminal;
		}
		public final static N S = new N();
		public final static N L = new N();
		public final static N E = new N();
	}
	public static class Nullable extends Tok {
		public Type type() {
			return Type.Nullable;
		}
		public final static Nullable nullable = new Nullable();
	}

}

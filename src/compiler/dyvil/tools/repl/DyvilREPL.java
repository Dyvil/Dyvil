package dyvil.tools.repl;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.parser.expression.ExpressionParser;

public class DyvilREPL
{
	public static final String		VERSION	= "1.0.0";
	
	private static BufferedReader	reader;
	private static REPLContext		context	= new REPLContext();
	private static REPLParser		parser	= new REPLParser();
	protected static String			currentCode;
	
	public static void main(String[] args) throws Exception
	{
		System.out.println("Dyvil REPL " + VERSION);
		
		Library.javaLibrary.loadLibrary();
		Library.dyvilLibrary.loadLibrary();
		Package.init();
		Types.init();
		
		reader = new BufferedReader(new InputStreamReader(System.in));
		
		do
		{
			loop();
		}
		while (currentCode != null);
	}
	
	public static synchronized void loop()
	{
		System.out.print("> ");
		
		try
		{
			currentCode = reader.readLine();
			TokenIterator tokens = Dlex.tokenIterator(currentCode + ";");
			if (parser.parse(tokens, new ExpressionParser(context)))
			{
				context.processValue();
				return;
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}

package dyvil.tools.repl;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.parser.expression.ExpressionParser;

public class DyvilREPL
{
	public static final String	VERSION	= "1.0.0";
	
	private static REPLContext	context	= new REPLContext();
	private static REPLParser	parser	= new REPLParser();
	protected static String		currentCode;
	
	public static void main(String[] args) throws Exception
	{
		System.out.println("Dyvil REPL " + VERSION);
		
		Library.javaLibrary.loadLibrary();
		Library.dyvilLibrary.loadLibrary();
		Package.init();
		Type.init();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		
		do
		{
			System.out.print("> ");
			try
			{
				line = reader.readLine();
				process(line);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		while (line != null);
	}
	
	public static void process(String text)
	{
		currentCode = text;
		TokenIterator tokens = Dlex.tokenIterator(text);
		if (parser.parse(tokens, new ExpressionParser(context)))
		{
			context.processValue();
			return;
		}
	}
}

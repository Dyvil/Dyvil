package dyvil.tools.repl;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.parser.classes.ClassBodyParser;
import dyvil.tools.compiler.parser.classes.DyvilUnitParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.repl.command.ExitCommand;
import dyvil.tools.repl.command.HelpCommand;
import dyvil.tools.repl.command.ICommand;
import dyvil.tools.repl.command.VersionCommand;

public class DyvilREPL
{
	public static final String VERSION = "1.0.0";
	
	private static BufferedReader	reader;
	protected static REPLContext	context	= new REPLContext();
	protected static REPLParser		parser	= new REPLParser();
	protected static String			currentCode;
	
	public static final Map<String, ICommand> commands = new HashMap();
	
	static
	{
		ICommand command = new HelpCommand();
		commands.put("?", command);
		commands.put("help", command);
		commands.put("version", new VersionCommand());
		command = new ExitCommand();
		commands.put("exit", command);
		commands.put("shutdown", command);
	}
	
	public static void main(String[] args) throws Exception
	{
		System.err.println("Dyvil REPL " + VERSION);
		
		Library.javaLibrary.loadLibrary();
		Library.dyvilLibrary.loadLibrary();
		if (Library.dyvilBinLibrary != null)
		{
			Library.dyvilBinLibrary.loadLibrary();
		}
		
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
			if (currentCode.startsWith(":"))
			{
				runCommand(currentCode);
				return;
			}
			
			TokenIterator tokens = Dlex.tokenIterator(currentCode + ";");
			REPLContext.reset();
			
			if (parser.parse(null, tokens, new DyvilUnitParser(context, false)))
			{
				return;
			}
			if (parser.parse(null, tokens, new ClassBodyParser(context)))
			{
				return;
			}
			
			parser.parse(REPLContext.markers, tokens, new ExpressionParser(context));
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	private static void runCommand(String line)
	{
		int index = line.indexOf(' ', 1);
		if (index < 0)
		{
			runCommand(line.substring(1), new String[] {});
			return;
		}
		
		runCommand(line.substring(1, index), line.substring(index + 1).split(" "));
	}

	private static void runCommand(String name, String... arguments)
	{
		ICommand command = commands.get(name);
		if (command == null)
		{
			System.out.println("Unknown Command: " + name);
			return;
		}
		
		command.execute(arguments);
	}

	public static void registerCommand(ICommand command)
	{
		commands.put(command.getName(), command);
	}
}

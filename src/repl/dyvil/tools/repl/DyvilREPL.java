package dyvil.tools.repl;

import dyvil.collection.Map;
import dyvil.collection.mutable.TreeMap;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.parser.classes.ClassBodyParser;
import dyvil.tools.compiler.parser.classes.DyvilUnitParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.transform.SemicolonInference;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.lexer.DyvilLexer;
import dyvil.tools.repl.command.*;

import java.io.*;

public final class DyvilREPL
{
	public static final String VERSION = "$$replVersion$$";
	
	private static DyvilREPL instance;

	private PrintStream    output;
	private PrintStream    errorOutput;
	private BufferedReader input;
	
	protected REPLContext context = new REPLContext(this);
	protected REPLParser  parser  = new REPLParser(this.context);
	
	protected File dumpDir;
	
	private static final Map<String, ICommand> commands = new TreeMap<>();

	private static boolean running;
	
	static
	{
		ICommand command = new HelpCommand();
		commands.put("?", command);
		commands.put("help", command);
		
		command = new ExitCommand();
		commands.put("exit", command);
		commands.put("quit", command);
		commands.put("q", command);

		commands.put("dump", new DumpCommand());
		commands.put("version", new VersionCommand());
		commands.put("debug", new DebugCommand());
		commands.put("variables", new VariablesCommand());
		commands.put("methods", new MethodsCommand());
		
		command = new CompleteCommand();
		commands.put("c", command);
		commands.put("complete", command);

		commands.put("javap", new JavapCommand());
	}
	
	public static void main(String[] args) throws Exception
	{
		running = true;

		instance = new DyvilREPL(System.in, System.out, System.err);
		instance.launch(args);
		
		while (running)
		{
			instance.readAndProcess();
		}
	}
	
	public DyvilREPL(InputStream input, PrintStream output)
	{
		this(input, output, output);
	}

	public DyvilREPL(InputStream input, PrintStream output, PrintStream errorOutput)
	{
		this.output = output;
		this.errorOutput = errorOutput;
		if (input != null)
		{
			this.input = new BufferedReader(new InputStreamReader(input));
		}
	}

	public REPLContext getContext()
	{
		return this.context;
	}

	public REPLParser getParser()
	{
		return this.parser;
	}

	public File getDumpDir()
	{
		return this.dumpDir;
	}

	public void setDumpDir(File dumpDir)
	{
		this.dumpDir = dumpDir;
	}

	public PrintStream getOutput()
	{
		return this.output;
	}

	public PrintStream getErrorOutput()
	{
		return this.errorOutput;
	}

	protected void launch(String[] args)
	{
		this.output.println("Dyvil REPL v" + VERSION + " for Dyvil v" + DyvilCompiler.DYVIL_VERSION);

		Names.init();

		for (String arg : args)
		{
			DyvilCompiler.processArgument(arg);
		}

		if (DyvilCompiler.debug)
		{
			this.output.println("Dyvil Compiler Version: v" + DyvilCompiler.VERSION);
		}

		for (Library library : DyvilCompiler.config.libraries)
		{
			library.loadLibrary();
		}

		long now = System.nanoTime();

		Package.init();
		Types.initHeaders();
		Types.initTypes();

		if (DyvilCompiler.debug)
		{
			this.output.println("Loaded REPL (" + Util.toTime(System.nanoTime() - now) + ")");
		}
	}
	
	public String readInput() throws IOException
	{
		StringBuilder buffer = new StringBuilder();
		int depth1 = 0;
		int depth2 = 0;
		int depth3 = 0;
		byte mode = 0;
		
		while (true)
		{
			String s = this.input.readLine();
			
			if (s == null)
			{
				if (buffer.length() > 0)
				{
					continue;
				}
				
				exit();
				return null;
			}

			int len = s.length();
			
			outer:
			for (int i = 0; i < len; i++)
			{
				char c = s.charAt(i);
				
				buffer.append(c);
				
				switch (c)
				{
				case '"':
					if (mode == 0 || mode == 2)
					{
						mode ^= 2;
					}
					break;
				case '\'':
					if (mode == 0 || mode == 4)
					{
						mode ^= 4;
					}
					break;
				case '\\':
					if (mode >= 2)
					{
						mode |= 1;
					}
					continue outer;
				case '{':
					if (mode == 0)
					{
						depth1++;
					}
					break;
				case '}':
					if (mode == 0)
					{
						depth1--;
					}
					break;
				case '(':
					if (mode == 0)
					{
						depth2++;
					}
					break;
				case ')':
					if (mode == 0)
					{
						depth2--;
					}
					break;
				case '[':
					if (mode == 0)
					{
						depth3++;
					}
					break;
				case ']':
					if (mode == 0)
					{
						depth3--;
					}
					break;
				}
				
				mode &= ~1;
			}
			
			buffer.append('\n');
			if (mode == 0 && depth1 + depth2 + depth3 <= 0)
			{
				break;
			}
			
			this.printIndent(depth1);
		}
		
		return buffer.toString();
	}

	private void printIndent(int indent)
	{
		this.output.print("  ");
		for (int j = 0; j < indent; j++)
		{
			this.output.print("    ");
		}
	}
	
	private static void exit()
	{
		running = false;
	}

	public void readAndProcess()
	{
		this.output.print("> ");

		String currentCode;
		try
		{
			currentCode = this.readInput();
			if (currentCode == null)
			{
				return;
			}
		}
		catch (IOException ignored)
		{
			return;
		}

		this.processInput(currentCode);

		// Wait to make sure the output isn't messed up in IDE consoles.
		try
		{
			Thread.sleep(4L);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace(this.output);
		}
	}
	
	public void processInput(String input)
	{
		try
		{
			String trim = input.trim();
			if (trim.length() > 1 && trim.charAt(0) == ':' && trim.charAt(1) != ':')
			{
				this.runCommand(trim);
				return;
			}
			
			this.evaluate(input);
		}
		catch (Throwable t)
		{
			t.printStackTrace(this.output);
		}
	}
	
	public void evaluate(String code)
	{
		this.context.startEvaluation(code);
		
		TokenIterator tokens = new DyvilLexer(this.context.markers, DyvilSymbols.INSTANCE).tokenize(code);
		SemicolonInference.inferSemicolons(tokens.first());
		
		if (this.parser.parse(null, tokens, new DyvilUnitParser(this.context, false)))
		{
			this.context.reportErrors();
			return;
		}
		if (this.parser.parse(null, tokens, new ClassBodyParser(this.context)))
		{
			this.context.reportErrors();
			return;
		}

		this.parser.parse(this.context.markers, tokens, new ExpressionParser(this.context));
		this.context.reportErrors();
	}
	
	private void runCommand(String line)
	{
		int index = line.indexOf(' ', 1);
		if (index < 0)
		{
			this.runCommand(line.substring(1), new String[] {});
			return;
		}
		
		this.runCommand(line.substring(1, index), line.substring(index + 1).split(" "));
	}
	
	public void runCommand(String name, String... arguments)
	{
		ICommand command = commands.get(name);
		if (command == null)
		{
			this.errorOutput.println("Unknown Command: " + name);
			return;
		}
		
		command.execute(instance, arguments);
	}
	
	public static void registerCommand(ICommand command)
	{
		commands.put(command.getName(), command);
	}
	
	public static Map<String, ICommand> getCommands()
	{
		return commands;
	}
}

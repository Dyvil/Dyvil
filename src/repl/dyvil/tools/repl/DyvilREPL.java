package dyvil.tools.repl;

import dyvil.collection.Map;
import dyvil.collection.mutable.TreeMap;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.parser.classes.ClassBodyParser;
import dyvil.tools.compiler.parser.classes.DyvilUnitParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.transform.SemicolonInference;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.lexer.DyvilLexer;
import dyvil.tools.parsing.lexer.LexerUtil;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.repl.command.*;
import dyvil.tools.repl.context.REPLContext;
import dyvil.tools.repl.input.REPLParser;

import java.io.File;
import java.io.PrintStream;

public final class DyvilREPL
{
	public static final String VERSION = "$$replVersion$$";
	
	protected DyvilCompiler compiler = new DyvilCompiler();
	
	protected REPLContext context = new REPLContext(this);
	protected REPLParser  parser  = new REPLParser(this.context);
	
	protected File dumpDir;
	
	private static final Map<String, ICommand> commands = new TreeMap<>();

	static
	{
		registerCommand(new CompleteCommand());
		registerCommand(new DebugCommand());
		registerCommand(new DumpCommand());
		registerCommand(new ExitCommand());
		registerCommand(new HelpCommand());
		registerCommand(new JavapCommand());
		registerCommand(new LibraryCommand());
		registerCommand(new MethodsCommand());
		registerCommand(new RenameCommand());
		registerCommand(new VariablesCommand());
		registerCommand(new VersionCommand());
	}
	
	public DyvilREPL(PrintStream output)
	{
		this(output, output);
	}

	public DyvilREPL(PrintStream output, PrintStream errorOutput)
	{
		this.compiler.setOutput(output);
		this.compiler.setErrorOutput(errorOutput);
	}

	public DyvilCompiler getCompiler()
	{
		return this.compiler;
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
		return this.compiler.getOutput();
	}

	public PrintStream getErrorOutput()
	{
		return this.compiler.getErrorOutput();
	}

	protected void launch(String[] args)
	{
		this.compiler.log("Dyvil REPL v" + VERSION + " for Dyvil v" + DyvilCompiler.DYVIL_VERSION);

		Names.init();

		this.compiler.processArguments(args);

		if (this.compiler.config.isDebug())
		{
			this.compiler.log("Dyvil Compiler Version: v" + DyvilCompiler.VERSION);
		}

		final long startTime = System.nanoTime();

		this.compiler.loadLibraries();

		Package.init();
		Types.initHeaders();
		Types.initTypes();

		this.compiler.checkLibraries();

		if (this.compiler.config.isDebug())
		{
			final long endTime = System.nanoTime();

			this.compiler.log("Loaded REPL (" + Util.toTime(endTime - startTime) + ")");
		}
	}

	public void shutdown()
	{
		this.compiler.shutdown();
	}
	
	public void processInput(String input)
	{
		try
		{
			final String trim = input.trim();
			if (trim.length() > 1 // not a single colon
					&& trim.charAt(0) == ':' // first character must be a colon
					&& LexerUtil.isIdentifierPart(trim.charAt(1))) // next character must be a letter
			{
				this.runCommand(trim);
				return;
			}
			
			this.evaluate(input);
		}
		catch (Throwable t)
		{
			t.printStackTrace(this.compiler.getErrorOutput());
		}
	}
	
	public void evaluate(String code)
	{
		this.context.startEvaluation(code);

		final MarkerList markers = this.context.getMarkers();
		final TokenIterator tokens = new DyvilLexer(markers, DyvilSymbols.INSTANCE).tokenize(code);

		SemicolonInference.inferSemicolons(tokens.first());
		
		if (this.parser.parse(markers, tokens, new DyvilUnitParser(this.context, false), 1))
		// 1 = DyvilHeaderParser.PACKAGE
		{
			this.context.reportErrors();
			return;
		}
		if (this.parser.parse(markers, tokens, new ClassBodyParser(this.context), 2))
		// 2 = ClassBodyParser.NAME
		{
			this.context.reportErrors();
			return;
		}

		this.parser.parse(markers, tokens, new ExpressionParser(this.context), -1);
		this.context.reportErrors();
	}
	
	private void runCommand(String line)
	{
		final int spaceIndex = line.indexOf(' ', 1);
		if (spaceIndex < 0)
		{
			this.runCommand(line.substring(1), new String[] {});
			return;
		}
		
		this.runCommand(line.substring(1, spaceIndex), line.substring(spaceIndex + 1).split(" "));
	}
	
	public void runCommand(String name, String... arguments)
	{
		ICommand command = commands.get(name);
		if (command == null)
		{
			this.compiler.error("Unknown Command: " + name);
			return;
		}
		
		command.execute(this, arguments);
	}
	
	public static void registerCommand(ICommand command)
	{
		commands.put(command.getName(), command);

		String[] aliases = command.getAliases();
		if (aliases != null)
		{
			for (String alias : aliases)
			{
				commands.put(alias, command);
			}
		}
	}
	
	public static Map<String, ICommand> getCommands()
	{
		return commands;
	}
}

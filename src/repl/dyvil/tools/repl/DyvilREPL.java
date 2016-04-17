package dyvil.tools.repl;

import dyvil.collection.Map;
import dyvil.collection.mutable.TreeMap;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.TryParserManager;
import dyvil.tools.compiler.parser.header.DyvilHeaderParser;
import dyvil.tools.compiler.parser.classes.MemberParser;
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

import java.io.File;
import java.io.PrintStream;

import static dyvil.tools.compiler.parser.classes.MemberParser.NO_UNINITIALIZED_VARIABLES;
import static dyvil.tools.compiler.parser.classes.MemberParser.OPERATOR_ERROR;

public final class DyvilREPL
{
	public static final String VERSION = "$$replVersion$$";

	protected DyvilCompiler compiler = new DyvilCompiler();

	protected REPLContext      context = new REPLContext(this);
	protected TryParserManager parser  = new TryParserManager(DyvilSymbols.INSTANCE);

	protected File    dumpDir;

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

	public TryParserManager getParser()
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

		this.processArguments(args);

		if (this.compiler.config.isDebug())
		{
			this.compiler.log("Dyvil Compiler Version: v" + DyvilCompiler.VERSION);
		}

		final long startTime = System.nanoTime();

		this.compiler.loadLibraries();

		Package.init();
		Types.initHeaders();

		this.compiler.checkLibraries();

		Types.initTypes();

		if (this.compiler.config.isDebug())
		{
			final long endTime = System.nanoTime();

			this.compiler.log("Loaded REPL (" + Util.toTime(endTime - startTime) + ")");
		}
	}

	private void processArguments(String[] args)
	{
		for (String arg : args)
		{
			if (!this.processArgument(arg))
			{
				this.compiler.processArgument(arg);
			}
		}
	}

	private boolean processArgument(String arg)
	{
		if (arg.startsWith("dumpDir"))
		{
			this.dumpDir = new File(arg.substring(arg.indexOf('=') + 1).trim());
			return true;
		}

		return false;
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

		if (this.tryParse(markers, tokens, new DyvilHeaderParser(this.context), false))
		{
			this.context.reportErrors();
			return;
		}

		if (this.tryParse(markers, tokens,
		                  new MemberParser<>(this.context).withFlag(NO_UNINITIALIZED_VARIABLES | OPERATOR_ERROR),
		                  false))
		{
			this.context.reportErrors();
			return;
		}

		this.tryParse(markers, tokens, new ExpressionParser(this.context), true);
		this.context.reportErrors();
	}

	private boolean tryParse(MarkerList markers, TokenIterator tokens, Parser parser, boolean reportErrors)
	{
		this.parser.reset(markers, tokens);
		this.parser.resetTo(tokens.first());
		return this.parser.parse(parser, reportErrors);
	}

	private void runCommand(String line)
	{
		final int spaceIndex = line.indexOf(' ', 1);
		if (spaceIndex < 0)
		{
			this.runCommand(line.substring(1), null);
			return;
		}

		this.runCommand(line.substring(1, spaceIndex), line.substring(spaceIndex + 1));
	}

	public void runCommand(String name, String argument)
	{
		ICommand command = commands.get(name);
		if (command == null)
		{
			this.compiler.error("Unknown Command: " + name);
			return;
		}

		command.execute(this, argument);
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

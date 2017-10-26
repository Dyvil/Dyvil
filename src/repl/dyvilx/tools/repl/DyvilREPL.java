package dyvilx.tools.repl;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.mutable.TreeMap;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.SemicolonInference;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.ParserManager;
import dyvilx.tools.parsing.TokenList;
import dyvilx.tools.parsing.lexer.CharacterTypes;
import dyvilx.tools.parsing.lexer.DyvilLexer;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.repl.command.*;
import dyvilx.tools.repl.context.REPLClassLoader;
import dyvilx.tools.repl.context.REPLContext;
import dyvilx.tools.repl.input.InputManager;
import dyvilx.tools.repl.input.REPLParser;
import dyvilx.tools.repl.lang.I18n;

import java.io.*;

public final class DyvilREPL
{
	public static final String VERSION = "$$replVersion$$";

	protected DyvilCompiler compiler = new DyvilCompiler();

	protected REPLContext     context     = new REPLContext(this);
	protected REPLParser      parser      = new REPLParser(this.context);
	protected REPLClassLoader classLoader = new REPLClassLoader(this);

	protected File dumpDir;
	private List<File> autoLoadFiles = new ArrayList<>();

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

	public REPLClassLoader getClassLoader()
	{
		return this.classLoader;
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
		this.compiler.log(I18n.get("repl.init", VERSION, DyvilCompiler.DYVIL_VERSION));

		Names.init();

		this.baseInit(args);

		if (this.compiler.config.isDebug())
		{
			this.compiler.log(I18n.get("repl.compiler", DyvilCompiler.VERSION));
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

			this.compiler.log(I18n.get("repl.loaded", Util.toTime(endTime - startTime)));
		}

		for (File file : this.autoLoadFiles)
		{
			this.processFile(file);
		}
	}

	private void baseInit(String[] args)
	{
		this.compiler.loadConfig(args);

		for (String arg : args)
		{
			this.processArgument(arg);
		}
	}

	private void processArgument(String arg)
	{
		if (arg.startsWith("dumpDir="))
		{
			this.dumpDir = new File(arg.substring(8).trim());
			return;
		}
		if (arg.startsWith("load="))
		{
			this.autoLoadFiles.add(new File(arg.substring(5).trim()));
			return;
		}

		this.compiler.processArgument(arg);
	}

	public void shutdown()
	{
		this.compiler.shutdown();
	}

	public void processFile(File file)
	{
		try (InputStream fileInput = new FileInputStream(file))
		{
			InputManager input = new InputManager(null, fileInput);
			String line;
			while ((line = input.readInput()) != null)
			{
				this.processInput(line);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace(this.getErrorOutput());
		}
	}

	public void processInput(String input)
	{
		try
		{
			final String trim = input.trim();
			if (trim.length() > 1 // not a single colon
			    && trim.charAt(0) == ':' // first character must be a colon
			    && CharacterTypes.isIdentifierPart(trim.charAt(1))) // next character must be a letter
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
		final TokenList tokens = new DyvilLexer(markers, DyvilSymbols.INSTANCE).tokenize(code);

		SemicolonInference.inferSemicolons(tokens.first());

		this.parser.reset();
		new ParserManager(DyvilSymbols.INSTANCE, tokens.iterator(), markers).parse(this.parser);

		this.context.endEvaluation();
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
			this.compiler.error(I18n.get("command.not_found", name));
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

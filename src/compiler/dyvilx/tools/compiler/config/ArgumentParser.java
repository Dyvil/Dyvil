package dyvilx.tools.compiler.config;

import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.lang.I18n;
import dyvilx.tools.compiler.phase.ICompilerPhase;
import dyvilx.tools.compiler.phase.PrintPhase;
import dyvilx.tools.compiler.util.Util;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;

public final class ArgumentParser
{
	private static final String USAGE  = "dyvilc <options>";

	public static boolean parseArguments(String[] arguments, DyvilCompiler compiler)
	{
		final Options options = new Options();
		compiler.config.addOptions(options);

		final CommandLineParser parser = new DefaultParser();
		final HelpFormatter formatter = new HelpFormatter();
		final CommandLine cmd;

		try
		{
			cmd = parser.parse(options, arguments);
		}
		catch (ParseException e)
		{
			compiler.getOutput().println(e.getMessage());
			final PrintWriter writer = new PrintWriter(compiler.getOutput());
			formatter.printHelp(writer, 80, USAGE, "", options, 2, 1, "");
			writer.flush();
			return false;
		}

		compiler.config.readOptions(cmd);

		for (final String extraArg : cmd.getArgList())
		{
			parseArgument(extraArg, compiler);
		}

		return true;
	}

	private static void parseArgument(String argument, DyvilCompiler compiler)
	{
		CompilerConfig config = compiler.config;
		Set<ICompilerPhase> phases = compiler.phases;

		// - - - - - - - - Simple Arguments - - - - - - - -

		switch (argument)
		{
		case "compile":
			phases.add(ICompilerPhase.TOKENIZE);
			phases.add(ICompilerPhase.PARSE);
			phases.add(ICompilerPhase.RESOLVE_HEADERS);
			phases.add(ICompilerPhase.RESOLVE_TYPES);
			phases.add(ICompilerPhase.RESOLVE);
			phases.add(ICompilerPhase.CHECK_TYPES);
			phases.add(ICompilerPhase.CHECK);
			phases.add(ICompilerPhase.COMPILE);
			phases.add(ICompilerPhase.CLEANUP);
			return;
		case "optimize":
			phases.add(ICompilerPhase.FOLD_CONSTANTS);
			config.setConstantFolding(CompilerConfig.OPTIMIZE_CONSTANT_FOLDING);
			return;
		case "jar":
			phases.add(ICompilerPhase.CLEAN);
			phases.add(ICompilerPhase.JAR);
			return;
		case "format":
			phases.add(ICompilerPhase.TOKENIZE);
			phases.add(ICompilerPhase.PARSE);
			phases.add(ICompilerPhase.FORMAT);
			return;
		case "clean":
			phases.add(ICompilerPhase.CLEAN);
			return;
		case "print":
			phases.add(ICompilerPhase.PRINT);
			return;
		case "test":
			phases.add(ICompilerPhase.TEST);
			return;
		}

		// - - - - - - - - Argument File - - - - - - - -

		if (argument.startsWith("@"))
		{
			loadConfigFile(argument.substring(1), compiler);
			return;
		}

		// - - - - - - - - Print - - - - - - - -

		if (argument.startsWith("print:"))
		{
			final String phase = argument.substring(6);
			compiler.warn(I18n.get("argument.print.deprecated", phase));

			for (ICompilerPhase compilerPhase : phases)
			{
				if (compilerPhase.getName().equalsIgnoreCase(phase))
				{
					phases.add(new PrintPhase(compilerPhase));
					return;
				}
			}

			compiler.warn(I18n.get("argument.print.phase", argument, phase));
			return;
		}

		// - - - - - - - - Properties - - - - - - - -

		// TODO handle source files
		compiler.warn(I18n.get("argument.invalid", argument));
	}

	private static void loadConfigFile(String source, DyvilCompiler compiler)
	{
		final File file = new File(source);
		if (!file.exists())
		{
			compiler.error(I18n.get("config.not_found", source));
			return;
		}

		try
		{
			final long startTime = System.nanoTime();

			Files.lines(file.toPath(), StandardCharsets.UTF_8).forEach(line -> parseArgument(line, compiler));

			if (compiler.config.isDebug())
			{
				compiler.log(I18n.get("config.loaded", source, Util.toTime(System.nanoTime() - startTime)));
			}
		}
		catch (IOException ex)
		{
			compiler.error(I18n.get("config.error", source), ex);
		}
	}
}

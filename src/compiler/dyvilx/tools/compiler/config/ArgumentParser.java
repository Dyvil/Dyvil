package dyvilx.tools.compiler.config;

import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.lang.I18n;
import dyvilx.tools.compiler.phase.ICompilerPhase;
import dyvilx.tools.compiler.phase.PrintPhase;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerStyle;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;

public final class ArgumentParser
{

	public static void parseArgument(String argument, DyvilCompiler compiler)
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
		case "--debug":
			phases.add(ICompilerPhase.PRINT); // print after parse
			phases.add(ICompilerPhase.TEST);
			config.setDebug(true);
			return;
		case "--ansi":
			config.setAnsiColors(true);
			return;
		case "-Mg":
		case "-Mgcc":
		case "--gcc-markers":
			config.setMarkerStyle(MarkerStyle.GCC);
			return;
		case "-Mm":
		case "-Mmachine":
		case "--machine-markers":
			config.setMarkerStyle(MarkerStyle.MACHINE);
			return;
		}

		// - - - - - - - - Optimization Level - - - - - - - -

		if (argument.startsWith("-o"))
		{
			final String level = argument.substring(2);
			try
			{
				phases.add(ICompilerPhase.FOLD_CONSTANTS);
				config.setConstantFolding(Integer.parseInt(level));
			}
			catch (Exception ignored)
			{
				compiler.warn(I18n.get("argument.optimisation.invalid", level));
			}
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

			for (ICompilerPhase compilerPhase : phases)
			{
				if (compilerPhase.getName().equalsIgnoreCase(phase))
				{
					phases.add(new PrintPhase(compilerPhase));
					return;
				}
			}

			compiler.warn(I18n.get("argument.print.phase", phase));
			return;
		}

		// - - - - - - - - Properties - - - - - - - -

		if (ArgumentParser.readProperty(argument, config))
		{
			return;
		}

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

			final long endTime = System.nanoTime();
			compiler.log(I18n.get("config.loaded", source, Util.toTime(endTime - startTime)));
		}
		catch (IOException ex)
		{
			compiler.error(I18n.get("config.error", source), ex);
		}
	}

	private static boolean readProperty(String arg, CompilerConfig config)
	{
		final int index = arg.indexOf('=');
		if (index <= 1)
		{
			return false;
		}

		final String key;
		final String value = arg.substring(index + 1);

		if (arg.charAt(index - 1) == '+')
		{
			// key+=value
			key = arg.substring(0, index - 1);
			return config.addProperty(key, value);
		}
		else
		{
			// key=value
			key = arg.substring(0, index);
			return config.setProperty(key, value);
		}
	}
}

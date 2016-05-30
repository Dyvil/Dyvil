package dyvil.tools.compiler;

import dyvil.io.Console;
import dyvil.tools.compiler.lang.I18n;
import dyvil.tools.compiler.util.Util;

public final class Main
{
	public static void main(String[] args)
	{
		final long startTime = System.nanoTime();

		System.out.println(I18n.get("compiler.init", DyvilCompiler.VERSION, DyvilCompiler.DYVIL_VERSION));
		System.out.println();

		final DyvilCompiler compiler = new DyvilCompiler();
		final int exitCode = compiler.run(System.in, System.out, System.err, args);

		final long endTime = System.nanoTime();
		final boolean colors = compiler.config.useAnsiColors();

		final StringBuilder builder = new StringBuilder();

		if (exitCode != 0)
		{
			if (colors)
			{
				builder.append(Console.ANSI_RED);
			}
			builder.append(I18n.get("compilation.failure"));
		}
		else
		{
			if (colors)
			{
				builder.append(Console.ANSI_GREEN);
			}
			builder.append(I18n.get("compilation.success"));
		}
		if (colors)
		{
			builder.append(Console.ANSI_RESET);
		}

		builder.append(" (").append(Util.toTime(endTime - startTime)).append(')');

		System.out.println(builder);
		System.exit(exitCode);
	}
}

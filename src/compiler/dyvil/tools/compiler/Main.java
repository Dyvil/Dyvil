package dyvil.tools.compiler;

import dyvil.tools.compiler.util.Util;

public final class Main
{
	public static void main(String[] args)
	{
		final long startTime = System.nanoTime();

		System.out.println("Dyvil Compiler v" + DyvilCompiler.VERSION + " for Dyvil v" + DyvilCompiler.VERSION);
		System.out.println();

		final DyvilCompiler compiler = new DyvilCompiler();
		final int exitCode = compiler.run(System.in, System.out, System.err, args);

		final long endTime = System.nanoTime();

		if (exitCode != 0)
		{
			System.err.println("Compilation FAILED (" + Util.toTime(endTime - startTime) + ")");
		}
		else
		{
			System.out.println("Compilation completed (" + Util.toTime(endTime - startTime) + ")");
		}

		System.exit(exitCode);
	}
}

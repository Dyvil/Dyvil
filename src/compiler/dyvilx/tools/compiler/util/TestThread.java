package dyvilx.tools.compiler.util;

import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.lang.I18n;
import dyvilx.tools.compiler.library.Library;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TestThread extends Thread
{
	private final DyvilCompiler compiler;

	public TestThread(DyvilCompiler compiler)
	{
		this.compiler = compiler;
	}

	@Override
	public void run()
	{
		final ProcessBuilder pb = new ProcessBuilder(this.getCommand());
		pb.directory(this.compiler.config.getTestDir());
		pb.inheritIO();

		try
		{
			final long start = System.nanoTime();
			Process p = pb.start();
			p.waitFor();

			final long end = System.nanoTime();
			final int exitCode = p.exitValue();

			this.compiler.log(I18n.get("test.completed", exitCode, Util.toTime(end - start)));
			if (exitCode != 0)
			{
				this.compiler.fail();
			}
		}
		catch (InterruptedException | IOException e)
		{
			e.printStackTrace();
		}
	}

	private void failTest(String mainType, String[] args, Throwable ex)
	{
		final String message =
			"Test Failed\n\n" + "Main Type: " + mainType + '\n' + "Main Args: " + Arrays.toString(args)
			+ "\n\n----- ERROR -----\n";
		this.compiler.error(message, ex);
	}

	public String[] getCommand()
	{
		final List<Library> libraries = this.compiler.config.libraries;

		StringBuilder classpath = new StringBuilder();
		classpath.append(this.compiler.config.getOutputDir());

		for (Library library : libraries)
		{
			classpath.append(':').append(library.getFile());
		}

		final List<String> command = new ArrayList<>();
		command.add("java");
		command.add("-cp");
		command.add(classpath.toString());
		command.add(this.compiler.config.getMainType());
		command.addAll(this.compiler.config.getMainArgs());
		return command.toArray(new String[0]);
	}
}

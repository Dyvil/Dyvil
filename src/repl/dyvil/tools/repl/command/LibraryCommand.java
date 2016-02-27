package dyvil.tools.repl.command;

import dyvil.tools.compiler.library.Library;
import dyvil.tools.repl.DyvilREPL;

import java.io.File;
import java.io.FileNotFoundException;

public class LibraryCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "library";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "lib", "loadlibrary", "cp" };
	}

	@Override
	public String getDescription()
	{
		return "Makes an external Jar File or Folder available on the Classpath";
	}

	@Override
	public String getUsage()
	{
		return ":<library|lib|loadlibrary|cp> <path>";
	}

	@Override
	public void execute(DyvilREPL repl, String argument)
	{
		if (argument == null)
		{
			repl.getErrorOutput().println("Missing Path Argument. Usage: " + this.getUsage());
			return;
		}

		final File path = new File(argument);
		try
		{
			final Library library = Library.load(path);
			library.loadLibrary();
			repl.getCompiler().config.addLibrary(library);

			repl.getOutput().println("Successfully added " + library + " to Classpath");
		}
		catch (FileNotFoundException ex)
		{
			repl.getErrorOutput().println(ex);
		}
	}
}

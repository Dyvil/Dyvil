package dyvilx.tools.repl.command;

import dyvilx.tools.compiler.library.Library;
import dyvilx.tools.repl.DyvilREPL;
import dyvilx.tools.repl.lang.I18n;

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
	public String getUsage()
	{
		return ":<library|lib|loadlibrary|cp> <path>";
	}

	@Override
	public void execute(DyvilREPL repl, String argument)
	{
		if (argument == null)
		{
			repl.getErrorOutput().println(I18n.get("command.library.missing", this.getUsage()));
			return;
		}

		final File path = new File(argument);
		try
		{
			final Library library = Library.load(path);
			library.loadLibrary();
			repl.getCompiler().config.addLibrary(library);

			repl.getOutput().println(I18n.get("command.library.success", library));
		}
		catch (FileNotFoundException ex)
		{
			repl.getErrorOutput().println(ex);
		}
	}
}

package dyvilx.tools.repl.command;

import dyvilx.tools.compiler.library.Library;
import dyvilx.tools.repl.DyvilREPL;
import dyvilx.tools.repl.lang.I18n;

import java.io.File;

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

		final Library library = Library.load(path);
		if (library == null)
		{
			repl.getOutput().println(I18n.get("command.library.not_found", path));
			return;
		}

		library.loadLibrary();
		repl.getCompiler().config.libraries.add(library);

		repl.getOutput().println(I18n.get("command.library.success", library));
	}
}

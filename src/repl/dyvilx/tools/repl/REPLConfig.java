package dyvilx.tools.repl;

import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.config.CompilerConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class REPLConfig extends CompilerConfig
{
	// =============== Fields ===============

	private       File       dumpDir;

	private final List<File> autoLoadFiles = new ArrayList<>();

	// =============== Constructors ===============

	public REPLConfig(DyvilCompiler compiler)
	{
		super(compiler);
	}

	// =============== Properties ===============

	public File getDumpDir()
	{
		return this.dumpDir;
	}

	public void setDumpDir(File dumpDir)
	{
		this.dumpDir = dumpDir;
	}

	public List<File> getAutoLoadFiles()
	{
		return this.autoLoadFiles;
	}

	// =============== Methods ===============

	@Override
	public boolean setProperty(String name, String value)
	{
		switch (name)
		{
		case "dump_dir":
			this.setDumpDir(new File(value));
			return true;
		case "load":
			this.autoLoadFiles.clear();
			this.autoLoadFiles.add(new File(value));
			return true;
		}

		return super.setProperty(name, value);
	}

	@Override
	public boolean addProperty(String name, String value)
	{
		if ("load".equals(name))
		{
			this.autoLoadFiles.add(new File(value));
		}

		return super.addProperty(name, value);
	}
}

package dyvil.tools.compiler;

import dyvil.collection.List;
import dyvil.collection.Set;
import dyvil.collection.mutable.TreeSet;
import dyvil.io.FileUtils;
import dyvil.tools.compiler.ast.external.ExternalHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.compiler.config.ConfigParser;
import dyvil.tools.compiler.lang.I18n;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.phase.ICompilerPhase;
import dyvil.tools.compiler.phase.PrintPhase;
import dyvil.tools.compiler.sources.DyvilFileType;
import dyvil.tools.compiler.sources.FileFinder;
import dyvil.tools.compiler.util.TestThread;
import dyvil.tools.compiler.util.Util;
import dyvilx.tools.BasicTool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class DyvilCompiler extends BasicTool
{
	public static final String VERSION         = "$$compilerVersion$$";
	public static final String DYVIL_VERSION   = "$$version$$";
	public static final String LIBRARY_VERSION = "$$libraryVersion$$";

	public static final int OPTIMIZE_CONSTANT_FOLDING = 5;

	private final Set<ICompilerPhase> phases     = new TreeSet<>();
	public final  CompilerConfig      config     = new CompilerConfig(this);
	public final  FileFinder          fileFinder = new FileFinder();

	@Override
	public int run(InputStream in, OutputStream out, OutputStream err, String... arguments)
	{
		this.initOutput(out, err);

		if (!this.baseInit(arguments))
		{
			this.shutdown();
			return 1;
		}

		this.loadLibraries();
		this.findFiles();

		if (!this.applyPhases())
		{
			this.shutdown();
			return 1;
		}

		this.shutdown();
		return this.getExitCode();
	}

	public boolean baseInit(String[] args)
	{
		this.loadConfig(args);

		// Sets up States from arguments
		this.processArguments(args);

		final List<File> sourceDirs = this.config.sourceDirs;
		if (sourceDirs.isEmpty())
		{
			this.log(I18n.get("config.source_path.missing"));
			return false;
		}

		for (File file : sourceDirs)
		{
			if (file.exists())
			{
				return true;
			}
		}

		this.log(I18n.get("config.source_path.not_found", sourceDirs));
		return false;
	}

	public void loadConfig(String[] args)
	{
		for (String arg : args)
		{
			if (arg.charAt(0) == '@')
			{
				this.loadConfigFile(arg.substring(1));
			}
		}
	}

	private void loadConfigFile(String source)
	{
		final File file = new File(source);
		this.config.setConfigFile(file);
		if (!file.exists())
		{
			this.error(I18n.get("config.not_found", source));
			return;
		}

		try
		{
			final long startTime = System.nanoTime();

			final String code = FileUtils.read(file);
			ConfigParser.parse(code, this.config);

			final long endTime = System.nanoTime();
			this.log(I18n.get("config.loaded", source, Util.toTime(endTime - startTime)));
		}
		catch (IOException ex)
		{
			this.error(I18n.get("config.error", source), ex);
		}
	}

	public void processArguments(String[] args)
	{
		for (String arg : args)
		{
			this.processArgument(arg);
		}
	}

	public void processArgument(String arg)
	{
		switch (arg)
		{
		case "compile":
			this.phases.add(ICompilerPhase.TOKENIZE);
			this.phases.add(ICompilerPhase.PARSE);
			this.phases.add(ICompilerPhase.RESOLVE_HEADERS);
			this.phases.add(ICompilerPhase.RESOLVE_TYPES);
			this.phases.add(ICompilerPhase.RESOLVE);
			this.phases.add(ICompilerPhase.CHECK_TYPES);
			this.phases.add(ICompilerPhase.CHECK);
			this.phases.add(ICompilerPhase.COMPILE);
			this.phases.add(ICompilerPhase.CLEANUP);
			return;
		case "optimize":
			this.phases.add(ICompilerPhase.FOLD_CONSTANTS);
			this.config.setConstantFolding(OPTIMIZE_CONSTANT_FOLDING);
			return;
		case "jar":
			this.phases.add(ICompilerPhase.CLEAN);
			this.phases.add(ICompilerPhase.JAR);
			return;
		case "format":
			this.phases.add(ICompilerPhase.TOKENIZE);
			this.phases.add(ICompilerPhase.PARSE);
			this.phases.add(ICompilerPhase.FORMAT);
			return;
		case "clean":
			this.phases.add(ICompilerPhase.CLEAN);
			return;
		case "print":
			this.phases.add(ICompilerPhase.PRINT);
			return;
		case "test":
			this.phases.add(ICompilerPhase.TEST);
			return;
		case "--debug":
			this.phases.add(ICompilerPhase.PRINT); // print after parse
			this.phases.add(ICompilerPhase.TEST);
			this.config.setDebug(true);
			return;
		case "--ansi":
			this.config.setAnsiColors(true);
			return;
		}

		if (arg.startsWith("-o"))
		{
			try
			{
				this.phases.add(ICompilerPhase.FOLD_CONSTANTS);
				this.config.setConstantFolding(Integer.parseInt(arg.substring(2)));
				return;
			}
			catch (Exception ignored)
			{
			}
		}
		if (arg.charAt(0) == '@')
		{
			return;
		}
		if (arg.startsWith("print["))
		{
			final int index = arg.lastIndexOf(']');
			final String phase = arg.substring(6, index);

			for (ICompilerPhase compilerPhase : this.phases)
			{
				if (compilerPhase.getName().equalsIgnoreCase(phase))
				{
					this.phases.add(new PrintPhase(compilerPhase));
					return;
				}
			}
		}

		if (!ConfigParser.readProperty(this.config, arg))
		{
			this.warn(I18n.get("argument.invalid", arg));
		}
	}

	public void loadLibraries()
	{
		final List<Library> libraries = this.config.libraries;

		// Make sure to add the dyvil and java libraries at the end
		libraries.add(Library.dyvilLibrary);
		libraries.add(Library.javaLibrary);

		final int libs = libraries.size();
		final long startTime = System.nanoTime();

		// Loads libraries
		for (Library library : libraries)
		{
			library.loadLibrary();
		}

		Package.initRoot(this);

		final long endTime = System.nanoTime();
		this.log(I18n.get("library.found", libs == 1 ? I18n.get("libraries.1") : I18n.get("libraries.n", libs),
		                  Util.toTime(endTime - startTime)));
	}

	private void findFiles()
	{
		final long startTime = System.nanoTime();

		final List<File> sourceDir = this.config.sourceDirs;
		final File outputDir = this.config.getOutputDir();

		this.log(I18n.get("compilation.init", sourceDir, outputDir));

		// Scan for Packages and Compilation Units
		DyvilFileType.setupFileFinder(this.fileFinder);
		this.config.findUnits(this.fileFinder);

		Package.init();

		final int fileCount = this.fileFinder.files.size();
		final int unitCount = this.fileFinder.units.size();

		final long endTime = System.nanoTime();

		this.log(I18n.get("files.found", fileCount == 1 ? I18n.get("files.1") : I18n.get("files.n", fileCount),
		                  unitCount == 1 ? I18n.get("units.1") : I18n.get("units.n", unitCount),
		                  Util.toTime(endTime - startTime)));
		this.log("");
	}

	public boolean applyPhases()
	{
		final int phases = this.phases.size();

		// Apply states
		if (this.config.isDebug())
		{
			this.log(phases == 1 ?
				         I18n.get("phase.applying.1", this.phases) :
				         I18n.get("phase.applying.n", phases, this.phases));

			for (ICompilerPhase phase : this.phases)
			{
				this.log(I18n.get("phase.applying", phase.getName()));
				if (!this.applyPhaseDebug(phase))
				{
					return false;
				}
			}

			return true;
		}

		for (ICompilerPhase phase : this.phases)
		{
			if (!this.applyPhase(phase))
			{
				return false;
			}
		}

		return true;
	}

	private boolean applyPhase(ICompilerPhase phase)
	{
		try
		{
			phase.apply(this);

			return true;
		}
		catch (Throwable t)
		{
			this.error(phase.getName(), "apply", t);

			return false;
		}
	}

	private boolean applyPhaseDebug(ICompilerPhase phase)
	{
		try
		{
			final long startTime = System.nanoTime();

			phase.apply(this);

			final long endTime = System.nanoTime();
			this.log(I18n.get("phase.completed", phase.getName(), Util.toTime(endTime - startTime)));

			return true;
		}
		catch (Throwable t)
		{
			this.log(I18n.get("phase.failed", phase.getName()));
			this.error(phase.getName(), "apply", t);
			return false;
		}
	}

	public void test()
	{
		String mainType = this.config.getMainType();
		if (mainType == null)
		{
			return;
		}

		File file = new File(this.config.getOutputDir(), this.config.getMainType().replace('.', '/') + ".class");
		if (!file.exists())
		{
			this.log(I18n.get("test.main_type.not_found", this.config.getMainType()));
			return;
		}

		final TestThread testThread = new TestThread(this);
		testThread.start();
		try
		{
			testThread.join();
		}
		catch (InterruptedException ignored)
		{
		}
	}

	public void clean()
	{
		File[] files = this.config.getOutputDir().listFiles();
		if (files == null)
		{
			return;
		}

		for (File file : files)
		{
			FileUtils.delete(file);
		}
	}

	public void checkLibraries()
	{
		if (Library.dyvilLibrary == null)
		{
			this.error(I18n.get("library.dyvil"));
		}
		if (Library.javaLibrary == null)
		{
			this.error(I18n.get("library.java"));
		}

		if (Types.LANG_HEADER == null)
		{
			this.error(I18n.get("library.lang_header", this.config.libraries));

			Types.LANG_HEADER = new ExternalHeader();
		}
	}

	public void shutdown()
	{
		for (Library library : this.config.libraries)
		{
			library.unloadLibrary();
		}
	}

	// ----- GETTERS AND SETTERS -----

	@Override
	protected boolean useAnsiColors()
	{
		return this.config.useAnsiColors();
	}

	public boolean isCompilationFailed()
	{
		return this.getExitCode() != 0;
	}
}

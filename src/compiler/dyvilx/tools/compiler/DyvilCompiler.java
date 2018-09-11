package dyvilx.tools.compiler;

import dyvil.collection.List;
import dyvil.collection.Set;
import dyvil.collection.mutable.TreeSet;
import dyvil.io.Console;
import dyvil.io.FileUtils;
import dyvilx.tools.BasicTool;
import dyvilx.tools.compiler.ast.external.ExternalHeader;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.config.ArgumentParser;
import dyvilx.tools.compiler.config.CompilerConfig;
import dyvilx.tools.compiler.lang.I18n;
import dyvilx.tools.compiler.library.Library;
import dyvilx.tools.compiler.phase.ICompilerPhase;
import dyvilx.tools.compiler.sources.FileFinder;
import dyvilx.tools.compiler.util.TestThread;
import dyvilx.tools.compiler.util.Util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import static dyvilx.tools.compiler.sources.DyvilFileType.*;

public class DyvilCompiler extends BasicTool
{
	public static final String VERSION         = "$$compilerVersion$$";
	public static final String DYVIL_VERSION   = "$$version$$";
	public static final String LIBRARY_VERSION = "$$libraryVersion$$";

	public final Set<ICompilerPhase> phases = new TreeSet<>();
	public final CompilerConfig      config = this.createConfig();

	public final FileFinder fileFinder = new FileFinder();

	protected CompilerConfig createConfig()
	{
		return new CompilerConfig(this);
	}

	@Override
	public int run(InputStream in, OutputStream out, OutputStream err, String... arguments)
	{
		final long startTime = System.nanoTime();

		this.initOutput(out, err);

		this.log(I18n.get("compiler.init", DyvilCompiler.VERSION, DyvilCompiler.DYVIL_VERSION));
		this.log("");

		final int exitCode = this.run(arguments);

		final long endTime = System.nanoTime();
		final boolean colors = this.config.useAnsiColors();

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

		this.log(builder.toString());
		return this.getExitCode();
	}

	private int run(String[] arguments)
	{
		if (!this.baseInit(arguments))
		{
			this.shutdown();
			return -1;
		}

		this.loadLibraries();
		this.findFiles();

		if (!this.applyPhases())
		{
			this.shutdown();
			return -1;
		}

		this.shutdown();
		return this.getExitCode();
	}

	public boolean baseInit(String[] args)
	{
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

	public void processArguments(String[] args)
	{
		for (String arg : args)
		{
			this.processArgument(arg);
		}
	}

	public void processArgument(String arg)
	{
		ArgumentParser.parseArgument(arg, this);
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

	protected void findFiles()
	{
		final long startTime = System.nanoTime();

		final List<File> sourceDirs = this.config.sourceDirs;
		final File outputDir = this.config.getOutputDir();

		this.log(I18n.get("compilation.init", sourceDirs, outputDir));

		// Scan for Packages and Compilation Units
		this.setupFileFinder();

		for (File sourceDir : sourceDirs)
		{
			this.fileFinder.process(this, sourceDir, outputDir, Package.rootPackage);
		}

		Package.init();

		final int fileCount = this.fileFinder.files.size();
		final int unitCount = this.fileFinder.units.size();

		final long endTime = System.nanoTime();

		this.log(I18n.get("files.found", fileCount == 1 ? I18n.get("files.1") : I18n.get("files.n", fileCount),
		                  unitCount == 1 ? I18n.get("units.1") : I18n.get("units.n", unitCount),
		                  Util.toTime(endTime - startTime)));
		this.log("");
	}

	protected void setupFileFinder()
	{
		this.fileFinder.registerFileType(DYVIL_EXTENSION, DYVIL_UNIT);
		this.fileFinder.registerFileType(".dyvil", DYVIL_UNIT); // legacy
		this.fileFinder.registerFileType(HEADER_EXTENSION, DYVIL_HEADER);
		this.fileFinder.registerFileType(".dyvilh", DYVIL_HEADER); // legacy
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
		if (this.config.getMainType() == null)
		{
			this.log(I18n.get("test.skipped"));
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

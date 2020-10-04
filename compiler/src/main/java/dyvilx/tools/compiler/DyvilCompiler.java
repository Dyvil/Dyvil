package dyvilx.tools.compiler;

import dyvil.io.Console;
import dyvil.io.Files;
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
import dyvilx.tools.compiler.util.Util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
		this.initOutput(out, err);
		try
		{
			this.run(arguments);
		}
		finally
		{
			this.shutdown();
		}
		return this.getExitCode();
	}

	private void run(String[] args)
	{
		long startTime = System.nanoTime();

		if (!ArgumentParser.parseArguments(args, this))
		{
			this.fail();
			return;
		}

		if (this.config.isDebug())
		{
			this.log(I18n.get("compiler.init", DyvilCompiler.VERSION, DyvilCompiler.DYVIL_VERSION));
			this.log("");
		}

		final List<File> sourceDirs = this.config.sourceDirs;
		if (sourceDirs.isEmpty())
		{
			this.warn(I18n.get("config.source_path.missing"));
			return;
		}

		if (sourceDirs.stream().noneMatch(File::exists))
		{
			this.warn(I18n.get("config.source_path.not_found", sourceDirs));
			return;
		}

		this.loadLibraries();
		this.findFiles();

		if (this.config.isDebug())
		{
			this.log(I18n.get("compilation.init", sourceDirs, this.config.getOutputDir()));
		}

		if (!this.applyPhases())
		{
			return; // applyPhases prints a message
		}

		if (!this.config.isDebug())
		{
			return;
		}

		final String time = Util.toTime(System.nanoTime() - startTime);
		final String key = this.isCompilationFailed() ? "compilation.failure" : "compilation.success";
		final String message = I18n.get(key, time);

		if (this.config.useAnsiColors())
		{
			final String color = this.isCompilationFailed() ? Console.ANSI_RED : Console.ANSI_GREEN;
			this.log(Console.styled(message, color));
		}
		else
		{
			this.log(message);
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

		if (this.config.isDebug())
		{
			final long endTime = System.nanoTime();
			this.log(I18n.get("library.found", libs == 1 ? I18n.get("libraries.1") : I18n.get("libraries.n", libs),
			                  Util.toTime(endTime - startTime)));
		}
	}

	private void findFiles()
	{
		this.setupFileFinder();

		final File outputDir = this.config.getOutputDir();
		final long startTime = System.nanoTime();

		// Scan for Packages and Compilation Units
		for (File sourceDir : this.config.getSourceDirs())
		{
			this.fileFinder.process(this, sourceDir, outputDir, Package.rootPackage);
		}

		Package.init();

		// sort compilation units by case insensitive absolute path to ensure consistency across filesystems
		this.fileFinder.units.sort(
			Comparator.comparing(unit -> unit.getFileSource().file().getAbsolutePath(), String.CASE_INSENSITIVE_ORDER));

		if (this.config.isDebug())
		{
			final int fileCount = this.fileFinder.files.size();
			final int unitCount = this.fileFinder.units.size();

			final long endTime = System.nanoTime();

			this.log(I18n.get("files.found", fileCount == 1 ? I18n.get("files.1") : I18n.get("files.n", fileCount),
			                  unitCount == 1 ? I18n.get("units.1") : I18n.get("units.n", unitCount),
			                  Util.toTime(endTime - startTime)));
		}
	}

	protected void setupFileFinder()
	{
		this.fileFinder.registerFileType(DYVIL_EXTENSION, DYVIL_UNIT);
		this.fileFinder.registerFileType(".dyvil", DYVIL_UNIT); // legacy
		this.fileFinder.registerFileType(HEADER_EXTENSION, DYVIL_HEADER);
		this.fileFinder.registerFileType(".dyvilh", DYVIL_HEADER); // legacy
	}

	protected boolean applyPhases()
	{
		if (!this.config.isDebug())
		{
			for (ICompilerPhase phase : this.phases)
			{
				try
				{
					phase.apply(this);
				}
				catch (Exception e)
				{
					this.error(I18n.get("phase.failed", phase.getName()), e);
					return false;
				}
			}

			return true;
		}

		final int nPhases = this.phases.size();
		this.log(nPhases == 1 ?
			         I18n.get("phase.applying.1", this.phases) :
			         I18n.get("phase.applying.n", nPhases, this.phases));

		for (ICompilerPhase phase : this.phases)
		{
			this.log(I18n.get("phase.applying", phase.getName()));

			try
			{
				final long startTime = System.nanoTime();

				phase.apply(this);

				final long endTime = System.nanoTime();
				this.log(I18n.get("phase.completed", phase.getName(), Util.toTime(endTime - startTime)));
			}
			catch (Exception e)
			{
				this.error(I18n.get("phase.failed", phase.getName()), e);
				return false;
			}
		}

		return true;
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
			Files.deleteRecursively(file);
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

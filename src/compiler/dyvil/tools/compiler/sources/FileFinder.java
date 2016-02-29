package dyvil.tools.compiler.sources;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.mutable.HashMap;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;

import java.io.File;

public class FileFinder
{
	private final Map<String, IFileType> fileTypes = new HashMap<>();

	public final List<File>             files = new ArrayList<>();
	public final List<ICompilationUnit> units = new ArrayList<>();

	public void registerFileType(String extension, IFileType fileType)
	{
		this.fileTypes.put(extension, fileType);
	}

	public void process(DyvilCompiler compiler, File source, File output, Package pack)
	{
		if (source.isDirectory())
		{
			this.processDirectory(compiler, source, output, pack);
		}
		else
		{
			this.processFile(compiler, source, output, pack);
		}
	}
	
	private void processDirectory(DyvilCompiler compiler, File source, File output, Package pack)
	{
		for (String fileName : source.list())
		{
			final File sourceFile = new File(source, fileName);
			final File outputFile = new File(output, fileName);

			if (sourceFile.isDirectory())
			{
				this.processDirectory(compiler, sourceFile, outputFile, pack.createSubPackage(fileName));
			}
			else
			{
				this.processFile(compiler, sourceFile, outputFile, pack);
			}
		}
	}
	
	private void processFile(DyvilCompiler compiler, File source, File output, Package pack)
	{
		final String fileName = source.getPath();
		if (!compiler.config.isExcluded(fileName))
		{
			return;
		}
		
		this.files.add(output);
		final String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		
		final IFileType fileType = this.fileTypes.get(extension);
		if (fileType == null)
		{
			return; // Skip: Unknown File Type
		}
		
		final ICompilationUnit unit = fileType.createUnit(compiler, pack, source, output);
		if (unit == null)
		{
			return; // Skip: Not a compilation unit
		}
		
		if (unit.isHeader())
		{
			this.units.insert(0, unit);
		}
		else
		{
			this.units.add(unit);
		}
	}
}

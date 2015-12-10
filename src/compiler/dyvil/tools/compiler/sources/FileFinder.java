package dyvil.tools.compiler.sources;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.parsing.CodeFile;

import java.io.File;

public class FileFinder
{
	public final List<File>             files = new ArrayList();
	public final List<ICompilationUnit> units = new ArrayList();
	
	public void process(File source, File output, Package pack)
	{
		if (source.isDirectory())
		{
			this.processDirectory(source, output, pack);
		}
		else
		{
			this.processFile((CodeFile) source, output, pack);
		}
	}
	
	private void processDirectory(File source, File output, Package pack)
	{
		for (String s : source.list())
		{
			CodeFile source1 = new CodeFile(source, s);
			File output1 = new File(output, s);
			if (source1.isDirectory())
			{
				this.processDirectory(source1, output1, pack.createSubPackage(s));
			}
			else
			{
				this.processFile(source1, output1, pack);
			}
		}
	}
	
	private void processFile(CodeFile source, File output, Package pack)
	{
		String fileName = source.getPath();
		if (!DyvilCompiler.config.isExcluded(fileName))
		{
			return;
		}
		
		this.files.add(output);
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		
		IFileType fileType = FileType.fileTypes.get(extension);
		if (fileType == null)
		{
			return; // Skip: Unknown File Type
		}
		
		ICompilationUnit unit = fileType.createUnit(pack, source, output);
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

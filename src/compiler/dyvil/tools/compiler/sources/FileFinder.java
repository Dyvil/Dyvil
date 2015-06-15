package dyvil.tools.compiler.sources;

import java.io.File;

import dyvil.collection.mutable.ArrayList;
import dyvil.lang.List;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.CodeFile;

public class FileFinder
{
	public final List<File>				files	= new ArrayList();
	public final List<ICompilationUnit>	units	= new ArrayList();
	
	public void findUnits(File source, File output, Package pack)
	{
		if (source.isDirectory())
		{
			String name = source.getName();
			for (String s : source.list())
			{
				findUnits(new CodeFile(source, s), new File(output, s), pack == null ? Package.rootPackage : pack.createSubPackage(name));
			}
			return;
		}
		
		String fileName = source.getPath();
		if (!DyvilCompiler.config.compileFile(fileName))
		{
			return;
		}
		
		if (fileName.endsWith("Thumbs.db") || fileName.endsWith(".DS_Store"))
		{
			return;
		}
		
		this.files.add(output);
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		
		IFileType fileType = FileTypes.fileTypes.apply(extension);
		if (fileType == null)
		{
			return; // Skip: Unknown File Type
		}
		
		ICompilationUnit unit = fileType.createUnit(pack, (CodeFile) source, output);
		if (unit == null)
		{
			return; // Skip: Not a compilation unit
		}
		
		if (unit.isHeader())
		{
			units.insert(0, unit);
		}
		else
		{
			units.add(unit);
		}
	}
}

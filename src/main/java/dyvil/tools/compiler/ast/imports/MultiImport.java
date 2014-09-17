package dyvil.tools.compiler.ast.imports;

import java.util.HashSet;
import java.util.Set;

import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ParserUtil;

public class MultiImport extends PackageImport
{
	public Set<String> theClasses = new HashSet();
	
	public MultiImport(ICodePosition position, String basePackage)
	{
		super(position, basePackage);
	}
	
	public void addClass(String name)
	{
		this.theClasses.add(name);
	}
	
	public boolean containsClass(String name)
	{
		return this.theClasses.contains(name);
	}
	
	@Override
	public boolean imports(String path)
	{
		int index = path.lastIndexOf('.');
		if (index != -1)
		{
			String packageName = path.substring(0, index);
			if (!this.thePackage.equals(path))
			{
				return false;
			}
			String className = path.substring(index + 1);
			return this.theClasses.contains(className);
		}
		return false;
	}
	
	@Override
	public boolean isClassName(String name)
	{
		return this.containsClass(name);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append("import ").append(this.thePackage).append(Formatting.Import.multiImportStart);
		ParserUtil.toString(this.theClasses, (String s) -> s, Formatting.Import.multiImportSeperator, buffer);
		buffer.append(Formatting.Import.multiImportEnd).append(';');
	}
}

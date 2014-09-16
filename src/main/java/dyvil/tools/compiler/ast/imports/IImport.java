package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.ast.api.IASTObject;


public interface IImport extends IASTObject
{
	public boolean imports(String path);
	
	public boolean isClassName(String name);
}

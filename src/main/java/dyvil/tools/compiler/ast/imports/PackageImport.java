package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class PackageImport extends ASTObject implements IImport
{
	public Package		pack;
	
	protected String	packageName;
	
	public PackageImport(ICodePosition position, String packageName)
	{
		this.position = position;
		this.packageName = packageName;
	}
	
	public void setPackage(String thePackage)
	{
		this.packageName = thePackage;
	}
	
	public String getPackage()
	{
		return this.packageName;
	}
	
	@Override
	public PackageImport applyState(CompilerState state)
	{
		if (state == CompilerState.RESOLVE)
		{
			Package pack = state.rootPackage.resolvePackage(this.packageName);
			if (pack == null)
			{
				state.addMarker(new SyntaxError(this.position, "Package could not be resolved"));
			}
			
			this.pack = pack;
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append("import ").append(this.packageName).append("_;");
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return null;
	}
	
	@Override
	public IField resolveField(String name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IMethod resolveMethodName(String name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		throw new UnsupportedOperationException();
	}
}

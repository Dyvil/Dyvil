package dyvil.tools.compiler.ast.external;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.parsing.Name;

public class ExternalHeader extends DyvilHeader
{
	private boolean importsResolved;
	private boolean staticImportsResolved;
	
	public ExternalHeader()
	{
	}
	
	public ExternalHeader(Name name)
	{
		super(name);
	}
	
	private void resolveImports()
	{
		this.importsResolved = true;
		for (int i = 0; i < this.importCount; i++)
		{
			this.imports[i].resolveTypes(null, this, false);
		}
	}
	
	private void resolveStaticImports()
	{
		this.staticImportsResolved = true;
		for (int i = 0; i < this.usingCount; i++)
		{
			this.usings[i].resolveTypes(null, this, true);
		}
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		if (!this.importsResolved)
		{
			this.resolveImports();
		}
		return super.resolveClass(name);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (!this.staticImportsResolved)
		{
			this.resolveStaticImports();
		}
		return super.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		if (!this.staticImportsResolved)
		{
			this.resolveStaticImports();
		}
		super.getMethodMatches(list, instance, name, arguments);
	}
}

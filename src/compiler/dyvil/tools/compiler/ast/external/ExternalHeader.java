package dyvil.tools.compiler.ast.external;

import java.util.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.DyvilHeader;

public class ExternalHeader extends DyvilHeader
{
	private boolean	importsResolved;
	private boolean	staticImportsResolved;
	
	public ExternalHeader(String name)
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
		for (int i = 0; i < this.staticImportCount; i++)
		{
			this.staticImports[i].resolveTypes(null, this, true);
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
	public IField resolveField(Name name)
	{
		if (!this.staticImportsResolved)
		{
			this.resolveStaticImports();
		}
		return super.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (!this.staticImportsResolved)
		{
			this.resolveStaticImports();
		}
		super.getMethodMatches(list, instance, name, arguments);
	}
}

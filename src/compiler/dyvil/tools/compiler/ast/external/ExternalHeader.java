package dyvil.tools.compiler.ast.external;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.parsing.Name;

public class ExternalHeader extends DyvilHeader
{
	private static final int IMPORT_DECLARATIONS  = 1;
	private static final int USING_DECLARATIONS   = 1 << 1;
	private static final int INCLUDE_DECLARATIONS = 1 << 2;
	private static final int TYPE_ALIASES         = 1 << 3;

	private int resolved;

	public ExternalHeader(DyvilCompiler compiler)
	{
		super(compiler);
	}

	public ExternalHeader(DyvilCompiler compiler, Name name)
	{
		super(compiler, name);
	}

	private void resolveIncludeDeclarations()
	{
		this.resolved |= INCLUDE_DECLARATIONS;

		for (int i = 0; i < this.includeCount; i++)
		{
			this.includes[i].resolve(null, this);
		}
	}

	private void resolveImportDeclarations()
	{
		if ((this.resolved & INCLUDE_DECLARATIONS) == 0)
		{
			this.resolveIncludeDeclarations();
		}

		this.resolved |= IMPORT_DECLARATIONS;

		for (int i = 0; i < this.importCount; i++)
		{
			this.importDeclarations[i].resolveTypes(null, this);
		}
	}

	private void resolveUsingDeclarations()
	{
		if ((this.resolved & INCLUDE_DECLARATIONS) == 0)
		{
			this.resolveIncludeDeclarations();
		}

		this.resolved |= USING_DECLARATIONS;

		for (int i = 0; i < this.usingCount; i++)
		{
			this.usingDeclarations[i].resolveTypes(null, this);
		}
	}

	private void resolveTypeAliases()
	{
		this.resolved |= TYPE_ALIASES;

		for (int i = 0; i < this.typeAliasCount; i++)
		{
			this.typeAliases[i].resolveTypes(null, this);
		}
	}

	@Override
	public IClass resolveClass(Name name)
	{
		if ((this.resolved & IMPORT_DECLARATIONS) == 0)
		{
			this.resolveImportDeclarations();
		}
		return super.resolveClass(name);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		if ((this.resolved & USING_DECLARATIONS) == 0)
		{
			this.resolveUsingDeclarations();
		}
		return super.resolveField(name);
	}

	@Override
	public ITypeAlias resolveTypeAlias(Name name, int arity)
	{
		if ((this.resolved & TYPE_ALIASES) == 0)
		{
			this.resolveTypeAliases();
		}

		return super.resolveTypeAlias(name, arity);
	}

	@Override
	public void getMethodMatches(MethodMatchList list, IValue receiver, Name name, IArguments arguments)
	{
		if ((this.resolved & USING_DECLARATIONS) == 0)
		{
			this.resolveUsingDeclarations();
		}
		super.getMethodMatches(list, receiver, name, arguments);
	}
}

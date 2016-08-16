package dyvil.tools.compiler.ast.external;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.header.ImportDeclaration;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.parsing.Name;

public class ExternalHeader extends DyvilHeader
{
	private static final int IMPORTS              = 1;
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

	@Override
	protected void resolveImports()
	{
		if ((this.resolved & INCLUDE_DECLARATIONS) == 0)
		{
			this.resolveIncludeDeclarations();
		}

		this.resolved |= IMPORTS;

		for (int i = 0; i < this.importCount; i++)
		{
			final ImportDeclaration declaration = this.importDeclarations[i];
			declaration.resolveTypes(null, this);
			declaration.resolve(null, this);
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
		if ((this.resolved & IMPORTS) == 0)
		{
			this.resolveImports();
		}
		return super.resolveClass(name);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		if ((this.resolved & IMPORTS) == 0)
		{
			this.resolveImports();
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
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		if ((this.resolved & IMPORTS) == 0)
		{
			this.resolveImports();
		}
		super.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		if ((this.resolved & IMPORTS) == 0)
		{
			this.resolveImports();
		}
		super.getImplicitMatches(list, value, targetType);
	}
}

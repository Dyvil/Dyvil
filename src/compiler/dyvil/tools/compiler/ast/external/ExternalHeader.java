package dyvil.tools.compiler.ast.external;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.header.ImportDeclaration;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.parsing.Name;

public class ExternalHeader extends DyvilHeader
{
	private static final int IMPORTS              = 1;
	private static final int TYPE_ALIASES         = 1 << 1;

	private int resolved;

	public ExternalHeader(DyvilCompiler compiler)
	{
		super(compiler);
	}

	public ExternalHeader(DyvilCompiler compiler, Name name)
	{
		super(compiler, name);
	}

	@Override
	protected void resolveImports()
	{
		this.resolved |= IMPORTS;

		for (int i = 0; i < this.includeCount; i++)
		{
			this.includes[i].resolveTypes(null, this);
		}

		for (int i = 0; i < this.importCount; i++)
		{
			final ImportDeclaration declaration = this.importDeclarations[i];
			declaration.resolveTypes(null);
			declaration.resolve(null);
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
	public IContext getContext()
	{
		if ((this.resolved & IMPORTS) == 0)
		{
			this.resolveImports();
		}
		return super.getContext();
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
}

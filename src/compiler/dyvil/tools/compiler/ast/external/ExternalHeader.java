package dyvil.tools.compiler.ast.external;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.header.HeaderUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.parsing.Name;

public class ExternalHeader extends HeaderUnit
{
	private static final int IMPORTS              = 1;
	private static final int TYPE_ALIASES         = 1 << 1;

	private int resolved;

	public ExternalHeader(DyvilCompiler compiler)
	{
		super(compiler);
	}

	public ExternalHeader(DyvilCompiler compiler, Name name, Package pack)
	{
		super(compiler, name);
		this.pack = pack;
	}

	@Override
	protected void resolveImports()
	{
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

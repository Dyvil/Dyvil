package dyvilx.tools.compiler.ast.external;

import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.header.AbstractHeader;
import dyvilx.tools.compiler.ast.imports.ImportDeclaration;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.lang.Name;

public class ExternalHeader extends AbstractHeader implements IDefaultContext
{
	private static final int IMPORTS      = 1;
	private static final int TYPE_ALIASES = 1 << 1;

	private byte resolved;

	public ExternalHeader()
	{
	}

	public ExternalHeader(Name name, Package pack)
	{
		super(name);
		this.pack = pack;
	}

	private void resolveImports()
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
	public DyvilCompiler getCompilationContext()
	{
		return Package.rootPackage.compiler;
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
	public void resolveTypeAlias(MatchList<ITypeAlias> matches, IType receiver, Name name, TypeList arguments)
	{
		if ((this.resolved & TYPE_ALIASES) == 0)
		{
			this.resolveTypeAliases();
		}

		super.resolveTypeAlias(matches, receiver, name, arguments);
	}
}

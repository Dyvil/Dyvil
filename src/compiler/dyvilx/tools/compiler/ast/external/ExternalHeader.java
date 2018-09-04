package dyvilx.tools.compiler.ast.external;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.lang.Name;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.classes.ClassList;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.expression.operator.Operator;
import dyvilx.tools.compiler.ast.header.AbstractHeader;
import dyvilx.tools.compiler.ast.header.HeaderDeclaration;
import dyvilx.tools.compiler.ast.imports.ImportDeclaration;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;
import dyvilx.tools.compiler.ast.type.alias.TypeAlias;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ExternalHeader extends AbstractHeader implements IDefaultContext
{
	private static final int IMPORTS      = 1;
	private static final int TYPE_ALIASES = 1 << 1;
	private static final int CLASSES = 1 << 2;

	private byte resolved;

	private List<String> classNames;

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
		if ((this.resolved & IMPORTS) != 0)
		{
			return;
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
		if ((this.resolved & TYPE_ALIASES) != 0)
		{
			return;
		}

		this.resolved |= TYPE_ALIASES;

		for (int i = 0; i < this.typeAliasCount; i++)
		{
			this.typeAliases[i].resolveTypes(null, this);
		}
	}

	private void resolveClasses()
	{
		if ((this.resolved & CLASSES) != 0)
		{
			return;
		}

		this.resolved |= CLASSES;

		if (this.classNames == null)
		{
			return;
		}

		for (String className : this.classNames)
		{
			final IClass iclass = Package.rootPackage.resolveClass(className);
			if (iclass != null)
			{
				this.addClass(iclass);
			}
		}

		// not needed anymore
		this.classNames = null;
	}

	@Override
	public DyvilCompiler getCompilationContext()
	{
		return Package.rootPackage.compiler;
	}

	@Override
	public boolean needsHeaderDeclaration()
	{
		return false;
	}

	@Override
	public IContext getContext()
	{
		this.resolveImports();
		return super.getContext();
	}

	@Override
	public ClassList getClasses()
	{
		this.resolveClasses();
		return super.getClasses();
	}

	@Override
	public IClass resolveClass(Name name)
	{
		// optimization
		if (this.classNames != null && !this.classNames.contains(name.qualified))
		{
			return null;
		}
		return super.resolveClass(name);
	}

	@Override
	public void resolveTypeAlias(MatchList<ITypeAlias> matches, IType receiver, Name name, TypeList arguments)
	{
		this.resolveTypeAliases();
		super.resolveTypeAlias(matches, receiver, name, arguments);
	}

	// --------------- Compilation ---------------

	@Override
	public void read(DataInput in) throws IOException
	{
		this.headerDeclaration = new HeaderDeclaration(this);
		this.headerDeclaration.read(in);

		this.name = this.headerDeclaration.getName();

		// Import Declarations
		final int imports = in.readShort();
		for (int i = 0; i < imports; i++)
		{
			final ImportDeclaration id = new ImportDeclaration(null);
			id.read(in);
			this.addImport(id);
		}

		final int operators = in.readShort();
		for (int i = 0; i < operators; i++)
		{
			this.addOperator(Operator.read(in));
		}

		final int typeAliases = in.readShort();
		for (int i = 0; i < typeAliases; i++)
		{
			final TypeAlias ta = new TypeAlias();
			ta.read(in);
			this.addTypeAlias(ta);
		}

		final int classes = in.readShort();
		if (classes > 0)
		{
			this.classNames = new ArrayList<>(classes);
			for (int i = 0; i < classes; i++)
			{
				this.classNames.add(in.readUTF());
			}
		}
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
	}
}

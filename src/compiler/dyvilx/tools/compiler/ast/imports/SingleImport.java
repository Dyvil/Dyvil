package dyvilx.tools.compiler.ast.imports;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.operator.IOperator;
import dyvilx.tools.compiler.ast.external.ExternalHeader;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.header.HeaderDeclaration;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class SingleImport extends Import implements IDefaultContext
{
	protected Name name;
	protected Name alias;

	// Metadata

	/**
	 * The context that will be used to resolve fields, methods, etc.
	 */
	private IImportContext resolver;

	/**
	 * The context that is returned by {@link #asContext()}. Can be either {@code this} object or a context that first
	 * searches {@code this} and then the {@code inline}d header.
	 */
	private IContext asContext = this;

	/**
	 * The context that is returned by {@link #asParentContext()}. Will be a chain that searches the class, header and
	 * package with this name, in that order.
	 */
	private IContext asParentContext;

	private int mask;

	public SingleImport()
	{
		super(null);
	}

	public SingleImport(SourcePosition position)
	{
		super(position);
	}

	public SingleImport(SourcePosition position, Name name)
	{
		super(position);
		this.name = name;
	}

	@Override
	public int importTag()
	{
		return SINGLE;
	}

	public Name getName()
	{
		return this.name;
	}

	public void setName(Name name)
	{
		this.name = name;
	}

	@Override
	public Name getAlias()
	{
		return this.alias;
	}

	@Override
	public void setAlias(Name alias)
	{
		this.alias = alias;
	}

	private boolean hasMask(int mask)
	{
		return (this.mask & mask) != 0;
	}

	private boolean checkName(int mask, Name name)
	{
		return this.hasMask(mask) && (name == this.name || name == this.alias);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context, IImportContext parentContext, int mask)
	{
		if (this.parent != null)
		{
			this.parent.resolveTypes(markers, context, parentContext, KindedImport.PARENT);
			parentContext = this.parent.asParentContext();
		}

		boolean resolved = false;
		this.resolver = parentContext;
		this.mask = mask;

		if ((mask & KindedImport.CLASS) != 0)
		{
			final IClass theClass = parentContext.resolveClass(this.name);
			if (theClass != null)
			{
				this.asParentContext = theClass;
				resolved = true;
			}
		}
		if ((mask & KindedImport.HEADER) != 0)
		{
			final IHeaderUnit header = parentContext.resolveHeader(this.name);
			if (header != null)
			{
				if ((mask & KindedImport.INLINE) != 0 && this.checkInline(markers, context, header))
				{
					this.asContext = new CombiningContext(this, header.getContext());
				}
				this.asParentContext = !resolved ? header : new CombiningContext(header, this.asParentContext);
				resolved = true;
			}
		}
		if ((mask & KindedImport.PACKAGE) != 0)
		{
			final Package thePackage = parentContext.resolvePackage(this.name);
			if (thePackage != null)
			{
				this.asParentContext = !resolved ? thePackage : new CombiningContext(thePackage, this.asParentContext);
				resolved = true;
			}
		}

		if (!resolved)
		{
			this.asParentContext = IDefaultContext.DEFAULT;

			if (mask == KindedImport.PARENT)
			{
				// when resolving as a parent import, the resolve method is never called,
				// so we need the error reporting to happen here.
				this.reportResolve(markers);
			}
		}

		// otherwise error later
	}

	@Override
	public void resolve(MarkerList markers, IContext context, IImportContext parentContext, int mask)
	{
		if (this.asParentContext != IDefaultContext.DEFAULT)
		{
			// A class, package or type was found with this name
			return;
		}

		parentContext = this.resolver;
		if ((mask & KindedImport.VAR) != 0 && parentContext.resolveField(this.name) != null)
		{
			return;
		}

		if ((mask & KindedImport.FUNC) != 0)
		{
			final MatchList<IMethod> matches = new MatchList<>(null);
			parentContext.getMethodMatches(matches, null, this.name, null);
			if (!matches.isEmpty())
			{
				return;
			}
		}

		if ((mask & KindedImport.OPERATOR) != 0 && parentContext.resolveOperator(this.name, IOperator.ANY) != null)
		{
			return;
		}

		if ((mask & KindedImport.TYPE) != 0 && IContext.resolveTypeAlias(parentContext, null, this.name, null)
		                                               .hasCandidate())
		{
			return;
		}

		this.reportResolve(markers);
	}

	public void reportResolve(MarkerList markers)
	{
		markers.add(Markers.semanticError(this.position, "import.resolve", this.name.qualified));
	}

	private boolean checkInline(MarkerList markers, IContext context, IHeaderUnit header)
	{
		// Check if the Header has a Header Declaration
		final HeaderDeclaration headerDeclaration = header.getHeaderDeclaration();
		if (headerDeclaration == null)
		{
			return header.needsHeaderDeclaration();
		}

		// Header Access Check
		int accessLevel = headerDeclaration.getAccessLevel();
		if ((accessLevel & Modifiers.INTERNAL) != 0)
		{
			if (header instanceof ExternalHeader)
			{
				markers.add(Markers.semanticError(this.position, "import.inline_header.internal", header.getName()));
				return false;
			}
			accessLevel &= 0b1111;
		}

		switch (accessLevel)
		{
		case Modifiers.PACKAGE:
		case Modifiers.PROTECTED:
			if (header.getPackage() == context.getHeader().getPackage())
			{
				return true;
			}
			// Fallthrough
		case Modifiers.PRIVATE:
			markers.add(Markers.semanticError(this.position, "import.inline_header.invisible", header.getName()));
			return false;
		}

		// All checks passed
		return true;
	}

	@Override
	public IImportContext asContext()
	{
		return this.asContext;
	}

	@Override
	public IImportContext asParentContext()
	{
		return this.asParentContext;
	}

	@Override
	public Package resolvePackage(Name name)
	{
		if (this.checkName(KindedImport.PACKAGE, name))
		{
			return this.resolver.resolvePackage(this.name);
		}
		return null;
	}

	@Override
	public IHeaderUnit resolveHeader(Name name)
	{
		if (this.checkName(KindedImport.HEADER, name))
		{
			return this.resolver.resolveHeader(this.name);
		}
		return null;
	}

	@Override
	public void resolveTypeAlias(MatchList<ITypeAlias> matches, IType receiver, Name name, TypeList arguments)
	{
		if (this.checkName(KindedImport.TYPE, name))
		{
			this.resolver.resolveTypeAlias(matches, receiver, name, arguments);
		}
	}

	@Override
	public IOperator resolveOperator(Name name, byte type)
	{
		if (this.checkName(KindedImport.OPERATOR, name))
		{
			return this.resolver.resolveOperator(this.name, type);
		}
		return null;
	}

	@Override
	public IClass resolveClass(Name name)
	{
		if (this.checkName(KindedImport.CLASS, name))
		{
			return this.resolver.resolveClass(this.name);
		}
		return null;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.checkName(KindedImport.VAR, name))
		{
			return this.resolver.resolveField(this.name);
		}
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		if (this.checkName(KindedImport.FUNC, name))
		{
			this.resolver.getMethodMatches(list, receiver, this.name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		if (this.hasMask(KindedImport.IMPLICIT))
		{
			this.resolver.getImplicitMatches(list, value, targetType);
		}
	}

	@Override
	public void writeData(DataOutput out) throws IOException
	{
		IImport.writeImport(this.parent, out);
		this.name.write(out); // non-null
		Name.write(this.alias, out); // nullable
	}

	@Override
	public void readData(DataInput in) throws IOException
	{
		this.parent = IImport.readImport(in);
		this.name = Name.read(in);
		this.alias = Name.read(in);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.appendParent(prefix, buffer);
		buffer.append(this.name);
		if (this.alias != null)
		{
			Formatting.appendSeparator(buffer, "import.alias", "=>");
			buffer.append(this.alias);
		}
	}
}

package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.operator.IOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class SingleImport extends Import implements IImportContext
{
	protected Name name;
	protected Name alias;

	private IImportContext parentContext;
	private IContext       thisContext;
	private int            mask;

	public SingleImport()
	{
		super(null);
	}

	public SingleImport(ICodePosition position)
	{
		super(position);
	}

	public SingleImport(ICodePosition position, Name name)
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
	public void resolveTypes(MarkerList markers, IImportContext context, int mask)
	{
		if (this.parent != null)
		{
			this.parent.resolveTypes(markers, context, KindedImport.PARENT);
			context = this.parent.asParentContext();
		}

		boolean resolved = false;
		this.parentContext = context;
		this.mask = mask;

		if ((mask & KindedImport.CLASS) != 0)
		{
			final IClass theClass = context.resolveClass(this.name);
			if (theClass != null)
			{
				this.thisContext = theClass;
				resolved = true;
			}
		}
		if ((mask & KindedImport.HEADER) != 0)
		{
			final IDyvilHeader header = context.resolveHeader(this.name);
			if (header != null)
			{
				this.thisContext = !resolved ? header : new CombiningContext(header, this.thisContext);
				resolved = true;
			}
		}
		if ((mask & KindedImport.PACKAGE) != 0)
		{
			final Package thePackage = context.resolvePackage(this.name);
			if (thePackage != null)
			{
				this.thisContext = !resolved ? thePackage : new CombiningContext(thePackage, this.thisContext);
				resolved = true;
			}
		}

		// error later
	}

	@Override
	public void resolve(MarkerList markers, IImportContext context, int mask)
	{
		if (this.thisContext != null)
		{
			// A class, package or type was found with this name
			return;
		}

		context = this.parentContext;
		if ((mask & KindedImport.VAR) != 0 && context.resolveField(this.name) != null)
		{
			return;
		}

		if ((mask & KindedImport.FUNC) != 0)
		{
			final MatchList<IMethod> methods = new MatchList<>(null);
			context.getMethodMatches(methods, null, this.name, null);
			if (!methods.isEmpty())
			{
				return;
			}
		}

		if ((mask & KindedImport.OPERATOR) != 0 && context.resolveOperator(this.name, -1) != null)
		{
			return;
		}

		if ((mask & KindedImport.TYPE) != 0 && context.resolveTypeAlias(this.name, -1) != null)
		{
			return;
		}

		markers.add(Markers.semanticError(this.position, "import.resolve", this.name.qualified));
	}

	@Override
	public IImportContext asContext()
	{
		return this;
	}

	@Override
	public IImportContext asParentContext()
	{
		return this.thisContext;
	}

	@Override
	public Package resolvePackage(Name name)
	{
		if (this.checkName(KindedImport.PACKAGE, name))
		{
			return this.parentContext.resolvePackage(this.name);
		}
		return null;
	}

	@Override
	public IDyvilHeader resolveHeader(Name name)
	{
		if (this.checkName(KindedImport.HEADER, name))
		{
			return this.parentContext.resolveHeader(this.name);
		}
		return null;
	}

	@Override
	public ITypeAlias resolveTypeAlias(Name name, int arity)
	{
		if (this.checkName(KindedImport.TYPE, name))
		{
			return this.parentContext.resolveTypeAlias(this.name, arity);
		}
		return null;
	}

	@Override
	public IOperator resolveOperator(Name name, int type)
	{
		if (this.checkName(KindedImport.OPERATOR, name))
		{
			return this.parentContext.resolveOperator(this.name, type);
		}
		return null;
	}

	@Override
	public IClass resolveClass(Name name)
	{
		if (this.checkName(KindedImport.CLASS, name))
		{
			return this.parentContext.resolveClass(this.name);
		}
		return null;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.checkName(KindedImport.VAR, name))
		{
			return this.parentContext.resolveField(this.name);
		}
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		if (this.checkName(KindedImport.FUNC, name))
		{
			this.parentContext.getMethodMatches(list, receiver, this.name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		if (this.hasMask(KindedImport.FUNC))
		{
			this.parentContext.getImplicitMatches(list, value, targetType);
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

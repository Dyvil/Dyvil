package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.Candidate;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
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

	private IClass  theClass;
	private Package thePackage;

	private IDataMember        field;
	private MatchList<IMethod> methods;

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

	@Override
	public void resolveTypes(MarkerList markers, IImportContext context, int mask)
	{
		if (this.parent != null)
		{
			this.parent.resolveTypes(markers, context, KindedImport.PARENT);
			context = this.parent.asParentContext();

			if (context == null)
			{
				return;
			}
		}

		boolean found = false;
		if ((mask & KindedImport.VAR) != 0)
		{
			final IDataMember field = context.resolveField(this.name);
			if (field != null)
			{
				this.field = field;
				found = true;
			}
		}

		if ((mask & KindedImport.FUNC) != 0)
		{
			this.methods = new MatchList<>(null);
			context.getMethodMatches(this.methods, null, this.name, null);
			if (!found && !this.methods.isEmpty())
			{
				found = true;
			}
		}

		if ((mask & KindedImport.CLASS) != 0)
		{
			final IClass theClass = context.resolveClass(this.name);
			if (theClass != null)
			{
				this.theClass = theClass;
				found = true;
			}
		}

		if ((mask & KindedImport.PACKAGE) != 0)
		{
			final Package thePackage = context.resolvePackage(this.name);
			if (thePackage != null)
			{
				this.thePackage = thePackage;
				found = true;
			}
		}

		if (!found)
		{
			markers.add(Markers.semantic(this.position, "import.resolve", this.name.qualified));
		}
	}

	@Override
	public IImportContext asContext()
	{
		return this;
	}

	@Override
	public IImportContext asParentContext()
	{
		return this.theClass == null ? this.thePackage : this.theClass;
	}

	@Override
	public Package resolvePackage(Name name)
	{
		if (name == this.name || name == this.alias)
		{
			return this.thePackage;
		}
		return null;
	}

	@Override
	public IClass resolveClass(Name name)
	{
		if (name == this.name || name == this.alias)
		{
			return this.theClass;
		}
		return null;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		if (name == null || name == this.name || name == this.alias)
		{
			return this.field;
		}
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		if (this.methods == null)
		{
			return;
		}
		if (name != null && name != this.name && name != this.alias)
		{
			return;
		}

		for (Candidate<IMethod> candidate : this.methods)
		{
			candidate.getMember().checkMatch(list, receiver, null, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		if (this.methods == null)
		{
			return;
		}

		for (Candidate<IMethod> method : this.methods)
		{
			method.getMember().checkImplicitMatch(list, value, targetType);
		}
	}

	@Override
	public void writeData(DataOutput out) throws IOException
	{
		IImport.writeImport(this.parent, out);

		out.writeUTF(this.name.qualified);
		if (this.alias != null)
		{
			out.writeUTF(this.alias.qualified);
		}
		else
		{
			out.writeUTF("");
		}
	}

	@Override
	public void readData(DataInput in) throws IOException
	{
		this.parent = IImport.readImport(in);

		this.name = Name.fromRaw(in.readUTF());

		String alias = in.readUTF();
		if (!alias.isEmpty())
		{
			this.alias = Name.fromRaw(alias);
		}
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

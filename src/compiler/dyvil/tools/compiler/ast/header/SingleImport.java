package dyvil.tools.compiler.ast.header;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
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

public final class SingleImport extends Import
{
	public Name name;
	public Name alias;

	private IClass  theClass;
	private Package thePackage;

	private IDataMember   field;
	private List<IMethod> methods;

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

	@Override
	public void setAlias(Name alias)
	{
		this.alias = alias;
	}

	@Override
	public Name getAlias()
	{
		return this.alias;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context, boolean using)
	{
		if (this.parent != null)
		{
			this.parent.resolveTypes(markers, context, false);
			context = this.parent.getContext();

			if (context == null)
			{
				return;
			}
		}

		if (using)
		{
			if (!(context instanceof IClass))
			{
				markers.add(Markers.semantic(this.position, "using.class.invalid"));
				return;
			}

			IClassBody body = ((IClass) context).getBody();

			IDataMember field = body.getField(this.name);
			if (field != null)
			{
				this.field = field;
				return;
			}

			this.methods = new ArrayList<>();
			int len = body.methodCount();
			for (int i = 0; i < len; i++)
			{
				IMethod m = body.getMethod(i);
				if (m.getName() == this.name)
				{
					this.methods.add(m);
				}
			}
			if (!this.methods.isEmpty())
			{
				return;
			}

			markers.add(Markers.semantic(this.position, "resolve.method_field", this.name.qualified));
			return;
		}

		Package pack = context.resolvePackage(this.name);
		if (pack != null)
		{
			this.thePackage = pack;
			return;
		}

		IClass iclass = context.resolveClass(this.name);
		if (iclass != null)
		{
			this.theClass = iclass;
			return;
		}

		markers.add(Markers.semantic(this.position, "resolve.package", this.name.qualified));
	}

	@Override
	public IContext getContext()
	{
		return this.theClass != null ? this.theClass : this.thePackage;
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
	public void getMethodMatches(MethodMatchList list, IValue receiver, Name name, IArguments arguments)
	{
		if (this.methods == null)
		{
			return;
		}
		if (name != null && name != this.name && name != this.alias)
		{
			return;
		}

		for (IMethod method : this.methods)
		{
			IContext.getMethodMatch(list, receiver, null, arguments, method);
		}
	}

	@Override
	public void getImplicitMatches(MethodMatchList list, IValue value, IType targetType)
	{
		if (this.methods == null)
		{
			return;
		}

		for (IMethod method : this.methods)
		{
			IContext.getImplicitMatch(list, value, targetType, method);
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

		this.name = Name.getQualified(in.readUTF());

		String alias = in.readUTF();
		if (!alias.isEmpty())
		{
			this.alias = Name.getQualified(alias);
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

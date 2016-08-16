package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class WildcardImport extends Import
{
	private IContext context;

	public WildcardImport()
	{
		super(null);
	}

	public WildcardImport(ICodePosition position)
	{
		super(position);
	}

	@Override
	public int importTag()
	{
		return WILDCARD;
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
				markers.add(Markers.semanticError(this.position, "using.wildcard.invalid"));
				this.context = IDefaultContext.DEFAULT;
				return;
			}

			this.context = context;
			return;
		}

		if (!(context instanceof Package))
		{
			markers.add(Markers.semanticError(this.position, "import.wildcard.invalid"));
			this.context = IDefaultContext.DEFAULT;
			return;
		}
		this.context = context;
	}

	@Override
	public IContext getContext()
	{
		return this.context;
	}

	@Override
	public Package resolvePackage(Name name)
	{
		if (this.context == null)
		{
			return null;
		}

		return this.context.resolvePackage(name);
	}

	@Override
	public IClass resolveClass(Name name)
	{
		if (this.context == null)
		{
			return null;
		}

		return this.context.resolveClass(name);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.context == null)
		{
			return null;
		}

		return this.context.resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		if (this.context == null)
		{
			return;
		}

		this.context.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		if (this.context == null)
		{
			return;
		}

		this.context.getImplicitMatches(list, value, targetType);
	}

	@Override
	public void writeData(DataOutput out) throws IOException
	{
		IImport.writeImport(this.parent, out);
	}

	@Override
	public void readData(DataInput in) throws IOException
	{
		this.parent = IImport.readImport(in);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.appendParent(prefix, buffer);
		buffer.append('_');
	}
}

package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
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
				markers.add(Markers.semantic(this.position, "using.wildcard.invalid"));
				return;
			}
			
			this.context = context;
			return;
		}
		
		if (!(context instanceof Package))
		{
			markers.add(Markers.semantic(this.position, "import.wildcard.invalid"));
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
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		if (this.context == null)
		{
			return;
		}

		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		IImport.writeImport(this.parent, out);
	}
	
	@Override
	public void read(DataInput in) throws IOException
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

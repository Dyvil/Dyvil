package dyvil.tools.compiler.ast.imports;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.collection.List;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class PackageImport extends Import
{
	private IContext	context;
	
	public PackageImport(ICodePosition position)
	{
		super(position);
	}
	
	@Override
	public int importTag()
	{
		return PACKAGE;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context, boolean using)
	{
		if (this.parent != null)
		{
			this.parent.resolveTypes(markers, context, false);
			context = this.parent.getContext();
		}
		
		if (using)
		{
			if (!(context instanceof IClass))
			{
				markers.add(this.position, "import.package.invalid");
				return;
			}
			
			this.context = context;
			return;
		}
		
		if (!(context instanceof Package))
		{
			markers.add(this.position, "import.package.invalid");
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
		return this.context.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.context.resolveClass(name);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return this.context.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
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

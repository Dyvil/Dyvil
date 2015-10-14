package dyvil.tools.compiler.ast.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.collection.List;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class NamedType implements IRawType
{
	protected ICodePosition	position;
	protected Name			name;
	
	public NamedType(ICodePosition position, Name name)
	{
		this.position = position;
		this.name = name;
	}
	
	protected static IType resolvePrimitive(Name name)
	{
		if (name == Names._void)
		{
			return Types.VOID;
		}
		if (name == Names._boolean)
		{
			return Types.BOOLEAN;
		}
		if (name == Names._byte)
		{
			return Types.BYTE;
		}
		if (name == Names._short)
		{
			return Types.SHORT;
		}
		if (name == Names._char)
		{
			return Types.CHAR;
		}
		if (name == Names._int)
		{
			return Types.INT;
		}
		if (name == Names._long)
		{
			return Types.LONG;
		}
		if (name == Names._float)
		{
			return Types.FLOAT;
		}
		if (name == Names._double)
		{
			return Types.DOUBLE;
		}
		if (name == Names.any)
		{
			return Types.ANY;
		}
		if (name == Names.dynamic)
		{
			return Types.DYNAMIC;
		}
		if (name == Names.auto)
		{
			return Types.UNKNOWN;
		}
		return null;
	}
	
	@Override
	public int typeTag()
	{
		return NAMED;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	public IClass getTheClass()
	{
		return Types.OBJECT_CLASS;
	}
	
	@Override
	public boolean isResolved()
	{
		return false;
	}
	
	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		// Try to resolve the name of this Type as a primitive type
		IType t = resolvePrimitive(this.name);
		if (t != null)
		{
			return t;
		}
		
		IType type = IContext.resolveType(context, this.name);
		if (type == null)
		{
			markers.add(I18n.createMarker(this.position, "resolve.type", this.toString()));
			return this;
		}
		
		return type;
	}
	
	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}
	
	@Override
	public String getInternalName()
	{
		return this.name.qualified;
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append(this.name.qualified);
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append(this.name.qualified);
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.name.qualified);
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		this.name = Name.getQualified(in.readUTF());
	}
	
	@Override
	public String toString()
	{
		return this.name.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
	}
	
	@Override
	public IType clone()
	{
		return new NamedType(this.position, this.name);
	}
}

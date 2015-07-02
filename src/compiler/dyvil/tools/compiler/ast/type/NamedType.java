package dyvil.tools.compiler.ast.type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dyvil.lang.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class NamedType implements IType
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
		if (name == Name._void)
		{
			return Types.VOID;
		}
		if (name == Name._boolean)
		{
			return Types.BOOLEAN;
		}
		if (name == Name._byte)
		{
			return Types.BYTE;
		}
		if (name == Name._short)
		{
			return Types.SHORT;
		}
		if (name == Name._char)
		{
			return Types.CHAR;
		}
		if (name == Name._int)
		{
			return Types.INT;
		}
		if (name == Name._long)
		{
			return Types.LONG;
		}
		if (name == Name._float)
		{
			return Types.FLOAT;
		}
		if (name == Name._double)
		{
			return Types.DOUBLE;
		}
		if (name == Name.any)
		{
			return Types.ANY;
		}
		if (name == Name.dynamic)
		{
			return Types.DYNAMIC;
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
		return null;
	}
	
	@Override
	public boolean isResolved()
	{
		return false;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context, TypePosition position)
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
			markers.add(this.position, "resolve.type", this.toString());
			return this;
		}
		
		if (type.typeTag() == TYPE_VAR_TYPE)
		{
			switch (position)
			{
			case CLASS:
			case TYPE:
				markers.add(this.position, "type.class.typevar");
				break;
			case SUPER_TYPE:
				markers.add(this.position, "type.super.typevar");
				break;
			default:
				break;
			}
		}
		
		return type;
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
	public void write(DataOutputStream dos) throws IOException
	{
		dos.writeUTF(this.name.qualified);
	}
	
	@Override
	public void read(DataInputStream dis) throws IOException
	{
		this.name = Name.getQualified(dis.readUTF());
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

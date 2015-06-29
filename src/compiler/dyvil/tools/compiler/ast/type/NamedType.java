package dyvil.tools.compiler.ast.type;

import dyvil.lang.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.TypeVarType;
import dyvil.tools.compiler.ast.member.IClassMember;
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
	public IType resolve(MarkerList markers, IContext context)
	{
		// Try to resolve the name of this Type as a primitive type
		IType t = resolvePrimitive(this.name);
		if (t != null)
		{
			// If the array dimensions of this type are 0, we can assume
			// that it is exactly the primitive type, so the primitive type
			// instance is returned.
			return t;
		}
		
		ITypeVariable typeVar = context.resolveTypeVariable(this.name);
		if (typeVar != null)
		{
			return new TypeVarType(typeVar);
		}
		
		// This type is probably not a primitive one, so resolve using
		// the context.
		IClass iclass = IContext.resolveClass(context, this.name);
		if (iclass != null)
		{
			return new ClassType(iclass);
		}
		
		markers.add(this.position, "resolve.type", this.toString());
		return this;
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
	public byte getVisibility(IClassMember member)
	{
		return 0;
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

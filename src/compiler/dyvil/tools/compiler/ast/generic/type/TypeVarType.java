package dyvil.tools.compiler.ast.generic.type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dyvil.collection.List;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.IGeneric;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class TypeVarType implements IType
{
	public ITypeVariable	typeVar;
	
	public TypeVarType()
	{
	}
	
	public TypeVarType(ITypeVariable typeVar)
	{
		this.typeVar = typeVar;
	}
	
	@Override
	public int typeTag()
	{
		return TYPE_VAR_TYPE;
	}
	
	@Override
	public Name getName()
	{
		return null;
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.typeVar.getTheClass();
	}
	
	@Override
	public IType getSuperType()
	{
		return null;
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		return this.typeVar.isSuperTypeOf(type);
	}
	
	@Override
	public boolean isSuperClassOf(IType type)
	{
		return this.typeVar.isSuperTypeOf(type);
	}
	
	@Override
	public boolean equals(IType type)
	{
		return this.typeVar.isSuperTypeOf(type);
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		return false;
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return true;
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		if (context == null)
		{
			return this;
		}
		
		IType t = context.resolveType(this.typeVar);
		if (t != Types.UNKNOWN)
		{
			if (t.isPrimitive())
			{
				return t.getReferenceType();
			}
			return t;
		}
		return this;
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		return this.typeVar == typeVar ? this : Types.UNKNOWN;
	}
	
	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		typeContext.addMapping(this.typeVar, concrete);
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context, TypePosition position)
	{
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
	public IMethod getFunctionalMethod()
	{
		return null;
	}
	
	@Override
	public String getInternalName()
	{
		if (this.typeVar.upperBoundCount() > 0)
		{
			return this.typeVar.getUpperBound(0).getInternalName();
		}
		return "java/lang/Object";
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append('L').append(this.getInternalName()).append(';');
	}
	
	@Override
	public String getSignature()
	{
		StringBuilder buffer = new StringBuilder();
		this.appendSignature(buffer);
		return buffer.toString();
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append('T').append(this.typeVar.getName().qualified).append(';');
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeLDC(this.typeVar.getName().qualified);
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/type/TypeArgument", "apply", "(Ljava/lang/String;)Ldyvil/reflect/type/TypeArgument;", false);
	}
	
	@Override
	public void write(DataOutputStream dos) throws IOException
	{
	}
	
	@Override
	public void read(DataInputStream dis) throws IOException
	{
	}
	
	@Override
	public IType clone()
	{
		return new TypeVarType(this.typeVar);
	}
	
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		IGeneric generic = this.typeVar.getGeneric();
		this.toString("", buf);
		if (generic instanceof INamed)
		{
			buf.append(" (of type ").append(((INamed) generic).getName()).append(")");
		}
		return buf.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.typeVar.getName());
	}
}

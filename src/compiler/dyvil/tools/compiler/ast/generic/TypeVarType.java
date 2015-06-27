package dyvil.tools.compiler.ast.generic;

import dyvil.lang.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class TypeVarType implements IType
{
	public ITypeVariable	typeVar;
	
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
	public boolean isSuperTypeOf2(IType type)
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
	public IType resolveType(ITypeVariable typeVar, IType concrete)
	{
		if (this.typeVar == typeVar)
		{
			return concrete.getReferenceType();
		}
		return null;
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
		if (t != null)
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
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context)
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

package dyvil.tools.compiler.ast.generic.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.method.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IRawType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class TypeVarType implements IRawType
{
	protected ITypeVariable typeVar;
	
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
	public ITypeVariable getTypeVariable()
	{
		return this.typeVar;
	}
	
	@Override
	public boolean isGenericType()
	{
		return false;
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
		return type == this || this.typeVar.isSuperTypeOf(type);
	}
	
	@Override
	public boolean isSuperClassOf(IType type)
	{
		return type == this || this.typeVar.isSuperTypeOf(type);
	}
	
	@Override
	public int getSuperTypeDistance(IType superType)
	{
		return this.typeVar.getSuperTypeDistance(superType);
	}
	
	@Override
	public boolean isSameType(IType type)
	{
		return type == this || this.typeVar.isSuperTypeOf(type);
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		return type == this;
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
			return t;
		}
		return this.typeVar.getDefaultType();
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		return this.typeVar == typeVar ? this : this.typeVar.getDefaultType().resolveType(typeVar);
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
	public IType resolveType(MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		switch (position)
		{
		case CLASS:
		case TYPE:
			markers.add(I18n.createMarker(this.getPosition(), "type.class.typevar"));
			break;
		case SUPER_TYPE:
			markers.add(I18n.createMarker(this.getPosition(), "type.super.typevar"));
			break;
		default:
			break;
		}
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return this.typeVar.getDefaultType().resolveField(name);
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		this.typeVar.getDefaultType().getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return this.typeVar.getDefaultType().getFunctionalMethod();
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
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/TypeArgument", "apply", "(Ljava/lang/String;)Ldyvil/reflect/types/TypeArgument;",
				false);
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
	}
	
	@Override
	public void read(DataInput in) throws IOException
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
		return this.typeVar.getName().toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.typeVar.getName());
	}
}

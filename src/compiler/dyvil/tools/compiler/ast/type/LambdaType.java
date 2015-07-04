package dyvil.tools.compiler.ast.type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dyvil.lang.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.util.Util;

public final class LambdaType implements IType, ITyped, ITypeList
{
	public static final IClass[]	functionClasses	= new IClass[22];
	
	protected IType					returnType;
	protected IType[]				parameterTypes;
	protected int					parameterCount;
	
	public LambdaType()
	{
		parameterTypes = new IType[2];
	}
	
	public LambdaType(IType type)
	{
		this.parameterTypes = new IType[1];
		this.parameterTypes[0] = type;
		this.parameterCount = 1;
	}
	
	public LambdaType(TupleType tupleType)
	{
		this.parameterTypes = tupleType.types;
		this.parameterCount = tupleType.typeCount;
	}
	
	public LambdaType(IType[] types, int typeCount, IType returnType)
	{
		this.parameterTypes = types;
		this.parameterCount = typeCount;
		this.returnType = returnType;
	}
	
	public LambdaType(int typeCount)
	{
		parameterTypes = new IType[typeCount];
	}
	
	public static IClass getLambdaClass(int typeCount)
	{
		IClass iclass = functionClasses[typeCount];
		if (iclass != null)
		{
			return iclass;
		}
		
		iclass = Package.dyvilFunction.resolveClass("Function" + typeCount);
		functionClasses[typeCount] = iclass;
		return iclass;
	}
	
	@Override
	public int typeTag()
	{
		return LAMBDA;
	}
	
	@Override
	public Name getName()
	{
		return this.getTheClass().getName();
	}
	
	@Override
	public void setType(IType type)
	{
		this.returnType = type;
	}
	
	@Override
	public IType getType()
	{
		return this.returnType;
	}
	
	// ITypeList Overrides
	
	@Override
	public int typeCount()
	{
		return this.parameterCount;
	}
	
	@Override
	public void setType(int index, IType type)
	{
		this.parameterTypes[index] = type;
	}
	
	@Override
	public void addType(IType type)
	{
		int index = this.parameterCount++;
		if (this.parameterCount > this.parameterTypes.length)
		{
			IType[] temp = new IType[this.parameterCount];
			System.arraycopy(this.parameterTypes, 0, temp, 0, index);
			this.parameterTypes = temp;
		}
		this.parameterTypes[index] = type;
	}
	
	@Override
	public IType getType(int index)
	{
		return this.parameterTypes[index];
	}
	
	// IType Overrides
	
	@Override
	public IClass getTheClass()
	{
		return getLambdaClass(this.parameterCount);
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (!IType.super.isSuperTypeOf(type))
		{
			return false;
		}
		
		IClass iclass = this.getTheClass();
		ITypeVariable typeVar;
		IType type1;
		for (int i = 0; i < this.parameterCount; i++)
		{
			typeVar = iclass.getTypeVariable(i);
			type1 = type.resolveType(typeVar);
			
			// Contravariance
			if (!type1.isSuperTypeOf(this.parameterTypes[i]))
			{
				return false;
			}
		}
		
		typeVar = iclass.getTypeVariable(this.parameterCount);
		type1 = type.resolveType(typeVar);
		// Covariance
		return this.returnType.isSuperTypeOf(type1);
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		int index = typeVar.getIndex();
		if (index == this.parameterCount)
		{
			return this.returnType;
		}
		return this.parameterTypes[index];
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return false;
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		LambdaType lt = new LambdaType(this.parameterCount);
		lt.parameterCount = this.parameterCount;
		for (int i = 0; i < this.parameterCount; i++)
		{
			lt.parameterTypes[i] = this.parameterTypes[i].getConcreteType(context);
		}
		lt.returnType = this.returnType.getConcreteType(context);
		return lt;
	}
	
	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		ITypeVariable typeVar;
		IType concreteType;
		IClass iclass = this.getTheClass();
		for (int i = 0; i < this.parameterCount; i++)
		{
			typeVar = iclass.getTypeVariable(i);
			concreteType = concrete.getConcreteType(typeContext);
			this.parameterTypes[i].inferTypes(concreteType, typeContext);
		}
		
		typeVar = iclass.getTypeVariable(this.parameterCount);
		concreteType = concrete.resolveType(typeVar);
		this.returnType.inferTypes(concreteType, typeContext);
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context, TypePosition position)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameterTypes[i] = this.parameterTypes[i].resolve(markers, context, TypePosition.GENERIC_ARGUMENT);
		}
		this.returnType = this.returnType.resolve(markers, context, TypePosition.GENERIC_ARGUMENT);
		
		if (position == TypePosition.CLASS)
		{
			markers.add(this.returnType.getPosition(), "type.class.lambda");
		}
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
		getLambdaClass(this.parameterCount).getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return getLambdaClass(this.parameterCount).getFunctionalMethod();
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.returnType.writeTypeExpression(writer);
		
		writer.writeLDC(this.parameterCount);
		writer.writeNewArray("dyvil/lang/Type", 1);
		for (int i = 0; i < this.parameterCount; i++)
		{
			writer.writeInsn(Opcodes.DUP);
			writer.writeLDC(i);
			this.parameterTypes[i].writeTypeExpression(writer);
			writer.writeInsn(Opcodes.AASTORE);
		}
		
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/type/FunctionType", "apply", "([Ldyvil/lang/Type;)Ldyvil/reflect/type/FunctionType;", false);
	}
	
	@Override
	public String getInternalName()
	{
		return "dyvil/function/Function" + this.parameterCount;
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append("Ldyvil/function/Function").append(this.parameterCount).append(';');
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append("Ldyvil/function/Function").append(this.parameterCount).append('<');
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameterTypes[i].appendSignature(buffer);
		}
		this.returnType.appendSignature(buffer);
		buffer.append(">;");
	}
	
	@Override
	public void write(DataOutputStream dos) throws IOException
	{
		dos.writeByte(this.parameterCount);
		for (int i = 0; i < this.parameterCount; i++)
		{
			IType.writeType(this.parameterTypes[i], dos);
		}
		IType.writeType(this.returnType, dos);
	}
	
	@Override
	public void read(DataInputStream dis) throws IOException
	{
		int len = this.parameterCount = dis.readByte();
		if (len > this.parameterTypes.length)
		{
			this.parameterTypes = new IType[len];
		}
		for (int i = 0; i < len; i++)
		{
			this.parameterTypes[i] = IType.readType(dis);
		}
		this.returnType = IType.readType(dis);
	}
	
	@Override
	public IType clone()
	{
		LambdaType lt = new LambdaType(this.parameterCount);
		lt.parameterCount = this.parameterCount;
		System.arraycopy(this.parameterTypes, 0, lt.parameterTypes, 0, parameterCount);
		lt.returnType = this.returnType;
		return lt;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		this.toString("", sb);
		return sb.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.parameterCount == 1)
		{
			this.parameterTypes[0].toString(prefix, buffer);
			buffer.append(' ');
		}
		else if (this.parameterCount > 0)
		{
			buffer.append(Formatting.Method.parametersStart);
			Util.astToString(prefix, this.parameterTypes, this.parameterCount, Formatting.Method.parameterSeperator, buffer);
			buffer.append(Formatting.Method.parametersEnd);
			buffer.append(' ');
		}
		
		buffer.append(Formatting.Expression.lambdaSeperator);
		this.returnType.toString("", buffer);
	}
}

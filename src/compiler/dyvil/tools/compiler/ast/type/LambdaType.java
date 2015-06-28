package dyvil.tools.compiler.ast.type;

import dyvil.lang.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
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
	
	public LambdaType(int typeCount)
	{
		parameterTypes = new IType[typeCount];
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
		IClass iclass = functionClasses[this.parameterCount];
		if (iclass != null)
		{
			return iclass;
		}
		
		iclass = Package.dyvilFunction.resolveClass("Function" + this.parameterCount);
		functionClasses[this.parameterCount] = iclass;
		return iclass;
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
	public IType resolveType(ITypeVariable typeVar)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			IType t = this.parameterTypes[i].resolveType(typeVar);
			if (t != null)
			{
				return t;
			}
		}
		
		return this.returnType.resolveType(typeVar);
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar, IType concrete)
	{
		if (concrete.typeTag() != LAMBDA)
		{
			return null;
		}
		
		LambdaType lambdaType = (LambdaType) concrete;
		if (lambdaType.parameterCount != this.parameterCount)
		{
			return null;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IType concreteType = lambdaType.parameterTypes[i];
			IType type = this.parameterTypes[i].resolveType(typeVar, concreteType);
			if (type != null)
			{
				return type;
			}
		}
		
		return this.returnType.resolveType(typeVar, lambdaType.returnType);
	}
	
	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		if (concrete.typeTag() != LAMBDA)
		{
			return;
		}
		
		LambdaType lambdaType = (LambdaType) concrete;
		if (lambdaType.parameterCount != this.parameterCount)
		{
			return;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IType concreteType = lambdaType.parameterTypes[i];
			this.parameterTypes[i].inferTypes(concreteType, typeContext);
		}
		
		this.returnType.inferTypes(lambdaType.returnType, typeContext);
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public LambdaType resolve(MarkerList markers, IContext context)
	{
		this.getTheClass();
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameterTypes[i] = this.parameterTypes[i].resolve(markers, context);
		}
		this.returnType = this.returnType.resolve(markers, context);
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
		this.getTheClass().getMethodMatches(list, instance, name, arguments);
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
		return this.getTheClass().getFunctionalMethod();
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
		buffer.append(Formatting.Method.parametersStart);
		Util.astToString(prefix, this.parameterTypes, this.parameterCount, Formatting.Method.parameterSeperator, buffer);
		buffer.append(Formatting.Method.parametersEnd);
		buffer.append(Formatting.Expression.lambdaSeperator);
		this.returnType.toString("", buffer);
	}
}

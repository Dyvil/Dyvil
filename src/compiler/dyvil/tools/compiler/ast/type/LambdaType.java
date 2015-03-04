package dyvil.tools.compiler.ast.type;

import java.util.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.util.Util;

public final class LambdaType extends Type implements ITyped, ITypeList
{
	public static final IClass[]	functionClasses	= new IClass[22];
	
	public IType					returnType;
	protected IType[]				parameterTypes	= new IType[2];
	protected int					parameterCount;
	
	public LambdaType()
	{
	}
	
	public LambdaType(IType type)
	{
		this.parameterTypes[0] = type;
		this.parameterCount = 1;
	}
	
	public LambdaType(TupleType tupleType)
	{
		this.parameterTypes = tupleType.types;
		this.parameterCount = tupleType.typeCount;
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
		return 0;
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
		if (this.theClass != null)
		{
			return this.theClass;
		}
		
		IClass iclass = functionClasses[this.parameterCount];
		if (iclass != null)
		{
			this.theClass = iclass;
			return iclass;
		}
		
		iclass = Package.dyvilLangFunction.resolveClass("Function" + this.parameterCount);
		functionClasses[this.parameterCount] = iclass;
		this.theClass = iclass;
		return iclass;
	}
	
	@Override
	public LambdaType resolve(List<Marker> markers, IContext context)
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
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Method.parametersStart);
		Util.astToString(prefix, this.parameterTypes, this.parameterCount, Formatting.Method.parameterSeperator, buffer);
		buffer.append(Formatting.Method.parametersEnd);
		buffer.append(Formatting.Expression.lambdaSeperator);
		this.returnType.toString("", buffer);
	}
}

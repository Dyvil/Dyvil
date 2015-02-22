package dyvil.tools.compiler.ast.type;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.util.Util;

public class LambdaType extends Type implements ITyped, ITypeList
{
	public static IClass[]	functionClasses	= new IClass[22];
	
	public IType		returnType;
	public List<IType>	argumentTypes;
	
	public LambdaType()
	{
		this.argumentTypes = new ArrayList(2);
	}
	
	public LambdaType(IType type)
	{
		this.argumentTypes = new ArrayList(1);
		this.argumentTypes.add(type);
	}
	
	public LambdaType(TupleType tupleType)
	{
		this.argumentTypes = tupleType.types;
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
	
	@Override
	public void setTypes(List<IType> types)
	{
		this.argumentTypes = types;
	}
	
	@Override
	public List<IType> getTypes()
	{
		return this.argumentTypes;
	}
	
	@Override
	public void addType(IType type)
	{
		this.argumentTypes.add(type);
	}
	
	@Override
	public IClass getTheClass()
	{
		if (this.theClass != null)
		{
			return this.theClass;
		}
		
		int len = this.argumentTypes.size();
		IClass iclass = functionClasses[len];
		if (iclass != null)
		{
			this.theClass = iclass;
			return iclass;
		}
		
		iclass = Package.dyvilLangFunction.resolveClass("Function" + len);
		functionClasses[len] = iclass;
		this.theClass = iclass;
		return iclass;
	}
	
	@Override
	public LambdaType resolve(List<Marker> markers, IContext context)
	{
		this.getTheClass();
		
		int len = this.argumentTypes.size();
		for (int i = 0; i < len; i++)
		{
			IType t1 = this.argumentTypes.get(i);
			IType t2 = t1.resolve(markers, context);
			if (t1 != t2)
			{
				this.argumentTypes.set(i, t2);
			}
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		Util.parametersToString(prefix, this.argumentTypes, buffer, false);
		buffer.append(Formatting.Expression.lambdaSeperator);
		this.returnType.toString("", buffer);
	}
}

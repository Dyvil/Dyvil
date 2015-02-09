package dyvil.tools.compiler.ast.type;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.util.Util;

public class TupleType extends Type implements ITypeList
{
	public static IClass[]	tupleClasses	= new IClass[22];
	public static String[]	descriptors		= new String[22];
	
	public List<IType>		types			= new ArrayList(2);
	
	public TupleType()
	{
		this.types = new ArrayList(2);
	}
	
	public TupleType(int size)
	{
		this.types = new ArrayList(size);
	}
	
	@Override
	public boolean isName(String name)
	{
		return false;
	}
	
	@Override
	public void setTypes(List<IType> types)
	{
		this.types = types;
	}
	
	@Override
	public List<IType> getTypes()
	{
		return this.types;
	}
	
	@Override
	public void addType(IType type)
	{
		this.types.add(type);
	}
	
	@Override
	public IClass getTheClass()
	{
		if (this.theClass != null)
		{
			return this.theClass;
		}
		
		int len = this.types.size();
		IClass iclass = tupleClasses[len];
		if (iclass != null)
		{
			this.theClass = iclass;
			return iclass;
		}
		
		iclass = Package.dyvilLangTuple.resolveClass("Tuple" + len);
		tupleClasses[len] = iclass;
		this.theClass = iclass;
		return iclass;
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (type instanceof TupleType)
		{
			TupleType tuple = (TupleType) type;
			
			int len = this.types.size();
			if (len != tuple.types.size())
			{
				return false;
			}
			
			for (int i = 0; i < len; i++)
			{
				IType t1 = this.types.get(i);
				IType t2 = tuple.types.get(i);
				if (!t1.isSuperTypeOf(t2))
				{
					return false;
				}
			}
			return true;
		}
		return OBJECT.classEquals(type);
	}
	
	@Override
	public IType getSuperType()
	{
		return Type.OBJECT;
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		if (type instanceof TupleType)
		{
			TupleType tuple = (TupleType) type;
			
			int len = this.types.size();
			if (len != tuple.types.size())
			{
				return false;
			}
			
			for (int i = 0; i < len; i++)
			{
				IType t1 = this.types.get(i);
				IType t2 = tuple.types.get(i);
				if (!t1.classEquals(t2))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public TupleType resolve(IContext context)
	{
		this.getTheClass();
		
		int len = this.types.size();
		for (int i = 0; i < len; i++)
		{
			IType t1 = this.types.get(i);
			IType t2 = t1.resolve(context);
			if (t1 != t2)
			{
				this.types.set(i, t2);
			}
		}
		return this;
	}
	
	@Override
	public String getInternalName()
	{
		return "dyvil/lang/tuple/Tuple" + this.types.size();
	}
	
	public String getConstructorDescriptor()
	{
		int len = this.types.size();
		if (len < 22)
		{
			String s = descriptors[len];
			if (s != null)
			{
				return s;
			}
		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		for (int i = 0; i < len; i++)
		{
			buffer.append("Ljava/lang/Object;");
		}
		buffer.append(")V");
		
		String s = buffer.toString();
		if (len < 22)
		{
			descriptors[len] = s;
		}
		return s;
	}
	
	@Override
	public String getSignature()
	{
		StringBuilder buf = new StringBuilder();
		this.appendSignature(buf);
		return buf.toString();
	}
	
	@Override
	public void appendSignature(StringBuilder buf)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buf.append('[');
		}
		buf.append('L').append(this.getInternalName());
		buf.append('<');
		for (IType t : this.types)
		{
			t.appendSignature(buf);
		}
		buf.append('>').append(';');
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		Util.parametersToString(this.types, buffer, true);
	}
}

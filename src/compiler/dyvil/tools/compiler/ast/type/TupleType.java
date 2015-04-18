package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.util.Util;

public final class TupleType extends Type implements ITypeList
{
	public static final IClass[]	tupleClasses	= new IClass[22];
	public static final String[]	descriptors		= new String[22];
	
	protected IType[]				types;
	protected int					typeCount;
	
	public TupleType()
	{
		this.types = new IType[2];
	}
	
	public TupleType(int size)
	{
		this.types = new IType[size];
	}
	
	// ITypeList Overrides
	
	@Override
	public int typeCount()
	{
		return this.typeCount;
	}
	
	@Override
	public void setType(int index, IType type)
	{
		this.types[index] = type;
	}
	
	@Override
	public void addType(IType type)
	{
		int index = this.typeCount++;
		if (this.typeCount > this.types.length)
		{
			IType[] temp = new IType[this.typeCount];
			System.arraycopy(this.types, 0, temp, 0, index);
			this.types = temp;
		}
		this.types[index] = type;
	}
	
	@Override
	public IType getType(int index)
	{
		return this.types[index];
	}
	
	// IType Overrides
	
	@Override
	public IClass getTheClass()
	{
		if (this.theClass != null)
		{
			return this.theClass;
		}
		
		IClass iclass = tupleClasses[this.typeCount];
		if (iclass != null)
		{
			this.theClass = iclass;
			return iclass;
		}
		
		iclass = Package.dyvilTuple.resolveClass("Tuple" + this.typeCount);
		tupleClasses[this.typeCount] = iclass;
		this.theClass = iclass;
		return iclass;
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (this.arrayDimensions != type.getArrayDimensions())
		{
			return false;
		}
		
		if (type.isGenericType())
		{
			if (this.theClass != type.getTheClass())
			{
				return false;
			}
			
			GenericType generic = (GenericType) type;
			if (this.typeCount != generic.typeCount())
			{
				return false;
			}
			
			for (int i = 0; i < this.typeCount; i++)
			{
				if (!generic.getType(i).equals(this.types[i]))
				{
					return false;
				}
			}
			return true;
		}
		if (type instanceof TupleType)
		{
			TupleType tuple = (TupleType) type;
			
			if (this.typeCount != tuple.typeCount)
			{
				return false;
			}
			
			for (int i = 0; i < this.typeCount; i++)
			{
				if (!tuple.types[i].equals(this.types[i]))
				{
					return false;
				}
			}
			return true;
		}
		return Types.OBJECT.classEquals(type);
	}
	
	public static boolean isSuperType(IType type, ITyped[] typedArray, int count)
	{
		if (type.isGenericType())
		{
			if (!type.getTheClass().getInternalName().equals("dyvil/tuple/Tuple" + count))
			{
				return false;
			}
			
			GenericType generic = (GenericType) type;
			if (count != generic.typeCount())
			{
				return false;
			}
			
			for (int i = 0; i < count; i++)
			{
				if (!typedArray[i].isType(generic.getType(i)))
				{
					return false;
				}
			}
			return true;
		}
		if (type instanceof TupleType)
		{
			TupleType tuple = (TupleType) type;
			
			if (count != tuple.typeCount)
			{
				return false;
			}
			
			for (int i = 0; i < count; i++)
			{
				if (!typedArray[i].isType(tuple.getType(i)))
				{
					return false;
				}
			}
			return true;
		}
		return type.classEquals(Types.OBJECT) || type.classEquals(Types.ANY);
	}
	
	@Override
	public IType getSuperType()
	{
		return Types.OBJECT;
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		return this.isSuperTypeOf(type);
	}
	
	@Override
	public TupleType resolve(MarkerList markers, IContext context)
	{
		this.getTheClass();
		
		for (int i = 0; i < this.typeCount; i++)
		{
			this.types[i] = this.types[i].resolve(markers, context);
		}
		return this;
	}
	
	public String getConstructorDescriptor()
	{
		int len = this.typeCount;
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
		buffer.append(Formatting.Expression.tupleStart);
		Util.astToString(prefix, this.types, this.typeCount, Formatting.Expression.tupleSeperator, buffer);
		buffer.append(Formatting.Expression.tupleEnd);
	}
}

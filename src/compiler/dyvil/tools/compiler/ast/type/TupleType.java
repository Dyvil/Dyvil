package dyvil.tools.compiler.ast.type;

import java.util.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.util.Util;

public final class TupleType implements IType, ITypeList
{
	public static final IClass[]	tupleClasses	= new IClass[22];
	public static final String[]	descriptors		= new String[22];
	
	private int						arrayDimensions;
	
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
	
	public static String getConstructorDescriptor(int typeCount)
	{
		if (typeCount < 22)
		{
			String s = descriptors[typeCount];
			if (s != null)
			{
				return s;
			}
		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		for (int i = 0; i < typeCount; i++)
		{
			buffer.append("Ljava/lang/Object;");
		}
		buffer.append(")V");
		
		String s = buffer.toString();
		if (typeCount < 22)
		{
			descriptors[typeCount] = s;
		}
		return s;
	}
	
	@Override
	public int typeTag()
	{
		return TUPLE_TYPE;
	}
	
	@Override
	public IClass getTheClass()
	{
		IClass iclass = tupleClasses[this.typeCount];
		if (iclass != null)
		{
			return iclass;
		}
		
		iclass = Package.dyvilTuple.resolveClass("Tuple" + this.typeCount);
		tupleClasses[this.typeCount] = iclass;
		return iclass;
	}
	
	@Override
	public void setName(Name name)
	{
	}
	
	@Override
	public Name getName()
	{
		return null;
	}
	
	@Override
	public void setClass(IClass theClass)
	{
	}
	
	@Override
	public void setArrayDimensions(int dimensions)
	{
		this.arrayDimensions = dimensions;
	}
	
	@Override
	public int getArrayDimensions()
	{
		return this.arrayDimensions;
	}
	
	@Override
	public boolean isArrayType()
	{
		return this.arrayDimensions > 0;
	}
	
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
	public boolean isSuperTypeOf(IType type)
	{
		if (this.arrayDimensions != type.getArrayDimensions())
		{
			return false;
		}
		
		if (type.isGenericType())
		{
			if (this.getTheClass() != type.getTheClass())
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
	public boolean hasTypeVariables()
	{
		return false;
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		TupleType tt = new TupleType(this.typeCount);
		for (int i = 0; i < this.typeCount; i++)
		{
			tt.types[i] = this.types[i].getConcreteType(context);
		}
		return tt;
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		return null;
	}
	
	@Override
	public boolean isResolved()
	{
		return false;
	}
	
	@Override
	public TupleType resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.typeCount; i++)
		{
			IType t = this.types[i].resolve(markers, context);
			
			if (t.isPrimitive())
			{
				this.types[i] = t.getReferenceType();
			}
			else
			{
				this.types[i] = t;
			}
		}
		return this;
	}
	
	@Override
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public IClass getThisClass()
	{
		return this.getTheClass();
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return null;
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return null;
	}
	
	@Override
	public IField resolveField(Name name)
	{
		return this.getTheClass().resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.getTheClass().getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		this.getTheClass().getConstructorMatches(list, arguments);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return this.getTheClass().getAccessibility(member);
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}
	
	@Override
	public void setInternalName(String name)
	{
	}
	
	@Override
	public String getInternalName()
	{
		return this.getTheClass().getInternalName();
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		buffer.append('L').append(this.getInternalName()).append(';');
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
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		buffer.append(Formatting.Expression.tupleStart);
		Util.astToString(prefix, this.types, this.typeCount, Formatting.Expression.tupleSeperator, buffer);
		buffer.append(Formatting.Expression.tupleEnd);
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append(']');
		}
	}
	
	@Override
	public IType clone()
	{
		TupleType tt = new TupleType(this.typeCount);
		System.arraycopy(this.types, 0, tt.types, 0, this.typeCount);
		return tt;
	}
}

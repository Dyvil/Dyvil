package dyvil.reflect.type;

import dyvil.lang.Type;

public class PrimitiveType implements Type
{
	public static final int					VOID	= 0;
	public static final int					BOOLEAN	= 1;
	public static final int					BYTE	= 2;
	public static final int					SHORT	= 3;
	public static final int					CHAR	= 4;
	public static final int					INT		= 5;
	public static final int					LONG	= 6;
	public static final int					FLOAT	= 7;
	public static final int					DOUBLE	= 8;
	
	private static final PrimitiveType[]	LOOKUP	= new PrimitiveType[9];
	
	private final int						id;
	private final Class						theClass;
	
	static
	{
		for (int i = VOID; i <= DOUBLE; i++)
		{
			LOOKUP[i] = new PrimitiveType(i);
		}
	}
	
	public static PrimitiveType apply(int id)
	{
		return LOOKUP[id];
	}
	
	protected PrimitiveType(int id)
	{
		this.id = id;
		switch (id)
		{
		case VOID:
			this.theClass = void.class;
			return;
		case BOOLEAN:
			this.theClass = boolean.class;
			return;
		case BYTE:
			this.theClass = byte.class;
			return;
		case SHORT:
			this.theClass = short.class;
			return;
		case CHAR:
			this.theClass = char.class;
			return;
		case INT:
			this.theClass = int.class;
			return;
		case LONG:
			this.theClass = long.class;
			return;
		case FLOAT:
			this.theClass = float.class;
			return;
		case DOUBLE:
			this.theClass = double.class;
			return;
		default:
			throw new IllegalArgumentException("id");
		}
	}
	
	public PrimitiveType(Class theClass)
	{
		this.theClass = theClass;
		if (theClass == void.class)
		{
			this.id = VOID;
			return;
		}
		if (theClass == boolean.class)
		{
			this.id = BOOLEAN;
			return;
		}
		if (theClass == byte.class)
		{
			this.id = BYTE;
			return;
		}
		if (theClass == short.class)
		{
			this.id = SHORT;
			return;
		}
		if (theClass == char.class)
		{
			this.id = CHAR;
			return;
		}
		if (theClass == int.class)
		{
			this.id = INT;
			return;
		}
		if (theClass == long.class)
		{
			this.id = LONG;
			return;
		}
		if (theClass == float.class)
		{
			this.id = FLOAT;
			return;
		}
		if (theClass == double.class)
		{
			this.id = DOUBLE;
			return;
		}
		throw new IllegalAccessError("class");
	}
	
	@Override
	public String getName()
	{
		switch (this.id)
		{
		case VOID:
			return "void";
		case BOOLEAN:
			return "boolean";
		case BYTE:
			return "byte";
		case SHORT:
			return "short";
		case CHAR:
			return "char";
		case INT:
			return "int";
		case LONG:
			return "long";
		case FLOAT:
			return "float";
		case DOUBLE:
			return "double";
		}
		return null;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.getName();
	}
	
	@Override
	public Class getTheClass()
	{
		return this.theClass;
	}
	
	@Override
	public String toString()
	{
		return this.getName();
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		switch (this.id)
		{
		case VOID:
			builder.append('V');
			return;
		case BOOLEAN:
			builder.append('Z');
			return;
		case BYTE:
			builder.append('B');
			return;
		case SHORT:
			builder.append('S');
			return;
		case CHAR:
			builder.append('C');
			return;
		case INT:
			builder.append('I');
			return;
		case LONG:
			builder.append('J');
			return;
		case FLOAT:
			builder.append('F');
			return;
		case DOUBLE:
			builder.append('D');
			return;
		}
	}
	
	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		switch (this.id)
		{
		case VOID:
			builder.append("Ldyvil/lang/Void;");
			return;
		case BOOLEAN:
			builder.append("Ldyvil/lang/Boolean;");
			return;
		case BYTE:
			builder.append("Ldyvil/lang/Byte;");
			return;
		case SHORT:
			builder.append("Ldyvil/lang/Short;");
			return;
		case CHAR:
			builder.append("Ldyvil/lang/Char;");
			return;
		case INT:
			builder.append("Ldyvil/lang/Int;");
			return;
		case LONG:
			builder.append("Ldyvil/lang/Long;");
			return;
		case FLOAT:
			builder.append("Ldyvil/lang/Float;");
			return;
		case DOUBLE:
			builder.append("Ldyvil/lang/Double;");
			return;
		}
	}
}

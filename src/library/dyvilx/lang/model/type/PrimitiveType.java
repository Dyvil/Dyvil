package dyvilx.lang.model.type;

public class PrimitiveType implements Type
{
	public static final int VOID_CODE    = 0;
	public static final int BOOLEAN_CODE = 1;
	public static final int BYTE_CODE    = 2;
	public static final int SHORT_CODE   = 3;
	public static final int CHAR_CODE    = 4;
	public static final int INT_CODE     = 5;
	public static final int LONG_CODE    = 6;
	public static final int FLOAT_CODE   = 7;
	public static final int DOUBLE_CODE  = 8;
	
	private static final PrimitiveType[] LOOKUP = new PrimitiveType[9];
	
	private final int   id;
	private final Class theClass;
	
	static
	{
		for (int i = VOID_CODE; i <= DOUBLE_CODE; i++)
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
		case VOID_CODE:
			this.theClass = void.class;
			return;
		case BOOLEAN_CODE:
			this.theClass = boolean.class;
			return;
		case BYTE_CODE:
			this.theClass = byte.class;
			return;
		case SHORT_CODE:
			this.theClass = short.class;
			return;
		case CHAR_CODE:
			this.theClass = char.class;
			return;
		case INT_CODE:
			this.theClass = int.class;
			return;
		case LONG_CODE:
			this.theClass = long.class;
			return;
		case FLOAT_CODE:
			this.theClass = float.class;
			return;
		case DOUBLE_CODE:
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
			this.id = VOID_CODE;
			return;
		}
		if (theClass == boolean.class)
		{
			this.id = BOOLEAN_CODE;
			return;
		}
		if (theClass == byte.class)
		{
			this.id = BYTE_CODE;
			return;
		}
		if (theClass == short.class)
		{
			this.id = SHORT_CODE;
			return;
		}
		if (theClass == char.class)
		{
			this.id = CHAR_CODE;
			return;
		}
		if (theClass == int.class)
		{
			this.id = INT_CODE;
			return;
		}
		if (theClass == long.class)
		{
			this.id = LONG_CODE;
			return;
		}
		if (theClass == float.class)
		{
			this.id = FLOAT_CODE;
			return;
		}
		if (theClass == double.class)
		{
			this.id = DOUBLE_CODE;
			return;
		}
		throw new IllegalAccessError("class");
	}
	
	@Override
	public String name()
	{
		switch (this.id)
		{
		case VOID_CODE:
			return "void";
		case BOOLEAN_CODE:
			return "boolean";
		case BYTE_CODE:
			return "byte";
		case SHORT_CODE:
			return "short";
		case CHAR_CODE:
			return "char";
		case INT_CODE:
			return "int";
		case LONG_CODE:
			return "long";
		case FLOAT_CODE:
			return "float";
		case DOUBLE_CODE:
			return "double";
		}
		return null;
	}
	
	@Override
	public String qualifiedName()
	{
		return this.name();
	}
	
	@Override
	public Class erasure()
	{
		return this.theClass;
	}
	
	@Override
	public String toString()
	{
		return this.name();
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		switch (this.id)
		{
		case VOID_CODE:
			builder.append('V');
			return;
		case BOOLEAN_CODE:
			builder.append('Z');
			return;
		case BYTE_CODE:
			builder.append('B');
			return;
		case SHORT_CODE:
			builder.append('S');
			return;
		case CHAR_CODE:
			builder.append('C');
			return;
		case INT_CODE:
			builder.append('I');
			return;
		case LONG_CODE:
			builder.append('J');
			return;
		case FLOAT_CODE:
			builder.append('F');
			return;
		case DOUBLE_CODE:
			builder.append('D');
			return;
		}
	}
	
	@Override
	public void appendGenericSignature(StringBuilder builder)
	{
		switch (this.id)
		{
		case VOID_CODE:
			builder.append("Ldyvil/lang/Void;");
			return;
		case BOOLEAN_CODE:
			builder.append("Ldyvil/lang/Boolean;");
			return;
		case BYTE_CODE:
			builder.append("Ldyvil/lang/Byte;");
			return;
		case SHORT_CODE:
			builder.append("Ldyvil/lang/Short;");
			return;
		case CHAR_CODE:
			builder.append("Ldyvil/lang/Char;");
			return;
		case INT_CODE:
			builder.append("Ldyvil/lang/Int;");
			return;
		case LONG_CODE:
			builder.append("Ldyvil/lang/Long;");
			return;
		case FLOAT_CODE:
			builder.append("Ldyvil/lang/Float;");
			return;
		case DOUBLE_CODE:
			builder.append("Ldyvil/lang/Double;");
			return;
		}
	}
}

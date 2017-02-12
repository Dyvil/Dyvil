package dyvil.reflect.types;

public final class PrimitiveType implements Type
{
	private static final int VOID_CODE    = 0;
	private static final int BOOLEAN_CODE = 1;
	private static final int BYTE_CODE    = 2;
	private static final int SHORT_CODE   = 3;
	private static final int CHAR_CODE    = 4;
	private static final int INT_CODE     = 5;
	private static final int LONG_CODE    = 6;
	private static final int FLOAT_CODE   = 7;
	private static final int DOUBLE_CODE  = 8;
	
	private static final PrimitiveType[] LOOKUP = new PrimitiveType[9];

	public static final PrimitiveType VOID    = new PrimitiveType(VOID_CODE, void.class);
	public static final PrimitiveType BOOLEAN = new PrimitiveType(BOOLEAN_CODE, boolean.class);
	public static final PrimitiveType BYTE    = new PrimitiveType(BYTE_CODE, byte.class);
	public static final PrimitiveType SHORT   = new PrimitiveType(SHORT_CODE, short.class);
	public static final PrimitiveType CHAR    = new PrimitiveType(CHAR_CODE, char.class);
	public static final PrimitiveType INT     = new PrimitiveType(INT_CODE, int.class);
	public static final PrimitiveType LONG    = new PrimitiveType(LONG_CODE, long.class);
	public static final PrimitiveType FLOAT   = new PrimitiveType(FLOAT_CODE, float.class);
	public static final PrimitiveType DOUBLE  = new PrimitiveType(DOUBLE_CODE, double.class);

	private final int   id;
	private final Class theClass;

	public static PrimitiveType apply(Class<?> theClass)
	{
		for (PrimitiveType type : LOOKUP)
		{
			if (type.theClass == theClass)
			{
				return type;
			}
		}
		return null;
	}
	
	private PrimitiveType(int id, Class<?> theClass)
	{
		this.id = id;
		this.theClass = theClass;

		LOOKUP[id] = this;
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
			builder.append("Ljava/lang/Void;");
			return;
		case BOOLEAN_CODE:
			builder.append("Ljava/lang/Boolean;");
			return;
		case BYTE_CODE:
			builder.append("Ljava/lang/Byte;");
			return;
		case SHORT_CODE:
			builder.append("Ljava/lang/Short;");
			return;
		case CHAR_CODE:
			builder.append("Ljava/lang/Char;");
			return;
		case INT_CODE:
			builder.append("Ljava/lang/Integer;");
			return;
		case LONG_CODE:
			builder.append("Ljava/lang/Long;");
			return;
		case FLOAT_CODE:
			builder.append("Ljava/lang/Float;");
			return;
		case DOUBLE_CODE:
			builder.append("Ljava/lang/Double;");
			return;
		}
	}
}

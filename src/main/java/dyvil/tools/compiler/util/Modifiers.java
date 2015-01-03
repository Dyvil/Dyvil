package dyvil.tools.compiler.util;

public enum Modifiers
{
	ACCESS, CLASS_TYPE, CLASS, INTERFACE, FIELD, METHOD, FIELD_OR_METHOD, PARAMETER;
	
	public String toString(int mod)
	{
		StringBuilder sb = new StringBuilder();
		switch (this)
		{
		case ACCESS:
			writeAccessModifiers(mod, sb);
			break;
		case CLASS_TYPE:
			writeClassTypeModifiers(mod, sb);
			break;
		case CLASS:
			writeAccessModifiers(mod, sb);
			writeClassModifiers(mod, sb);
			break;
		case INTERFACE:
			writeAccessModifiers(mod, sb);
			writeInterfaceModifiers(mod, sb);
			break;
		case FIELD_OR_METHOD:
			writeAccessModifiers(mod, sb);
			writeFieldOrMethodModifiers(mod, sb);
			break;
		case FIELD:
			writeAccessModifiers(mod, sb);
			writeFieldModifiers(mod, sb);
			break;
		case METHOD:
			writeAccessModifiers(mod, sb);
			writeMethodModifiers(mod, sb);
			break;
		case PARAMETER:
			writeParameterModifier(mod, sb);
			break;
		}
		return sb.toString();
	}
	
	public int parse(String mod)
	{
		int m = 0;
		switch (this)
		{
		case ACCESS:
			m = readAccessModifier(mod);
			break;
		case CLASS_TYPE:
			m = readClassTypeModifier(mod);
			break;
		case CLASS:
			if ((m = readAccessModifier(mod)) == -1)
			{
				m = readClassModifier(mod);
			}
			break;
		case INTERFACE:
			if ((m = readAccessModifier(mod)) == -1)
			{
				m = readInterfaceModifier(mod);
			}
			break;
		case FIELD_OR_METHOD:
			if ((m = readAccessModifier(mod)) == -1)
			{
				if ((m = readFieldModifier(mod)) == -1)
				{
					m = readMethodModifier(mod);
				}
			}
			break;
		case FIELD:
			if ((m = readAccessModifier(mod)) == -1)
			{
				m = readFieldModifier(mod);
			}
			break;
		case METHOD:
			if ((m = readAccessModifier(mod)) == -1)
			{
				m = readMethodModifier(mod);
			}
			break;
		case PARAMETER:
			if ((m = readAccessModifier(mod)) == -1)
			{
				m = readParameterModifier(mod);
			}
			break;
		}
		return m;
	}
	
	public static final int	PACKAGE					= 0x00000000;
	public static final int	PUBLIC					= 0x00000001;
	public static final int	PRIVATE					= 0x00000002;
	public static final int	PROTECTED				= 0x00000004;
	
	/**
	 * Dyvil derived access modifier.
	 */
	public static final int	DERIVED					= PRIVATE | PROTECTED;
	
	public static final int	STATIC					= 0x00000008;
	public static final int	FINAL					= 0x00000010;
	
	/**
	 * Dyvil constant modifier. This modifier is just a shortcut for
	 * {@code static final}.
	 */
	public static final int	CONST					= STATIC | FINAL;
	
	public static final int	SYNCHRONIZED			= 0x00000020;
	public static final int	VOLATILE				= 0x00000040;
	public static final int	BRIDGE					= 0x00000040;
	public static final int	TRANSIENT				= 0x00000080;
	public static final int	VARARGS					= 0x00000080;
	public static final int	NATIVE					= 0x00000100;
	
	public static final int	INTERFACE_CLASS			= 0x00000200;
	public static final int	ABSTRACT				= 0x00000400;
	
	/**
	 * Strictfp modifier. This is used for classes and methods and marks that
	 * floating point numbers (floats and doubles) have to be handled specially.
	 */
	public static final int	STRICT					= 0x00000800;
	
	/**
	 * Synthetic modifier. This is used for fields of inner classes that hold
	 * the outer class.
	 */
	public static final int	SYNTHETIC				= 0x00001000;
	public static final int	ANNOTATION				= 0x00002000;
	public static final int	ENUM_CLASS				= 0x00004000;
	
	/**
	 * Mandated modifier. This is used for constructors of inner classes that
	 * have the outer class as a parameter.
	 */
	public static final int	MANDATED				= 0x00008000;
	
	/**
	 * Dyvil lazy modifier. If a field is marked with this modifier, it will be
	 * evaluated every time it is demanded and is thus not saved in the memory.
	 * This behavior can be compared with a method without parameters.
	 */
	public static final int	LAZY					= 0x00010000;
	
	/**
	 * Dyvil inline modifier. If a method is marked with this modifier, it will
	 * be inlined by the compiler to reduce method call overhead.
	 */
	public static final int	INLINE					= 0x00010000;
	
	/**
	 * Dyvil object modifier. If a class is marked with this modifier, it is a
	 * singleton object class.
	 */
	public static final int	OBJECT_CLASS			= 0x00010000;
	
	/**
	 * Dyvil implicit modifier. If a method is marked with this modifier, it is
	 * a method that can be called on any Object and virtually has the instance
	 * as the first parameter. An implicit method is always static.
	 */
	public static final int	IMPLICIT				= 0x00020000 | STATIC;
	
	/**
	 * Dyvil module modifier. If a class is marked with this modifier, it is a
	 * module class.
	 */
	public static final int	MODULE					= 0x00020000;
	
	/**
	 * Dyvil ref modifier. This is used to mark that a parameter is
	 * Call-By-Reference. If a parameter doesn't have this flag, it is
	 * Call-By-Value.
	 */
	public static final int	BYREF					= 0x00040000;
	
	/**
	 * Dyvil prefix modifier.
	 */
	public static final int	PREFIX					= 0x00040000;
	
	/**
	 * Dyvil override modifier. This modifier is a shortcut for the @Override
	 * annotation.
	 */
	public static final int	OVERRIDE				= 0x00080000;
	
	public static final int	CLASS_TYPE_MODIFIERS	= INTERFACE_CLASS | ANNOTATION | ENUM_CLASS | OBJECT_CLASS | MODULE;
	public static final int	ACCESS_MODIFIERS		= PUBLIC | PROTECTED | PRIVATE;
	public static final int	MEMBER_MODIFIERS		= ACCESS_MODIFIERS | STATIC | FINAL;
	public static final int	CLASS_MODIFIERS			= MEMBER_MODIFIERS | ABSTRACT | STRICT;
	public static final int	INTERFACE_MODIFIERS		= ACCESS_MODIFIERS | ABSTRACT | STATIC | STRICT;
	
	public static final int	FIELD_MODIFIERS			= MEMBER_MODIFIERS | TRANSIENT | VOLATILE | LAZY | SYNTHETIC;
	public static final int	METHOD_MODIFIERS		= MEMBER_MODIFIERS | SYNCHRONIZED | NATIVE | STRICT | INLINE | IMPLICIT | PREFIX | BRIDGE | VARARGS | MANDATED;
	public static final int	PARAMETER_MODIFIERS		= FINAL | BYREF;
	
	private static void writeAccessModifiers(int mod, StringBuilder sb)
	{
		if ((mod & PUBLIC) == PUBLIC)
		{
			sb.append("public ");
		}
		
		if ((mod & DERIVED) == DERIVED)
		{
			sb.append("derived ");
		}
		else
		{
			if ((mod & PROTECTED) == PROTECTED)
			{
				sb.append("protected ");
			}
			if ((mod & PRIVATE) == PRIVATE)
			{
				sb.append("private ");
			}
		}
	}
	
	private static void writeClassTypeModifiers(int mod, StringBuilder sb)
	{
		if (mod == 0)
		{
			sb.append("class ");
		}
		else if ((mod & INTERFACE_CLASS) == INTERFACE_CLASS)
		{
			sb.append("interface ");
		}
		else if ((mod & ANNOTATION) == ANNOTATION)
		{
			sb.append("annotation ");
		}
		else if ((mod & ENUM_CLASS) == ENUM_CLASS)
		{
			sb.append("enum ");
		}
		else if ((mod & OBJECT_CLASS) == OBJECT_CLASS)
		{
			sb.append("object ");
		}
		else if ((mod & MODULE) == MODULE)
		{
			sb.append("module ");
		}
		else
		{
			sb.append("class ");
		}
	}
	
	private static void writeClassModifiers(int mod, StringBuilder sb)
	{
		if ((mod & STATIC) == STATIC)
		{
			sb.append("static ");
		}
		if ((mod & ABSTRACT) == ABSTRACT)
		{
			sb.append("abstract ");
		}
		if ((mod & FINAL) == FINAL)
		{
			sb.append("final ");
		}
		if ((mod & STRICT) == STRICT)
		{
			sb.append("strictfp ");
		}
	}
	
	private static void writeInterfaceModifiers(int mod, StringBuilder sb)
	{
		if ((mod & STATIC) == STATIC)
		{
			sb.append("static ");
		}
		if ((mod & ABSTRACT) == ABSTRACT)
		{
			sb.append("abstract ");
		}
		if ((mod & STRICT) == STRICT)
		{
			sb.append("strictfp ");
		}
	}
	
	private static void writeFieldOrMethodModifiers(int mod, StringBuilder sb)
	{
		if ((mod & STATIC) == STATIC)
		{
			sb.append("static ");
		}
		if ((mod & FINAL) == FINAL)
		{
			sb.append("final ");
		}
		
		if ((mod & TRANSIENT) == TRANSIENT)
		{
			sb.append("transient ");
		}
		if ((mod & VOLATILE) == VOLATILE)
		{
			sb.append("volatile ");
		}
		if ((mod & LAZY) == LAZY)
		{
			sb.append("lazy ");
		}
		
		if ((mod & SYNCHRONIZED) == SYNCHRONIZED)
		{
			sb.append("synchronized ");
		}
		if ((mod & NATIVE) == NATIVE)
		{
			sb.append("native ");
		}
		if ((mod & STRICT) == STRICT)
		{
			sb.append("strictfp ");
		}
		if ((mod & INLINE) == INLINE)
		{
			sb.append("inline ");
		}
		if ((mod & IMPLICIT) == IMPLICIT)
		{
			sb.append("implicit ");
		}
		if ((mod & OVERRIDE) == OVERRIDE)
		{
			sb.append("override ");
		}
	}
	
	private static void writeFieldModifiers(int mod, StringBuilder sb)
	{
		if ((mod & CONST) == CONST)
		{
			sb.append("const ");
		}
		else
		{
			if ((mod & STATIC) == STATIC)
			{
				sb.append("static ");
			}
			if ((mod & FINAL) == FINAL)
			{
				sb.append("final ");
			}
		}
		
		if ((mod & TRANSIENT) == TRANSIENT)
		{
			sb.append("transient ");
		}
		if ((mod & VOLATILE) == VOLATILE)
		{
			sb.append("volatile ");
		}
		if ((mod & LAZY) == LAZY)
		{
			sb.append("lazy ");
		}
	}
	
	private static void writeMethodModifiers(int mod, StringBuilder sb)
	{
		if ((mod & STATIC) == STATIC)
		{
			sb.append("static ");
		}
		if ((mod & FINAL) == FINAL)
		{
			sb.append("final ");
		}
		
		if ((mod & SYNCHRONIZED) == SYNCHRONIZED)
		{
			sb.append("synchronized ");
		}
		if ((mod & NATIVE) == NATIVE)
		{
			sb.append("native ");
		}
		if ((mod & STRICT) == STRICT)
		{
			sb.append("strictfp ");
		}
		if ((mod & INLINE) == INLINE)
		{
			sb.append("inline ");
		}
		if ((mod & IMPLICIT) == IMPLICIT)
		{
			sb.append("implicit ");
		}
		if ((mod & PREFIX) == PREFIX)
		{
			sb.append("prefix ");
		}
		if ((mod & OVERRIDE) == OVERRIDE)
		{
			sb.append("override ");
		}
	}
	
	private static void writeParameterModifier(int mod, StringBuilder sb)
	{
		if ((mod & FINAL) == FINAL)
		{
			sb.append("final ");
		}
		if ((mod & BYREF) == BYREF)
		{
			sb.append("byref ");
		}
	}
	
	private static int readAccessModifier(String mod)
	{
		switch (mod)
		{
		case "package":
			return PACKAGE;
		case "public":
			return PUBLIC;
		case "private":
			return PRIVATE;
		case "protected":
			return PROTECTED;
		case "derived":
			return DERIVED;
		}
		return -1;
	}
	
	private static int readClassTypeModifier(String mod)
	{
		switch (mod)
		{
		case "class":
			return 0;
		case "interface":
			return INTERFACE_CLASS;
		case "annotation":
			return ANNOTATION;
		case "enum":
			return ENUM_CLASS;
		case "object":
			return OBJECT_CLASS;
		case "module":
			return MODULE;
		}
		return -1;
	}
	
	private static int readClassModifier(String mod)
	{
		switch (mod)
		{
		case "static":
			return STATIC;
		case "abstract":
			return ABSTRACT;
		case "final":
			return FINAL;
		case "strictfp":
			return STRICT;
		}
		return -1;
	}
	
	private static int readInterfaceModifier(String mod)
	{
		switch (mod)
		{
		case "static":
			return STATIC;
		case "strictfp":
			return STRICT;
		}
		return -1;
	}
	
	private static int readFieldModifier(String mod)
	{
		switch (mod)
		{
		case "static":
			return STATIC;
		case "final":
			return FINAL;
		case "const":
			return CONST;
		case "transient":
			return TRANSIENT;
		case "volatile":
			return VOLATILE;
		case "lazy":
			return LAZY;
		}
		return -1;
	}
	
	private static int readMethodModifier(String mod)
	{
		switch (mod)
		{
		case "static":
			return STATIC;
		case "final":
			return FINAL;
		case "const":
			return CONST;
		case "synchronized":
			return SYNCHRONIZED;
		case "native":
			return NATIVE;
		case "strictfp":
			return STRICT;
		case "inline":
			return INLINE;
		case "implicit":
			return IMPLICIT;
		case "prefix":
			return PREFIX;
		case "override":
			return OVERRIDE;
		}
		return -1;
	}
	
	private static int readParameterModifier(String mod)
	{
		switch (mod)
		{
		case "final":
			return FINAL;
		case "const":
			return FINAL;
		case "byref":
			return BYREF;
		}
		return -1;
	}
}

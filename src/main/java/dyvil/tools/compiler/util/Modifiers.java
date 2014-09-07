package dyvil.tools.compiler.util;

public class Modifiers
{
	public static String toString(int mod)
	{
		StringBuilder sb = new StringBuilder();
		int len;
		
		if ((mod & PUBLIC) != 0)
			sb.append("public ");
		
		if ((mod & DERIVED) != 0)
		{
			sb.append("derived ");
		}
		else
		{
			if ((mod & PROTECTED) != 0)
				sb.append("protected ");
			if ((mod & PRIVATE) != 0)
				sb.append("private ");
		}
		
		/* Canonical order */
		if ((mod & ABSTRACT) != 0)
			sb.append("abstract ");
		
		if ((mod & CONST) != 0)
		{
			sb.append("const ");
		}
		else
		{
			if ((mod & STATIC) != 0)
				sb.append("static ");
			if ((mod & FINAL) != 0)
				sb.append("final ");
		}
		
		if ((mod & TRANSIENT) != 0)
			sb.append("transient ");
		if ((mod & VOLATILE) != 0)
			sb.append("volatile ");
		if ((mod & SYNCHRONIZED) != 0)
			sb.append("synchronized ");
		if ((mod & NATIVE) != 0)
			sb.append("native ");
		if ((mod & STRICT) != 0)
			sb.append("strictfp ");
		if ((mod & INTERFACE) != 0)
			sb.append("interface ");
		if ((mod & LAZY) != 0)
			sb.append("lazy ");
		if ((mod & INLINE) != 0)
			sb.append("inline ");
		if ((mod & IMPLICIT) != 0)
			sb.append("implicit ");
		if ((mod & BYREF) != 0)
			sb.append("ref ");
		
		if ((len = sb.length()) > 0) /* trim trailing space */
			return sb.toString().substring(0, len - 1);
		return "";
	}
	
	public static final int	PACKAGE				= 0x00000000;
	public static final int	PUBLIC				= 0x00000001;
	public static final int	PRIVATE				= 0x00000002;
	public static final int	PROTECTED			= 0x00000004;
	
	/**
	 * Dyvil derived access modifier.
	 */
	public static final int	DERIVED				= PRIVATE | PROTECTED;
	
	public static final int	STATIC				= 0x00000008;
	public static final int	FINAL				= 0x00000010;
	
	/**
	 * Dyvil constant modifier. This modifier is just a shortcut for
	 * {@code static final}.
	 */
	public static final int	CONST				= STATIC | FINAL;
	
	public static final int	SYNCHRONIZED		= 0x00000020;
	public static final int	VOLATILE			= 0x00000040;
	public static final int	BRIDGE				= 0x00000040;
	public static final int	TRANSIENT			= 0x00000080;
	public static final int	VARARGS				= 0x00000080;
	public static final int	NATIVE				= 0x00000100;
	static final int		INTERFACE			= 0x00000200;
	static final int		ABSTRACT			= 0x00000400;
	
	/**
	 * Strictfp modifier. This is used for classes and methods and marks that
	 * floating point numbers (floats and doubles) have to be handled specially.
	 */
	public static final int	STRICT				= 0x00000800;
	
	/**
	 * Synthetic modifier. This is used for fields of inner classes that hold
	 * the outer class.
	 */
	public static final int	SYNTHETIC			= 0x00001000;
	public static final int	ANNOTATION			= 0x00002000;
	public static final int	ENUM				= 0x00004000;
	
	/**
	 * Mandated modifier. This is used for constructors of inner classes that
	 * have the outer class as a parameter.
	 */
	public static final int	MANDATED			= 0x00008000;
	
	/**
	 * Dyvil lazy modifier. If a field is marked with this modifier, it is going
	 * to be evaluated / calculated every time it is demanded and is thus not
	 * saved in the memory. This behavior can be compared with a method without
	 * parameters.
	 */
	public static final int	LAZY				= 0x00010000;
	
	/**
	 * Dyvil inline modifier. If a method is marked with this modifier, it will
	 * be inlined by the compiler to reduce method call overhead.
	 */
	public static final int	INLINE				= 0x00010000;
	
	/**
	 * Dyvil implicit modifier. If a method is marked with this modifier, it is
	 * a method that can be called on any Object and virtually has the instance
	 * as the first parameter.
	 */
	public static final int	IMPLICIT			= 0x00020000;
	
	/**
	 * Dyvil ref modifier. This is used to mark that a parameter is
	 * Call-By-Reference. If a parameter doesn't have this flag, it is
	 * Call-By-Value.
	 */
	public static final int	BYREF				= 0x00040000;
	
	public static final int	ACCESS_MODIFIERS	= PUBLIC | PROTECTED | PRIVATE;
	public static final int	CLASS_MODIFIERS		= PUBLIC | PROTECTED | PRIVATE | STATIC | FINAL | STRICT;
	public static final int	INTERFACE_MODIFIERS	= PUBLIC | PROTECTED | PRIVATE | STATIC | STRICT;
	
	public static final int	FIELD_MODIFIERS		= PUBLIC | PROTECTED | PRIVATE | STATIC | FINAL | TRANSIENT | VOLATILE | LAZY;
	public static final int	METHOD_MODIFIERS	= PUBLIC | PROTECTED | PRIVATE | STATIC | FINAL | SYNCHRONIZED | NATIVE | STRICT | INLINE | IMPLICIT;
	public static final int	PARAMETER_MODIFIERS	= FINAL | BYREF;
	
	public static int parseModifier(String mod)
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
		case "static":
			return STATIC;
		case "final":
			return FINAL;
		case "const":
			return CONST;
		case "synchronized":
			return SYNCHRONIZED;
		case "volatile":
			return VOLATILE;
		case "transient":
			return TRANSIENT;
		case "native":
			return NATIVE;
		case "strictfp":
			return STRICT;
		case "lazy":
			return LAZY;
		case "inline":
			return INLINE;
		case "implicit":
			return IMPLICIT;
		case "ref":
			return BYREF;
		}
		return -1;
	}
	
	public static int parseAccessModifier(String mod)
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
	
	public static int parseClassModifier(String mod)
	{
		int i = parseAccessModifier(mod);
		if (i != -1)
			return i;
		switch (mod)
		{
		case "static":
			return STATIC;
		case "final":
			return FINAL;
		case "strictfp":
			return STRICT;
		}
		return -1;
	}
	
	public static int parseInterfaceModifier(String mod)
	{
		int i = parseAccessModifier(mod);
		if (i != -1)
			return i;
		switch (mod)
		{
		case "static":
			return STATIC;
		case "strictfp":
			return STRICT;
		}
		return -1;
	}
	
	public static int parseFieldModifier(String mod)
	{
		int i = parseAccessModifier(mod);
		if (i != -1)
			return i;
		switch (mod)
		{
		case "static":
			return STATIC;
		case "final":
			return FINAL;
		case "const":
			return CONST;
		case "volatile":
			return VOLATILE;
		case "transient":
			return TRANSIENT;
		case "lazy":
			return LAZY;
		}
		return -1;
	}
	
	public static int parseMethodModifier(String mod)
	{
		int i = parseAccessModifier(mod);
		if (i != -1)
			return i;
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
		}
		return -1;
	}
	
	public static int parseParameterModifier(String mod)
	{
		switch (mod)
		{
		case "final":
			return FINAL;
		case "const":
			return CONST;
		case "ref":
			return BYREF;
		}
		return -1;
	}
}

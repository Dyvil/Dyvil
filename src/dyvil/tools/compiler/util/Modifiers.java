package dyvil.tools.compiler.util;

import java.lang.reflect.Modifier;

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
		
		if ((len = sb.length()) > 0) /* trim trailing space */
			return sb.toString().substring(0, len - 1);
		return "";
	}
	
	public static final int	PACKAGE				= 0x00000000;
	public static final int	PUBLIC				= 0x00000001;
	public static final int	PRIVATE				= 0x00000002;
	public static final int	PROTECTED			= 0x00000004;
	// Dyvil derived
	public static final int	DERIVED				= PRIVATE | PROTECTED;
	
	public static final int	STATIC				= 0x00000008;
	public static final int	FINAL				= 0x00000010;
	// Dyvil const
	public static final int	CONST				= STATIC | FINAL;
	
	public static final int	SYNCHRONIZED		= 0x00000020;
	public static final int	VOLATILE			= 0x00000040;
	public static final int	TRANSIENT			= 0x00000080;
	public static final int	NATIVE				= 0x00000100;
	// Non-Dyvil modifiers
	static final int		INTERFACE			= 0x00000200;
	static final int		ABSTRACT			= 0x00000400;
	// Strictfp
	public static final int	STRICT				= 0x00000800;
	// No real modifiers
	public static final int	BRIDGE				= 0x00000040;
	public static final int	VARARGS				= 0x00000080;
	public static final int	SYNTHETIC			= 0x00001000;
	public static final int	ANNOTATION			= 0x00002000;
	public static final int	ENUM				= 0x00004000;
	public static final int	MANDATED			= 0x00008000;
	
	public static final int	ACCESS_MODIFIERS	= Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;
	public static final int	CLASS_MODIFIERS		= Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL | Modifier.STRICT;
	public static final int	INTERFACE_MODIFIERS	= Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.STATIC | Modifier.STRICT;
	
	public static final int	FIELD_MODIFIERS		= Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT | Modifier.VOLATILE;
	public static final int	METHOD_MODIFIERS	= Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL | Modifier.SYNCHRONIZED | Modifier.NATIVE | Modifier.STRICT;
	public static final int	PARAMETER_MODIFIERS	= Modifier.STATIC | Modifier.FINAL;
	
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
		}
		return 0;
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
		return 0;
	}
	
	public static int parseClassModifier(String mod)
	{
		int i = parseAccessModifier(mod);
		if (i != 0)
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
		return 0;
	}
	
	public static int parseInterfaceModifier(String mod)
	{
		int i = parseAccessModifier(mod);
		if (i != 0)
			return i;
		switch (mod)
		{
		case "static":
			return STATIC;
		case "strictfp":
			return STRICT;
		}
		return 0;
	}
	
	public static int parseFieldModifier(String mod)
	{
		int i = parseAccessModifier(mod);
		if (i != 0)
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
		}
		return 0;
	}
	
	public static int parseMethodModifier(String mod)
	{
		int i = parseAccessModifier(mod);
		if (i != 0)
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
		}
		return 0;
	}
	
	public static int parseParameterModifier(String mod)
	{
		switch (mod)
		{
		case "final":
			return FINAL;
		case "const":
			return CONST;
		}
		return 0;
	}
}

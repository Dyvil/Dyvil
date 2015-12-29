package dyvil.tools.compiler.transform;

import dyvil.tools.parsing.Name;

public final class Names
{
	public static final Name instance      = new Name("instance");
	public static final Name apply         = new Name("apply");
	public static final Name unapply       = new Name("unapply");
	public static final Name update        = new Name("update");
	public static final Name subscript     = new Name("subscript");
	public static final Name subscript_$eq = new Name("subscript_=", "subscript_$eq");
	public static final Name match         = new Name("match");
	public static final Name equals        = new Name("equals");
	public static final Name hashCode      = new Name("hashCode");
	public static final Name toString      = new Name("toString");
	public static final Name $it           = new Name("$it");
	
	public static final Name writeReplace = new Name("writeReplace");
	public static final Name readResolve  = new Name("readResolve");
	
	public static final Name Function = new Name("Function");
	public static final Name Tuple    = new Name("Tuple");
	
	public static final Name _this    = new Name("this");
	public static final Name _null    = new Name("null");
	public static final Name _void    = new Name("void");
	public static final Name _boolean = new Name("boolean");
	public static final Name _byte    = new Name("byte");
	public static final Name _short   = new Name("short");
	public static final Name _char    = new Name("char");
	public static final Name _int     = new Name("int");
	public static final Name _long    = new Name("long");
	public static final Name _float   = new Name("float");
	public static final Name _double  = new Name("double");
	
	public static final Name dynamic = new Name("dynamic");
	public static final Name any     = new Name("any");
	public static final Name auto    = new Name("auto");
	
	public static final Name plus    = new Name("+", "$plus");
	public static final Name minus   = new Name("-", "$minus");
	public static final Name times   = new Name("*", "$times");
	public static final Name div     = new Name("/", "$div");
	public static final Name bslash  = new Name("\\", "$bslash");
	public static final Name percent = new Name("%", "$percent");
	public static final Name amp     = new Name("&", "$amp");
	public static final Name bar     = new Name("|", "$bar");
	public static final Name up      = new Name("^", "$up");
	public static final Name ltlt    = new Name("<<", "$lt$lt");
	public static final Name gtgt    = new Name(">>", "$gt$gt");
	public static final Name gtgtgt  = new Name(">>>", "$gt$gt$gt");
	public static final Name ampamp  = new Name("&&", "$amp$amp");
	public static final Name barbar  = new Name("||", "$bar$bar");
	
	public static final Name eqeq         = new Name("==", "$eq$eq");
	public static final Name bangeq       = new Name("!=", "$bang$eq");
	public static final Name eqeqeq       = new Name("===", "$eq$eq$eq");
	public static final Name bangeqeq     = new Name("!==", "$bang$eq$eq");
	
	public static final Name lt      = new Name("<", "$lt");
	public static final Name lteq    = new Name("<=", "$lt$eq");
	public static final Name ltcolon = new Name("<:", "$lt$colon");
	public static final Name gt      = new Name(">", "$gt");
	public static final Name gteq    = new Name(">=", "$gt$eq");
	public static final Name gtcolon = new Name(">:", "$gt$colon");
	public static final Name qmark   = new Name("?", "$qmark");
	public static final Name bang    = new Name("!", "$bang");
	public static final Name tilde   = new Name("~", "$tilde");
	public static final Name minusgt = new Name("->", "$minus$gt");
	public static final Name ltminus = new Name("<-", "$lt$minus");
	
	public static final Name dotdot   = new Name("..", "$dot$dot");
	public static final Name dotdotlt = new Name("..<", "$dot$dot$lt");
	
	public static final Name pluseq = new Name("+=", "$plus$eq");

	public static final Name plusplus   = new Name("++", "$plus$plus");
	public static final Name minusminus = new Name("--", "$minus$minus");

	private Names()
	{
		// no instances
	}
	
	public static void init()
	{
		// run static initializer of this class
	}
}

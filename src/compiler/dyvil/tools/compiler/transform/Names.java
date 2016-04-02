package dyvil.tools.compiler.transform;

import dyvil.tools.parsing.Name;

public final class Names
{
	public static final Name instance       = Name.getQualified("instance");
	public static final Name init           = Name.getQualified("init");
	public static final Name get            = Name.getQualified("get");
	public static final Name set            = Name.getQualified("set");
	public static final Name apply          = Name.getQualified("apply");
	public static final Name applyStatement = Name.getQualified("applyStatement");
	public static final Name apply_$amp     = Name.get("apply_&", "apply_$amp");
	public static final Name update         = Name.getQualified("update");
	public static final Name subscript      = Name.getQualified("subscript");
	public static final Name subscript_$amp = Name.get("subscript_&", "subscript_$amp");
	public static final Name subscript_$eq  = Name.get("subscript_=", "subscript_$eq");
	public static final Name in             = Name.getQualified("in");
	public static final Name length         = Name.getQualified("length");
	public static final Name equals         = Name.getQualified("equals");
	public static final Name hashCode       = Name.getQualified("hashCode");
	public static final Name toString       = Name.getQualified("toString");
	public static final Name newValue       = Name.getQualified("newValue");
	public static final Name $0             = Name.getQualified("$0");

	public static final Name writeReplace = Name.getQualified("writeReplace");
	public static final Name readResolve  = Name.getQualified("readResolve");

	public static final Name Function     = Name.getQualified("Function");
	public static final Name Tuple        = Name.getQualified("Tuple");
	public static final Name Union        = Name.getQualified("Union");
	public static final Name Intersection = Name.getQualified("Intersection");

	public static final Name _null    = Name.getQualified("null");
	public static final Name _void    = Name.getQualified("void");
	public static final Name _boolean = Name.getQualified("boolean");
	public static final Name _byte    = Name.getQualified("byte");
	public static final Name _short   = Name.getQualified("short");
	public static final Name _char    = Name.getQualified("char");
	public static final Name _int     = Name.getQualified("int");
	public static final Name _long    = Name.getQualified("long");
	public static final Name _float   = Name.getQualified("float");
	public static final Name _double  = Name.getQualified("double");

	public static final Name dynamic = Name.getQualified("dynamic");
	public static final Name any     = Name.getQualified("any");
	public static final Name auto    = Name.getQualified("auto");

	public static final Name eq    = Name.get("=", "$eq");
	public static final Name colon = Name.get(":", "$colon");

	public static final Name plus    = Name.get("+", "$plus");
	public static final Name minus   = Name.get("-", "$minus");
	public static final Name times   = Name.get("*", "$times");
	public static final Name div     = Name.get("/", "$div");
	public static final Name bslash  = Name.get("\\", "$bslash");
	public static final Name percent = Name.get("%", "$percent");
	public static final Name amp     = Name.get("&", "$amp");
	public static final Name bar     = Name.get("|", "$bar");
	public static final Name up      = Name.get("^", "$up");
	public static final Name ltlt    = Name.get("<<", "$lt$lt");
	public static final Name gtgt    = Name.get(">>", "$gt$gt");
	public static final Name gtgtgt  = Name.get(">>>", "$gt$gt$gt");
	public static final Name ampamp  = Name.get("&&", "$amp$amp");
	public static final Name barbar  = Name.get("||", "$bar$bar");

	public static final Name eqeq     = Name.get("==", "$eq$eq");
	public static final Name bangeq   = Name.get("!=", "$bang$eq");
	public static final Name eqeqeq   = Name.get("===", "$eq$eq$eq");
	public static final Name bangeqeq = Name.get("!==", "$bang$eq$eq");

	public static final Name lt      = Name.get("<", "$lt");
	public static final Name lteq    = Name.get("<=", "$lt$eq");
	public static final Name ltcolon = Name.get("<:", "$lt$colon");
	public static final Name gt      = Name.get(">", "$gt");
	public static final Name gteq    = Name.get(">=", "$gt$eq");
	public static final Name gtcolon = Name.get(">:", "$gt$colon");
	public static final Name qmark   = Name.get("?", "$qmark");
	public static final Name bang    = Name.get("!", "$bang");
	public static final Name tilde   = Name.get("~", "$tilde");

	public static final Name dotdot    = Name.get("..", "$dot$dot");
	public static final Name dotdotdot = Name.get("...", "$dot$dot$dot");
	public static final Name dotdotlt  = Name.get("..<", "$dot$dot$lt");

	public static final Name pluseq = Name.get("+=", "$plus$eq");

	public static final Name plusplus   = Name.get("++", "$plus$plus");
	public static final Name minusminus = Name.get("--", "$minus$minus");

	private Names()
	{
		// no instances
	}

	public static void init()
	{
		// run static initializer of this class
	}
}

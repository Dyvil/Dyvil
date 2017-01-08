package dyvil.tools.compiler.transform;

import dyvil.tools.parsing.Name;

public final class Names
{
	public static final Name instance       = Name.fromRaw("instance");
	public static final Name init           = Name.fromRaw("init");
	public static final Name get            = Name.fromRaw("get");
	public static final Name set            = Name.fromRaw("set");
	public static final Name apply          = Name.fromRaw("apply");
	public static final Name applyStatement = Name.fromRaw("applyStatement");
	public static final Name apply_$amp     = Name.from("apply_&", "apply_$amp");
	public static final Name update         = Name.fromRaw("update");
	public static final Name subscript      = Name.fromRaw("subscript");
	public static final Name subscript_$amp = Name.from("subscript_&", "subscript_$amp");
	public static final Name subscript_$eq  = Name.from("subscript_=", "subscript_$eq");
	public static final Name value          = Name.fromRaw("value");
	public static final Name length         = Name.fromRaw("length");
	public static final Name equals         = Name.fromRaw("equals");
	public static final Name hashCode       = Name.fromRaw("hashCode");
	public static final Name toString       = Name.fromRaw("toString");
	public static final Name newValue       = Name.fromRaw("newValue");
	public static final Name $0             = Name.fromRaw("$0");

	public static final Name Function     = Name.fromRaw("Function");
	public static final Name Tuple        = Name.fromRaw("Tuple");
	public static final Name Union        = Name.fromRaw("Union");
	public static final Name Intersection = Name.fromRaw("Intersection");

	public static final Name _null    = Name.fromRaw("null");
	public static final Name _void    = Name.fromRaw("void");
	public static final Name _boolean = Name.fromRaw("boolean");
	public static final Name _byte    = Name.fromRaw("byte");
	public static final Name _short   = Name.fromRaw("short");
	public static final Name _char    = Name.fromRaw("char");
	public static final Name _int     = Name.fromRaw("int");
	public static final Name _long    = Name.fromRaw("long");
	public static final Name _float   = Name.fromRaw("float");
	public static final Name _double  = Name.fromRaw("double");

	public static final Name dynamic = Name.fromRaw("dynamic");
	public static final Name any     = Name.fromRaw("any");
	public static final Name none    = Name.fromRaw("none");
	public static final Name auto    = Name.fromRaw("auto");

	public static final Name eq    = Name.from("=", "$eq");
	public static final Name colon = Name.from(":", "$colon");

	public static final Name plus    = Name.from("+", "$plus");
	public static final Name minus   = Name.from("-", "$minus");
	public static final Name times   = Name.from("*", "$times");
	public static final Name div     = Name.from("/", "$div");
	public static final Name bslash  = Name.from("\\", "$bslash");
	public static final Name percent = Name.from("%", "$percent");
	public static final Name amp     = Name.from("&", "$amp");
	public static final Name bar     = Name.from("|", "$bar");
	public static final Name up      = Name.from("^", "$up");
	public static final Name ltlt    = Name.from("<<", "$lt$lt");
	public static final Name gtgt    = Name.from(">>", "$gt$gt");
	public static final Name gtgtgt  = Name.from(">>>", "$gt$gt$gt");
	public static final Name ampamp  = Name.from("&&", "$amp$amp");
	public static final Name barbar  = Name.from("||", "$bar$bar");

	public static final Name eqeq     = Name.from("==", "$eq$eq");
	public static final Name bangeq   = Name.from("!=", "$bang$eq");
	public static final Name eqeqeq   = Name.from("===", "$eq$eq$eq");
	public static final Name bangeqeq = Name.from("!==", "$bang$eq$eq");

	public static final Name lt      = Name.from("<", "$lt");
	public static final Name lteq    = Name.from("<=", "$lt$eq");
	public static final Name ltcolon = Name.from("<:", "$lt$colon");
	public static final Name gt      = Name.from(">", "$gt");
	public static final Name gteq    = Name.from(">=", "$gt$eq");
	public static final Name gtcolon = Name.from(">:", "$gt$colon");
	public static final Name qmark   = Name.from("?", "$qmark");
	public static final Name bang    = Name.from("!", "$bang");
	public static final Name tilde   = Name.from("~", "$tilde");

	public static final Name dotdot    = Name.from("..", "$dot$dot");
	public static final Name dotdotdot = Name.from("...", "$dot$dot$dot");
	public static final Name dotdotlt  = Name.from("..<", "$dot$dot$lt");

	public static final Name pluseq = Name.from("+=", "$plus$eq");

	public static final Name plusplus   = Name.from("++", "$plus$plus");
	public static final Name minusminus = Name.from("--", "$minus$minus");

	private Names()
	{
		// no instances
	}

	public static void init()
	{
		// run static initializer of this class
	}
}

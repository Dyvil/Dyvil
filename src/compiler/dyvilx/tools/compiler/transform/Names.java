package dyvilx.tools.compiler.transform;

import dyvil.lang.Name;

public final class Names
{
	// =============== Constants ===============

	// --------------- Operators and Symbols ---------------

	public static final Name $amp        = Name.from("&", "$amp");
	public static final Name $bang       = Name.from("!", "$bang");
	public static final Name $bar        = Name.from("|", "$bar");
	public static final Name $bslash     = Name.from("\\", "$bslash");
	public static final Name $colon      = Name.from(":", "$colon");
	public static final Name $div        = Name.from("/", "$div");
	public static final Name $dot$dot    = Name.from("..", "$dot$dot");
	public static final Name $dot$dot$lt = Name.from("..<", "$dot$dot$lt");
	public static final Name $eq         = Name.from("=", "$eq");
	public static final Name $gt$gt      = Name.from(">>", "$gt$gt");
	public static final Name $gt$gt$gt   = Name.from(">>>", "$gt$gt$gt");
	public static final Name $lt$lt      = Name.from("<<", "$lt$lt");
	public static final Name $minus      = Name.from("-", "$minus");
	public static final Name $percent    = Name.from("%", "$percent");
	public static final Name $plus       = Name.from("+", "$plus");
	public static final Name $qmark      = Name.from("?", "$qmark");
	public static final Name $tilde      = Name.from("~", "$tilde");
	public static final Name $times      = Name.from("*", "$times");
	public static final Name $up         = Name.from("^", "$up");

	// --------------- Special Names ---------------

	public static final Name $0      = Name.fromRaw("$0");
	public static final Name $VALUES = Name.fromRaw("$VALUES");

	// --------------- Regular Names ---------------

	public static final Name any            = Name.fromRaw("any");
	public static final Name apply          = Name.fromRaw("apply");
	public static final Name apply_$amp     = Name.from("apply_&", "apply_$amp");
	public static final Name apply_$eq      = Name.from("apply_=", "apply_$eq");
	public static final Name applyStatement = Name.fromRaw("applyStatement");
	public static final Name boolean_       = Name.fromRaw("boolean");
	public static final Name byte_          = Name.fromRaw("byte");
	public static final Name char_          = Name.fromRaw("char");
	public static final Name double_        = Name.fromRaw("double");
	public static final Name equals         = Name.fromRaw("equals");
	public static final Name float_         = Name.fromRaw("float");
	public static final Name Function       = Name.fromRaw("Function");
	public static final Name get            = Name.fromRaw("get");
	public static final Name hashCode       = Name.fromRaw("hashCode");
	public static final Name init           = Name.fromRaw("init");
	public static final Name instance       = Name.fromRaw("instance");
	public static final Name int_           = Name.fromRaw("int");
	public static final Name Intersection   = Name.fromRaw("Intersection");
	public static final Name length         = Name.fromRaw("length");
	public static final Name long_          = Name.fromRaw("long");
	public static final Name name           = Name.fromRaw("name");
	public static final Name newValue       = Name.fromRaw("newValue");
	public static final Name none           = Name.fromRaw("none");
	public static final Name null_          = Name.fromRaw("null");
	public static final Name ordinal        = Name.fromRaw("ordinal");
	public static final Name set            = Name.fromRaw("set");
	public static final Name short_         = Name.fromRaw("short");
	public static final Name subscript      = Name.fromRaw("subscript");
	public static final Name subscript_$amp = Name.from("subscript_&", "subscript_$amp");
	public static final Name subscript_$eq  = Name.from("subscript_=", "subscript_$eq");
	public static final Name toString       = Name.fromRaw("toString");
	public static final Name Tuple          = Name.fromRaw("Tuple");
	public static final Name unapply        = Name.fromRaw("unapply");
	public static final Name Union          = Name.fromRaw("Union");
	public static final Name value          = Name.fromRaw("value");
	public static final Name valueOf        = Name.fromRaw("valueOf");
	public static final Name values         = Name.fromRaw("values");
	public static final Name void_          = Name.fromRaw("void");

	// =============== Constructors ===============

	private Names()
	{
		// no instances
	}

	// =============== Static Methods ===============

	public static void init()
	{
		// run static initializer of this class
	}
}

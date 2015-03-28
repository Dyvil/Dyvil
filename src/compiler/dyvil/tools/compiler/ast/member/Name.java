package dyvil.tools.compiler.ast.member;

import java.util.HashMap;
import java.util.Map;

import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Symbols;

public final class Name
{
	private static final Map<String, Name>	map						= new HashMap();
	
	public static final Name				instance				= new Name("instance");
	public static final Name				apply					= new Name("apply");
	public static final Name				unapply					= new Name("unapply");
	public static final Name				update					= new Name("update");
	
	public static final Name				_this					= new Name("this");
	public static final Name				_class					= new Name("class");
	
	public static final Name				_void					= new Name("void", "Void");
	public static final Name				_boolean				= new Name("boolean", "Boolean");
	public static final Name				_byte					= new Name("byte", "Byte");
	public static final Name				_short					= new Name("short", "Short");
	public static final Name				_char					= new Name("char", "Char");
	public static final Name				_int					= new Name("int", "Int");
	public static final Name				_long					= new Name("long", "Long");
	public static final Name				_float					= new Name("float", "Float");
	public static final Name				_double					= new Name("double", "Double");
	public static final Name				dynamic					= new Name("dynamic", "Dynamic");
	public static final Name				any						= new Name("any", "Any");
	
	public static final Name				plus					= new Name("+", "$plus");
	public static final Name				minus					= new Name("-", "$minus");
	public static final Name				times					= new Name("*", "$times");
	public static final Name				div						= new Name("/", "$div");
	public static final Name				bslash					= new Name("\\", "$bslash");
	public static final Name				percent					= new Name("%", "$percent");
	public static final Name				amp						= new Name("&", "$amp");
	public static final Name				bar						= new Name("|", "$bar");
	public static final Name				up						= new Name("^", "$up");
	public static final Name				lessLess				= new Name("<<", "$less$less");
	public static final Name				greaterGreater			= new Name(">>", "$greater$greater");
	public static final Name				greaterGreaterGreater	= new Name(">>>", "$greater$greater$greater");
	public static final Name				ampAmp					= new Name("&&", "$amp$amp");
	public static final Name				barBar					= new Name("||", "$bar$bar");
	
	public static final Name				eqEq					= new Name("==", "$eq$eq");
	public static final Name				bangEq					= new Name("!=", "$bang$eq");
	public static final Name				colonEqColon			= new Name(":=:", "$colon$eq$colon");
	
	public static final Name				less					= new Name("<", "$less");
	public static final Name				lessEq					= new Name("<=", "$less$eq");
	public static final Name				greater					= new Name(">", "$greater");
	public static final Name				greaterEq				= new Name(">=", "$greater$eq");
	
	public static final Name				bang					= new Name("!", "$bang");
	public static final Name				tilde					= new Name("~", "$tilde");
	
	public static final Name				minusGreater			= new Name("->", "$minus$greater");
	public static final Name				lessMinus				= new Name("<-", "$less$minus");
	
	public static final Name				colonGreater			= new Name(":>", "$colon$greater");
	public static final Name				lessColon				= new Name("<:", "$less$colon");

	public final String						qualified;
	public final String						unqualified;
	
	protected Name(String name)
	{
		this.qualified = this.unqualified = name;
		map.put(name, this);
	}
	
	protected Name(String unqualified, String qualified)
	{
		this.qualified = qualified;
		this.unqualified = unqualified;
		
		map.put(qualified, this);
		map.put(unqualified, this);
	}
	
	public static Name get(String unqualified, String qualified)
	{
		Name name = map.get(qualified);
		if (name != null)
		{
			return name;
		}
		
		name = map.get(unqualified);
		if (name != null)
		{
			return name;
		}
		
		return new Name(unqualified, qualified);
	}
	
	public static Name get(String value)
	{
		Name name = map.get(value);
		if (name != null)
		{
			return name;
		}
		
		name = new Name(Symbols.unqualify(value), Symbols.qualify(value));
		return name;
	}
	
	public static Name getQualified(String value)
	{
		Name name = map.get(value);
		if (name != null)
		{
			return name;
		}
		
		return new Name(value);
	}
	
	public boolean equals(String name)
	{
		return this.qualified.equals(name);
	}
	
	public boolean endsWith(String name)
	{
		return this.qualified.endsWith(name);
	}
	
	@Override
	public String toString()
	{
		return Formatting.Method.convertQualifiedNames ? this.qualified : this.unqualified;
	}
}

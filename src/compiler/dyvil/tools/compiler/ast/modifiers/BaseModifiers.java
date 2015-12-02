package dyvil.tools.compiler.ast.modifiers;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.token.IToken;

public enum BaseModifiers implements Modifier
{
	// Visibility Modifiers
	PACKAGE_PRIVATE(0, "package private"),
	PRIVATE(Modifiers.PRIVATE, "private", DyvilKeywords.PRIVATE),
	PRIVATE_PROTECTED(Modifiers.PROTECTED, "private protected"),
	PROTECTED(Modifiers.PROTECTED, "protected", DyvilKeywords.PROTECTED),
	PACKAGE_PROTECTED(Modifiers.PUBLIC, "package protected"),
	PUBLIC(Modifiers.PUBLIC, "public", DyvilKeywords.PUBLIC),
	INTERNAL(Modifiers.INTERNAL, "internal", DyvilKeywords.INTERNAL),
	// Access Modifiers
	STATIC(Modifiers.STATIC, "static", DyvilKeywords.STATIC),
	FINAL(Modifiers.FINAL, "final", DyvilKeywords.FINAL),
	ABSTRACT(Modifiers.ABSTRACT, "abstract", DyvilKeywords.ABSTRACT),
	// Method Modifiers
	PREFIX(Modifiers.PREFIX, "prefix", DyvilKeywords.PREFIX),
	INFIX(Modifiers.INFIX, "infix", DyvilKeywords.INFIX),
	POSTFIX(Modifiers.INFIX, "postfix", DyvilKeywords.POSTFIX),
	OVERRIDE(Modifiers.OVERRIDE, "override", DyvilKeywords.OVERRIDE),
	INLINE(Modifiers.INLINE, "inline", DyvilKeywords.INLINE),
	SYNCHRONIZED(Modifiers.SYNCHRONIZED, "synchronized", DyvilKeywords.SYNCHRONIZED),
	EXTENSION(Modifiers.EXTENSION, "extension", DyvilKeywords.EXTENSION),
	// Class Modifiers
	SEALED(Modifiers.SEALED, "sealed", DyvilKeywords.SEALED),
	CASE(Modifiers.CASE_CLASS, "case", DyvilKeywords.CASE),
	FUNCTIONAL(Modifiers.FUNCTIONAL, "functional", DyvilKeywords.FUNCTIONAL),
	// Field Modifiers
	CONST(Modifiers.CONST, "const", DyvilKeywords.CONST),
	LAZY(Modifiers.LAZY, "lazy", DyvilKeywords.LAZY),
	// Parameter Modifiers
	VAR(Modifiers.VAR, "var", DyvilKeywords.VAR);

	private final int    intValue;
	private final int    keyword;
	private final String name;

	BaseModifiers(int intValue, String name)
	{
		this.intValue = intValue;
		this.name = name;
		this.keyword = -1;
	}

	BaseModifiers(int intValue, String name, int keyword)
	{
		this.intValue = intValue;
		this.name = name;
		this.keyword = keyword;
	}

	public static Modifier parseVisibilityModifier(IToken token, IParserManager parserManager)
	{
		switch (token.type())
		{
		case DyvilKeywords.PRIVATE:
			if (token.next().type() == DyvilKeywords.PROTECTED)
			{
				parserManager.skip();
				return PRIVATE_PROTECTED;
			}
			return PRIVATE;
		case DyvilKeywords.PACKAGE:
			switch (token.next().type())
			{
			case DyvilKeywords.PROTECTED:
				parserManager.skip();
				return PACKAGE_PROTECTED;
			case DyvilKeywords.PRIVATE:
				parserManager.skip();
				return PACKAGE_PRIVATE;
			}
			return null;
		case DyvilKeywords.PROTECTED:
			return PROTECTED;
		case DyvilKeywords.PUBLIC:
			return PUBLIC;
		case DyvilKeywords.INTERNAL:
			return INTERNAL;
		}
		return null;
	}

	public static Modifier parseMethodModifier(IToken token, IParserManager parserManager)
	{
		switch (token.type())
		{
		case DyvilKeywords.PREFIX:
			return PREFIX;
		case DyvilKeywords.INFIX:
			return INFIX;
		case DyvilKeywords.POSTFIX:
			return POSTFIX;
		case DyvilKeywords.EXTENSION:
			return EXTENSION;
		case DyvilKeywords.ABSTRACT:
			return ABSTRACT;
		case DyvilKeywords.FINAL:
			return FINAL;
		case DyvilKeywords.STATIC:
			return STATIC;
		case DyvilKeywords.OVERRIDE:
			return OVERRIDE;
		case DyvilKeywords.INLINE:
			return INLINE;
		case DyvilKeywords.SYNCHRONIZED:
			return SYNCHRONIZED;
		}
		return parseVisibilityModifier(token, parserManager);
	}

	public static Modifier parseClassModifier(IToken token, IParserManager parserManager)
	{
		switch (token.type())
		{
		case DyvilKeywords.STATIC:
			return STATIC;
		case DyvilKeywords.ABSTRACT:
			return ABSTRACT;
		case DyvilKeywords.FINAL:
			return FINAL;
		case DyvilKeywords.FUNCTIONAL:
			return FUNCTIONAL;
		}
		return parseVisibilityModifier(token, parserManager);
	}

	private static Modifier parseFieldModifier(IToken token, IParserManager parserManager)
	{
		switch (token.type())
		{
		case DyvilKeywords.STATIC:
			return STATIC;
		case DyvilKeywords.FINAL:
			return FINAL;
		case DyvilKeywords.CONST:
			return CONST;
		case DyvilKeywords.LAZY:
			return LAZY;
		}
		return parseVisibilityModifier(token, parserManager);
	}

	public static Modifier parseMemberModifier(IToken token, IParserManager parserManager)
	{
		Modifier modifier = parseMethodModifier(token, parserManager);
		if (modifier != null)
		{
			return modifier;
		}

		modifier = parseClassModifier(token, parserManager);
		if (modifier != null)
		{
			return modifier;
		}

		return parseFieldModifier(token, parserManager);
	}

	public static Modifier parseParameterModifier(IToken token, IParserManager parserManager)
	{
		switch (token.type())
		{
		case DyvilKeywords.FINAL:
			return FINAL;
		case DyvilKeywords.VAR:
			return VAR;
		}
		return parseFieldModifier(token, parserManager);
	}

	@Override
	public int intValue()
	{
		return this.intValue;
	}

	@Override
	public void toString(StringBuilder builder)
	{
		builder.append(this.name);
	}
}

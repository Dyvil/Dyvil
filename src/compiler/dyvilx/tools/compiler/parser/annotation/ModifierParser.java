package dyvilx.tools.compiler.parser.annotation;

import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.attribute.modifiers.BaseModifiers;
import dyvilx.tools.compiler.ast.attribute.modifiers.Modifier;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.token.IToken;

public class ModifierParser
{
	public static Modifier parseModifier(IToken token, IParserManager parserManager)
	{
		switch (token.type())
		{
		case DyvilKeywords.PRIVATE:
			if (token.next().type() == DyvilKeywords.PROTECTED)
			{
				parserManager.skip();
				return BaseModifiers.PRIVATE_PROTECTED;
			}
			return BaseModifiers.PRIVATE;
		case DyvilKeywords.PACKAGE:
			if (token.next().type() == DyvilKeywords.PRIVATE)
			{
				parserManager.skip();
				return BaseModifiers.PACKAGE_PRIVATE;
			}
			return null;
		case DyvilKeywords.PROTECTED:
			return BaseModifiers.PROTECTED;
		case DyvilKeywords.PUBLIC:
			return BaseModifiers.PUBLIC;
		case DyvilKeywords.INTERNAL:
			return BaseModifiers.INTERNAL;
		case DyvilKeywords.PREFIX:
			return BaseModifiers.PREFIX;
		case DyvilKeywords.INFIX:
			return BaseModifiers.INFIX;
		case DyvilKeywords.POSTFIX:
			return BaseModifiers.POSTFIX;
		case DyvilKeywords.EXTENSION:
			return BaseModifiers.EXTENSION;
		case DyvilKeywords.ABSTRACT:
			return BaseModifiers.ABSTRACT;
		case DyvilKeywords.FINAL:
			return BaseModifiers.FINAL;
		case DyvilKeywords.STATIC:
			return BaseModifiers.STATIC;
		case DyvilKeywords.OVERRIDE:
			return BaseModifiers.OVERRIDE;
		case DyvilKeywords.INLINE:
			return BaseModifiers.INLINE;
		case DyvilKeywords.IMPLICIT:
			return BaseModifiers.IMPLICIT;
		case DyvilKeywords.EXPLICIT:
			return BaseModifiers.EXPLICIT;
		case DyvilKeywords.SYNCHRONIZED:
			return BaseModifiers.SYNCHRONIZED;
		case DyvilKeywords.CONST:
			return BaseModifiers.CONST;
		case DyvilKeywords.LAZY:
			return BaseModifiers.LAZY;
		}
		return null;
	}

	public static int parseClassTypeModifier(IToken token, IParserManager parserManager)
	{
		switch (token.type())
		{
		case DyvilSymbols.AT:
			// @interface
			if (token.next().type() == DyvilKeywords.INTERFACE)
			{
				parserManager.skip();
				return Modifiers.ANNOTATION_CLASS;
			}
			return -1;
		case DyvilKeywords.CASE:
			switch (token.next().type())
			{
			// case class
			case DyvilKeywords.CLASS:
				parserManager.skip();
				return Modifiers.CASE_CLASS;
			// case object
			case DyvilKeywords.OBJECT:
				parserManager.skip();
				return Modifiers.OBJECT_CLASS | Modifiers.CASE_CLASS;
			}
			return -1;
		case DyvilKeywords.CLASS:
			return 0;
		case DyvilKeywords.INTERFACE:
			return Modifiers.INTERFACE_CLASS;
		case DyvilKeywords.TRAIT:
			return Modifiers.TRAIT_CLASS;
		case DyvilKeywords.ENUM:
			return Modifiers.ENUM_CLASS;
		case DyvilKeywords.OBJECT:
			return Modifiers.OBJECT_CLASS;
		case DyvilKeywords.EXTENSION:
			if (token.next().type() != DyvilKeywords.FUNC)
			{
				return Modifiers.EXTENSION;
			}
			return -1;
		}
		return -1;
	}
}

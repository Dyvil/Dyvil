package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.reference.ImplicitReferenceType;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Mutability;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.*;
import dyvil.tools.compiler.ast.type.generic.GenericType;
import dyvil.tools.compiler.ast.type.generic.NamedGenericType;
import dyvil.tools.compiler.ast.type.raw.NamedType;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.position.CodePosition;
import dyvil.tools.parsing.token.IToken;

public final class TypeParser extends Parser implements ITypeConsumer
{
	public static final int NAME               = 0;
	public static final int GENERICS           = 1;
	public static final int GENERICS_END       = 2;
	public static final int ANGLE_GENERICS_END = 4;
	public static final int ARRAY_COLON        = 8;
	public static final int ARRAY_END          = 16;
	public static final int WILDCARD_TYPE      = 32;
	public static final int TUPLE_END          = 64;
	public static final int LAMBDA_END         = 128;
	public static final int ANNOTATION_END     = 256;

	// Flags

	public static final int NAMED_ONLY    = 1;
	public static final int IGNORE_LAMBDA = 2;

	protected ITypeConsumer consumer;

	private IType parentType;
	private IType type;

	private int flags;

	public TypeParser(ITypeConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = NAME;
	}

	public TypeParser(ITypeConsumer consumer, IType parentType)
	{
		this.consumer = consumer;
		this.parentType = parentType;
	}

	public TypeParser withFlags(int flags)
	{
		this.flags |= flags;
		return this;
	}

	public TypeParser namedOnly()
	{
		this.flags |= NAMED_ONLY;
		return this;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case END:
		{
			if ((this.flags & NAMED_ONLY) == 0)
			{
				if (ParserUtil.isIdentifier(type))
				{
					switch (token.stringValue().charAt(0))
					{
					case '?':
						this.type = new OptionType(this.type);
						pm.splitJump(token, 1);
						return;
					case '!':
						this.type = new ImplicitOptionType(this.type);
						pm.splitJump(token, 1);
						return;
					case '*':
						this.type = new ReferenceType(this.type);
						pm.splitJump(token, 1);
						return;
					case '^':
						this.type = new ImplicitReferenceType(this.type);
						pm.splitJump(token, 1);
						return;
					}
				}
				else if ((type == DyvilSymbols.DOUBLE_ARROW_RIGHT || type == DyvilSymbols.ARROW_RIGHT)
					         && this.parentType == null && (this.flags & IGNORE_LAMBDA) == 0)
				{
					if (type == DyvilSymbols.DOUBLE_ARROW_RIGHT)
					{
						pm.report(Markers.syntaxWarning(token, "type.lambda.double_arrow.deprecated"));
					}

					final LambdaType lambdaType = new LambdaType(token.raw(), this.type);
					this.type = lambdaType;
					this.mode = LAMBDA_END;
					pm.pushParser(new TypeParser(lambdaType));
					return;
				}
			}
			if (type == BaseSymbols.DOT)
			{
				pm.pushParser(new TypeParser(this, this.type));
				return;
			}

			if (this.type != null)
			{
				this.consumer.setType(this.type);
			}
			pm.popParser(true);
			return;
		}
		case NAME:
			if ((this.flags & NAMED_ONLY) == 0)
			{
				switch (type)
				{
				case DyvilSymbols.AT:
					Annotation a = new Annotation();
					pm.pushParser(new AnnotationParser(a));
					this.type = new AnnotatedType(a);
					this.mode = ANNOTATION_END;
					return;
				case BaseSymbols.OPEN_PARENTHESIS:
					TupleType tupleType = new TupleType();
					pm.pushParser(new TypeListParser(tupleType));
					this.type = tupleType;
					this.mode = TUPLE_END;
					return;
				case BaseSymbols.OPEN_SQUARE_BRACKET:
				{
					final ArrayType arrayType = new ArrayType();

					switch (token.next().type())
					{
					case DyvilKeywords.FINAL:
						arrayType.setMutability(Mutability.IMMUTABLE);
						pm.skip();
						break;
					case DyvilKeywords.VAR:
						arrayType.setMutability(Mutability.MUTABLE);
						pm.skip();
						break;
					}

					this.mode = ARRAY_COLON;
					this.type = arrayType;
					pm.pushParser(new TypeParser(arrayType));
					return;
				}

				case DyvilSymbols.DOUBLE_ARROW_RIGHT:
					pm.report(Markers.syntaxWarning(token, "type.lambda.double_arrow.deprecated"));
					// Fallthrough
				case DyvilSymbols.ARROW_RIGHT:
				{
					final LambdaType lambdaType = new LambdaType(token.raw(), this.parentType);
					pm.pushParser(new TypeParser(lambdaType));
					this.type = lambdaType;
					this.mode = LAMBDA_END;
					return;
				}
				case DyvilKeywords.NULL:
					this.consumer.setType(Types.NULL);
					pm.popParser();
					return;
				case DyvilSymbols.UNDERSCORE:
					this.type = new WildcardType(token.raw());
					this.mode = WILDCARD_TYPE;
					return;
				}

				if (ParserUtil.isIdentifier(type))
				{
					final String identifier = token.stringValue();
					switch (identifier.charAt(0))
					{
					case '_':
						this.type = new WildcardType(new CodePosition(token.startLine(), token.startIndex(),
						                                              token.startIndex() + 1));
						this.mode = WILDCARD_TYPE;
						pm.splitJump(token, 1);
						return;
					case '+':
					{
						final WildcardType wildcardType = new WildcardType(Variance.COVARIANT);
						pm.pushParser(new TypeParser(wildcardType));
						this.type = wildcardType;
						this.mode = END;
						return;
					}
					case '-':
					{
						final WildcardType wildcardType = new WildcardType(Variance.CONTRAVARIANT);
						pm.pushParser(new TypeParser(wildcardType));
						this.type = wildcardType;
						this.mode = END;
						return;
					}
					}
				}
			}
			if (ParserUtil.isIdentifier(type))
			{
				final Name name = token.nameValue();
				final IToken next = token.next();
				final int nextType = next.type();

				if (nextType == BaseSymbols.OPEN_SQUARE_BRACKET || isGenericStart(next, nextType))
				{
					this.type = new NamedGenericType(token.raw(), name, this.parentType);
					this.mode = GENERICS;
					return;
				}

				this.type = new NamedType(token.raw(), name, this.parentType);
				this.mode = END;
				return;
			}
			if (ParserUtil.isTerminator(type))
			{
				pm.popParser(true);
				return;
			}
			pm.report(Markers.syntaxError(token, "type.invalid", token.toString()));
			return;
		case TUPLE_END:
		{
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "type.tuple.close_paren");
			}

			final IToken nextToken = token.next();
			switch (nextToken.type())
			{
			case DyvilSymbols.DOUBLE_ARROW_RIGHT:
				pm.report(Markers.syntaxWarning(nextToken, "type.lambda.double_arrow.deprecated"));
				// Fallthrough
			case DyvilSymbols.ARROW_RIGHT:
				final LambdaType lambdaType = new LambdaType(nextToken.raw(), this.parentType, (TupleType) this.type);
				this.type = lambdaType;
				this.mode = LAMBDA_END;

				pm.skip();
				pm.pushParser(new TypeParser(lambdaType));
				return;
			}

			if (this.parentType != null)
			{
				pm.report(nextToken, "type.tuple.lambda_arrow");
			}
			this.type.expandPosition(token);
			this.mode = END;
			return;
		}
		case LAMBDA_END:
			this.type.expandPosition(token.prev());
			this.consumer.setType(this.type);
			pm.popParser(true);
			return;
		case ARRAY_COLON:
			if (type == BaseSymbols.COLON)
			{
				final MapType mapType = new MapType(this.type.getElementType(), null, this.type.getMutability());
				this.type = mapType;
				this.mode = ARRAY_END;
				pm.pushParser(new TypeParser(mapType::setValueType));
				return;
			}
			if (type == DyvilSymbols.ELLIPSIS)
			{
				this.type = new ListType(this.type.getElementType(), this.type.getMutability());
				this.mode = ARRAY_END;
				return;
			}
			// Fallthrough
		case ARRAY_END:
			this.type.expandPosition(token);
			this.mode = END;
			if (type != BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.reparse();
				pm.report(token, "type.array.close_bracket");
			}
			return;
		case GENERICS:
			if (isGenericStart(token, type))
			{
				pm.splitJump(token, 1);
				pm.pushParser(new TypeListParser((GenericType) this.type));
				this.mode = ANGLE_GENERICS_END;
				return;
			}
			if (type == BaseSymbols.OPEN_SQUARE_BRACKET)
			{
				pm.report(Markers.syntaxWarning(token, "type.generic.open_bracket.deprecated"));
				pm.pushParser(new TypeListParser((GenericType) this.type));
				this.mode = GENERICS_END;
				return;
			}
			return;
		case WILDCARD_TYPE:
		{
			final Name name = token.nameValue();
			final WildcardType wildcardType = (WildcardType) this.type;
			if (name == Names.ltcolon) // <: + Upper Bound
			{
				pm.report(Markers.syntaxWarning(token, "type.wildcard.upper.deprecated"));

				wildcardType.setVariance(Variance.COVARIANT);
				pm.pushParser(new TypeParser(wildcardType));
				this.mode = END;
				return;
			}
			if (name == Names.gtcolon) // >: - Lower Bound
			{
				pm.report(Markers.syntaxWarning(token, "type.wildcard.lower.deprecated"));

				wildcardType.setVariance(Variance.CONTRAVARIANT);
				pm.pushParser(new TypeParser(wildcardType));
				this.mode = END;
				return;
			}
			this.consumer.setType(this.type);
			pm.popParser(true);
			return;
		}
		case GENERICS_END:
			this.mode = END;
			if (type != BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.reparse();
				pm.report(token, "type.generic.close_bracket");
			}
			return;
		case ANGLE_GENERICS_END:
			this.mode = END;
			if (isGenericEnd(token, type))
			{
				pm.splitJump(token, 1);
				return;
			}

			pm.reparse();
			pm.report(token, "type.generic.close_angle");
			return;
		case ANNOTATION_END:
			this.mode = END;
			pm.pushParser(new TypeParser((ITyped) this.type), true);
		}
	}

	public static boolean isGenericStart(IToken token, int type)
	{
		return type == DyvilSymbols.ARROW_LEFT
			       || ParserUtil.isIdentifier(type) && token.nameValue().unqualified.charAt(0) == '<';
	}

	public static boolean isGenericEnd(IToken token, int type)
	{
		return ParserUtil.isIdentifier(type) && token.nameValue().unqualified.charAt(0) == '>';
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}

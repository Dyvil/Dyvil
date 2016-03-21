package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Mutability;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.*;
import dyvil.tools.compiler.ast.type.generic.GenericType;
import dyvil.tools.compiler.ast.type.generic.NamedGenericType;
import dyvil.tools.compiler.ast.type.raw.NamedType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class TypeParser extends Parser implements ITypeConsumer
{
	public static final int NAME           = 0;
	public static final int GENERICS       = 1;
	public static final int GENERICS_END   = 2;
	public static final int ARRAY_COLON    = 4;
	public static final int ARRAY_END      = 8;
	public static final int WILDCARD_TYPE  = 16;
	public static final int TUPLE_END      = 32;
	public static final int LAMBDA_END     = 64;
	public static final int ANNOTATION_END = 128;

	protected ITypeConsumer consumer;

	private IType parentType;
	private IType type;

	private boolean namedOnly;

	public TypeParser(ITypeConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = NAME;
	}

	public void setNamedOnly(boolean namedOnly)
	{
		this.namedOnly = namedOnly;
	}

	public TypeParser namedOnly()
	{
		this.setNamedOnly(true);
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
			if (type == Tokens.SYMBOL_IDENTIFIER && !this.namedOnly)
			{
				final Name name = token.nameValue();
				if (name == Names.qmark)
				{
					this.type = new OptionType(this.type);
					return;
				}
				if (name == Names.bang)
				{
					this.type = new ImplicitOptionType(this.type);
					return;
				}
				if (name == Names.times)
				{
					this.type = new ReferenceType(this.type);
					return;
				}
			}

			if (this.type != null)
			{
				this.consumer.setType(this.type);
			}
			pm.popParser(true);
			return;
		}
		case NAME:
			if (!this.namedOnly)
			{

				switch (type)
				{
				case DyvilSymbols.AT:
					Annotation a = new Annotation();
					pm.pushParser(pm.newAnnotationParser(a));
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
					pm.pushParser(pm.newTypeParser(arrayType));
					return;
				}
				case DyvilSymbols.DOUBLE_ARROW_RIGHT:
				{
					final LambdaType lambdaType = new LambdaType(token.raw());
					pm.pushParser(pm.newTypeParser(lambdaType));
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
			}
			if (ParserUtil.isIdentifier(type))
			{
				IToken next = token.next();
				switch (next.type())
				{
				case BaseSymbols.OPEN_SQUARE_BRACKET:
					this.type = new NamedGenericType(token.raw(), token.nameValue(), this.parentType);
					this.mode = GENERICS;
					return;
				case DyvilSymbols.DOUBLE_ARROW_RIGHT:
					if (this.parentType == null && !this.namedOnly)
					{
						LambdaType lt = new LambdaType(new NamedType(token.raw(), token.nameValue()));
						lt.setPosition(next.raw());
						this.type = lt;
						this.mode = LAMBDA_END;
						pm.skip();
						pm.pushParser(pm.newTypeParser(lt));
						return;
					}
					break; // intentional
				case BaseSymbols.DOT:
					NamedType namedType = new NamedType(token.raw(), token.nameValue(), this.parentType);
					TypeParser parser = new TypeParser(this);
					parser.parentType = namedType;

					pm.pushParser(parser);
					pm.skip();
					this.mode = END;
					return;
				}

				this.type = new NamedType(token.raw(), token.nameValue(), this.parentType);
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
			if (nextToken.type() == DyvilSymbols.DOUBLE_ARROW_RIGHT)
			{
				final LambdaType lambdaType = new LambdaType(nextToken.raw(), this.parentType, (TupleType) this.type);
				this.type = lambdaType;
				this.mode = LAMBDA_END;

				pm.skip();
				pm.pushParser(pm.newTypeParser(lambdaType));
				return;
			}
			else if (this.parentType != null)
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
			if (type == BaseSymbols.OPEN_SQUARE_BRACKET)
			{
				pm.pushParser(new TypeListParser((GenericType) this.type));
				this.mode = GENERICS_END;
				return;
			}
			this.type.expandPosition(token.prev());
			this.consumer.setType(this.type);
			pm.popParser(true);
			return;
		case WILDCARD_TYPE:
		{
			final Name name = token.nameValue();
			final WildcardType wildcardType = (WildcardType) this.type;
			if (name == Names.ltcolon) // <: - Upper Bound
			{
				wildcardType.setVariance(Variance.COVARIANT);
				pm.pushParser(pm.newTypeParser(wildcardType));
				this.mode = END;
				return;
			}
			if (name == Names.gtcolon) // >: - Lower Bound
			{
				wildcardType.setVariance(Variance.CONTRAVARIANT);
				pm.pushParser(pm.newTypeParser(wildcardType));
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
		case ANNOTATION_END:
			this.mode = END;
			pm.pushParser(pm.newTypeParser((ITyped) this.type), true);
		}
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}

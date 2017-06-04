package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Mutability;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.*;
import dyvil.tools.compiler.ast.type.generic.*;
import dyvil.tools.compiler.ast.type.raw.NamedType;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

import static dyvil.tools.compiler.parser.ParserUtil.isTerminator;
import static dyvil.tools.compiler.parser.ParserUtil.neighboring;

public final class TypeParser extends Parser implements ITypeConsumer
{
	protected static final int NAME           = 0;
	protected static final int GENERICS       = 1;
	protected static final int GENERICS_END   = 1 << 1;
	protected static final int ARRAY_COLON    = 1 << 2;
	protected static final int ARRAY_END      = 1 << 3;
	protected static final int TUPLE_END      = 1 << 5;
	protected static final int LAMBDA_END     = 1 << 6;
	protected static final int ANNOTATION_END = 1 << 7;

	// Flags

	public static final int NAMED_ONLY      = 1;
	public static final int IGNORE_LAMBDA   = 2;
	public static final int CLOSE_ANGLE     = 4;
	public static final int IGNORE_OPERATOR = 8;

	protected ITypeConsumer consumer;

	private IType parentType;
	private IType type;

	private int flags;

	public TypeParser(ITypeConsumer consumer)
	{
		this.consumer = consumer;
		// this.mode = NAME;
	}

	public TypeParser(ITypeConsumer consumer, boolean closeAngle)
	{
		this.consumer = consumer;

		if (closeAngle)
		{
			this.flags = CLOSE_ANGLE;
		}
		// this.mode = NAME;
	}

	protected TypeParser(ITypeConsumer consumer, IType parentType, int flags)
	{
		this.consumer = consumer;
		this.parentType = parentType;
		this.flags = flags;
		// this.mode = NAME;
	}

	public TypeParser withFlags(int flags)
	{
		this.flags |= flags;
		return this;
	}

	private TypeParser subParser(ITypeConsumer consumer)
	{
		return new TypeParser(consumer).withFlags(this.flags);
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case NAME:
		{
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
				{
					final TypeList arguments;
					if (this.parentType != null)
					{
						final LambdaType lambdaType = new LambdaType();
						lambdaType.setExtension(true);
						this.type = lambdaType;
						arguments = lambdaType.getArguments();
						arguments.add(this.parentType);
					}
					else
					{
						final TupleType tupleType = new TupleType();
						this.type = tupleType;
						arguments = tupleType.getArguments();
					}

					pm.pushParser(new TypeListParser(arguments));
					this.mode = TUPLE_END;
					return;
				}
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
					pm.pushParser(new TypeParser(arrayType::setElementType));
					return;
				}
				case DyvilSymbols.ARROW_RIGHT:
				{
					if ((this.flags & IGNORE_LAMBDA) != 0)
					{
						pm.popParser(true);
						return;
					}

					final LambdaType lambdaType = new LambdaType(token.raw());
					final TypeList arguments = lambdaType.getArguments();
					if (this.parentType != null)
					{
						arguments.add(this.parentType);
					}
					pm.pushParser(this.subParser(arguments));
					this.type = lambdaType;
					this.mode = LAMBDA_END;
					return;
				}
				case DyvilKeywords.NULL:
					this.type = Types.NULL;
					this.mode = END;
					return;
				case DyvilSymbols.UNDERSCORE:
					this.type = new WildcardType(token.raw(), Variance.COVARIANT);
					this.mode = END;
					return;
				case Tokens.SYMBOL_IDENTIFIER:
					final Name name = token.nameValue();

					final int closeAngleIndex;
					if ((this.flags & CLOSE_ANGLE) == 0 || (closeAngleIndex = name.unqualified.indexOf('>')) < 0)
					{
						// SYMBOL_IDENTIFIER type
						final PrefixType prefixType = new PrefixType(token.raw(), name);
						pm.pushParser(
							this.subParser(prefixType.getArguments()).withFlags(IGNORE_OPERATOR | IGNORE_LAMBDA));
						this.type = prefixType;
						this.mode = END;
						return;
					}
					if (closeAngleIndex == 0)
					{
						// Token starts with a >
						// Handles Type< > gracefully

						pm.popParser(true);
						return;
					}

					// strip the trailing > and reparse the first part of the token
					// Handles Type<_> gracefully
					pm.splitReparse(token, closeAngleIndex);
					return;
				}
			}

			if (!ParserUtil.isIdentifier(type))
			{
				if (isTerminator(type))
				{
					pm.popParser(true);
					return;
				}
				pm.report(Markers.syntaxError(token, "type.invalid", token.toString()));
				return;
			}

			final Name name = token.nameValue();
			final IToken next = token.next();

			if (isGenericStart(next, next.type()))
			{
				this.type = new NamedGenericType(token.raw(), this.parentType, name);
				this.mode = GENERICS;
				return;
			}

			this.type = new NamedType(token.raw(), name, this.parentType);
			this.mode = END;
			return;
		}
		case TUPLE_END:
		{
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "type.tuple.close_paren");
			}

			final IToken nextToken = token.next();
			if (nextToken.type() == DyvilSymbols.ARROW_RIGHT)
			{
				final LambdaType lambdaType;
				if (this.type instanceof LambdaType)
				{
					lambdaType = (LambdaType) this.type;
				}
				else
				{
					lambdaType = new LambdaType(nextToken.raw(), ((TupleType) this.type).getArguments());
					this.type = lambdaType;
				}
				lambdaType.setPosition(nextToken);

				this.mode = LAMBDA_END;
				pm.skip();
				pm.pushParser(this.subParser(lambdaType.getArguments()));
				return;
			}

			if (this.parentType != null)
			{
				pm.report(nextToken, "type.tuple.lambda_arrow");
			}
			Util.expandPosition(this.type, token);
			this.mode = END;
			return;
		}
		case LAMBDA_END:
			Util.expandPosition(this.type, token.prev());
			this.consumer.setType(this.type);
			pm.popParser(true);
			return;
		case ARRAY_COLON:
			if (type == BaseSymbols.COLON)
			{
				final MapType mapType = new MapType(this.type.getMutability(),
				                                    ((ArrayType) this.type).getElementType());
				this.type = mapType;
				this.mode = ARRAY_END;
				pm.pushParser(new TypeParser(mapType.getArguments()));
				return;
			}
			// Fallthrough
		case ARRAY_END:
			Util.expandPosition(this.type, token);
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
				pm.pushParser(new TypeListParser(((GenericType) this.type).getArguments(), true));
				this.mode = GENERICS_END;
				return;
			}
			return;
		case ANNOTATION_END:
			this.mode = END;
			pm.pushParser(this.subParser((ITyped) this.type), true);
			return;
		case GENERICS_END:
			this.mode = END;
			if (isGenericEnd(token, type))
			{
				pm.splitJump(token, 1);
				return;
			}

			pm.report(token, "type.generic.close_angle");
			// Fallthrough
		case END:
		{
			switch (type)
			{
			case BaseSymbols.DOT:
				pm.pushParser(new TypeParser(this, this.type, this.flags));
				return;
			case BaseSymbols.OPEN_SQUARE_BRACKET:
			{
				final IToken next = token.next();
				if (next.type() == BaseSymbols.CLOSE_SQUARE_BRACKET)
				{
					this.type = new ArrayType(this.type);
					pm.report(Markers.syntaxWarning(token.to(next), "type.array.java"));
					pm.skip();
					return;
				}
				break;
			}
			case Tokens.SYMBOL_IDENTIFIER:
			{
				if ((this.flags & NAMED_ONLY) != 0)
				{
					break;
				}

				if ((this.flags & CLOSE_ANGLE) != 0)
				{
					final String string = token.stringValue();
					int index = string.indexOf('>');
					if (index == 0)
					{
						// ... >

						pm.splitJump(token, 1);
						break;
					}
					else if (index > 0)
					{
						// ... SYMBOL>

						pm.splitJump(token, index);
						this.type = new PostfixType(token.raw(), Name.fromUnqualified(string.substring(0, index)),
						                            this.type);
						return;
					}
				}

				final IToken next = token.next();
				final boolean leftNeighbor = neighboring(token.prev(), token);
				final boolean rightNeighbor = neighboring(token, next);
				if (isTerminator(next.type()) || leftNeighbor && !rightNeighbor)
				{
					// type_OPERATOR
					this.type = new PostfixType(token.raw(), token.nameValue(), this.type);
					// move stays END
					return;
				}
				if (leftNeighbor != rightNeighbor || (this.flags & IGNORE_OPERATOR) != 0)
				{
					break; // type end
				}

				// Parse part of an infix operator
				// type SYMBOL type
				// type_SYMBOL_type
				final InfixTypeChain chain;
				if (this.type.typeTag() == IType.INFIX_CHAIN)
				{
					chain = (InfixTypeChain) this.type;
				}
				else
				{
					chain = new InfixTypeChain();
					chain.addOperand(this.type);
					this.type = chain;
				}

				chain.addOperator(token.nameValue(), token.raw());
				pm.pushParser(this.subParser(chain::addOperand).withFlags(IGNORE_OPERATOR));
				return;
			}
			case DyvilSymbols.ARROW_RIGHT:
				// all these flags have to be unset
				if (this.parentType == null && (this.flags & (NAMED_ONLY | IGNORE_OPERATOR | IGNORE_LAMBDA)) == 0)
				{
					final LambdaType lambdaType = new LambdaType(token.raw(), this.type);
					this.type = lambdaType;
					this.mode = LAMBDA_END;
					pm.pushParser(this.subParser(lambdaType.getArguments()));
					return;
				}
				break;
			}

			if (this.type != null)
			{
				this.consumer.setType(this.type);
			}
			pm.popParser(true);
		}
		}
	}

	/**
	 * Returns {@code true} iff the given token is a symbol token that starts with {@code <}.
	 * This includes the special left-arrow token {@code <-}.
	 */
	public static boolean isGenericStart(IToken token, int type)
	{
		switch (type)
		{
		case DyvilSymbols.ARROW_LEFT:
			return true;
		case Tokens.SYMBOL_IDENTIFIER:
		case Tokens.LETTER_IDENTIFIER:
			return token.nameValue().unqualified.charAt(0) == '<';
		}
		return false;
	}

	/**
	 * Returns {@code true} iff the given token is a symbol token that starts with {@code >}.
	 */
	public static boolean isGenericEnd(IToken token, int type)
	{
		switch (type)
		{
		case Tokens.SYMBOL_IDENTIFIER:
		case Tokens.LETTER_IDENTIFIER:
			return token.nameValue().unqualified.charAt(0) == '>';
		}
		return false;
	}

	/**
	 * Returns {@code true} iff the given token is a symbol token that ends with {@code >}.
	 * This includes the special right-arrow {@code ->} and double-right-arrow {@code =>} tokens.
	 */
	public static boolean isGenericEnd2(IToken token, int type)
	{
		switch (type)
		{
		case DyvilSymbols.ARROW_RIGHT:
		case DyvilSymbols.DOUBLE_ARROW_RIGHT:
			return true;
		// case Tokens.LETTER_IDENTIFIER: // if it is a LETTER_IDENTIFIER token, the last token cannot be a '>'
		case Tokens.SYMBOL_IDENTIFIER:
			final String unqualified = token.nameValue().unqualified;
			return unqualified.charAt(unqualified.length() - 1) == '>';
		}
		return false;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
}

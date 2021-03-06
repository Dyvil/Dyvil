package dyvilx.tools.compiler.parser.type;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.CodeAnnotation;
import dyvilx.tools.compiler.ast.generic.Variance;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.Mutability;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.*;
import dyvilx.tools.compiler.ast.type.generic.*;
import dyvilx.tools.compiler.ast.type.raw.NamedType;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.annotation.AnnotationParser;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

import static dyvilx.tools.parsing.lexer.BaseSymbols.isTerminator;

public final class TypeParser extends Parser implements Consumer<IType>
{
	// =============== Constants ===============

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
	public static final int IGNORE_LAMBDA   = 1 << 1;
	public static final int CLOSE_ANGLE     = 1 << 2;
	public static final int IGNORE_OPERATOR = 1 << 3;
	public static final int OPTIONAL        = 1 << 4;

	// =============== Fields ===============

	protected Consumer<IType> consumer;

	private IType parentType;
	private IType type;

	private int flags;

	// =============== Constructors ===============

	public TypeParser(Consumer<IType> consumer)
	{
		this.consumer = consumer;
		// this.mode = NAME;
	}

	public TypeParser(Consumer<IType> consumer, boolean closeAngle)
	{
		this.consumer = consumer;

		if (closeAngle)
		{
			this.flags = CLOSE_ANGLE;
		}
		// this.mode = NAME;
	}

	protected TypeParser(Consumer<IType> consumer, IType parentType, int flags)
	{
		this.consumer = consumer;
		this.parentType = parentType;
		this.flags = flags;
		// this.mode = NAME;
	}

	// =============== Static Methods ===============

	/**
	 * @param token
	 * 	the token
	 *
	 * @return {@code true} iff the given token is a symbol token that starts with {@code <}. This includes the special
	 * left-arrow token {@code <-}.
	 */
	public static boolean isGenericStart(IToken token)
	{
		return isGenericStart(token, token.type());
	}

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
	 * @param token
	 * 	the token
	 *
	 * @return {@code true} iff the given token is a symbol token that starts with {@code >}
	 */
	public static boolean isGenericEnd(IToken token)
	{
		return isGenericEnd(token, token.type());
	}

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
	 * @param token
	 * 	the token
	 *
	 * @return {@code true} iff the given token is a symbol token that ends with {@code >}. This includes the special
	 * right-arrow {@code ->} and double-right-arrow {@code =>} tokens.
	 */
	public static boolean isGenericEnd2(IToken token)
	{
		return isGenericEnd2(token, token.type());
	}

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

	// =============== Methods ===============

	public TypeParser withFlags(int flags)
	{
		this.flags |= flags;
		return this;
	}

	private TypeParser subParser(Consumer<IType> consumer)
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
					Annotation a = new CodeAnnotation(token.raw());
					pm.pushParser(new AnnotationParser(a));
					this.type = new AnnotatedType(a);
					this.mode = ANNOTATION_END;
					return;
				case BaseSymbols.OPEN_PARENTHESIS:
				{
					final TypeList arguments;
					if (this.parentType != null)
					{
						final FunctionType functionType = new FunctionType();
						functionType.setExtension(true);
						this.type = functionType;
						arguments = functionType.getArguments();
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

					final FunctionType functionType = new FunctionType(token.raw());
					final TypeList arguments = functionType.getArguments();
					if (this.parentType != null)
					{
						arguments.add(this.parentType);
					}
					pm.pushParser(this.subParser(arguments));
					this.type = functionType;
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

						this.end(pm, token);
						return;
					}

					// strip the trailing > and reparse the first part of the token
					// Handles Type<_> gracefully
					pm.splitReparse(token, closeAngleIndex);
					return;
				}
			}

			if (!Tokens.isIdentifier(type))
			{
				if (isTerminator(type))
				{
					this.end(pm, token);
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
				final FunctionType functionType;
				if (this.type instanceof FunctionType)
				{
					functionType = (FunctionType) this.type;
				}
				else
				{
					functionType = new FunctionType(nextToken.raw(), ((TupleType) this.type).getArguments());
					this.type = functionType;
				}
				functionType.setPosition(nextToken);

				this.mode = LAMBDA_END;
				pm.skip();
				pm.pushParser(this.subParser(functionType.getArguments()));
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

				final TypeList arguments = ((GenericType) this.type).getArguments();
				pm.pushParser(new TypeListParser(arguments).withFlags(TypeListParser.CLOSE_ANGLE));
				this.mode = GENERICS_END;
				return;
			}
			return;
		case ANNOTATION_END:
			this.mode = END;
			pm.pushParser(this.subParser(((AnnotatedType) this.type)::setType), true);
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
				final boolean leftNeighbor = token.prev().isNeighboring(token);
				final boolean rightNeighbor = token.isNeighboring(next);
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
					final FunctionType functionType = new FunctionType(token.raw(), this.type);
					this.type = functionType;
					this.mode = LAMBDA_END;
					pm.pushParser(this.subParser(functionType.getArguments()));
					return;
				}
				break;
			}
			// Fallthrough
		case LAMBDA_END:
			this.end(pm, token);
			return;
		}
	}

	private void end(IParserManager pm, IToken token)
	{
		if (this.type != null)
		{
			this.consumer.accept(this.type);
		}
		else if ((this.flags & OPTIONAL) == 0)
		{
			pm.report(Markers.syntaxError(token, "type.expected", token));
			this.consumer.accept(Types.UNKNOWN);
		}
		pm.popParser(true);
	}

	@Override
	public void accept(IType type)
	{
		this.type = type;
	}
}

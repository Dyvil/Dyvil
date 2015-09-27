package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.generic.type.GenericType;
import dyvil.tools.compiler.ast.generic.type.NamedGenericType;
import dyvil.tools.compiler.ast.generic.type.WildcardType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.*;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.compiler.util.ParserUtil;

public final class TypeParser extends Parser
{
	public static final int	NAME			= 1;
	public static final int	GENERICS		= 2;
	public static final int	GENERICS_END	= 4;
	public static final int	ARRAY_COLON		= 8;
	public static final int	ARRAY_END		= 16;
	public static final int	WILDCARD_TYPE	= 32;
	public static final int	TUPLE_END		= 64;
	public static final int	LAMBDA_TYPE		= 128;
	public static final int	LAMBDA_END		= 256;
	public static final int	ANNOTATION_END	= 512;
	
	protected ITypeConsumer typed;
	
	private IType type;
	
	public TypeParser(ITypeConsumer consumer)
	{
		this.typed = consumer;
		this.mode = NAME;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case END:
			if (type == Tokens.SYMBOL_IDENTIFIER)
			{
				Name name = token.nameValue();
				if (name == Name.qmark)
				{
					this.typed.setType(new OptionType(this.type));
					pm.popParser();
					return;
				}
			}
			
			if (this.type != null)
			{
				this.typed.setType(this.type);
			}
			pm.popParser(true);
			return;
		case NAME:
			switch (type)
			{
			case Symbols.AT:
				Annotation a = new Annotation();
				pm.pushParser(pm.newAnnotationParser(a));
				this.type = new AnnotatedType(a);
				this.mode = ANNOTATION_END;
				return;
			case Symbols.OPEN_PARENTHESIS:
				TupleType tupleType = new TupleType();
				pm.pushParser(new TypeListParser(tupleType));
				this.type = tupleType;
				this.mode = TUPLE_END;
				return;
			case Symbols.OPEN_SQUARE_BRACKET:
				this.mode = ARRAY_COLON;
				ArrayType at = new ArrayType();
				this.type = at;
				pm.pushParser(pm.newTypeParser(at));
				return;
			case Symbols.ARROW_OPERATOR:
				LambdaType lt = new LambdaType();
				this.type = lt;
				pm.pushParser(pm.newTypeParser(lt));
				this.mode = LAMBDA_END;
				return;
			case Keywords.NULL:
				this.typed.setType(Types.NULL);
				pm.popParser();
				return;
			case Symbols.WILDCARD:
				this.type = new WildcardType(token.raw());
				this.mode = WILDCARD_TYPE;
				return;
			}
			if (ParserUtil.isIdentifier(type))
			{
				IToken next = token.next();
				int nextType = next.type();
				if (nextType == Symbols.OPEN_SQUARE_BRACKET || nextType == Symbols.GENERIC_CALL)
				{
					this.type = new NamedGenericType(token.raw(), token.nameValue());
					this.mode = GENERICS;
					return;
				}
				if (nextType == Symbols.ARROW_OPERATOR)
				{
					LambdaType lt = new LambdaType(new NamedType(token.raw(), token.nameValue()));
					this.type = lt;
					this.mode = LAMBDA_END;
					pm.skip();
					pm.pushParser(pm.newTypeParser(lt));
					return;
				}
				
				this.type = new NamedType(token.raw(), token.nameValue());
				this.mode = END;
				return;
			}
			if (ParserUtil.isTerminator(type))
			{
				pm.popParser(true);
				return;
			}
			pm.report(token, "Invalid Type - Invalid " + token);
			return;
		case TUPLE_END:
			if (type != Symbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "Invalid Tuple Type - ')' expected");
			}
			IToken next = token.next();
			int nextType = next.type();
			if (nextType == Symbols.ARROW_OPERATOR)
			{
				TupleType tupleType = (TupleType) this.type;
				this.type = new LambdaType(tupleType);
				this.mode = LAMBDA_TYPE;
				return;
			}
			
			this.type.expandPosition(token);
			this.mode = END;
			return;
		case LAMBDA_TYPE:
			pm.pushParser(pm.newTypeParser((LambdaType) this.type));
			this.mode = LAMBDA_END;
			return;
		case LAMBDA_END:
			this.type.expandPosition(token.prev());
			this.typed.setType(this.type);
			pm.popParser(true);
			return;
		case ARRAY_COLON:
			if (type == Symbols.COLON)
			{
				this.mode = ARRAY_END;
				MapType mt = new MapType(this.type.getElementType(), null);
				this.type = mt;
				pm.pushParser(new TypeParser(mt::setValueType));
				return;
			}
			//$FALL-THROUGH$
		case ARRAY_END:
			this.type.expandPosition(token);
			this.mode = END;
			if (type != Symbols.CLOSE_SQUARE_BRACKET)
			{
				pm.reparse();
				pm.report(token, "Invalid Array Type - ']' expected");
			}
			return;
		case GENERICS:
			if (type == Symbols.OPEN_SQUARE_BRACKET || type == Symbols.GENERIC_CALL)
			{
				pm.pushParser(new TypeListParser((GenericType) this.type));
				this.mode = GENERICS_END;
				return;
			}
			this.type.expandPosition(token.prev());
			this.typed.setType(this.type);
			pm.popParser(true);
			return;
		case WILDCARD_TYPE:
			Name name = token.nameValue();
			WildcardType wt = (WildcardType) this.type;
			if (name == Name.ltcolon) // <: - Upper Bound
			{
				wt.setVariance(Variance.COVARIANT);
				pm.pushParser(pm.newTypeParser(wt));
				this.mode = END;
				return;
			}
			if (name == Name.gtcolon) // >: - Lower Bound
			{
				wt.setVariance(Variance.CONTRAVARIANT);
				pm.pushParser(pm.newTypeParser(wt));
				this.mode = END;
				return;
			}
			this.typed.setType(this.type);
			pm.popParser(true);
			return;
		case GENERICS_END:
			this.mode = END;
			if (type != Symbols.CLOSE_SQUARE_BRACKET)
			{
				pm.reparse();
				pm.report(token, "Invalid Generic Type - ']' expected");
			}
			return;
		case ANNOTATION_END:
			this.mode = END;
			pm.pushParser(pm.newTypeParser((ITyped) this.type), true);
			return;
		}
	}
}

package dyvil.tools.compiler.parser.annotation;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.member.IAnnotationList;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionMapParser;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class AnnotationParser extends Parser
{
	public static final int	NAME				= 0;
	public static final int	PARAMETERS_START	= 1;
	public static final int	PARAMETERS_END		= 2;
	
	protected IAnnotationList	annotatable;
	
	private Annotation		annotation;
	
	public AnnotationParser(IAnnotationList annotatable)
	{
		this.annotatable = annotatable;
	}
	
	@Override
	public void parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == NAME)
		{
			if (ParserUtil.isIdentifier(type))
			{
				this.annotation = new Annotation(token.raw(), token.value().substring(1));
				this.mode = PARAMETERS_START;
				
				if (token.next().type() != Tokens.OPEN_PARENTHESIS)
				{
					this.annotatable.addAnnotation(this.annotation);
					pm.popParser();
				}
				
				return;
			}
			throw new SyntaxError(token, "Invalid Annotation - Name expected");
		}
		if (this.mode == PARAMETERS_START)
		{
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ExpressionMapParser(this.annotation.arguments));
				this.mode = PARAMETERS_END;
				return;
			}
			throw new SyntaxError(token, "Invalid Annotation - '(' expected");
		}
		if (this.mode == PARAMETERS_END)
		{
			if (type == Tokens.CLOSE_PARENTHESIS)
			{
				this.annotation.expandPosition(token);
				this.annotatable.addAnnotation(this.annotation);
				pm.popParser();
				return;
			}
			throw new SyntaxError(token, "Invalid Annotation - ')' expected");
		}
	}
}

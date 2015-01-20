package dyvil.tools.compiler.parser.annotation;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.member.IAnnotated;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionMapParser;
import dyvil.tools.compiler.util.Tokens;

public class AnnotationParser extends Parser
{
	public static final int	NAME				= 0;
	public static final int	PARAMETERS_START	= 1;
	public static final int	PARAMETERS_END		= 2;
	
	protected IAnnotated	annotatable;
	
	private Annotation		annotation;
	
	public AnnotationParser(IAnnotated annotatable)
	{
		this.annotatable = annotatable;
	}
	
	@Override
	public boolean parse(ParserManager pm, IToken token) throws SyntaxError
	{
		if (this.mode == NAME)
		{
			this.annotation = new Annotation(token.raw(), token.value().substring(1));
			this.mode = PARAMETERS_START;
			
			if (!token.next().isType(Tokens.OPEN_PARENTHESIS))
			{
				this.annotatable.addAnnotation(this.annotation);
				pm.popParser();
			}
			
			return true;
		}
		if (this.isInMode(PARAMETERS_START))
		{
			if (token.isType(Tokens.OPEN_PARENTHESIS))
			{
				pm.pushParser(new ExpressionMapParser(this.annotation));
				this.mode = PARAMETERS_END;
				return true;
			}
		}
		if (this.isInMode(PARAMETERS_END))
		{
			if (token.isType(Tokens.CLOSE_PARENTHESIS))
			{
				this.annotation.expandPosition(token);
				this.annotatable.addAnnotation(this.annotation);
				pm.popParser();
				return true;
			}
		}
		
		return false;
	}
}

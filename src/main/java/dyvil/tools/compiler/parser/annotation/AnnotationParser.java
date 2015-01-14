package dyvil.tools.compiler.parser.annotation;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IAnnotated;
import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionMapParser;

public class AnnotationParser extends Parser
{
	public static final int	NAME				= 0;
	public static final int	PARAMETERS_START	= 1;
	public static final int	PARAMETERS_END		= 2;
	
	protected IContext		context;
	protected IAnnotated	annotatable;
	
	private Annotation		annotation;
	
	public AnnotationParser(IContext context, IAnnotated annotatable)
	{
		this.context = context;
		this.annotatable = annotatable;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == NAME)
		{
			this.annotation = new Annotation(token.raw(), value.substring(1));
			this.mode = PARAMETERS_START;
			
			if (!token.next().equals("("))
			{
				this.annotatable.addAnnotation(this.annotation);
				pm.popParser();
			}
			
			return true;
		}
		if (this.isInMode(PARAMETERS_START))
		{
			if ("(".equals(value))
			{
				pm.pushParser(new ExpressionMapParser(this.context, this.annotation));
				this.mode = PARAMETERS_END;
				return true;
			}
		}
		if (this.isInMode(PARAMETERS_END))
		{
			if (")".equals(value))
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

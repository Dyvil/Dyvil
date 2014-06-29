package dyvil.tools.compiler.parser.annotation;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.field.ValueParser;

public class AnnotationParametersParser extends Parser
{
	protected Annotation annotation;
	
	private Variable parameter;
	
	public AnnotationParametersParser(Annotation annotation)
	{
		this.annotation = annotation;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (",".equals(value))
		{
			this.annotation.addParameter(this.parameter);
			return true;
		}
		else if (")".equals(value))
		{
			pm.popParser(token);
			return true;
		}
		else if ("=".equals(value))
		{
			this.parameter = new Variable(token.prev().value());
			pm.pushParser(new ValueParser(this.parameter));
			return true;
		}
		return false;
	}
}

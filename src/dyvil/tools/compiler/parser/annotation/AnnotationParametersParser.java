package dyvil.tools.compiler.parser.annotation;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.field.Variable;
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
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxException
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

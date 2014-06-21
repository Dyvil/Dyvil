package dyvil.tools.compiler.parser.classbody;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.member.Variable;

public class AnnotationParser extends Parser
{
	private Annotation annotation;
	private Variable parameter;
	
	private String name;
	
	public AnnotationParser(Annotation annotation)
	{
		this.annotation = annotation;
	}
	
	@Override
	public void parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		if (",".equals(value))
		{
			this.parameter.setName(this.name);
			this.annotation.addParameter(this.parameter);
			this.parameter = new Variable();
		}
		else if (")".equals(value))
		{
			jcp.popParser();
		}
		else if (!"=".equals(value))
		{
			if (this.name == null)
			{
				this.name = value;
			}
			else
			{
				jcp.pushParser(new ValueParser(this.parameter.getValue(), ","));
			}
		}
	}
}

package dyvil.tools.compiler.parser.annotation;

import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.compiler.parser.expression.ArgumentListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class AnnotationParser extends Parser
{
	public static final int NAME             = 1;
	public static final int PARAMETERS_START = 2;
	public static final int PARAMETERS_END   = 4;
	
	private IAnnotation annotation;
	
	public AnnotationParser(IAnnotation annotation)
	{
		this.annotation = annotation;
		this.mode = NAME;
	}
	
	public void reset(IAnnotation annotation)
	{
		this.annotation = annotation;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case NAME:
			this.annotation.setPosition(token.prev());

			pm.pushParser(new TypeParser(this.annotation), true);
			this.mode = PARAMETERS_START;
			return;
		case PARAMETERS_START:
			this.annotation.expandPosition(token.prev());

			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				ArgumentListParser.parseArguments(pm, token.next(), this.annotation);
				this.mode = PARAMETERS_END;
				return;
			}
			
			pm.popParser(true);
			return;
		case PARAMETERS_END:
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "annotation.parenthesis");
			}
			pm.popParser();
		}
	}
}

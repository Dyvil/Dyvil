package dyvilx.tools.compiler.parser.annotation;

import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.parser.expression.ArgumentListParser;
import dyvilx.tools.compiler.parser.type.TypeParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

public class AnnotationParser extends Parser
{
	public static final int NAME             = 1;
	public static final int PARAMETERS_START = 2;
	public static final int PARAMETERS_END   = 4;

	private Annotation annotation;

	public AnnotationParser(Annotation annotation)
	{
		this.annotation = annotation;
		this.mode = NAME;
	}

	public void reset(Annotation annotation)
	{
		this.annotation = annotation;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case NAME:
			this.annotation.setPosition(token.prev());

			pm.pushParser(new TypeParser(this.annotation::setType), true);
			this.mode = PARAMETERS_START;
			return;
		case PARAMETERS_START:
			this.annotation.expandPosition(token.prev());

			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				ArgumentListParser.parseArguments(pm, token.next(), this.annotation::setArguments);
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

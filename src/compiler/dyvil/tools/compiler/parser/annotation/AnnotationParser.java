package dyvil.tools.compiler.parser.annotation;

import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.ArgumentMap;
import dyvil.tools.compiler.ast.type.NamedType;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.expression.ExpressionListParser;
import dyvil.tools.compiler.parser.expression.ExpressionMapParser;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.token.IToken;

public class AnnotationParser extends Parser
{
	public static final int	NAME				= 1;
	public static final int	PARAMETERS_START	= 2;
	public static final int	PARAMETERS_END		= 4;
	
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
			if (ParserUtil.isIdentifier(type))
			{
				Name name = token.nameValue();
				ICodePosition position = token.raw();
				this.annotation.setPosition(position);
				this.annotation.setType(new NamedType(position, name));
				
				this.mode = PARAMETERS_START;
				return;
			}
			pm.report(token, "Invalid Annotation - Name expected");
			return;
		case PARAMETERS_START:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				IToken next = token.next();
				if (ParserUtil.isIdentifier(next.type()) && next.next().type() == BaseSymbols.COLON)
				{
					ArgumentMap map = new ArgumentMap();
					this.annotation.setArguments(map);
					pm.pushParser(new ExpressionMapParser(map));
				}
				else
				{
					ArgumentList list = new ArgumentList();
					this.annotation.setArguments(list);
					pm.pushParser(new ExpressionListParser(list));
				}
				
				this.mode = PARAMETERS_END;
				return;
			}
			
			pm.popParser(true);
			return;
		case PARAMETERS_END:
			if (type == BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.popParser();
				return;
			}
			pm.report(token, "Invalid Annotation - ')' expected");
			return;
		}
	}
}

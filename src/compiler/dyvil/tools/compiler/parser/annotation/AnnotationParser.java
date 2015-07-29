package dyvil.tools.compiler.parser.annotation;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.ArgumentMap;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.type.NamedType;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.expression.ExpressionListParser;
import dyvil.tools.compiler.parser.expression.ExpressionMapParser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public class AnnotationParser extends Parser
{
	public static final int	NAME				= 1;
	public static final int	PARAMETERS_START	= 2;
	public static final int	PARAMETERS_END		= 4;
	
	private Annotation annotation;
	
	public AnnotationParser(Annotation annotation)
	{
		this.annotation = annotation;
		this.mode = NAME;
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
				this.annotation.setName(name);
				this.annotation.setType(new NamedType(token.raw(), name));
				
				this.mode = PARAMETERS_START;
				return;
			}
			pm.report(new SyntaxError(token, "Invalid Annotation - Name expected"));
			return;
		case PARAMETERS_START:
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				IToken next = token.next();
				if (ParserUtil.isIdentifier(next.type()) && next.next().type() == Symbols.COLON)
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
			
			if (type == Symbols.OPEN_SQUARE_BRACKET)
			{
				SingleArgument arg = new SingleArgument();
				this.annotation.setArguments(arg);
				pm.popParser();
				pm.pushParser(pm.newExpressionParser(arg), true);
				return;
			}
			
			pm.popParser(true);
			return;
		case PARAMETERS_END:
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				pm.popParser();
				return;
			}
			pm.report(new SyntaxError(token, "Invalid Annotation - ')' expected"));
			return;
		}
	}
}

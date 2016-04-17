package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.consumer.IMemberConsumer;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public class ClassBodyParser extends Parser
{
	protected static final int OPEN_BRACE = 0;
	protected static final int SEPARATOR  = 1;

	protected IMemberConsumer<IField> consumer;

	public ClassBodyParser(IMemberConsumer<IField> consumer)
	{
		this.consumer = consumer;
		// this.mode = OPEN_BRACE
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case OPEN_BRACE:
			pm.pushParser(new MemberParser<>(this.consumer));
			this.mode = SEPARATOR;
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.report(token, "class.body.open_brace");
				pm.reparse();
			}
			return;
		case SEPARATOR:
			switch (type)
			{
			case Tokens.EOF:
				pm.report(token, "class.body.declaration.end");
				pm.popParser();
				return;
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				pm.popParser(true);
				return;
			case BaseSymbols.SEMICOLON:
				pm.pushParser(new MemberParser<>(this.consumer));
				return;
			}

			pm.pushParser(new MemberParser<>(this.consumer), true);
			pm.report(token, "class.body.declaration.end");
		}
	}
}

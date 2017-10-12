package dyvilx.tools.compiler.parser.classes;

import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.type.TypeParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class DataMemberParser<T extends IDataMember> extends AbstractMemberParser
{
	protected static final int DECLARATOR = 0;
	protected static final int NAME       = 1;
	protected static final int TYPE       = 2;

	protected IDataMemberConsumer<T> consumer;

	private T dataMember;

	public DataMemberParser(IDataMemberConsumer<T> consumer)
	{
		this.consumer = consumer;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case DECLARATOR:
			switch (type)
			{
			case DyvilSymbols.AT:
				this.parseAnnotation(pm, token);
				return;
			case DyvilKeywords.LET:
				this.attributes.addFlag(Modifiers.FINAL);
				// Fallthrough
			case DyvilKeywords.VAR:
				this.mode = NAME;
				return;
			}

			if (this.parseModifier(pm, token))
			{
				return;
			}
			// Fallthrough
		case NAME:
			if (!Tokens.isIdentifier(type))
			{
				pm.report(token, "variable.identifier");
				return;
			}

			this.dataMember = this.consumer
				                  .createDataMember(token.raw(), token.nameValue(), Types.UNKNOWN, this.attributes);

			this.mode = TYPE;
			return;
		case TYPE:
			if (type == BaseSymbols.COLON)
			{
				// ... IDENTIFIER : TYPE ...
				pm.pushParser(new TypeParser(this.dataMember));
				this.mode = END;
				return;
			}
			// Fallthrough
		case END:
			this.consumer.addDataMember(this.dataMember);
			pm.popParser(type != Tokens.EOF);
		}
	}
}

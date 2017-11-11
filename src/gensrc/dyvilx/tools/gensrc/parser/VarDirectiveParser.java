package dyvilx.tools.gensrc.parser;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.consumer.IDataMemberConsumer;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.parser.classes.DataMemberParser;
import dyvilx.tools.gensrc.ast.directive.VarDirective;
import dyvilx.tools.gensrc.lexer.GenSrcSymbols;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

public class VarDirectiveParser extends Parser implements IDataMemberConsumer<IVariable>
{
	private static final int KEYWORD     = 0;
	private static final int OPEN_PAREN  = 1;
	private static final int CLOSE_PAREN = 3;
	private static final int BODY        = 4;
	private static final int BODY_END    = 5;

	private final StatementList directives;

	private VarDirective varDirective;

	public VarDirectiveParser(StatementList directives)
	{
		this.directives = directives;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case KEYWORD:
			AttributeList attributes = new AttributeList();
			switch (type)
			{
			case GenSrcSymbols.VAR:
				break;
			case GenSrcSymbols.CONST:
			case GenSrcSymbols.LET:
				attributes.addFlag(Modifiers.FINAL);
				break;
			default:
				pm.report(token, "var.declarator");
				return;
			}

			this.mode = OPEN_PAREN;
			return;
		case OPEN_PAREN:
			if (type != BaseSymbols.OPEN_PARENTHESIS)
			{
				this.directives.add(this.varDirective);
				pm.popParser(true);
				return;
			}

			pm.pushParser(new DataMemberParser<>(this));
			return;
		case CLOSE_PAREN:
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "var.close_paren");
				return;
			}

			this.mode = BODY;
			return;
		case BODY:
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				this.directives.add(this.varDirective);
				pm.popParser(true);
				return;
			}

			final StatementList body = new StatementList();
			pm.pushParser(new BlockParser(body));
			this.varDirective.setBlock(body);
			this.mode = BODY_END;
			return;
		case BODY_END:
			assert type == BaseSymbols.CLOSE_CURLY_BRACKET;
			this.directives.add(this.varDirective);
			pm.popParser();
		}
	}

	@Override
	public void addDataMember(IVariable variable)
	{
		this.varDirective = new VarDirective(variable);
	}

	@Override
	public IVariable createDataMember(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new Variable(position, name, type, attributes);
	}
}

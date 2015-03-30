package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.access.FieldAssign;
import dyvil.tools.compiler.ast.access.FieldInitializer;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.statement.Label;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public final class StatementListParser extends Parser implements IValued, ITyped, IParserManager
{
	private static final int	EXPRESSION	= 1;
	private static final int	TYPE		= 2;
	private static final int	SEPARATOR	= 4;
	
	protected IValueList		statementList;
	
	private Name				label;
	
	private IToken				firstToken;
	private TypeParser			typeParser;
	private IType				type;
	
	private Parser				parser;
	private IParserManager		pm;
	
	public StatementListParser(IValueList valueList)
	{
		this.statementList = valueList;
		this.mode = EXPRESSION;
	}
	
	@Override
	public void reset()
	{
		this.mode = EXPRESSION;
		this.label = null;
		this.firstToken = null;
		this.typeParser = null;
		this.pm = null;
		this.type = null;
		this.parser = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		
		if (type == Symbols.CLOSE_CURLY_BRACKET)
		{
			pm.popParser(true);
			return;
		}
		
		if (this.mode == EXPRESSION)
		{
			if (ParserUtil.isIdentifier(type) && token.next().type() == Symbols.COLON)
			{
				this.label = token.nameValue();
				pm.skip();
				return;
			}
			
			this.firstToken = token;
			this.parser = this.typeParser = new TypeParser(this);
			this.pm = pm;
			this.mode = TYPE;
		}
		if (this.mode == TYPE)
		{
			if (ParserUtil.isIdentifier(type) && token.next().type() == Symbols.EQUALS)
			{
				if (this.type == null)
				{
					this.typeParser.end();
				}
				
				if (this.type != null)
				{
					FieldInitializer fi = new FieldInitializer(token.raw(), token.nameValue(), this.type);
					pm.pushParser(new ExpressionParser(fi));
					this.statementList.addValue(fi);
				}
				else
				{
					FieldAssign fa = new FieldAssign(token.raw(), null, token.nameValue());
					pm.pushParser(new ExpressionParser(fa));
					this.statementList.addValue(fa);
				}
				
				this.reset();
				this.mode = SEPARATOR;
				pm.skip();
				return;
			}
			else if (this.typeParser == null)
			{
				pm.jump(this.firstToken);
				this.reset();
				pm.pushParser(new ExpressionParser(this));
				this.mode = SEPARATOR;
				return;
			}
			
			if (type == Symbols.SEMICOLON && token.isInferred())
			{
				return;
			}
			
			try
			{
				this.parser.parse(this, token);
			}
			catch (Throwable ex)
			{
				pm.jump(this.firstToken);
				this.reset();
				pm.pushParser(new ExpressionParser(this));
				this.mode = SEPARATOR;
			}
			
			return;
		}
		if (this.mode == SEPARATOR)
		{
			if (type == Symbols.SEMICOLON)
			{
				this.mode = EXPRESSION;
				return;
			}
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			if (token.prev().type() == Symbols.CLOSE_CURLY_BRACKET)
			{
				this.mode = EXPRESSION;
				pm.reparse();
				return;
			}
			throw new SyntaxError(token, "Invalid Expression List - ';' expected");
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.label != null)
		{
			this.statementList.addValue(value, new Label(this.label, value));
			this.label = null;
		}
		else
		{
			this.statementList.addValue(value);
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public void skip()
	{
		this.pm.skip();
	}
	
	@Override
	public void skip(int n)
	{
		this.pm.skip(n);
	}
	
	@Override
	public void reparse()
	{
		this.pm.reparse();
	}
	
	@Override
	public void jump(IToken token)
	{
		this.pm.jump(token);
	}
	
	@Override
	public void setParser(Parser parser)
	{
		this.parser = parser;
	}
	
	@Override
	public Parser getParser()
	{
		return this.parser;
	}
	
	@Override
	public void pushParser(Parser parser)
	{
		parser.setParent(this.parser);
		this.parser = parser;
	}
	
	@Override
	public void pushParser(Parser parser, boolean reparse)
	{
		parser.setParent(this.parser);
		this.parser = parser;
		this.pm.reparse();
	}
	
	@Override
	public void popParser()
	{
		if (this.parser == this.typeParser)
		{
			this.typeParser = null;
			return;
		}
		
		this.parser = this.parser.getParent();
	}
	
	@Override
	public void popParser(boolean reparse) throws SyntaxError
	{
		if (reparse)
		{
			this.pm.reparse();
		}
		
		if (this.parser == this.typeParser)
		{
			this.typeParser = null;
			return;
		}
		
		this.parser = this.parser.getParent();
	}
	
	// ----- Ignore -----
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
}

package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public final class TypeListParser extends Parser implements ITyped
{
	protected ITypeList	typeList;
	
	public TypeListParser(ITypeList typeList)
	{
		this.typeList = typeList;
	}
	
	@Override
	public void reset()
	{
		this.mode = 0;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Symbols.OPEN_CURLY_BRACKET)
		{
			pm.popParser(true);
			return;
		}
		if (type == Symbols.SEMICOLON && token.isInferred())
		{
			pm.popParser(true);
			return;
		}
		
		if (this.mode == 0)
		{
			this.mode = 1;
			pm.pushParser(new TypeParser(this), true);
			return;
		}
		if (this.mode == 1)
		{
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			this.mode = 0;
			if (ParserUtil.isSeperator(type))
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Type List - ',' expected");
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.typeList.addType(type);
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
}

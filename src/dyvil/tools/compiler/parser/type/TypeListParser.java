package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.api.ITypeList;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.SyntaxException;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class TypeListParser extends Parser implements ITyped
{
	protected ITypeList	typeList;
	private Type type;
	
	public TypeListParser(ITypeList typeList)
	{
		this.typeList = typeList;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxException
	{
		if (")".equals(value))
		{
			pm.popParser();
			this.typeList.addType(this.type);
			return true;
		}
		else if (",".equals(value) || ";".equals(value) || ":".equals(value))
		{
			this.type.setSeperator(value.charAt(0));
			this.typeList.addType(this.type);
			return true;
		}
		else
		{
			pm.pushParser(new TypeParser(this));
			return true;
		}
	}

	@Override
	public void setType(Type type)
	{
		this.type = type;
	}

	@Override
	public Type getType()
	{
		return this.type;
	}
}

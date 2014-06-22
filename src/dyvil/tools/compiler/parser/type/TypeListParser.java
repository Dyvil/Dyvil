package dyvil.tools.compiler.parser.type;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.api.ITypeList;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.type.Type;

public class TypeListParser extends Parser implements ITyped
{
	protected ITypeList	typeList;
	private Type type;
	
	public TypeListParser(ITypeList typeList)
	{
		this.typeList = typeList;
	}
	
	@Override
	public void parse(ParserManager pm, String value, IToken token) throws SyntaxException
	{
		if (")".equals(value))
		{
			this.typeList.addType(this.type);
			pm.popParser();
		}
		else if (",".equals(value) || ";".equals(value) || ":".equals(value))
		{
			this.type.setSeperator(value.charAt(0));
			this.typeList.addType(this.type);
		}
		else
		{
			pm.pushParser(new TypeParser(this));
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

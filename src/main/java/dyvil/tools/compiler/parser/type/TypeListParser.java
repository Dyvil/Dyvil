package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.api.ITypeList;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class TypeListParser extends Parser implements ITyped
{
	protected ITypeList	typeList;
	private Type		type;
	
	public TypeListParser(ITypeList typeList)
	{
		this.typeList = typeList;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == 0)
		{
			pm.pushParser(new TypeParser(this), token);
			this.mode = 1;
			return true;
		}
		if (this.mode == 1)
		{
			if (",".equals(value) || ";".equals(value) || ":".equals(value))
			{
				this.type.setSeperator(value.charAt(0));
				this.typeList.addType(this.type);
				this.mode = 0;
				return true;
			}
		}
		
		pm.popParser(token);
		this.typeList.addType(this.type);
		return true;
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

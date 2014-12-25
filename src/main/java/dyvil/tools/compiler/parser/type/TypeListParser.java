package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.api.ITypeList;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class TypeListParser extends Parser implements ITyped
{
	protected IContext	context;
	protected ITypeList	typeList;
	
	private Type		type;
	
	public TypeListParser(IContext context, ITypeList typeList)
	{
		this.context = context;
		this.typeList = typeList;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == 0)
		{
			this.mode = 1;
			pm.pushParser(new TypeParser(this.context, this), true);
			return true;
		}
		if (this.mode == 1)
		{
			if (",".equals(value) || ";".equals(value))
			{
				this.typeList.addType(this.type);
				this.mode = 0;
				return true;
			}
		}
		
		pm.popParser(true);
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

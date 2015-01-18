package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.ParserUtil;

public class TypeListParser extends Parser implements ITyped
{
	protected ITypeList	typeList;
	public boolean		generic;
	
	private IType		type;
	
	public TypeListParser(ITypeList typeList)
	{
		this.typeList = typeList;
	}
	
	public TypeListParser(ITypeList typeList, boolean generic)
	{
		this.typeList = typeList;
		this.generic = generic;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == 0)
		{
			this.mode = 1;
			pm.pushParser(new TypeParser(this, this.generic), true);
			return true;
		}
		if (this.mode == 1)
		{
			if (ParserUtil.isTerminator(token.type()))
			{
				this.typeList.addType(this.type);
				this.mode = 0;
				return true;
			}
		}
		
		pm.popParser(true);
		if (this.type != null)
		{
			this.typeList.addType(this.type);
		}
		return true;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
}

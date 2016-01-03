package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.generic.ITypeParameterized;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.TypeParameter;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.token.IToken;

public final class TypeParameterParser extends Parser implements ITyped
{
	public static final int NAME          = 1;
	public static final int TYPE_VARIABLE = 16;
	
	public static final int UPPER = 1;
	public static final int LOWER = 2;
	
	protected ITypeParameterized typeParameterized;
	
	private byte           boundMode;
	private ITypeParameter typeParameter;
	
	public TypeParameterParser(ITypeParameterized typeParameterized)
	{
		this.typeParameterized = typeParameterized;
		this.mode = NAME;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (this.mode == NAME)
		{
			if (ParserUtil.isIdentifier(type))
			{
				Name name = token.nameValue();
				if (name == Names.plus || name == Names.minus)
				{
					IToken next = token.next();
					if (ParserUtil.isIdentifier(next.type()))
					{
						Variance v = name == Names.minus ? Variance.CONTRAVARIANT : Variance.COVARIANT;
						this.typeParameter = new TypeParameter(next, this.typeParameterized, next.nameValue(), v);
						this.mode = TYPE_VARIABLE;
						pm.skip();
						return;
					}
				}
				
				this.typeParameter = new TypeParameter(token, this.typeParameterized, token.nameValue(), Variance.INVARIANT);
				this.mode = TYPE_VARIABLE;
				return;
			}
			pm.report(token, "typeparameter.identifier");
			return;
		}
		if (this.mode == TYPE_VARIABLE)
		{
			if (ParserUtil.isTerminator(type))
			{
				if (this.typeParameter != null)
				{
					this.typeParameterized.addTypeParameter(this.typeParameter);
				}
				pm.popParser(true);
				return;
			}
			
			if (ParserUtil.isIdentifier(type))
			{
				Name name = token.nameValue();
				if (this.boundMode == 0)
				{
					if (name == Names.gtcolon) // >: - Lower Bound
					{
						pm.pushParser(pm.newTypeParser(this));
						this.boundMode = LOWER;
						return;
					}
					if (name == Names.ltcolon) // <: - Upper Bounds
					{
						pm.pushParser(pm.newTypeParser(this));
						this.boundMode = UPPER;
						return;
					}
				}
				else if (this.boundMode == UPPER)
				{
					if (name == Names.amp)
					{
						pm.pushParser(pm.newTypeParser(this));
						return;
					}
				}
			}

			if (this.typeParameter != null)
			{
				this.typeParameterized.addTypeParameter(this.typeParameter);
			}
			pm.popParser(true);
			pm.report(token, "typeparameter.bound.invalid");
			return;
		}
	}
	
	@Override
	public void setType(IType type)
	{
		if (this.boundMode == UPPER)
		{
			this.typeParameter.addUpperBound(type);
		}
		else if (this.boundMode == LOWER)
		{
			this.typeParameter.setLowerBound(type);
		}
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
}

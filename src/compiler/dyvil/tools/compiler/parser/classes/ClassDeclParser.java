package dyvil.tools.compiler.parser.classes;

import java.util.List;

import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ClassDeclParser extends Parser implements ITyped, ITypeList
{
	public static final int		MODIFIERS		= 0;
	public static final int		NAME			= 1;
	public static final int		GENERICS		= 2;
	public static final int		GENERICS_END	= 4;
	public static final int		EXTENDS			= 8;
	public static final int		IMPLEMENTS		= 16;
	public static final int		BODY			= 32;
	public static final int		BODY_END		= 64;
	
	protected CompilationUnit	unit;
	
	private CodeClass			theClass;
	
	public ClassDeclParser(CompilationUnit unit)
	{
		this.unit = unit;
		this.theClass = new CodeClass(null, unit);
		this.unit.addClass(this.theClass);
	}
	
	protected ClassDeclParser(CodeClass theClass)
	{
		this.unit = theClass.getUnit();
		this.theClass = theClass;
		this.mode = NAME;
	}
	
	@Override
	public boolean parse(ParserManager pm, IToken token) throws SyntaxError
	{
		String value = token.value();
		
		if (this.isInMode(MODIFIERS))
		{
			int i = 0;
			if ((i = Modifiers.CLASS.parse(value)) != -1)
			{
				if (this.theClass.addModifier(i))
				{
					throw new SyntaxError(token, "Duplicate Modifier '" + value + "' - Remove this Modifier");
				}
				return true;
			}
			else if ((i = Modifiers.CLASS_TYPE.parse(value)) != -1)
			{
				this.theClass.addModifier(i);
				this.mode = NAME;
				return true;
			}
			else if (value.charAt(0) == '@')
			{
				pm.pushParser(new AnnotationParser(this.theClass), true);
				return true;
			}
		}
		if (this.isInMode(NAME))
		{
			if (token.isType(Tokens.TYPE_IDENTIFIER))
			{
				this.theClass.setPosition(token.raw());
				this.theClass.setName(value);
				this.mode = GENERICS | EXTENDS | IMPLEMENTS | BODY;
				return true;
			}
			return false;
		}
		int type = token.type();
		if (this.isInMode(GENERICS))
		{
			if (type == Tokens.OPEN_SQUARE_BRACKET)
			{
				pm.pushParser(new TypeListParser(this, true));
				this.theClass.setGeneric();
				this.mode = GENERICS_END;
				return true;
			}
		}
		if (this.isInMode(GENERICS_END))
		{
			if (type == Tokens.CLOSE_SQUARE_BRACKET)
			{
				this.mode = EXTENDS | IMPLEMENTS | BODY;
				return true;
			}
			return false;
		}
		if (this.isInMode(EXTENDS))
		{
			if ("extends".equals(value))
			{
				pm.pushParser(new TypeParser(this));
				this.mode = IMPLEMENTS | BODY;
				return true;
			}
		}
		if (this.isInMode(IMPLEMENTS))
		{
			if ("implements".equals(value))
			{
				pm.pushParser(new TypeListParser(this));
				this.mode = BODY;
				return true;
			}
		}
		if (this.isInMode(BODY))
		{
			if (type == Tokens.OPEN_CURLY_BRACKET)
			{
				pm.pushParser(new ClassBodyParser(this.theClass));
				this.mode = BODY_END;
				return true;
			}
			if (ParserUtil.isTerminator(type))
			{
				this.theClass.expandPosition(token);
				pm.popParser();
				return true;
			}
			return false;
		}
		if (this.isInMode(BODY_END))
		{
			if (token.isType(Tokens.CLOSE_CURLY_BRACKET))
			{
				this.theClass.expandPosition(token);
				pm.popParser();
				return true;
			}
			return false;
		}
		return false;
	}
	
	@Override
	public void setType(IType type)
	{
		this.theClass.setSuperType(type);
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
	
	@Override
	public void setTypes(List<IType> types)
	{
	}
	
	@Override
	public List<IType> getTypes()
	{
		return null;
	}
	
	@Override
	public void addType(IType type)
	{
		if (this.mode == GENERICS_END)
		{
			this.theClass.addTypeVariable((ITypeVariable) type);
		}
		else
		{
			this.theClass.addInterface(type);
		}
	}
}

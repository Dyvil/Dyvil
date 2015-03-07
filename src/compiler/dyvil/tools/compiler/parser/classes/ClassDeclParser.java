package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.structure.DyvilFile;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.parser.type.TypeVariableListParser;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public final class ClassDeclParser extends Parser implements ITyped, ITypeList
{
	public static final int	MODIFIERS		= 0;
	public static final int	NAME			= 1;
	public static final int	GENERICS		= 2;
	public static final int	GENERICS_END	= 4;
	public static final int	EXTENDS			= 8;
	public static final int	IMPLEMENTS		= 16;
	public static final int	BODY			= 32;
	public static final int	BODY_END		= 64;
	
	protected DyvilFile		unit;
	protected CodeClass		theClass;
	
	public ClassDeclParser(DyvilFile unit)
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
	public void reset()
	{
		this.mode = NAME;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		String value = token.value();
		
		if (this.isInMode(MODIFIERS))
		{
			int i = 0;
			if ((i = ModifierTypes.CLASS.parse(value)) != -1)
			{
				if (this.theClass.addModifier(i))
				{
					throw new SyntaxError(token, "Duplicate Modifier '" + value + "' - Remove this Modifier");
				}
				return;
			}
			else if ((i = ModifierTypes.CLASS_TYPE.parse(value)) != -1)
			{
				this.theClass.addModifier(i);
				this.mode = NAME;
				return;
			}
			else if (value.charAt(0) == '@')
			{
				pm.pushParser(new AnnotationParser(this.theClass), true);
				return;
			}
		}
		int type = token.type();
		if (this.isInMode(NAME))
		{
			if (ParserUtil.isIdentifier(type))
			{
				this.theClass.setPosition(token.raw());
				this.theClass.setName(value);
				this.mode = GENERICS | EXTENDS | IMPLEMENTS | BODY;
				return;
			}
			throw new SyntaxError(token, "Invalid Class Declaration - Name expected");
		}
		if (this.isInMode(GENERICS))
		{
			if (type == Tokens.OPEN_SQUARE_BRACKET)
			{
				pm.pushParser(new TypeVariableListParser(this.theClass));
				this.theClass.setGeneric();
				this.mode = GENERICS_END;
				return;
			}
		}
		if (this.isInMode(GENERICS_END))
		{
			this.mode = EXTENDS | IMPLEMENTS | BODY;
			if (type == Tokens.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Generic Type Variable List - ']' expected", true);
		}
		if (this.isInMode(EXTENDS))
		{
			if ("extends".equals(value))
			{
				pm.pushParser(new TypeParser(this));
				this.mode = IMPLEMENTS | BODY;
				return;
			}
		}
		if (this.isInMode(IMPLEMENTS))
		{
			if ("implements".equals(value))
			{
				pm.pushParser(new TypeListParser(this));
				this.mode = BODY;
				return;
			}
		}
		if (this.isInMode(BODY))
		{
			if (type == Tokens.OPEN_CURLY_BRACKET)
			{
				pm.pushParser(new ClassBodyParser(this.theClass));
				this.mode = BODY_END;
				return;
			}
			pm.popParser();
			if (ParserUtil.isTerminator(type))
			{
				this.theClass.expandPosition(token);
				return;
			}
			throw new SyntaxError(token, "Invalid Class Declaration - '{' or ';' expected", true);
		}
		if (this.isInMode(BODY_END))
		{
			pm.popParser();
			if (type == Tokens.CLOSE_CURLY_BRACKET)
			{
				this.theClass.expandPosition(token);
				return;
			}
			throw new SyntaxError(token, "Invalid Class Declaration - '}' expected", true);
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.theClass.setSuperType(type);
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
	
	// Override Methods
	
	@Override
	public IType getType()
	{
		return null;
	}
	
	@Override
	public int typeCount()
	{
		return 0;
	}
	
	@Override
	public void setType(int index, IType type)
	{
	}
	
	@Override
	public IType getType(int index)
	{
		return null;
	}
}

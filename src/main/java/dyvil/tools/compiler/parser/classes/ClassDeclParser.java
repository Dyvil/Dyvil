package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.Classes;
import dyvil.tools.compiler.util.Modifiers;

public class ClassDeclParser extends Parser
{
	public static final int		MODIFIERS		= 0;
	public static final int		NAME			= 1;
	public static final int		EXTENDS			= 2;
	public static final int		SUPERCLASSES	= 4;
	public static final int		BODY			= 8;
	public static final int		BODY_END		= 16;
	
	protected CompilationUnit	unit;
	
	private AbstractClass		theClassDecl;
	private int					modifiers;
	
	public ClassDeclParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxError
	{
		if (this.isInMode(MODIFIERS))
		{
			int i = 0;
			if ((i = Modifiers.parseClassModifier(value)) != -1)
			{
				this.modifiers |= i;
				return true;
			}
			else if ((i = Classes.parse(value)) != -1)
			{
				this.theClassDecl = AbstractClass.create(i);
				this.theClassDecl.setModifiers(this.modifiers);
				this.unit.addClass(this.theClassDecl);
				
				this.modifiers = 0;
				this.mode = NAME;
				return true;
			}
		}
		if (this.isInMode(NAME))
		{
			if (token.isType(Token.TYPE_IDENTIFIER))
			{
				this.theClassDecl.setName(value);
				this.mode = EXTENDS | BODY;
				return true;
			}
		}
		if (this.isInMode(EXTENDS))
		{
			if ("extends".equals(value))
			{
				this.mode = SUPERCLASSES;
				return true;
			}
		}
		if (this.isInMode(SUPERCLASSES))
		{
			if (token.isType(Token.TYPE_IDENTIFIER))
			{
				this.theClassDecl.addSuperClass(this.unit.resolveClass(value));
				return true;
			}
		}
		if (this.isInMode(BODY))
		{
			if ("{".equals(value))
			{
				jcp.pushParser(new ClassBodyParser(this.theClassDecl));
				this.mode = BODY_END;
				return true;
			}
		}
		if (this.isInMode(BODY_END))
		{
			if ("}".equals(value))
			{
				jcp.popParser();
				return true;
			}
		}
		return false;
	}
}

package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.Modifiers;

public class ClassDeclParser extends Parser
{
	public static final int		MODIFIERS	= 0;
	public static final int		NAME		= 1;
	public static final int		EXTENDS		= 2;
	public static final int		IMPLEMENTS	= 4;
	public static final int		BODY		= 8;
	public static final int		BODY_END	= 16;
	
	protected CompilationUnit	unit;
	
	private CodeClass			theClassDecl;
	
	public ClassDeclParser(CompilationUnit unit)
	{
		this.unit = unit;
		this.theClassDecl = new CodeClass(null, unit, 0, null);
		this.unit.addClass(this.theClassDecl);
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.isInMode(MODIFIERS))
		{
			int i = 0;
			if ((i = Modifiers.CLASS.parse(value)) != -1)
			{
				if (this.theClassDecl.addModifier(i))
				{
					throw new SyntaxError(token, "Duplicate Modifier '" + value + "'", "Remove this Modifier");
				}
				return true;
			}
			else if ((i = Modifiers.CLASS_TYPE.parse(value)) != -1)
			{
				this.theClassDecl.setClassType(i);
				this.mode = NAME;
				return true;
			}
			else if (value.charAt(0) == '@')
			{
				pm.pushParser(new AnnotationParser(this.unit, this.theClassDecl), true);
				return true;
			}
		}
		if (this.isInMode(NAME))
		{
			if (token.isType(IToken.TYPE_IDENTIFIER))
			{
				this.theClassDecl.setPosition(token.raw());
				this.theClassDecl.setName(value);
				this.mode = EXTENDS | IMPLEMENTS | BODY;
				return true;
			}
		}
		if (this.isInMode(EXTENDS))
		{
			if ("extends".equals(value))
			{
				pm.pushParser(new TypeParser(this.unit, this.theClassDecl));
				this.mode = IMPLEMENTS | BODY;
				return true;
			}
		}
		if (this.isInMode(IMPLEMENTS))
		{
			if ("implements".equals(value))
			{
				pm.pushParser(new TypeListParser(this.unit, this.theClassDecl));
				this.mode = BODY;
				return true;
			}
		}
		if (this.isInMode(BODY))
		{
			if ("{".equals(value))
			{
				pm.pushParser(new ClassBodyParser(this.theClassDecl));
				this.mode = BODY_END;
				return true;
			}
		}
		if (this.isInMode(BODY_END))
		{
			if ("}".equals(value))
			{
				this.theClassDecl.expandPosition(token);
				pm.popParser();
				return true;
			}
		}
		return false;
	}
}

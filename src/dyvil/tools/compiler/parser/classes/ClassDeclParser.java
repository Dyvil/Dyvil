package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.lexer.SyntaxException;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class ClassDeclParser extends Parser
{
	public static final int		NAME			= 0;
	public static final int		SUPERCLASSES	= 1;
	
	protected CompilationUnit	unit;
	private int					mode;
	private AbstractClass		theClassDecl;
	
	public ClassDeclParser(CompilationUnit unit, AbstractClass decl)
	{
		this.unit = unit;
		this.theClassDecl = decl;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		if ("extends".equals(value))
		{
			this.mode = SUPERCLASSES;
			return true;
		}
		else if ("{".equals(value))
		{
			// TODO Modifiers
			jcp.pushParser(new ClassBodyParser(this.theClassDecl));
			return true;
		}
		else if (this.mode == NAME)
		{
			this.theClassDecl.setName(value);
			return true;
		}
		else if (this.mode == SUPERCLASSES)
		{			
			this.theClassDecl.addSuperClass(value);
			return true;
		}
		return false;
	}
}

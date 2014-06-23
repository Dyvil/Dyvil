package dyvil.tools.compiler.parser.classes;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.ast.classes.AnnotationClass;
import dyvil.tools.compiler.ast.classes.Interface;

public class ClassDeclParser extends Parser
{
	public static final int		NAME			= 0;
	public static final int		SUPERCLASSES	= 1;
	
	protected CompilationUnit	unit;
	private int					mode;
	private AbstractClass		theClassDecl;
	
	public ClassDeclParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		if ("class".equals(value))
		{
			this.mode = NAME;
			this.theClassDecl = new dyvil.tools.compiler.ast.classes.Class();
			return true;
		}
		else if ("interface".equals(value))
		{
			this.mode = NAME;
			this.theClassDecl = new Interface();
			return true;
		}
		else if ("enum".equals(value))
		{
			this.mode = NAME;
			this.theClassDecl = new dyvil.tools.compiler.ast.classes.Enum();
			return true;
		}
		else if ("annotation".equals(value))
		{
			this.mode = NAME;
			this.theClassDecl = new AnnotationClass();
			return true;
		}
		else if ("extends".equals(value))
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

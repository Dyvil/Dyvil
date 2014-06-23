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
	public void parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		switch (value)
		{
		case "class":
			this.mode = NAME;
			this.theClassDecl = new dyvil.tools.compiler.ast.classes.Class();
			return;
		case "interface":
			this.mode = NAME;
			this.theClassDecl = new Interface();
			return;
		case "enum":
			this.mode = NAME;
			this.theClassDecl = new dyvil.tools.compiler.ast.classes.Enum();
			return;
		case "annotation":
			this.mode = NAME;
			this.theClassDecl = new AnnotationClass();
			return;
		case "extends":
			this.mode = SUPERCLASSES;
			return;
		case "{":
			this.theClassDecl.setModifiers(this.modifiers);
			jcp.pushParser(new ClassBodyParser(this.theClassDecl));
			return;
		}
		
		switch (this.mode)
		{
		case NAME:
			this.theClassDecl.setName(value);
		case SUPERCLASSES:
			this.theClassDecl.addSuperClass(value);
		}
	}
}

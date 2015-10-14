package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.imports.IncludeDeclaration;
import dyvil.tools.compiler.ast.imports.PackageDeclaration;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.HeaderDeclaration;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.type.alias.TypeAlias;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.imports.ImportParser;
import dyvil.tools.compiler.parser.imports.IncludeParser;
import dyvil.tools.compiler.parser.imports.PackageParser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.token.IToken;

public class DyvilHeaderParser extends Parser
{
	protected static final int	PACKAGE		= 1;
	protected static final int	IMPORT		= 2;
	protected static final int	METADATA	= 4;
	
	protected IDyvilHeader	unit;
	protected boolean		unitHeader;
	
	protected int				modifiers;
	protected AnnotationList	annotations;
	
	protected IToken lastToken;
	
	public DyvilHeaderParser(IDyvilHeader unit, boolean unitHeader)
	{
		this.unit = unit;
		this.unitHeader = unitHeader;
		this.mode = PACKAGE;
	}
	
	protected boolean parsePackage(IParserManager pm, IToken token, int type)
	{
		if (type == Keywords.PACKAGE)
		{
			PackageDeclaration pack = new PackageDeclaration(token.raw());
			this.unit.setPackageDeclaration(pack);
			pm.pushParser(new PackageParser(pack));
			return true;
		}
		return false;
	}
	
	protected boolean parseImport(IParserManager pm, IToken token, int type)
	{
		switch (type)
		{
		case Keywords.IMPORT:
		{
			ImportDeclaration i = new ImportDeclaration(token.raw());
			pm.pushParser(new ImportParser(im -> {
				i.setImport(im);
				this.unit.addImport(i);
			}));
			return true;
		}
		case Keywords.USING:
		{
			ImportDeclaration i = new ImportDeclaration(token.raw(), true);
			pm.pushParser(new ImportParser(im -> {
				i.setImport(im);
				this.unit.addUsing(i);
			}));
			return true;
		}
		case Keywords.OPERATOR:
			pm.pushParser(new OperatorParser(this.unit, true), true);
			return true;
		case Keywords.PREFIX:
		case Keywords.POSTFIX:
		case Keywords.INFIX:
			pm.pushParser(new OperatorParser(this.unit, false), true);
			return true;
		case Keywords.INCLUDE:
		{
			IncludeDeclaration i = new IncludeDeclaration(token.raw());
			pm.pushParser(new IncludeParser(this.unit, i));
			return true;
		}
		case Keywords.TYPE:
		{
			TypeAlias typeAlias = new TypeAlias();
			pm.pushParser(new TypeAliasParser(this.unit, typeAlias));
			return true;
		}
		}
		return false;
	}
	
	protected boolean parseMetadata(IParserManager pm, IToken token, int type)
	{
		int i;
		if ((i = ModifierTypes.MEMBER.parse(type)) != -1)
		{
			this.modifiers |= i;
			return true;
		}
		if (type == Symbols.AT && token.next().type() != Keywords.INTERFACE)
		{
			this.parseAnnotation(pm, token);
			return true;
		}
		if (type == Keywords.HEADER)
		{
			IToken next = token.next();
			if (ParserUtil.isIdentifier(next.type()))
			{
				pm.skip();
				if (this.unit.getHeaderDeclaration() != null)
				{
					pm.report(token, "Duplicate Header Declaration");
					return true;
				}
				
				Name name = next.nameValue();
				this.unit.setHeaderDeclaration(new HeaderDeclaration(this.unit, next.raw(), name, this.modifiers, this.annotations));
				this.modifiers = 0;
				this.annotations = null;
				this.lastToken = null;
				this.mode = IMPORT;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (type == Symbols.SEMICOLON)
		{
			return;
		}
		switch (this.mode)
		{
		case PACKAGE:
			if (this.parsePackage(pm, token, type))
			{
				this.mode = IMPORT;
				return;
			}
		case IMPORT:
			if (this.parseImport(pm, token, type))
			{
				return;
			}
		case METADATA:
			if (this.mode != METADATA)
			{
				this.lastToken = token;
				this.mode = METADATA;
			}
			if (this.parseMetadata(pm, token, type))
			{
				return;
			}
		}
		
		if (this.unitHeader)
		{
			if (this.lastToken != null)
			{
				pm.jump(this.lastToken);
			}
			pm.popParser();
			pm.stop();
			return;
		}
		
		pm.report(token, "Invalid Header Element - Invalid " + token);
		return;
	}
	
	private void parseAnnotation(IParserManager pm, IToken token)
	{
		if (this.annotations == null)
		{
			this.annotations = new AnnotationList();
		}
		
		Annotation annotation = new Annotation(token.raw());
		this.annotations.addAnnotation(annotation);
		pm.pushParser(pm.newAnnotationParser(annotation));
		return;
	}
}

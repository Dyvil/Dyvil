package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.header.HeaderDeclaration;
import dyvil.tools.compiler.ast.header.ImportDeclaration;
import dyvil.tools.compiler.ast.header.IncludeDeclaration;
import dyvil.tools.compiler.ast.header.PackageDeclaration;
import dyvil.tools.compiler.ast.modifiers.BaseModifiers;
import dyvil.tools.compiler.ast.modifiers.Modifier;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.type.alias.TypeAlias;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.imports.ImportParser;
import dyvil.tools.compiler.parser.imports.IncludeParser;
import dyvil.tools.compiler.parser.imports.PackageParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public class DyvilHeaderParser extends Parser
{
	protected static final int PACKAGE  = 1;
	protected static final int IMPORT   = 2;
	protected static final int METADATA = 4;
	
	protected IDyvilHeader unit;
	protected boolean      unitHeader;
	
	protected ModifierSet    modifiers;
	protected AnnotationList annotations;
	
	protected IToken lastToken;
	
	public DyvilHeaderParser(IDyvilHeader unit, boolean unitHeader)
	{
		this.unit = unit;
		this.unitHeader = unitHeader;
		this.mode = PACKAGE;
	}
	
	protected boolean parsePackage(IParserManager pm, IToken token, int type)
	{
		if (type == DyvilKeywords.PACKAGE)
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
		case DyvilKeywords.IMPORT:
		{
			ImportDeclaration i = new ImportDeclaration(token.raw());
			pm.pushParser(new ImportParser(im -> {
				i.setImport(im);
				this.unit.addImport(i);
			}));
			return true;
		}
		case DyvilKeywords.USING:
		{
			ImportDeclaration i = new ImportDeclaration(token.raw(), true);
			pm.pushParser(new ImportParser(im -> {
				i.setImport(im);
				this.unit.addUsing(i);
			}));
			return true;
		}
		case DyvilKeywords.OPERATOR:
			pm.pushParser(new OperatorParser(this.unit, true), true);
			return true;
		case DyvilKeywords.PREFIX:
		case DyvilKeywords.POSTFIX:
		case DyvilKeywords.INFIX:
			pm.pushParser(new OperatorParser(this.unit, false), true);
			return true;
		case DyvilKeywords.INCLUDE:
		{
			IncludeDeclaration i = new IncludeDeclaration(token.raw());
			pm.pushParser(new IncludeParser(this.unit, i));
			return true;
		}
		case DyvilKeywords.TYPE:
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
		Modifier modifier;
		if ((modifier = BaseModifiers.parseClassModifier(token, pm)) != null)
		{
			if (this.modifiers == null)
			{
				this.modifiers = new ModifierList();
			}

			this.modifiers.addModifier(modifier);
			return true;
		}
		if (type == DyvilSymbols.AT && token.next().type() != DyvilKeywords.INTERFACE)
		{
			this.parseAnnotation(pm, token);
			return true;
		}
		if (type == DyvilKeywords.HEADER)
		{
			IToken next = token.next();
			if (ParserUtil.isIdentifier(next.type()))
			{
				pm.skip();
				if (this.unit.getHeaderDeclaration() != null)
				{
					pm.report(token, "header.declaration.duplicate");
					return true;
				}
				
				Name name = next.nameValue();
				this.unit.setHeaderDeclaration(
						new HeaderDeclaration(this.unit, next.raw(), name, this.modifiers, this.annotations));
				this.modifiers = null;
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
		if (type == BaseSymbols.SEMICOLON)
		{
			return;
		}
		if (type == Tokens.EOF)
		{
			pm.popParser();
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
		
		reportInvalidElement(pm, token);
	}

	protected static void reportInvalidElement(IParserManager pm, IToken token)
	{
		pm.report(MarkerMessages.createSyntaxError(token, "header.element.invalid", token.toString()));
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

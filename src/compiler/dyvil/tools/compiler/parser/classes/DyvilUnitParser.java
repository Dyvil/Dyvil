package dyvil.tools.compiler.parser.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public final class DyvilUnitParser extends DyvilHeaderParser
{
	private static final int CLASS = 4;
	
	public DyvilUnitParser(IDyvilHeader unit, boolean classMode)
	{
		super(unit, true);
		if (classMode)
		{
			this.mode = CLASS;
		}
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (type == BaseSymbols.SEMICOLON)
		{
			return;
		}
		if (type == 0) // EOF
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
		case CLASS:
			if (type == DyvilSymbols.AT && token.next().type() == DyvilKeywords.INTERFACE)
			{
				this.modifiers |= Modifiers.ANNOTATION;
				return;
			}
			int i;
			if ((i = ModifierTypes.CLASS_TYPE.parse(type)) != -1)
			{
				pm.pushParser(new ClassDeclarationParser(this.unit, this.modifiers | i, this.annotations));
				this.modifiers = 0;
				this.annotations = null;
				return;
			}
			if (this.parseMetadata(pm, token, type))
			{
				this.mode = CLASS;
				return;
			}
		}
		pm.report(token, "Invalid Header Element - Invalid " + token);
		return;
	}
}

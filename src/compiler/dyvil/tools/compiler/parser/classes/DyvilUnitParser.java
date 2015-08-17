package dyvil.tools.compiler.parser.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ModifierTypes;

public final class DyvilUnitParser extends DyvilHeaderParser
{
	private static final int CLASS = 4;
	
	public DyvilUnitParser(IDyvilHeader unit, boolean classMode)
	{
		super(unit);
		if (classMode)
		{
			this.mode = CLASS;
		}
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
		case CLASS:
			if (type == Symbols.AT && token.next().type() == Keywords.INTERFACE)
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
		pm.report(new SyntaxError(token, "Invalid Header Element - Invalid " + token));
		return;
	}
}

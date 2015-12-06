package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.parser.IParserManager;
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
			int i;
			if ((i = ModifierUtil.readClassTypeModifier(token, pm)) >= 0)
			{
				if (this.modifiers == null)
				{
					this.modifiers = new ModifierList();
				}

				this.modifiers.addIntModifier(i);
				pm.pushParser(new ClassDeclarationParser(this.unit, this.modifiers, this.annotations));
				this.modifiers = null;
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
	}
}

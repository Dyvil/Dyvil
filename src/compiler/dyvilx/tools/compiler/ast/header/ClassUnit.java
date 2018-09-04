package dyvilx.tools.compiler.ast.header;

import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.consumer.IClassConsumer;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.header.SourceFileParser;
import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.parsing.ParserManager;

import java.io.File;

public class ClassUnit extends SourceHeader implements IClassConsumer
{
	public ClassUnit(DyvilCompiler compiler, Package pack, File input, File output)
	{
		super(compiler, pack, input, output);
	}

	@Override
	public boolean needsHeaderDeclaration()
	{
		for (IClass iclass : this.classes)
		{
			if (iclass.hasModifier(Modifiers.EXTENSION))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void parse()
	{
		new ParserManager(DyvilSymbols.INSTANCE, this.tokens.iterator(), this.markers)
			.parse(new SourceFileParser(this));
		this.tokens = null;
	}

	@Override
	protected boolean printMarkers()
	{
		return ICompilationUnit
			       .printMarkers(this.compiler, this.markers, DyvilFileType.DYVIL_UNIT, this.name, this.fileSource);
	}
}

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
	// =============== Constructors ===============

	public ClassUnit(DyvilCompiler compiler, Package pack, File input, File output)
	{
		super(compiler, pack, input, output);
	}

	// =============== Methods ===============

	// --------------- Header Declaration ---------------

	@Override
	public boolean needsHeaderDeclaration()
	{
		// class units need an implicit header declaration if they contain at least one extension class or a class with
		// a custom bytecode name, otherwise those classes cannot be resolved correctly by the external class resolution
		// algorithms in the Package class.

		for (IClass iclass : this.classes)
		{
			if (iclass.hasModifier(Modifiers.EXTENSION) //
			    || iclass.getInternalName().endsWith(iclass.getName().qualified))
			{
				return true;
			}
		}

		return false;
	}

	// --------------- Phases ---------------

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

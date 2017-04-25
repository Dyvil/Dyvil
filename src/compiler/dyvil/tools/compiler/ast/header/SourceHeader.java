package dyvil.tools.compiler.ast.header;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.ObjectFormat;
import dyvil.tools.compiler.lang.I18n;
import dyvil.tools.compiler.parser.header.DyvilHeaderParser;
import dyvil.tools.compiler.sources.DyvilFileType;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.transform.SemicolonInference;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.source.FileSource;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ParserManager;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.lexer.DyvilLexer;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.io.File;
import java.io.IOException;

public class SourceHeader extends AbstractHeader implements ISourceHeader, IDefaultContext
{
	protected TokenIterator tokens;
	protected MarkerList markers = new MarkerList(Markers.INSTANCE);

	public final FileSource sourceFile;
	public final File       outputDirectory;
	public final File       outputFile;

	protected final DyvilCompiler compiler;

	public SourceHeader(DyvilCompiler compiler, Package pack, File input, File output)
	{
		this.compiler = compiler;

		this.pack = pack;
		this.sourceFile = new FileSource(input);

		String name = input.getAbsolutePath();
		int start = name.lastIndexOf(File.separatorChar);
		int end = name.lastIndexOf('.');
		this.name = Name.fromQualified(name.substring(start + 1, end));

		name = output.getPath();
		start = name.lastIndexOf(File.separatorChar);
		end = name.lastIndexOf('.');
		this.outputDirectory = new File(name.substring(0, start));
		this.outputFile = new File(name.substring(0, end) + DyvilFileType.OBJECT_EXTENSION);
	}

	@Override
	public MarkerList getMarkers()
	{
		return this.markers;
	}

	@Override
	public DyvilCompiler getCompilationContext()
	{
		return this.compiler;
	}

	@Override
	public FileSource getSourceFile()
	{
		return this.sourceFile;
	}

	@Override
	public File getOutputFile()
	{
		return this.outputFile;
	}

	protected boolean load()
	{
		try
		{
			this.sourceFile.load();
			return true;
		}
		catch (IOException ex)
		{
			this.compiler.error(I18n.get("source.error", this.sourceFile), ex);
			return false;
		}
	}

	@Override
	public void tokenize()
	{
		if (this.load())
		{
			this.tokens = new DyvilLexer(this.markers, DyvilSymbols.INSTANCE).tokenize(this.sourceFile.getText());
			SemicolonInference.inferSemicolons(this.tokens.first());
		}
	}

	@Override
	public void parse()
	{
		new ParserManager(DyvilSymbols.INSTANCE, this.tokens, this.markers).parse(new DyvilHeaderParser(this));
	}

	@Override
	public void resolveHeaders()
	{
		for (int i = 0; i < this.importCount; i++)
		{
			this.importDeclarations[i].resolveTypes(this.markers, this);
		}
	}

	@Override
	public void resolveTypes()
	{
		final IContext context = this.getContext();

		for (int i = 0; i < this.typeAliasCount; i++)
		{
			this.typeAliases[i].resolveTypes(this.markers, context);
		}
	}

	@Override
	public void resolve()
	{
		if (this.headerDeclaration == null)
		{
			this.headerDeclaration = new HeaderDeclaration(this, SourcePosition.ORIGIN, this.name,
			                                               new FlagModifierSet(Modifiers.PUBLIC), null);
		}

		this.resolveImports();
	}

	protected void resolveImports()
	{
		for (int i = 0; i < this.importCount; i++)
		{
			this.importDeclarations[i].resolve(this.markers, this);
		}
	}

	@Override
	public void checkTypes()
	{
	}

	@Override
	public void check()
	{
		this.pack.check(this.packageDeclaration, this.markers);

		if (this.headerDeclaration != null)
		{
			this.headerDeclaration.check(this.markers);
		}
	}

	@Override
	public void foldConstants()
	{
	}

	@Override
	public void cleanup()
	{
	}

	protected boolean printMarkers()
	{
		return ICompilationUnit
			       .printMarkers(this.compiler, this.markers, DyvilFileType.DYVIL_HEADER, this.name, this.sourceFile);
	}

	@Override
	public void compile()
	{
		if (this.printMarkers())
		{
			return;
		}

		ObjectFormat.write(this.compiler, this.outputFile, this);
	}
}

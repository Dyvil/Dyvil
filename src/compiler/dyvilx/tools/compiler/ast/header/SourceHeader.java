package dyvilx.tools.compiler.ast.header;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.FileSource;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.backend.ObjectFormat;
import dyvilx.tools.compiler.lang.I18n;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.SemicolonInference;
import dyvilx.tools.compiler.parser.header.SourceFileParser;
import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.ParserManager;
import dyvilx.tools.parsing.TokenList;
import dyvilx.tools.parsing.lexer.DyvilLexer;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.File;
import java.io.IOException;

public class SourceHeader extends AbstractHeader implements ISourceHeader, IDefaultContext
{
	// =============== Fields ===============

	protected TokenList tokens;
	protected MarkerList markers = new MarkerList(Markers.INSTANCE);

	public final FileSource fileSource;
	public final File       outputDirectory;
	public final File       outputFile;

	protected final DyvilCompiler compiler;

	// =============== Constructors ===============

	public SourceHeader(DyvilCompiler compiler, Package pack, File input, File output)
	{
		this.compiler = compiler;

		this.pack = pack;
		this.fileSource = new FileSource(input);

		this.setNameFromFile(input);

		final String name = output.getPath();
		final int start = name.lastIndexOf(File.separatorChar);
		final int end = name.lastIndexOf('.');

		this.outputDirectory = new File(name.substring(0, start));
		this.outputFile = new File(name.substring(0, end) + DyvilFileType.OBJECT_EXTENSION);
	}

	protected void setNameFromFile(File input)
	{
		final String name = input.getAbsolutePath();
		final int start = name.lastIndexOf(File.separatorChar);
		final int end = name.lastIndexOf('.');
		this.name = Name.fromQualified(name.substring(start + 1, end));
	}

	// =============== Methods ===============

	// --------------- Getters and Setters ---------------

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
	public FileSource getFileSource()
	{
		return this.fileSource;
	}

	@Override
	public File getOutputFile()
	{
		return this.outputFile;
	}

	// --------------- Phases ---------------

	protected boolean load()
	{
		try
		{
			this.fileSource.load();
			return true;
		}
		catch (IOException ex)
		{
			this.compiler.error(I18n.get("source.error", this.fileSource.file()), ex);
			return false;
		}
	}

	@Override
	public void tokenize()
	{
		if (this.load())
		{
			this.tokens = new DyvilLexer(this.markers, DyvilSymbols.INSTANCE).tokenize(this.fileSource.text());
			SemicolonInference.inferSemicolons(this.tokens.first());
		}
	}

	@Override
	public void parse()
	{
		new ParserManager(DyvilSymbols.INSTANCE, this.tokens.iterator(), this.markers)
			.parse(new SourceFileParser(this).withFlags(SourceFileParser.NO_CLASSES));
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
			                                               AttributeList.of(Modifiers.PUBLIC));
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
			       .printMarkers(this.compiler, this.markers, DyvilFileType.DYVIL_HEADER, this.name, this.fileSource);
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

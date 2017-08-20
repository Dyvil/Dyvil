package dyvilx.tools.gensrc.ast;

import dyvil.io.FileUtils;
import dyvil.source.FileSource;
import dyvilx.tools.gensrc.GenSrc;
import dyvilx.tools.gensrc.ast.directive.DirectiveList;
import dyvilx.tools.gensrc.ast.scope.TemplateScope;
import dyvilx.tools.gensrc.lang.I18n;
import dyvilx.tools.gensrc.lexer.GenSrcLexer;
import dyvilx.tools.gensrc.lexer.GenSrcSymbols;
import dyvilx.tools.gensrc.parser.BlockParser;
import dyvilx.tools.parsing.ParserManager;
import dyvilx.tools.parsing.TokenList;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Template
{
	private final FileSource sourceFile;
	private final File       targetDirectory;
	private final String     fileName;

	private List<Specialization> specializations = new ArrayList<>();

	private DirectiveList directives;

	public Template(File sourceFile, File targetDir, String fileName)
	{
		this.sourceFile = new FileSource(sourceFile);
		this.targetDirectory = targetDir;
		this.fileName = fileName;
	}

	public FileSource getSourceFile()
	{
		return this.sourceFile;
	}

	public String getFileName()
	{
		return this.fileName;
	}

	public void addSpecialization(Specialization spec)
	{
		this.specializations.add(spec);
	}

	private boolean load(MarkerList markers)
	{
		try
		{
			this.sourceFile.load();
		}
		catch (IOException ignored)
		{
			return false;
		}

		this.directives = new DirectiveList();

		final TokenList tokens = new GenSrcLexer(markers).tokenize(this.sourceFile.getText());

		new ParserManager(GenSrcSymbols.INSTANCE, tokens.iterator(), markers).parse(new BlockParser(this.directives));

		return true;
	}

	public void specialize(GenSrc gensrc)
	{
		if (!this.targetDirectory.exists() && !this.targetDirectory.mkdirs())
		{
			gensrc.getOutput().println(I18n.get("template.directory.error", this.targetDirectory));
			return;
		}

		final MarkerList markers = new MarkerList(I18n.INSTANCE);
		if (!this.load(markers))
		{
			gensrc.getOutput().println(I18n.get("template.file.error", this.sourceFile.getInputFile()));
			return;
		}

		if (markers.getErrors() > 0)
		{
			this.printMarkers(gensrc, markers, null);
			return;
		}

		final int count = this.specializeAll(gensrc);

		gensrc.getOutput().println(I18n.get("template.specialized", count, this.sourceFile.getInputFile()));
	}

	private void printMarkers(GenSrc gensrc, MarkerList markers, Specialization spec)
	{
		final StringBuilder builder = new StringBuilder();

		if (spec == null)
		{
			builder.append(I18n.get("template.problems", this.sourceFile.getInputFile()));
		}
		else
		{
			builder.append(I18n.get("template.problems.for_spec", this.sourceFile.getInputFile(), spec.getFileName()));
		}

		builder.append('\n').append('\n');

		final boolean colors = gensrc.useAnsiColors();
		markers.log(this.sourceFile, builder, colors);
		gensrc.getOutput().println(builder);
	}

	private int specializeAll(GenSrc gensrc)
	{
		if (this.specializations.isEmpty())
		{
			this.specialize(gensrc, Specialization.createDefault(this.fileName));
			return 1;
		}

		int count = 0;
		for (Specialization spec : this.specializations)
		{
			if (spec.isEnabled())
			{
				this.specialize(gensrc, spec);
				count++;
			}
		}
		return count;
	}

	private void specialize(GenSrc gensrc, Specialization spec)
	{
		final String fileName = spec.getFileName();
		if (fileName == null)
		{
			return;
		}

		final int estSize = this.sourceFile.getText().length() * 2;

		final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream(estSize);
		final PrintStream writer = new PrintStream(byteArrayOut);

		final File inputFile = this.sourceFile.getInputFile();
		final TemplateScope scope = new TemplateScope(inputFile, spec);

		// Specialize and check for errors
		final MarkerList markers = new MarkerList(I18n.INSTANCE);
		this.directives.specialize(gensrc, scope, markers, writer);

		if (!markers.isEmpty())
		{
			this.printMarkers(gensrc, markers, spec);
			return;
		}

		final File outputFile = new File(this.targetDirectory, fileName);

		try
		{
			FileUtils.write(outputFile, byteArrayOut.toByteArray());
		}
		catch (IOException ignored)
		{
			gensrc.error(I18n.get("template.specialize.error", outputFile));
		}
	}
}

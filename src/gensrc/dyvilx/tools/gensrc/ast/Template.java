package dyvilx.tools.gensrc.ast;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.ICompilable;
import dyvilx.tools.compiler.ast.header.ICompilationUnit;
import dyvilx.tools.compiler.ast.header.SourceHeader;
import dyvilx.tools.compiler.ast.imports.ImportDeclaration;
import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.sources.FileType;
import dyvilx.tools.gensrc.ast.header.TemplateDirective;
import dyvilx.tools.gensrc.lang.I18n;
import dyvilx.tools.gensrc.lexer.GenSrcLexer;
import dyvilx.tools.gensrc.parser.BlockParser;
import dyvilx.tools.parsing.ParserManager;

import java.io.File;

public class Template extends SourceHeader
{
	public static final FileType TEMPLATE = new FileType()
	{
		@Override
		public String getLocalizedName()
		{
			return I18n.get("unit.filetype.template");
		}

		@Override
		public ICompilationUnit createUnit(DyvilCompiler compiler, Package pack, File input, File output)
		{
			return new Template(compiler, pack, input, output);
		}
	};

	private List<TemplateDirective> templateDirectives;
	private IValue directives;

	// Metadata
	private List<ICompilable> compilables;

	public Template(DyvilCompiler compiler, Package pack, File input, File output)
	{
		super(compiler, pack, input, output);
	}

	public void addTemplateDirective(TemplateDirective directive)
	{
		this.templateDirectives.add(directive);
	}

	// Accessors

	@Override
	public void addCompilable(ICompilable compilable)
	{
		if (this.compilables == null)
		{
			this.compilables = new ArrayList<>();
		}
		this.compilables.add(compilable);
	}

	@Override
	public int compilableCount()
	{
		return this.compilables == null ? 0 : this.compilables.size();
	}

	// Phases

	@Override
	public void tokenize()
	{
		if (this.load())
		{
			this.tokens = new GenSrcLexer(this.markers).tokenize(this.sourceFile.getText());
		}
	}

	@Override
	public void parse()
	{
		final StatementList directives = new StatementList();
		this.directives = directives;
		this.templateDirectives = new ArrayList<>(1);
		new ParserManager(DyvilSymbols.INSTANCE, this.tokens.iterator(), this.markers)
			.parse(new BlockParser(this, directives));
	}

	@Override
	public void resolveTypes()
	{
		super.resolveTypes();
		final IContext context = this.getContext();
		this.directives.resolveTypes(this.markers, context);
	}

	@Override
	public void resolve()
	{
		super.resolve();
		final IContext context = this.getContext();
		this.directives = this.directives.resolve(this.markers, context);
	}

	@Override
	public void checkTypes()
	{
		super.checkTypes();
		final IContext context = this.getContext();
		this.directives.checkTypes(this.markers, context);
	}

	@Override
	public void check()
	{
		super.check();
		final IContext context = this.getContext();
		this.directives.check(this.markers, context);
	}

	@Override
	public void foldConstants()
	{
		super.foldConstants();
		this.directives = this.directives.foldConstants();
	}

	@Override
	public void cleanup()
	{
		super.cleanup();
		this.directives.cleanup(this, null);
	}

	@Override
	protected boolean printMarkers()
	{
		return ICompilationUnit.printMarkers(this.compiler, this.markers, TEMPLATE, this.name, this.sourceFile);
	}

	@Override
	public void compile()
	{
		if (this.printMarkers())
		{
			return;
		}

		// TODO
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		super.toString(indent, buffer);

		if (this.importCount > 0)
		{
			for (int i = 0; i < this.importCount; i++)
			{
				buffer.append(indent);
				appendImport(indent, buffer, this.importDeclarations[i]);
				buffer.append('\n');
			}
			if (Formatting.getBoolean("import.newline"))
			{
				buffer.append('\n');
			}
		}

		if (!this.templateDirectives.isEmpty())
		{
			for (TemplateDirective template : this.templateDirectives)
			{
				buffer.append(indent);
				template.toString(indent, buffer);
				buffer.append('\n');
			}
			buffer.append('\n');
		}

		// FIXME the entire body is placed in a block
		buffer.append('#');
		this.directives.toString(indent, buffer);
	}

	public static void appendImport(@NonNull String indent, @NonNull StringBuilder buffer,
		                               ImportDeclaration importDeclaration)
	{
		buffer.append('#');
		final int position = buffer.length() + "import".length();
		importDeclaration.toString(indent, buffer);
		buffer.setCharAt(position, '('); // insert open paren in place of the space after import
		buffer.append(')');
	}
}

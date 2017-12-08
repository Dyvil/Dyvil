package dyvilx.tools.gensrc.ast;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.CodeClass;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.ConstructorCall;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.expression.access.MethodCall;
import dyvilx.tools.compiler.ast.expression.constant.StringValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.header.ClassUnit;
import dyvilx.tools.compiler.ast.header.ICompilationUnit;
import dyvilx.tools.compiler.ast.header.PackageDeclaration;
import dyvilx.tools.compiler.ast.imports.ImportDeclaration;
import dyvilx.tools.compiler.ast.method.CodeMethod;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.CodeParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.ArrayType;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.gensrc.lang.I18n;
import dyvilx.tools.gensrc.lexer.GenSrcLexer;
import dyvilx.tools.gensrc.parser.BlockParser;
import dyvilx.tools.gensrc.sources.GenSrcFileType;
import dyvilx.tools.parsing.ParserManager;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.File;

public class Template extends ClassUnit
{
	public static class LazyTypes
	{
		public static final Package dyvilxToolsGensrc = Package.rootPackage
			                                                .resolveInternalPackage("dyvilx/tools/gensrc");

		public static final IType Writer       = Package.javaIO.resolveClass("Writer").getClassType();
		public static final IType StringWriter = Package.javaIO.resolveClass("StringWriter").getClassType();
		public static final IType IOException  = Package.javaIO.resolveClass("IOException").getClassType();

		public static final IType Specialization = dyvilxToolsGensrc.resolveClass("Specialization").getClassType();
		public static final IType Template       = dyvilxToolsGensrc.resolveClass("Template").getClassType();

		public static final IClass Builtins_CLASS = dyvilxToolsGensrc.resolveClass("Builtins");
	}

	private IClass templateClass;

	private IMethod genMethod;
	private IMethod mainMethod;

	public Template(DyvilCompiler compiler, Package pack, File input, File output)
	{
		super(compiler, pack, input, output);
		this.markers = new MarkerList(I18n.SYNTAX);
	}

	@Override
	protected void setNameFromFile(File input)
	{
		final String path = input.getAbsolutePath();
		final int startIndex = path.lastIndexOf('/');
		final int endIndex = path.lastIndexOf('.');

		final String name = path.substring(startIndex + 1, endIndex).replace(".", "_");

		this.name = Name.fromRaw(name);
	}

	public String getTemplateName()
	{
		return this.getPackage().getInternalName() + this.fileSource.file().getName();
	}

	// Resolution

	@Override
	public IDataMember resolveField(Name name)
	{
		final IDataMember superField = super.resolveField(name);
		if (superField != null)
		{
			return superField;
		}
		return LazyTypes.Builtins_CLASS.resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		super.getMethodMatches(list, receiver, name, arguments);
		if (list.hasCandidate())
		{
			return;
		}

		LazyTypes.Builtins_CLASS.getMethodMatches(list, receiver, name, arguments);
	}

	// Phases

	@Override
	public void tokenize()
	{
		if (this.load())
		{
			this.tokens = new GenSrcLexer(this.markers).tokenize(this.fileSource.text());
		}
	}

	@Override
	public void parse()
	{
		// class NAME { }

		final CodeClass theClass = new CodeClass(null, this.name, AttributeList.of(Modifiers.PUBLIC));
		final ClassBody classBody = new ClassBody(theClass);
		theClass.setBody(classBody);

		this.addClass(theClass);
		this.templateClass = theClass;

		// func generate(spec, writer) -> void = { ... }

		final CodeMethod genMethod = new CodeMethod(theClass, Name.fromRaw("generate"), Types.VOID,
		                                            AttributeList.of(Modifiers.PUBLIC | Modifiers.OVERRIDE));

		final StatementList directives = new StatementList();
		genMethod.setValue(directives);

		classBody.addMethod(genMethod);
		this.genMethod = genMethod;

		// func main(args) -> void

		final CodeMethod mainMethod = new CodeMethod(theClass, Name.fromRaw("main"), Types.VOID,
		                                             AttributeList.of(Modifiers.PUBLIC | Modifiers.STATIC));
		classBody.addMethod(mainMethod);
		this.mainMethod = mainMethod;

		// Parse

		new ParserManager(DyvilSymbols.INSTANCE, this.tokens.iterator(), this.markers)
			.parse(new BlockParser(this, directives));
	}

	@Override
	public void resolveTypes()
	{
		this.makeGenerateSpecMethod();
		this.makeMainMethod();

		// automatically infer package declaration
		this.packageDeclaration = new PackageDeclaration(null, this.getPackage().getFullName());

		this.templateClass.setSuperType(LazyTypes.Template);
		this.templateClass.setSuperConstructorArguments(new ArgumentList(new StringValue(this.getTemplateName())));

		super.resolveTypes();
	}

	private void makeMainMethod()
	{
		// func main(args: [String]) -> void = new TemplateName().mainImpl(args)

		final ParameterList params = this.mainMethod.getParameters();
		final CodeParameter argsParam = new CodeParameter(this.mainMethod, null, Name.fromRaw("args"),
		                                                  new ArrayType(Types.STRING));
		params.add(argsParam);

		final IValue newTemplate = new ConstructorCall(null, this.templateClass.getClassType(), ArgumentList.EMPTY);
		this.mainMethod.setValue(
			new MethodCall(null, newTemplate, Name.fromRaw("mainImpl"), new ArgumentList(new FieldAccess(argsParam))));
	}

	private void makeGenerateSpecMethod()
	{
		// func generate(spec: Specialization, writer: java.io.Writer) throws IOException -> void = { ... }

		final ParameterList params = this.genMethod.getParameters();
		final CodeParameter specParam = new CodeParameter(this.genMethod, null, Name.fromRaw("spec"),
		                                                  Template.LazyTypes.Specialization);

		final CodeParameter writerParam = new CodeParameter(this.genMethod, null, Name.fromRaw("writer"),
		                                                    Template.LazyTypes.Writer);

		params.add(specParam);
		params.add(writerParam);

		this.genMethod.getExceptions().add(Template.LazyTypes.IOException);
	}

	@Override
	protected boolean printMarkers()
	{
		return ICompilationUnit
			       .printMarkers(this.compiler, this.markers, GenSrcFileType.TEMPLATE, this.name, this.fileSource);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		for (int i = 0; i < this.importCount; i++)
		{
			buffer.append(indent);
			appendImport(indent, buffer, this.importDeclarations[i]);
			buffer.append('\n');
		}

		final IValue directives = this.genMethod.getValue();
		if (!(directives instanceof StatementList))
		{
			directives.toString(indent, buffer);
			return;
		}

		final StatementList statements = (StatementList) directives;
		for (int i = 0, count = statements.size(); i < count; i++)
		{
			statements.get(i).toString(indent, buffer);
			buffer.append('\n');
		}
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

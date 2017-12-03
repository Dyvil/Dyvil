package dyvilx.tools.gensrc.ast;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.CodeClass;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.header.ClassUnit;
import dyvilx.tools.compiler.ast.header.ICompilationUnit;
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
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.gensrc.lexer.GenSrcLexer;
import dyvilx.tools.gensrc.parser.BlockParser;
import dyvilx.tools.gensrc.sources.GenSrcFileType;
import dyvilx.tools.parsing.ParserManager;

import java.io.File;

public class Template extends ClassUnit
{
	public static class LazyTypes
	{
		public static final IType SPECIALIZATION = Package.rootPackage
			                                           .resolveInternalClass("dyvilx/tools/gensrc/Specialization")
			                                           .getClassType();
		public static final IType WRITER         = Package.javaIO.resolveClass("Writer").getClassType();
		public static final IType IO_EXCEPTION   = Package.javaIO.resolveClass("IOException").getClassType();

		public static final IClass BUILTINS_CLASS = Package.rootPackage.resolveInternalClass("dyvilx/tools/gensrc/Builtins");
	}

	private IMethod genMethod;

	public Template(DyvilCompiler compiler, Package pack, File input, File output)
	{
		super(compiler, pack, input, output);
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
		return LazyTypes.BUILTINS_CLASS.resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		super.getMethodMatches(list, receiver, name, arguments);
		if (list.hasCandidate())
		{
			return;
		}

		LazyTypes.BUILTINS_CLASS.getMethodMatches(list, receiver, name, arguments);
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

		// func generate(spec: Specialization, writer: java.io.Writer) -> void

		final CodeMethod genMethod = new CodeMethod(theClass, Name.fromRaw("generate"), Types.VOID,
		                                            AttributeList.of(Modifiers.PUBLIC | Modifiers.STATIC));
		final CodeParameter specParam = new CodeParameter(genMethod, null, Name.fromRaw("spec"), Types.UNKNOWN);

		final CodeParameter writerParam = new CodeParameter(genMethod, null, Name.fromRaw("writer"), Types.UNKNOWN);

		genMethod.getParameters().add(specParam);
		genMethod.getParameters().add(writerParam);

		final StatementList directives = new StatementList();

		genMethod.setValue(directives);

		// func generate(in: File, out: File) -> void

		// Assign the new AST nodes

		this.addClass(theClass);
		this.genMethod = genMethod;

		// Parse

		new ParserManager(DyvilSymbols.INSTANCE, this.tokens.iterator(), this.markers)
			.parse(new BlockParser(this, directives));
	}

	@Override
	public void resolveTypes()
	{
		final ParameterList params = this.genMethod.getParameters();
		params.get(0).setType(LazyTypes.SPECIALIZATION);
		params.get(1).setType(LazyTypes.WRITER);

		this.genMethod.getExceptions().add(LazyTypes.IO_EXCEPTION);

		super.resolveTypes();
	}

	@Override
	protected boolean printMarkers()
	{
		return ICompilationUnit.printMarkers(this.compiler, this.markers, GenSrcFileType.TEMPLATE, this.name, this.fileSource);
	}

	public static void writeGetSpec(MethodWriter writer) throws BytecodeException
	{
		writer.visitVarInsn(Opcodes.ALOAD, 0);
	}

	public static void writeGetWriter(MethodWriter writer) throws BytecodeException
	{
		writer.visitVarInsn(Opcodes.ALOAD, 1);
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

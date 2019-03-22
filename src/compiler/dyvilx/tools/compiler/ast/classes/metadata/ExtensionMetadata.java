package dyvilx.tools.compiler.ast.classes.metadata;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.ITypeParametric;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.ArrayType;
import dyvilx.tools.compiler.ast.type.typevar.TypeVarType;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class ExtensionMetadata implements IClassMetadata
{
	private final IClass theClass;

	public ExtensionMetadata(IClass theClass)
	{
		this.theClass = theClass;
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.CLASS;
	}

	// Phases

	@Override
	public void resolveTypesBeforeBody(MarkerList markers, IContext context)
	{
		final IType superType = this.theClass.getSuperType();
		this.theClass.setPosition(superType.getPosition());
		this.theClass.setName(mangleName(superType));

		for (IMethod method : this.theClass.getBody().methods())
		{
			method.getAttributes().addFlag(Modifiers.EXTENSION);

			// make array extension methods final by default
			if (superType.canExtract(ArrayType.class))
			{
				method.getAttributes().addFlag(Modifiers.FINAL);
			}

			if (!method.isStatic())
			{
				ITypeParametric.prependTypeParameters(this.theClass, method);

				method.setThisType(replaceTypeVars(superType, this.theClass, method));
			}
			else
			{
				// TODO use raw type?
				method.setThisType(superType);
			}
		}

		// immediately remove these to avoid further warnings or errors
		this.theClass.setSuperType(Types.OBJECT);
		this.theClass.setTypeParameters(null);
	}

	private static IType replaceTypeVars(IType type, ITypeParametric origin, ITypeParametric target)
	{
		return type.getConcreteType(typeParameter -> {
			assert typeParameter.getGeneric() == origin;
			final ITypeParameter methodTypeParameter = target.getTypeParameters().get(typeParameter.getIndex());
			return new TypeVarType(methodTypeParameter);
		});
	}

	private static Name mangleName(IType type)
	{
		final String signature = type.getSignature();
		final StringBuilder mangled = new StringBuilder("extension_");

		for (int i = 0; i < signature.length(); i++)
		{
			final char c = signature.charAt(i);
			switch (c)
			{
			case '<':
				mangled.append("_$__");
				break;
			case '>':
				mangled.append("__$_");
				break;
			case '[':
				mangled.append("_$_");
				break;
			case ',':
			case ';':
				mangled.append("__");
				break;
			case '/':
				mangled.append("_");
				break;
			default:
				mangled.append(c);
				break;
			}
		}

		return Name.fromQualified(mangled.toString());
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		final ClassBody body = this.theClass.getBody();

		for (IField field : body.fields())
		{
			if (!field.hasModifier(Modifiers.PRIVATE | Modifiers.CONST))
			{
				markers.add(Markers.semanticError(field.getPosition(), "extension.field.invalid"));
			}
		}

		for (IProperty property : body.properties())
		{
			markers.add(Markers.semanticError(property.getPosition(), "extension.property.invalid"));
		}
	}

	// Compilation

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{

	}
}

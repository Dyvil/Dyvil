package dyvilx.tools.compiler.ast.classes.metadata;

import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.CodeConstructor;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.ThisExpr;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.expression.access.FieldAssignment;
import dyvilx.tools.compiler.ast.expression.access.InitializerCall;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.ICallableMember;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public class ClassMetadata implements IClassMetadata
{
	protected static final int CONSTRUCTOR = 1;
	protected static final int APPLY       = 1 << 1;
	protected static final int EQUALS      = 1 << 2;
	protected static final int HASHCODE    = 1 << 3;
	protected static final int TOSTRING    = 1 << 4;
	protected static final int UNAPPLY     = 1 << 5;
	protected static final int UNAPPLY_ANY = 1 << 6;

	protected static final int READ_RESOLVE  = 1 << 7;
	protected static final int WRITE_REPLACE = 1 << 8;
	// protected static final int WRITE_OBJECT  = 1 << 9;
	// protected static final int READ_OBJECT   = 1 << 10;

	protected static final int INSTANCE_FIELD = 1 << 16;

	protected final IClass theClass;

	protected int members;

	protected IConstructor constructor;

	public ClassMetadata(IClass iclass)
	{
		this.theClass = iclass;
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.CLASS;
	}

	protected boolean hasDefaultConstructor()
	{
		return this.constructor != null && (this.members & CONSTRUCTOR) == 0;
	}

	private void checkMembers(ClassBody body)
	{
		for (int i = 0, count = body.methodCount(); i < count; i++)
		{
			this.checkMethod(body.getMethod(i));
		}

		for (int i = 0, count = body.propertyCount(); i < count; i++)
		{
			final IMethod getter = body.getProperty(i).getGetter();
			if (getter != null)
			{
				this.checkMethod(getter);
			}

			// only check the getter
		}

		for (int i = 0, count = body.fieldCount(); i < count; i++)
		{
			final IField field = body.getField(i);
			this.checkField(field);

			final IProperty property = field.getProperty();
			final IMethod getter;
			if (property != null && (getter = property.getGetter()) != null)
			{
				this.checkMethod(getter);
			}

			// only check the getter
		}
	}

	private void checkField(IField field)
	{
		if ("instance".equals(field.getInternalName()))
		{
			this.members |= INSTANCE_FIELD;
		}
	}

	private void checkMethod(IMethod method)
	{
		final ParameterList parameters = method.getParameters();
		switch (method.getName().unqualified)
		{
		case "equals":
			if (parameters.size() == 1 && parameters.get(0).getType().getTheClass() == Types.OBJECT_CLASS)
			{
				this.members |= EQUALS;
			}
			return;
		case "hashCode":
			if (parameters.isEmpty())
			{
				this.members |= HASHCODE;
			}
			return;
		case "toString":
			if (parameters.isEmpty())
			{
				this.members |= TOSTRING;
			}
			return;
		case "apply":
			if (parameters.matches(this.theClass.getParameters()))
			{
				this.members |= APPLY;
			}
			return;
		case "unapply":
			if (parameters.size() == 1)
			{
				final IClass paramClass = parameters.get(0).getType().getTheClass();
				if (paramClass == Types.OBJECT_CLASS)
				{
					this.members |= UNAPPLY_ANY;
				}
				else if (paramClass == this.theClass)
				{
					this.members |= UNAPPLY;
				}
			}
			return;
		case "readResolve":
			if (parameters.isEmpty())
			{
				this.members |= READ_RESOLVE;
			}
			return;
		case "writeReplace":
			if (parameters.isEmpty())
			{
				this.members |= WRITE_REPLACE;
			}
		}
	}

	@Override
	public void resolveTypesBody(MarkerList markers, IContext context)
	{
		// Check if a constructor needs to be generated

		final ClassBody body = this.theClass.getBody();
		if (body == null)
		{
			return;
		}

		this.checkMembers(body);

		if (body.constructorCount() <= 0)
		{
			return;
		}

		final ParameterList parameters = this.theClass.getParameters();
		final IConstructor constructor = body.getConstructor(parameters);
		if (constructor != null)
		{
			this.constructor = constructor;
			this.members |= CONSTRUCTOR;
			return;
		}

		if (parameters.isEmpty())
		{
			// Do not generate an empty default constructor if there is any other constructor defined
			this.members |= CONSTRUCTOR;
		}
	}

	@Override
	public void resolveTypesGenerate(MarkerList markers, IContext context)
	{
		if ((this.members & CONSTRUCTOR) == 0)
		{
			// Generate the constructor signature
			final CodeConstructor constructor = this.createDefaultConstructor();

			// resolves attributes too
			constructor.resolveTypes(markers, context);

			this.constructor = constructor;
			this.theClass.createBody().addConstructor(constructor);
		}
	}

	private CodeConstructor createDefaultConstructor()
	{
		// init(classParams...)

		final SourcePosition position = this.theClass.position();
		final AttributeList attributes = this.theClass.getConstructorAttributes();
		attributes.addFlag(Modifiers.GENERATED);
		final CodeConstructor constructor = new CodeConstructor(this.theClass, attributes);

		constructor.setPosition(position);
		this.copyClassParameters(constructor);

		// : super(superParams...)

		final IType superType = this.theClass.getSuperType();
		if (superType != null)
		{
			// Generate the constructor body
			ArgumentList arguments = this.theClass.getSuperConstructorArguments();
			if (arguments == null)
			{
				arguments = ArgumentList.empty();
			}

			final InitializerCall init = new InitializerCall(this.theClass.getPosition(), true, arguments, superType);
			constructor.setInitializer(init);
		}

		// { this.classParams... = classParams... }

		final ParameterList classParams = this.theClass.getParameters();
		final StatementList ctorBody = new StatementList();
		final ParameterList ctorParams = constructor.getParameters();

		// j is the counter for class parameters, as there may be leading synthetic constructor parameters
		for (int i = 0, j = 0, count = ctorParams.size(); i < count; i++)
		{
			final IParameter ctorParam = ctorParams.get(i);
			if (ctorParam.hasModifier(Modifiers.SYNTHETIC))
			{
				continue;
			}

			final IParameter classParam = classParams.get(j++);
			if (classParam.hasModifier(Modifiers.OVERRIDE))
			{
				continue;
			}

			final IValue receiver = new ThisExpr(this.theClass);
			final FieldAccess access = new FieldAccess(ctorParam);
			final FieldAssignment assignment = new FieldAssignment(position, receiver, classParam, access);
			ctorBody.add(assignment);
		}

		constructor.setValue(ctorBody);

		return constructor;
	}

	protected void copyClassParameters(ICallableMember constructor)
	{
		ParameterList from = this.theClass.getParameters();
		final int parameterCount = from.size();

		final ParameterList ctrParams = constructor.getParameters();
		for (int i = 0; i < parameterCount; i++)
		{
			final IParameter classParameter = from.get(i);

			AttributeList attributes = classParameter.getAttributes().filtered(Modifiers.PARAMETER_MODIFIERS);
			ctrParams.add(constructor.createParameter(classParameter.getPosition(), classParameter.getName(),
			                                          classParameter.getType(), attributes));
		}

		if (ctrParams.isLastVariadic())
		{
			constructor.getAttributes().addFlag(Modifiers.ACC_VARARGS);
		}
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}
}

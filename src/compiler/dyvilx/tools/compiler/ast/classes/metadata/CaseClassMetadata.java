package dyvilx.tools.compiler.ast.classes.metadata;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.CastOperator;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.InstanceOfOperator;
import dyvilx.tools.compiler.ast.expression.TupleExpr;
import dyvilx.tools.compiler.ast.expression.access.ConstructorCall;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.expression.access.MethodCall;
import dyvilx.tools.compiler.ast.expression.constant.NullValue;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.CodeMethod;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.ClassParameter;
import dyvilx.tools.compiler.ast.parameter.CodeParameter;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.statement.IfStatement;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.NullableType;
import dyvilx.tools.compiler.ast.type.compound.TupleType;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.MethodWriterImpl;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.CaseClasses;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.parsing.marker.MarkerList;

public final class CaseClassMetadata extends ClassMetadata
{
	protected IMethod applyMethod;
	protected IMethod unapplyMethod;
	protected IMethod unapplyAnyMethod;

	public CaseClassMetadata(IClass iclass)
	{
		super(iclass);
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.CLASS;
	}

	@Override
	public void resolveTypesPre(MarkerList markers, IContext context)
	{
		super.resolveTypesPre(markers, context);

		for (IParameter param : this.theClass.getParameters())
		{
			final ClassParameter classParameter = (ClassParameter) param;
			if (classParameter.isOverride() || classParameter.getProperty() != null)
			{
				// Ignore override class parameters and class parameters that already have a property
				continue;
			}

			// Create a property with getter
			final IProperty property = classParameter.createProperty();
			property.getAttributes().addFlag(Modifiers.GENERATED);
			property.initGetter();

			if (!classParameter.hasModifier(Modifiers.FINAL))
			{
				// and setter, for non-final class parameters
				property.initSetter();
			}
		}
	}

	@Override
	public void resolveTypesHeader(MarkerList markers, IContext context)
	{
		super.resolveTypesHeader(markers, context);

		if (!this.theClass.isSubClassOf(Types.SERIALIZABLE))
		{
			this.theClass.getInterfaces().add(Types.SERIALIZABLE);
		}
	}

	@Override
	public void resolveTypesGenerate(MarkerList markers, IContext context)
	{
		super.resolveTypesGenerate(markers, context);

		final int modifiers = Modifiers.PUBLIC | Modifiers.STATIC_FINAL | Modifiers.GENERATED;
		if ((this.members & APPLY) == 0)
		{
			// Generate the apply method signature

			final CodeMethod applyMethod = new CodeMethod(this.theClass, Names.apply, this.theClass.getThisType(),
			                                              AttributeList.of(modifiers));
			applyMethod.getTypeParameters().addAll(this.theClass.getTypeParameters());

			this.copyClassParameters(applyMethod);

			this.applyMethod = applyMethod;
		}

		if ((this.members & UNAPPLY) != 0 && (this.members & UNAPPLY_ANY) != 0)
		{
			return;
		}

		final TupleType returnType = new TupleType();
		final TypeList typeArgs = returnType.getArguments();
		for (IParameter param : this.theClass.getParameters())
		{
			typeArgs.add(param.getType());
		}

		if ((this.members & UNAPPLY) == 0)
		{
			// static final func unapply<TypeParams...>(value: This) -> (T...)
			this.unapplyMethod = new CodeMethod(this.theClass, Names.unapply, returnType, AttributeList.of(modifiers));
			this.unapplyMethod.getTypeParameters().addAll(this.theClass.getTypeParameters());
			final CodeParameter parameter = new CodeParameter(this.unapplyMethod, null, Names.value,
			                                                  this.theClass.getThisType());
			this.unapplyMethod.getParameters().add(parameter);
		}

		if ((this.members & UNAPPLY_ANY) == 0)
		{
			// static final func unapply<TypeParams...>(value: any) -> (T...)?
			this.unapplyAnyMethod = new CodeMethod(this.theClass, Names.unapply, NullableType.apply(returnType),
			                                       AttributeList.of(modifiers));
			this.unapplyAnyMethod.getTypeParameters().addAll(this.theClass.getTypeParameters());

			final CodeParameter parameter = new CodeParameter(this.unapplyAnyMethod, null, Names.value,
			                                                  Types.NULLABLE_ANY);
			this.unapplyAnyMethod.getParameters().add(parameter);
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);

		if (this.applyMethod != null)
		{
			final ArgumentList arguments = new ArgumentList();

			for (IParameter param : this.applyMethod.getParameters())
			{
				// no need to check for override class parameters here, since we are dealing with parameters of the
				// apply method

				arguments.add(new FieldAccess(param));
			}

			// = new This(params...)
			this.applyMethod.setValue(new ConstructorCall(this.constructor, arguments));
		}

		final SourcePosition position = this.theClass.getPosition();

		if (this.unapplyMethod != null)
		{
			final IParameter thisParam = this.unapplyMethod.getParameters().get(0);

			final TupleExpr tupleExpr = new TupleExpr(position);
			final ArgumentList arguments = tupleExpr.getValues();

			for (IParameter param : this.theClass.getParameters())
			{
				// value
				final FieldAccess thisAccess = new FieldAccess(position, null, thisParam);
				// value.classParam
				final IValue fieldAccess;
				if (param.isOverride())
				{
					// if the class parameter is marked as 'override', we have to resolve it from a super-class
					// the easiest way to do this is by name
					fieldAccess = new FieldAccess(position, thisAccess, param.getName()).resolve(markers, context);
				}
				else
				{
					fieldAccess = new FieldAccess(position, thisAccess, param);
				}
				arguments.add(fieldAccess);
			}

			// = (value.classParams...)
			this.unapplyMethod.setValue(tupleExpr);
		}

		if (this.unapplyAnyMethod != null)
		{
			final IParameter param = this.unapplyAnyMethod.getParameters().get(0);

			// param is This
			final InstanceOfOperator isOperator = new InstanceOfOperator(new FieldAccess(param),
			                                                             this.theClass.getClassType());
			// param as This
			final CastOperator castOperator = new CastOperator(new FieldAccess(param), this.theClass.getThisType());
			// unapply(param as This)
			final IValue call = new MethodCall(position, null, Names.unapply, new ArgumentList(castOperator))
				                    .resolveCall(markers, context, true);
			// (param is This) ? unapply(param as This) : null
			final IfStatement ifStatement = new IfStatement(isOperator, call, new NullValue());

			this.unapplyAnyMethod.setValue(ifStatement);
		}
	}

	@Override
	public boolean checkImplements(IMethod candidate, ITypeContext typeContext)
	{
		return (this.applyMethod != null && this.applyMethod.overrides(candidate, typeContext)) //
		       | (this.unapplyMethod != null && this.unapplyMethod.overrides(candidate, typeContext)) //
		       | (this.unapplyAnyMethod != null && this.unapplyAnyMethod.overrides(candidate, typeContext));
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		if (name == Names.apply && this.applyMethod != null)
		{
			this.applyMethod.checkMatch(list, receiver, name, arguments);
		}

		if (name == Names.unapply)
		{
			if (this.unapplyMethod != null)
			{
				this.unapplyMethod.checkMatch(list, receiver, name, arguments);
			}

			if (this.unapplyAnyMethod != null)
			{
				this.unapplyAnyMethod.checkMatch(list, receiver, name, arguments);
			}
		}

		super.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		super.write(writer);
		MethodWriter mw;

		if (this.applyMethod != null)
		{
			this.applyMethod.write(writer);
		}

		if (this.unapplyMethod != null)
		{
			this.unapplyMethod.write(writer);
		}

		if (this.unapplyAnyMethod != null)
		{
			this.unapplyAnyMethod.write(writer);
		}

		String internal = this.theClass.getInternalName();
		if ((this.members & EQUALS) == 0)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "equals", "(Ljava/lang/Object;)Z",
			                                                     null, null));
			mw.setThisType(internal);
			mw.visitParameter(1, "obj", Types.OBJECT, 0);
			mw.visitCode();
			CaseClasses.writeEquals(mw, this.theClass);
			mw.visitEnd();
		}

		if ((this.members & HASHCODE) == 0)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "hashCode", "()I", null, null));
			mw.setThisType(internal);
			mw.visitCode();
			CaseClasses.writeHashCode(mw, this.theClass);
			mw.visitEnd();
		}

		if ((this.members & TOSTRING) == 0)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "toString", "()Ljava/lang/String;",
			                                                     null, null));
			mw.setThisType(internal);
			mw.visitCode();
			CaseClasses.writeToString(mw, this.theClass);
			mw.visitEnd();
		}
	}
}

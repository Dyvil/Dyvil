package dyvil.tools.compiler.ast.classes.metadata;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.CodeMethod;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.parameter.ClassParameter;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.CaseClasses;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public final class CaseClassMetadata extends ClassMetadata
{
	protected IMethod applyMethod;

	public CaseClassMetadata(IClass iclass)
	{
		super(iclass);
	}

	@Override
	public void resolveTypesPre(MarkerList markers, IContext context)
	{
		super.resolveTypesPre(markers, context);

		final IParameterList parameters = this.theClass.getParameterList();
		for (int i = 0, count = parameters.size(); i < count; i++)
		{
			final ClassParameter classParameter = (ClassParameter) parameters.get(i);
			final IProperty property = classParameter.createProperty();
			property.initGetter();

			if (!classParameter.hasModifier(Modifiers.FINAL))
			{
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
			this.theClass.addInterface(Types.SERIALIZABLE);
		}
	}

	@Override
	public void resolveTypesGenerate(MarkerList markers, IContext context)
	{
		super.resolveTypesGenerate(markers, context);

		if ((this.members & APPLY) != 0)
		{
			return;
		}

		// Generate the apply method signature

		final CodeMethod applyMethod = new CodeMethod(this.theClass, Names.apply, this.theClass.getThisType(),
		                                              new FlagModifierSet(Modifiers.PUBLIC | Modifiers.STATIC_FINAL));
		applyMethod.setTypeParameters(this.theClass.getTypeParameters(), this.theClass.typeParameterCount());

		if (this.constructor != null && (this.members & CONSTRUCTOR) == 0)
		{
			this.constructor.getParameterList().copyTo(applyMethod.getParameterList());
		}
		else
		{
			this.copyClassParameters(applyMethod);
		}

		applyMethod.resolveTypes(markers, context);
		this.applyMethod = applyMethod;
	}

	@Override
	public boolean checkImplements(IMethod candidate, ITypeContext typeContext)
	{
		return this.applyMethod != null && this.applyMethod.overrides(candidate, typeContext);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		if (name == Names.apply && this.applyMethod != null)
		{
			this.applyMethod.checkMatch(list, receiver, name, arguments);
		}

		super.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		super.write(writer);
		MethodWriter mw;

		if ((this.members & APPLY) == 0)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(this.applyMethod.getModifiers().toFlags(), "apply",
			                                                     this.applyMethod.getDescriptor(),
			                                                     this.applyMethod.getSignature(), null));
			mw.visitCode();
			mw.visitTypeInsn(Opcodes.NEW, this.theClass.getInternalName());
			mw.visitInsn(Opcodes.DUP);

			final IParameterList parameterList = this.theClass.getParameterList();
			for (int i = 0, count = parameterList.size(); i < count; i++)
			{
				IParameter param = parameterList.get(i);
				param.writeInit(mw);
				mw.visitVarInsn(param.getType().getLoadOpcode(), param.getLocalIndex());
			}
			this.constructor.writeInvoke(mw, 0);
			mw.visitInsn(Opcodes.ARETURN);
			mw.visitEnd(this.theClass.getThisType());
		}

		String internal = this.theClass.getInternalName();
		if ((this.members & EQUALS) == 0)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "equals",
			                                                     "(Ljava/lang/Object;)Z", null, null));
			mw.setThisType(internal);
			mw.visitParameter(1, "obj", Types.OBJECT, 0);
			mw.visitCode();
			CaseClasses.writeEquals(mw, this.theClass);
			mw.visitEnd();
		}

		if ((this.members & HASHCODE) == 0)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "hashCode",
			                                                     "()I", null, null));
			mw.setThisType(internal);
			mw.visitCode();
			CaseClasses.writeHashCode(mw, this.theClass);
			mw.visitEnd();
		}

		if ((this.members & TOSTRING) == 0)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC, "toString",
			                                                     "()Ljava/lang/String;", null, null));
			mw.setThisType(internal);
			mw.visitCode();
			CaseClasses.writeToString(mw, this.theClass);
			mw.visitEnd();
		}
	}
}

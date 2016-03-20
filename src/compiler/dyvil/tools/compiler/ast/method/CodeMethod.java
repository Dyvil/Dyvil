package dyvil.tools.compiler.ast.method;

import dyvil.collection.Set;
import dyvil.collection.mutable.HashSet;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.Label;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.intrinsic.Intrinsics;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CodeMethod extends AbstractMethod
{
	public CodeMethod(IClass iclass)
	{
		super(iclass);
	}

	public CodeMethod(IClass iclass, Name name)
	{
		super(iclass, name);
	}

	public CodeMethod(IClass iclass, Name name, IType type)
	{
		super(iclass, name, type);
	}

	public CodeMethod(IClass iclass, Name name, IType type, ModifierSet modifiers)
	{
		super(iclass, name, type, modifiers);
	}

	public CodeMethod(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.resolveTypes(markers, context);

		if (this.receiverType != null)
		{
			this.receiverType = this.receiverType.resolveType(markers, context);

			// Check the self type for compatibility
			final IClass selfTypeClass = this.receiverType.getTheClass();
			if (selfTypeClass != null && selfTypeClass != this.enclosingClass)
			{
				final Marker marker = Markers.semanticError(this.receiverType.getPosition(),
				                                            "method.receivertype.incompatible", this.getName());
				marker.addInfo(Markers.getSemantic("method.receivertype", this.receiverType));
				marker.addInfo(Markers.getSemantic("method.classtype", this.enclosingClass.getFullName()));
				markers.add(marker);
			}
		}
		else if (!this.isStatic())
		{
			this.receiverType = this.enclosingClass.getType();
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].resolveTypes(markers, context);
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(markers, context);
		}

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolveType(markers, context);
		}

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
		else if (this.enclosingClass.hasModifier(Modifiers.INTERFACE_CLASS))
		{
			this.modifiers.addIntModifier(Modifiers.ABSTRACT | Modifiers.PUBLIC);
		}
		else if (this.enclosingClass.hasModifier(Modifiers.ABSTRACT))
		{
			this.modifiers.addIntModifier(Modifiers.ABSTRACT);
		}

		context.pop();
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.resolve(markers, context);

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].resolve(markers, context);
		}

		if (this.receiverType != null)
		{
			this.receiverType.resolve(markers, context);
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolve(markers, context);
		}

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].resolve(markers, context);
		}

		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);

			boolean inferType = false;
			if (this.type == Types.UNKNOWN)
			{
				inferType = true;
				this.type = this.value.getType();
				if (this.type == Types.UNKNOWN)
				{
					markers.add(Markers.semantic(this.position, "method.type.infer", this.name.unqualified));
					this.type = Types.ANY;
				}
			}

			final TypeChecker.MarkerSupplier markerSupplier = TypeChecker.markerSupplier("method.type.incompatible",
			                                                                             "method.type", "value.type",
			                                                                             this.name);
			this.value = TypeChecker.convertValue(this.value, this.type, this.type, markers, context, markerSupplier);

			if (inferType)
			{
				this.type = this.value.getType();
			}
		}
		else if (this.type == Types.UNKNOWN)
		{
			markers.add(Markers.semantic(this.position, "method.type.abstract", this.name.unqualified));
			this.type = Types.ANY;
		}

		context.pop();
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.checkTypes(markers, context);

		if (this.receiverType != null)
		{
			this.receiverType.checkType(markers, context, TypePosition.PARAMETER_TYPE);
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].checkTypes(markers, context);
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].checkTypes(markers, context);
		}

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].checkType(markers, context, TypePosition.RETURN_TYPE);
		}

		if (this.value != null)
		{
			this.value.resolveStatement(this, markers);
			this.value.checkTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.check(markers, context);

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].check(markers, this);
		}

		if (this.receiverType != null)
		{
			this.receiverType.check(markers, context);
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].check(markers, this);
		}

		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType exceptionType = this.exceptions[i];
			exceptionType.check(markers, this);

			if (!Types.isSuperType(Types.THROWABLE, exceptionType))
			{
				Marker marker = Markers.semantic(exceptionType.getPosition(), "method.exception.type");
				marker.addInfo(Markers.getSemantic("exception.type", exceptionType));
				markers.add(marker);
			}
		}

		if (this.value != null)
		{
			this.value.check(markers, this);
		}

		// Check for illegal modifiers
		ModifierUtil.checkModifiers(markers, this, this.modifiers, Modifiers.METHOD_MODIFIERS);

		// Check illegal modifier combinations
		ModifierUtil.checkMethodModifiers(markers, this, this.modifiers.toFlags(), this.value != null);

		if (!this.modifiers.hasIntModifier(Modifiers.STATIC))
		{
			this.checkOverride(markers);
		}

		// Check for duplicate methods
		this.checkDuplicates(markers);

		context.pop();
	}

	private void checkDuplicates(MarkerList markers)
	{
		final IClassBody body = this.enclosingClass.getBody();
		if (body == null)
		{
			return;
		}

		final String desc = this.getDescriptor();
		for (int i = body.methodCount() - 1; i >= 0; i--)
		{
			final IMethod method = body.getMethod(i);
			if (method == this || method.getName() != this.name || method.parameterCount() != this.parameterCount)
			{
				continue;
			}

			if (method.getDescriptor().equals(desc))
			{
				markers.add(Markers.semantic(this.position, "method.duplicate", this.name, desc));
			}
		}
	}

	private void checkOverride(MarkerList markers)
	{
		if (this.overrideMethods == null)
		{
			if (this.modifiers.hasIntModifier(Modifiers.OVERRIDE))
			{
				markers.add(Markers.semantic(this.position, "method.override.notfound", this.name));
			}
			return;
		}

		if (!this.modifiers.hasIntModifier(Modifiers.OVERRIDE))
		{
			markers.add(Markers.semantic(this.position, "method.overrides", this.name));
		}

		final boolean thisTypeResolved = this.type.isResolved();
		final ITypeContext typeContext = this.enclosingClass.getType();

		for (IMethod overrideMethod : this.overrideMethods)
		{
			if (overrideMethod.hasModifier(Modifiers.FINAL))
			{
				markers.add(Markers.semantic(this.position, "method.override.final", this.name));
			}

			if (thisTypeResolved)
			{
				final IType superReturnType = overrideMethod.getType().getConcreteType(typeContext);
				if (superReturnType != this.type && superReturnType.isResolved() // avoid extra error
					    && !Types.isSuperType(superReturnType, this.type))
				{
					overrideMethod.getType().getConcreteType(typeContext);
					Marker marker = Markers.semantic(this.position, "method.override.type.incompatible", this.name);
					marker.addInfo(Markers.getSemantic("method.type", this.type));
					marker.addInfo(Markers.getSemantic("method.override.type", superReturnType));

					marker.addInfo(Markers.getSemantic("method.override", Util.methodSignatureToString(overrideMethod),
					                                   overrideMethod.getEnclosingClass().getFullName()));
					markers.add(marker);
				}
			}
		}
	}

	@Override
	public void foldConstants()
	{
		super.foldConstants();

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].foldConstants();
		}

		if (this.receiverType != null)
		{
			this.receiverType.foldConstants();
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].foldConstants();
		}

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].foldConstants();
		}

		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		context = context.push(this);

		super.cleanup(context, compilableList);

		if (this.annotations != null)
		{
			IAnnotation intrinsic = this.annotations.getAnnotation(Types.INTRINSIC_CLASS);
			if (intrinsic != null)
			{
				this.intrinsicData = Intrinsics.readAnnotation(this, intrinsic);
			}
		}

		if (this.receiverType != null)
		{
			this.receiverType.cleanup(context, compilableList);
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].cleanup(context, compilableList);
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].cleanup(context, compilableList);
		}

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].cleanup(context, compilableList);
		}

		if (this.value != null)
		{
			this.value = this.value.cleanup(context, compilableList);
		}

		context.pop();
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final boolean interfaceClass = this.enclosingClass.isInterface();

		int modifiers = this.modifiers.toFlags();
		if (this.value == null)
		{
			modifiers |= Modifiers.ABSTRACT;
		}
		if (interfaceClass)
		{
			modifiers = modifiers & ~3 | Modifiers.PUBLIC;
		}

		final String internalThisClassName = this.enclosingClass.getInternalName();
		final String[] exceptionTypes = this.getInternalExceptions();
		MethodWriter methodWriter = new MethodWriterImpl(writer, writer.visitMethod(
			modifiers & ModifierUtil.JAVA_MODIFIER_MASK, this.name.qualified, this.getDescriptor(), this.getSignature(),
			exceptionTypes));

		if ((modifiers & Modifiers.STATIC) == 0)
		{
			methodWriter.setThisType(internalThisClassName);
		}

		this.writeAnnotations(methodWriter, modifiers);

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].writeInit(methodWriter);
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].writeParameter(methodWriter);
		}

		final Label start = new Label();
		final Label end = new Label();

		if (this.value != null)
		{
			methodWriter.visitCode();
			methodWriter.visitLabel(start);
			this.value.writeExpression(methodWriter, this.type);
			methodWriter.visitLabel(end);
			methodWriter.visitEnd(this.type);
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].writeLocal(methodWriter, start, end);
		}

		if ((modifiers & Modifiers.STATIC) != 0)
		{
			return;
		}

		methodWriter.visitLocalVariable("this", 'L' + internalThisClassName + ';', null, start, end, 0);

		if (this.overrideMethods == null)
		{
			return;
		}

		final int lineNumber = this.getLineNumber();

		final Set<String> descriptors = new HashSet<>(1 + this.overrideMethods.size());
		descriptors.add(this.descriptor);

		for (IMethod overrideMethod : this.overrideMethods)
		{
			final String desc = overrideMethod.getDescriptor();

			// Check if a bridge method for the descriptor has not yet been
			// generated
			if (descriptors.contains(desc))
			{
				continue;
			}

			descriptors.add(desc);

			// Generate a bridge method
			methodWriter = new MethodWriterImpl(writer, writer.visitMethod(
				Modifiers.PUBLIC | Modifiers.SYNTHETIC | Modifiers.BRIDGE, this.name.qualified, desc, null,
				exceptionTypes));

			methodWriter.visitCode();
			methodWriter.setThisType(internalThisClassName);

			methodWriter.visitVarInsn(Opcodes.ALOAD, 0);

			for (int p = 0; p < this.parameterCount; p++)
			{
				final IParameter overrideParameter = overrideMethod.getParameter(p);
				final IType parameterType = this.parameters[p].getInternalType();
				final IType overrideParameterType = overrideParameter.getInternalType();

				overrideParameter.writeInit(methodWriter);
				methodWriter.visitVarInsn(overrideParameterType.getLoadOpcode(), overrideParameter.getLocalIndex());
				overrideParameterType.writeCast(methodWriter, parameterType, lineNumber);
			}

			IType overrideReturnType = overrideMethod.getType();

			methodWriter.visitLineNumber(lineNumber);
			methodWriter.visitMethodInsn((modifiers & Modifiers.ABSTRACT) != 0 && interfaceClass ?
				                             Opcodes.INVOKEINTERFACE :
				                             Opcodes.INVOKEVIRTUAL, internalThisClassName, this.name.qualified, this.getDescriptor(), interfaceClass);
			this.type.writeCast(methodWriter, overrideReturnType, lineNumber);
			methodWriter.visitInsn(overrideReturnType.getReturnOpcode());
			methodWriter.visitEnd();
		}
	}

	protected void writeAnnotations(MethodWriter writer, int modifiers)
	{
		if (this.annotations != null)
		{
			this.annotations.write(writer);
		}

		if (this.receiverType != null && this.receiverType != this.enclosingClass.getType())
		{
			final String signature = this.receiverType.getSignature();
			if (signature != null)
			{
				AnnotationVisitor annotationVisitor = writer.visitAnnotation(AnnotationUtil.RECEIVER_TYPE, false);
				annotationVisitor.visit("value", signature);
				annotationVisitor.visitEnd();
			}
		}

		ModifierUtil.writeModifiers(writer, this.modifiers);

		if ((modifiers & Modifiers.DEPRECATED) != 0 && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			writer.visitAnnotation(Deprecation.DYVIL_EXTENDED, true);
		}

		// Type Variable Annotations
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].write(writer);
		}

		this.type.writeAnnotations(writer, TypeReference.newTypeReference(TypeReference.METHOD_RETURN), "");
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].writeAnnotations(writer, TypeReference.newExceptionReference(i), "");
		}
	}

	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
		out.writeUTF(this.name.qualified);
		IType.writeType(this.type, out);

		out.writeByte(this.parameterCount);
		for (int i = 0; i < this.parameterCount; i++)
		{
			IType.writeType(this.parameters[i].getType(), out);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		this.writeAnnotations(out);

		out.writeUTF(this.name.qualified);
		IType.writeType(this.type, out);

		out.writeByte(this.parameterCount);
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].write(out);
		}
	}

	@Override
	public void readSignature(DataInput in) throws IOException
	{
		this.type = IType.readType(in);

		int parameterCount = in.readByte();
		if (this.parameterCount != 0)
		{

			for (int i = 0; i < parameterCount; i++)
			{
				this.parameters[i].setType(IType.readType(in));
			}
			this.parameterCount = parameterCount;
			return;
		}

		this.parameters = new IParameter[parameterCount];
		for (int i = 0; i < parameterCount; i++)
		{
			this.parameters[i] = new MethodParameter(Name.getQualified("par" + i), IType.readType(in));
		}
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.readAnnotations(in);

		this.name = Name.get(in.readUTF());
		this.type = IType.readType(in);

		int parameterCount = in.readByte();
		this.parameters = new IParameter[parameterCount];
		for (int i = 0; i < parameterCount; i++)
		{
			MethodParameter param = new MethodParameter();
			param.read(in);
			this.parameters[i] = param;
		}
	}
}

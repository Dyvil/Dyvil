package dyvil.tools.compiler.ast.method;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.intrinsic.Intrinsics;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.util.AnnotationUtils;
import dyvil.tools.compiler.util.MarkerMessages;
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
	protected IMethod[] overrideMethods;
	
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
	
	public CodeMethod(ICodePosition position, IClass iclass, Name name, IType type, ModifierSet modifiers)
	{
		super(position, iclass, name, type, modifiers);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, this);

		if (this.selfType == null)
		{
			this.selfType = this.theClass.getType();
		}
		else
		{
			this.selfType = this.selfType.resolveType(markers, context);
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].resolveTypes(markers, this);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(markers, this);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolveType(markers, this);
		}
		
		if (this.value != null)
		{
			this.value.resolveTypes(markers, this);
		}
		else if (this.theClass.hasModifier(Modifiers.INTERFACE_CLASS))
		{
			this.modifiers.addIntModifier(Modifiers.ABSTRACT | Modifiers.PUBLIC);
		}
		else if (this.theClass.hasModifier(Modifiers.ABSTRACT))
		{
			this.modifiers.addIntModifier(Modifiers.ABSTRACT);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, this);
		
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].resolve(markers, this);
		}

		if (this.selfType != null)
		{
			this.selfType.resolve(markers, context);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolve(markers, this);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].resolve(markers, this);
		}
		
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, this);
			
			boolean inferType = false;
			if (this.type == Types.UNKNOWN)
			{
				inferType = true;
				this.type = this.value.getType();
				if (this.type == Types.UNKNOWN)
				{
					markers.add(MarkerMessages.createMarker(this.position, "method.type.infer", this.name.unqualified));
					this.type = Types.ANY;
				}
			}
			
			IValue value1 = this.type.convertValue(this.value, this.type, markers, this);
			if (value1 == null)
			{
				Marker marker = MarkerMessages
						.createMarker(this.position, "method.type.incompatible", this.name.unqualified);
				marker.addInfo(MarkerMessages.getMarker("method.type", this.type));
				marker.addInfo(MarkerMessages.getMarker("value.type", this.value.getType()));
				markers.add(marker);
			}
			else
			{
				this.value = value1;
				if (inferType)
				{
					this.type = value1.getType();
				}
			}
			return;
		}
		if (this.type == Types.UNKNOWN)
		{
			markers.add(MarkerMessages.createMarker(this.position, "method.type.abstract", this.name.unqualified));
			this.type = Types.ANY;
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, this);

		if (this.selfType != null)
		{
			this.selfType.checkType(markers, context, TypePosition.PARAMETER_TYPE);
		}
		
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].checkTypes(markers, this);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].checkTypes(markers, this);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].checkType(markers, this, TypePosition.RETURN_TYPE);
		}
		
		if (this.value != null)
		{
			this.value.resolveStatement(this, markers);
			this.value.checkTypes(markers, this);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, this);

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].check(markers, this);
		}

		if (this.selfType != null)
		{
			this.selfType.check(markers, context);
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].check(markers, this);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType exceptionType = this.exceptions[i];
			exceptionType.check(markers, this);
			
			if (!Types.THROWABLE.isSuperTypeOf(exceptionType))
			{
				Marker marker = MarkerMessages.createMarker(exceptionType.getPosition(), "method.exception.type");
				marker.addInfo(MarkerMessages.getMarker("exception.type", exceptionType));
				markers.add(marker);
			}
		}
		
		if (this.value != null)
		{
			this.value.check(markers, this);
		}
		
		// Check for illegal modifiers
		int illegalModifiers = this.modifiers.toFlags() & ~Modifiers.METHOD_MODIFIERS;
		if (illegalModifiers != 0)
		{
			markers.add(MarkerMessages.createError(this.position, "method.illegal_modifiers", this.name,
			                                       ModifierUtil.fieldModifiersToString(illegalModifiers)));
		}
		
		// Check illegal modifier combinations
		ModifierUtil.checkMethodModifiers(markers, this, this.modifiers.toFlags(), this.value != null, "method");
		
		if (!this.modifiers.hasIntModifier(Modifiers.STATIC))
		{
			this.checkOverride(markers);
		}
		
		// Check for duplicate methods
		this.checkDuplicates(markers);
	}
	
	private void checkDuplicates(MarkerList markers)
	{
		String desc = this.getDescriptor();
		IClassBody body = this.theClass.getBody();
		if (body == null)
		{
			return;
		}
		
		for (int i = body.methodCount() - 1; i >= 0; i--)
		{
			IMethod m = body.getMethod(i);
			if (m == this || m.getName() != this.name || m.parameterCount() != this.parameterCount)
			{
				continue;
			}
			
			if (m.getDescriptor().equals(desc))
			{
				markers.add(MarkerMessages.createMarker(this.position, "method.duplicate", this.name, desc));
			}
		}
	}
	
	@Override
	protected void addOverride(IMethod override)
	{
		if (this.overrideMethods == null)
		{
			this.overrideMethods = new IMethod[] { override };
			return;
		}
		
		IMethod[] overrideMethods = new IMethod[this.overrideMethods.length + 1];
		System.arraycopy(this.overrideMethods, 0, overrideMethods, 0, this.overrideMethods.length);
		overrideMethods[this.overrideMethods.length] = override;
		this.overrideMethods = overrideMethods;
	}
	
	private void checkOverride(MarkerList markers)
	{
		if (this.overrideMethods == null)
		{
			if (this.modifiers.hasIntModifier(Modifiers.OVERRIDE))
			{
				markers.add(MarkerMessages.createMarker(this.position, "method.override.notfound", this.name));
			}
			return;
		}
		
		if (!this.modifiers.hasIntModifier(Modifiers.OVERRIDE))
		{
			markers.add(MarkerMessages.createMarker(this.position, "method.overrides", this.name));
		}
		
		for (IMethod overrideMethod : this.overrideMethods)
		{
			if (overrideMethod.hasModifier(Modifiers.FINAL))
			{
				markers.add(MarkerMessages.createMarker(this.position, "method.override.final", this.name));
			}
			
			final IType type = overrideMethod.getType().getConcreteType(this.theClass.getType());
			if (type != this.type && !type.isSuperTypeOf(this.type))
			{
				Marker marker = MarkerMessages
						.createMarker(this.position, "method.override.type.incompatible", this.name);
				marker.addInfo(MarkerMessages.getMarker("method.type", this.type));
				marker.addInfo(MarkerMessages.getMarker("method.override.type", type));

				marker.addInfo(MarkerMessages.getMarker("method.override", Util.methodSignatureToString(overrideMethod),
				                                        overrideMethod.getTheClass().getFullName()));
				markers.add(marker);
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

		if (this.selfType != null)
		{
			this.selfType.foldConstants();
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
		super.cleanup(this, compilableList);
		
		if (this.annotations != null)
		{
			IAnnotation intrinsic = this.annotations.getAnnotation(Types.INTRINSIC_CLASS);
			if (intrinsic != null)
			{
				this.intrinsicData = Intrinsics.readAnnotation(this, intrinsic);
			}
		}

		if (this.selfType != null)
		{
			this.selfType.cleanup(context, compilableList);
		}
		
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].cleanup(this, compilableList);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].cleanup(this, compilableList);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].cleanup(this, compilableList);
		}
		
		if (this.value != null)
		{
			this.value = this.value.cleanup(this, compilableList);
		}
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		int modifiers = this.modifiers.toFlags();
		if (this.value == null)
		{
			modifiers |= Modifiers.ABSTRACT;
		}
		if (this.theClass.isInterface())
		{
			modifiers = modifiers & ~3 | Modifiers.PUBLIC;
		}

		final String internalThisClassName = this.theClass.getInternalName();
		final String[] exceptionTypes = this.getInternalExceptions();
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers & 0xFFFF, this.name.qualified,
		                                                                  this.getDescriptor(), this.getSignature(),
		                                                                  exceptionTypes));

		if ((modifiers & Modifiers.STATIC) == 0)
		{
			mw.setThisType(internalThisClassName);
		}
		
		this.writeAnnotations(mw, modifiers);
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].write(mw);
		}
		
		final Label start = new Label();
		final Label end = new Label();
		
		if (this.value != null)
		{
			mw.begin();
			mw.writeLabel(start);
			this.value.writeExpression(mw, this.type);
			mw.writeLabel(end);
			mw.end(this.type);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			final IParameter parameter = this.parameters[i];
			mw.writeLocal(parameter.getLocalIndex(), parameter.getName().qualified, parameter.getDescription(),
			              parameter.getSignature(), start, end);
		}
		
		if ((modifiers & Modifiers.STATIC) != 0)
		{
			return;
		}
		
		mw.writeLocal(0, "this", 'L' + internalThisClassName + ';', null, start, end);
		
		if (this.overrideMethods == null)
		{
			return;
		}
		
		String[] descriptors = new String[1 + this.overrideMethods.length];
		descriptors[0] = this.descriptor;

		methodLoop:
		for (int i = 0; i < this.overrideMethods.length; i++)
		{
			final IMethod overrideMethod = this.overrideMethods[i];
			final String desc = overrideMethod.getDescriptor();

			// Check if a bridge method for the descriptor has not yet been
			// generated
			for (int j = 0; j <= i; j++)
			{
				if (desc.equals(descriptors[j]))
				{
					continue methodLoop;
				}
			}
			descriptors[i + 1] = desc;

			// Generate a bridge method
			mw = new MethodWriterImpl(writer,
			                          writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC | Modifiers.BRIDGE,
			                                             this.name.qualified, desc, null, exceptionTypes));
			
			mw.begin();
			mw.setThisType(internalThisClassName);

			mw.writeVarInsn(Opcodes.ALOAD, 0);
			
			final int lineNumber = this.getLineNumber();
			
			for (int p = 0; p < this.parameterCount; p++)
			{
				final IParameter overrideParameter = overrideMethod.getParameter(p);
				final IType parameterType = this.parameters[p].getInternalType();
				final IType overrideParameterType = overrideParameter.getInternalType();
				
				overrideParameter.write(mw);
				mw.writeVarInsn(overrideParameterType.getLoadOpcode(), overrideParameter.getLocalIndex());
				overrideParameterType.writeCast(mw, parameterType, lineNumber);
			}
			
			IType overrideReturnType = overrideMethod.getType();
			
			mw.writeLineNumber(lineNumber);
			boolean itf = this.theClass.isInterface();
			mw.writeInvokeInsn(
					(modifiers & Modifiers.ABSTRACT) != 0 && itf ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
					internalThisClassName, this.name.qualified, this.getDescriptor(), itf);
			this.type.writeCast(mw, overrideReturnType, lineNumber);
			mw.writeInsn(overrideReturnType.getReturnOpcode());
			mw.end();
		}
	}
	
	protected void writeAnnotations(MethodWriter mw, int modifiers)
	{
		if (this.annotations != null)
		{
			int count = this.annotations.annotationCount();
			for (int i = 0; i < count; i++)
			{
				this.annotations.getAnnotation(i).write(mw);
			}
		}

		AnnotationUtils.writeModifiers(mw, this.modifiers);

		if ((modifiers & Modifiers.DEPRECATED) != 0 && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			mw.visitAnnotation(Deprecation.DYVIL_EXTENDED, true);
		}

		this.type.writeAnnotations(mw, TypeReference.newTypeReference(TypeReference.METHOD_RETURN), "");
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].writeAnnotations(mw, TypeReference.newExceptionReference(i), "");
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

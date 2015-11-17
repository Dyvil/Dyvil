package dyvil.tools.compiler.ast.method;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

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
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

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
	
	public CodeMethod(IClass iclass, Name name, IType type, int modifiers)
	{
		super(iclass, name, type, modifiers);
	}
	
	public CodeMethod(ICodePosition position, IClass iclass, Name name, IType type, int modifiers)
	{
		super(position, iclass, name, type, modifiers);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, this);
		
		if ((this.modifiers & Modifiers.PREFIX) != 0)
		{
			// Static & Prefix will cause errors but does happen, so remove the
			// prefix modifier
			this.modifiers &= ~Modifiers.PREFIX;
		}
		
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].resolveTypes(markers, this);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			param.resolveTypes(markers, this);
			param.setIndex(i);
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
			this.modifiers |= Modifiers.ABSTRACT | Modifiers.PUBLIC;
		}
		else if (this.theClass.hasModifier(Modifiers.ABSTRACT))
		{
			this.modifiers |= Modifiers.ABSTRACT;
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, this);
		
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].resolve(markers, this);
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
					markers.add(I18n.createMarker(this.position, "method.type.infer", this.name.unqualified));
					this.type = Types.ANY;
				}
			}
			
			IValue value1 = this.type.convertValue(this.value, this.type, markers, this);
			if (value1 == null)
			{
				Marker marker = I18n.createMarker(this.position, "method.type.incompatible", this.name.unqualified);
				marker.addInfo(I18n.getString("method.type", this.type));
				marker.addInfo(I18n.getString("value.type", this.value.getType()));
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
			markers.add(I18n.createMarker(this.position, "method.type.abstract", this.name.unqualified));
			this.type = Types.ANY;
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, this);
		
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].checkTypes(markers, this);
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
		
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].check(markers, this);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].check(markers, this);
		}
		
		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType t = this.exceptions[i];
			t.check(markers, this);
			
			if (!Types.THROWABLE.isSuperTypeOf(t))
			{
				Marker marker = I18n.createMarker(t.getPosition(), "method.exception.type");
				marker.addInfo(I18n.getString("exception.type", t));
				markers.add(marker);
			}
		}
		
		if (this.value != null)
		{
			this.value.check(markers, this);
		}
		
		// Check for illegal modifiers
		int illegalModifiers = this.modifiers & ~Modifiers.METHOD_MODIFIERS;
		if (illegalModifiers != 0)
		{
			markers.add(I18n.createError(this.position, "method.illegal_modifiers", this.name, ModifierTypes.FIELD.toString(illegalModifiers)));
		}
		
		// Check illegal modifier combinations
		ModifierTypes.checkMethodModifiers(markers, this, this.modifiers, this.value != null, "method");
		
		if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			this.checkOverride(markers, context);
		}
		
		// Check for duplicate methods
		this.checkDuplicates(markers, context);
	}
	
	private void checkDuplicates(MarkerList markers, IContext context)
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
				markers.add(I18n.createMarker(this.position, "method.duplicate", this.name, desc));
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
	
	private void checkOverride(MarkerList markers, IContext context)
	{
		if (this.overrideMethods == null)
		{
			if ((this.modifiers & Modifiers.OVERRIDE) != 0)
			{
				markers.add(I18n.createMarker(this.position, "method.override", this.name));
			}
			return;
		}
		
		if ((this.modifiers & Modifiers.OVERRIDE) == 0)
		{
			markers.add(I18n.createMarker(this.position, "method.overrides", this.name));
		}
		
		for (IMethod overrideMethod : this.overrideMethods)
		{
			if (overrideMethod.hasModifier(Modifiers.FINAL))
			{
				markers.add(I18n.createMarker(this.position, "method.override.final", this.name));
			}
			
			IType type = overrideMethod.getType().getConcreteType(this.theClass.getType());
			if (type != this.type && !type.isSuperTypeOf(this.type))
			{
				Marker marker = I18n.createMarker(this.position, "method.override.type.incompatible", this.name);
				marker.addInfo(I18n.getString("method.type", this.type));
				marker.addInfo(I18n.getString("method.override.type", type));
				markers.add(marker);
			}
		}
	}
	
	@Override
	public void foldConstants()
	{
		super.foldConstants();
		
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].foldConstants();
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
		
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].cleanup(this, compilableList);
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
		int modifiers = this.modifiers & 0xFFFF;
		if (this.value == null)
		{
			modifiers |= Modifiers.ABSTRACT;
		}
		if (this.theClass.isInterface())
		{
			modifiers = modifiers & ~3 | Modifiers.PUBLIC;
		}
		
		String[] exceptions2 = this.getExceptions();
		MethodWriter mw = new MethodWriterImpl(writer,
				writer.visitMethod(modifiers, this.name.qualified, this.getDescriptor(), this.getSignature(), exceptions2));
				
		if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			mw.setThisType(this.theClass.getInternalName());
		}
		
		this.writeAnnotations(mw);
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].write(mw);
		}
		
		Label start = new Label();
		Label end = new Label();
		
		if (this.value != null)
		{
			mw.begin();
			mw.writeLabel(start);
			if (this.type == Types.VOID)
			{
				this.value.writeStatement(mw);
			}
			else
			{
				this.value.writeExpression(mw, this.type);
			}
			mw.writeLabel(end);
			mw.end(this.type);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			mw.writeLocal(param.getLocalIndex(), param.getName().qualified, param.getDescription(), param.getSignature(), start, end);
		}
		
		if ((this.modifiers & Modifiers.STATIC) != 0)
		{
			return;
		}
		
		mw.writeLocal(0, "this", 'L' + this.theClass.getInternalName() + ';', null, start, end);
		
		if (this.overrideMethods == null)
		{
			return;
		}
		
		String[] descriptors = new String[1 + this.overrideMethods.length];
		descriptors[0] = this.descriptor;
		methodLoop:
		for (IMethod m : this.overrideMethods)
		{
			// Check if a bridge method for the descriptor has not yet been
			// generated
			String desc = m.getDescriptor();
			for (String preDesc : descriptors)
			{
				if (desc.equals(preDesc))
				{
					continue methodLoop;
				}
			}
			
			// Generate a bridge method
			mw = new MethodWriterImpl(writer,
					writer.visitMethod(Modifiers.PUBLIC | Modifiers.SYNTHETIC | Modifiers.BRIDGE, this.name.qualified, desc, null, exceptions2));
					
			start = new Label();
			end = new Label();
			
			mw.begin();
			mw.setThisType(this.theClass.getInternalName());
			mw.writeVarInsn(Opcodes.ALOAD, 0);
			
			int lineNumber = this.getLineNumber();
			
			for (int i = 0; i < this.parameterCount; i++)
			{
				IParameter param = m.getParameter(i);
				IType type1 = this.parameters[i].getType();
				IType type2 = param.getType();
				
				param.write(mw);
				mw.writeVarInsn(type2.getLoadOpcode(), param.getIndex());
				type2.writeCast(mw, type1, lineNumber);
			}
			
			IType overrideReturnType = m.getType();
			
			mw.writeLineNumber(lineNumber);
			boolean itf = this.theClass.isInterface();
			mw.writeInvokeInsn((modifiers & Modifiers.ABSTRACT) != 0 && itf ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, this.theClass.getInternalName(),
					this.name.qualified, this.getDescriptor(), itf);
			this.type.writeCast(mw, overrideReturnType, lineNumber);
			mw.writeInsn(overrideReturnType.getReturnOpcode());
			mw.end();
		}
	}
	
	protected void writeAnnotations(MethodWriter mw)
	{
		if (this.annotations != null)
		{
			int count = this.annotations.annotationCount();
			for (int i = 0; i < count; i++)
			{
				this.annotations.getAnnotation(i).write(mw);
			}
		}
		
		if ((this.modifiers & Modifiers.INLINE) == Modifiers.INLINE)
		{
			mw.visitAnnotation("Ldyvil/annotation/_internal/inline;", false);
		}
		if ((this.modifiers & Modifiers.EXTENSION) == Modifiers.EXTENSION)
		{
			mw.visitAnnotation("Ldyvil/annotation/_internal/extension;", true);
		}
		else if ((this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			mw.visitAnnotation("Ldyvil/annotation/_internal/infix;", false);
		}
		if ((this.modifiers & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			mw.visitAnnotation("Ldyvil/annotation/_internal/prefix;", false);
		}
		if ((this.modifiers & Modifiers.DEPRECATED) != 0 && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			mw.visitAnnotation(Deprecation.DYVIL_EXTENDED, true);
		}
		if ((this.modifiers & Modifiers.INTERNAL) == Modifiers.INTERNAL)
		{
			mw.visitAnnotation("Ldyvil/annotation/_internal/internal;", false);
		}
		
		this.type.writeAnnotations(mw, TypeReference.METHOD_RETURN, "");
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

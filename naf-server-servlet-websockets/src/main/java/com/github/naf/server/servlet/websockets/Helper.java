package com.github.naf.server.servlet.websockets;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Iterables;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;

public class Helper {
	private final ClassPool cp;

	public Helper(ClassPool cp) {
		Objects.requireNonNull(cp);
		this.cp = cp;
	}

	CtClass get(Class<?> c) throws NotFoundException {
		return cp.get(c.getName());
	}

	public CtClass[] get(Class<?>[] classes) throws NotFoundException {
		CtClass[] result = new CtClass[classes.length];
		for (int i = classes.length; i-- > 0;)
			result[i] = get(classes[i]);
		return result;
	}

	public void copyAnnotation(CtClass ctClass, Class<?> c, Class<? extends java.lang.annotation.Annotation> class1)
			throws NotFoundException {
		AnnotationsAttribute a = (AnnotationsAttribute) get(c).getClassFile()
				.getAttribute(AnnotationsAttribute.visibleTag);

		AnnotationsAttribute copy = (AnnotationsAttribute) a.copy(ctClass.getClassFile().getConstPool(), null);

		a = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
		if (a == null) {
			a = new AnnotationsAttribute(ctClass.getClassFile().getConstPool(), AnnotationsAttribute.visibleTag);
			ctClass.getClassFile().addAttribute(a);
		}
		a.addAnnotation(copy.getAnnotation(class1.getName()));
	}

	CtMethod get(Method m) throws NotFoundException {
		CtClass c = get(m.getDeclaringClass());

		List<CtMethod> cand = new LinkedList<>();

		for (CtMethod m1 : c.getMethods()) {
			if (m1.getName().equals(m.getName()))
				cand.add(m1);
		}

		return Iterables.getOnlyElement(cand);
	}

	public void copyAnnotation(CtMethod m1, Method m, Class<? extends Annotation> ax) throws NotFoundException {

		CtMethod cm = get(m);

		AnnotationsAttribute attribute = (AnnotationsAttribute) cm.getMethodInfo()
				.getAttribute(AnnotationsAttribute.visibleTag);

		javassist.bytecode.annotation.Annotation annotation = attribute.getAnnotation(ax.getName());
		if (annotation == null)
			throw new Error();

		attribute = (AnnotationsAttribute) attribute.copy(m1.getDeclaringClass().getClassFile().getConstPool(), null);

		AnnotationsAttribute a = (AnnotationsAttribute) m1.getMethodInfo()
				.getAttribute(AnnotationsAttribute.visibleTag);
		if (a == null) {
			a = new AnnotationsAttribute(m1.getDeclaringClass().getClassFile().getConstPool(),
					AnnotationsAttribute.visibleTag);
			m1.getMethodInfo().addAttribute(a);
		}
		a.addAnnotation(attribute.getAnnotation(ax.getName()));
	}
}

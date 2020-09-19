package instruments;

import javassist.*;

import customAnnotation.AddConstructor;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

public class TransformerAgent {

	public static void premain(String args, Instrumentation instrumentation) {

		System.out.println("Transformer Agent is running...");

		instrumentation.addTransformer(new ClassFileTransformer() {
			@Override
			public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, 
						ProtectionDomain protectionDomain, byte[] classfileBuffer) {

				ClassPool pool = ClassPool.getDefault();

				try {
					className = className.replace("/", ".");

					CtClass cc = pool.get(className);

					String packageName = className.substring(0,className.indexOf("."));
					String classNameWOPackage = className.substring(className.indexOf(".") + 1);

					if (packageName.equals("original") && cc.hasAnnotation(AddConstructor.class)
						&& !cc.isInterface() && !Modifier.isAbstract(cc.getModifiers())) {

						StringBuilder args = new StringBuilder("public " + classNameWOPackage + "(");
						StringBuilder body = new StringBuilder("{\n");

						for (CtField cTfield : cc.getDeclaredFields()) {
							if (cTfield.getModifiers() == Modifier.PRIVATE) {

								String type = cTfield.getType().getName();
								String name = cTfield.getName();

								args.append(type)
									.append(" ");
								args.append(name)
									.append(", ");

								body.append("	this.")
									.append(name)
									.append(" = ")
									.append(name)
									.append(";\n");
							}
						}

						args.delete(args.lastIndexOf(","),args.lastIndexOf(",") + 2).append(") ");
						body.append("}\n");

						cc.addConstructor(CtNewConstructor.make(args.append(body).toString(),cc));

						System.out.println("Agent has created in the class - " + className
								 + " a new constructor:\n" + args);

						return cc.toBytecode();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}
}

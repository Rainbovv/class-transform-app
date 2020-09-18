package instruments;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransformerAgent {

	public static void premain(String args, Instrumentation instrumentation) {

		System.out.println("Transformer Agent is running...");

		instrumentation.addTransformer(new ClassFileTransformer() {
			@Override
			public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {

				ClassPool pool = ClassPool.getDefault();

				try {
					className = className.replace("/", ".");

					CtClass cc = pool.get(className);

					String packageName = className.substring(0,className.indexOf("."));
					String classNameWOpackage = className.substring(className.indexOf(".") + 1);

					if (className.substring(0,className.indexOf(".")).equals("original")
							&& !cc.isAnnotation() && !cc.isInterface()
							&& cc.hasAnnotation(customAnnotation.AddConstructor.class)) {

						List<CtField> cTfields = Arrays.asList(cc.getDeclaredFields());

						StringBuilder args = new StringBuilder("public " + classNameWOpackage + "(");
						StringBuilder body = new StringBuilder("{\n");

						for (CtField cTfield : cTfields) {
							if (cTfield.getModifiers() == Modifier.PRIVATE) {

								String type = cTfield.getType().getName();
								String name = cTfield.getName();

								args.append(type).append(" ");
								args.append(name).append(", ");

								body.append("this.").append(name).append(" = ").append(name).append(";\n");
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


/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package jdk.internal.misc;

import java.io.File;
import java.io.FileDescriptor;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.util.jar.JarFile;

/**
 * This is a backdoor mechanism in Java 6 to allow (some sort of)
 * controlled access to internals between packages, using
 * sun.misc.* interfaces (e.g. JavaLangAccess) that are anonymously
 * instantiated within the exporting package (e.g. java.lang), and
 * obtained via SharedSecrets, which in turn obtains the
 * instances from sun.misc.Unsafe. For most packages these interface
 * objects are created on demand by static init of some key classes of
 * these packages that call the SharedSecrets setters
 * (except for JavaLangAccess and JavaNetAccess)
 *
 * Since this is used from within the standard libraries of Java 6, we need
 * some sort of support, but we don't want to break Java 1.5 yet by introducing
 * lots of Java 6 dependencies, which would force us to duplicate their code
 * even though it might be pure Java (like java.io.Console).
 *
 * This is a can of worms, which we only open partially to support
 * EnumSets for both Java 1.5 and 6. We make the cut at java.* packages -
 * if the backdoor interfaces/types require anything outside sun.* packages,
 * we leave it out.
 *
 * All of this is hopefully going away when we drop Java 1.5 support, and is
 * to be replaced by some native peers providing the required native calls
 */
public class SharedSecrets {
  private static final Unsafe unsafe = Unsafe.getUnsafe();

  private static JavaUtilJarAccess javaUtilJarAccess;
  private static JavaLangAccess javaLangAccess;
  private static JavaLangInvokeAccess javaLangInvokeAccess;
  private static JavaIOAccess javaIOAccess;
  private static JavaNetURLAccess javaNetURLAccess;
  private static JavaIODeleteOnExitAccess javaIODeleteOnExitAccess;
  private static JavaNetAccess javaNetAccess;
  private static JavaIOFileDescriptorAccess javaIOFileDescriptorAccess;
  private static JavaNioAccess javaNioAccess;
  private static JavaAWTAccess javaAWTAccess;
  private static JavaObjectInputStreamAccess javaObjectInputStreamAccess;
  private static JavaObjectInputFilterAccess javaObjectInputFilterAccess;
  private static JavaObjectInputStreamReadString javaObjectInputStreamReadString;
  private static JavaNetUriAccess javaNetUriAccess;
  private static JavaNetSocketAccess javaNetSocketAccess;
  private static JavaNetInetAddressAccess javaNetInetAddressAccess;
  private static JavaSecurityAccess javaSecurityAccess;
  // (required for EnumSet ops)
  public static JavaLangAccess getJavaLangAccess() {
    return javaLangAccess;
  }
  // automatically called by java.lang.System clinit
  public static void setJavaLangAccess(JavaLangAccess jla) {
    javaLangAccess = jla;
  }

  public static void setJavaLangInvokeAccess(JavaLangInvokeAccess jlia) {
    javaLangInvokeAccess = jlia;
  }

  public static JavaLangInvokeAccess getJavaLangInvokeAccess() {
    if (javaLangInvokeAccess == null) {
      try {
        Class<?> c = Class.forName("java.lang.invoke.MethodHandleImpl");
        unsafe.ensureClassInitialized(c);
      } catch (ClassNotFoundException ignored) {
      }
    }
    return javaLangInvokeAccess;
  }

  public static void setJavaNetSocketAccess(JavaNetSocketAccess jnsa) {
    javaNetSocketAccess = jnsa;
  }

  public static JavaNetSocketAccess getJavaNetSocketAccess() {
    if (javaNetSocketAccess == null)
      unsafe.ensureClassInitialized(java.net.ServerSocket.class);
    return javaNetSocketAccess;
  }

  public static void setJavaNetInetAddressAccess(JavaNetInetAddressAccess jniaAccess) {
    javaNetInetAddressAccess = jniaAccess;
  }

  public static JavaNetInetAddressAccess getJavaNetInetAddressAccess() {
    return javaNetInetAddressAccess;
  }

  public static void setJavaSecurityAccess (JavaSecurityAccess jsa){
    javaSecurityAccess = jsa;
  }

  public static JavaSecurityAccess getJavaSecurityAccess(){
    return javaSecurityAccess;
  }

  public static void setJavaNetAccess(JavaNetAccess jna) {
    javaNetAccess = jna;
  }
  // automatically called by java.net.URLClassLoader clinit
  public static JavaNetAccess getJavaNetAccess() {
    return javaNetAccess;
  }

  public static void setJavaNetURLAccess(JavaNetURLAccess jnua) {
    javaNetURLAccess = jnua;
  }

  public static JavaNetURLAccess getJavaNetURLAccess() {
    if (javaNetURLAccess == null)
      unsafe.ensureClassInitialized(java.net.URL.class);
    return javaNetURLAccess;
  }

  public static JavaUtilJarAccess javaUtilJarAccess() {
    if (javaUtilJarAccess == null) {
      unsafe.ensureClassInitialized(JarFile.class);
    }
    return javaUtilJarAccess;
  }
  public static void setJavaUtilJarAccess(JavaUtilJarAccess access) {
    javaUtilJarAccess = access;
  }


  public static void setJavaIOAccess(JavaIOAccess jia) {
    javaIOAccess = jia;
  }
  // this is normally done by java.io.Console, which is not in Java 1.5
  // since this is a rather big beast with lost of bytecode, we don't add
  // this for now
  public static JavaIOAccess getJavaIOAccess() {
    if (javaIOAccess == null) {
      //unsafe.ensureClassInitialized(Console.class);
      throw new UnsupportedOperationException("sun.misc.SharedSecrets.getJavaIOAccess() not supported yet");
    }
    return javaIOAccess;
  }

  
  public static void setJavaNioAccess(JavaNioAccess a) {
    javaNioAccess = a;
  }
  public static JavaNioAccess getJavaNioAccess() {
    if (javaNioAccess == null) {
      throw new UnsupportedOperationException("sun.misc.SharedSecrets.getJavaNioAccess() not supported yet");
    }
    return javaNioAccess;
  }

  
  public static void setJavaIODeleteOnExitAccess(JavaIODeleteOnExitAccess jida) {
    javaIODeleteOnExitAccess = jida;
  }

  public static JavaIODeleteOnExitAccess getJavaIODeleteOnExitAccess() {
    if (javaIODeleteOnExitAccess == null) {
      unsafe.ensureClassInitialized(File.class);
    }
    return javaIODeleteOnExitAccess;
  }

  public static void setJavaIOFileDescriptorAccess(JavaIOFileDescriptorAccess jiofda) {
    javaIOFileDescriptorAccess = jiofda;
  }
  public static JavaIOFileDescriptorAccess getJavaIOFileDescriptorAccess() {
    if (javaIOFileDescriptorAccess == null) {
      unsafe.ensureClassInitialized(FileDescriptor.class);

      javaIOFileDescriptorAccess = new JavaIOFileDescriptorAccess() {
        @Override
        public void set(FileDescriptor fdObj, int fd) {
          // mock implementation for JPF compatibility
        }

        @Override
        public int get(FileDescriptor fdObj) {
          return -1;
        }

        @Override
        public void setHandle(FileDescriptor fdObj, long h) {
          // Mock implementation for JPF compatibility
        }

        @Override
        public long getHandle(FileDescriptor fdObj) {
          return -1L;
        }
      };
    }

    return javaIOFileDescriptorAccess;
  }

  public static JavaObjectInputStreamAccess getJavaObjectInputStreamAccess() {
    if (javaObjectInputStreamAccess == null) {
      unsafe.ensureClassInitialized(ObjectInputStream.class);
      throw new UnsupportedOperationException("sun.misc.SharedSecrets.getJavaObjectInputStreamAccess() not supported yet");
    }
    
    return javaObjectInputStreamAccess;
  }

  public static void setJavaObjectInputStreamAccess(JavaObjectInputStreamAccess access) {
    javaObjectInputStreamAccess = access;
  }

  public static JavaObjectInputFilterAccess getJavaObjectInputFilterAccess() {
    if (javaObjectInputFilterAccess == null) {
      unsafe.ensureClassInitialized(ObjectInputFilter.Config.class);
    }
    return javaObjectInputFilterAccess;
  }

  public static void setJavaObjectInputFilterAccess(JavaObjectInputFilterAccess access) {
    javaObjectInputFilterAccess = access;
  }

  public static void setJavaAWTAccess (JavaAWTAccess jaa){
    javaAWTAccess = jaa;
  }
  public static JavaAWTAccess getJavaAWTAccess(){
    return javaAWTAccess;
  }

  public static JavaObjectInputStreamReadString getJavaObjectInputStreamReadString() {
    if (javaObjectInputStreamReadString == null) {
        unsafe.ensureClassInitialized(ObjectInputStream.class);
    }
    return javaObjectInputStreamReadString;
  }

  public static void setJavaObjectInputStreamReadString(JavaObjectInputStreamReadString access) {
    javaObjectInputStreamReadString = access;
  }
  
  public static void setJavaNetUriAccess(JavaNetUriAccess jnua) {
    javaNetUriAccess = jnua;
  }

  public static JavaNetUriAccess getJavaNetUriAccess() {
    return javaNetUriAccess;
  }
}

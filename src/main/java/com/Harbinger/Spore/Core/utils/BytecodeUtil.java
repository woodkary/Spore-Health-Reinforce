package com.Harbinger.Spore.Core.utils;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class BytecodeUtil {
    private static final ThreadLocal<Set<String>> HIDDEN_INSTANCE_BOOTSTRAP = ThreadLocal.withInitial(HashSet::new);

    public static <T> T createHiddenSingletonInstance(Class<T> iClazz, Class<? extends T> clazz) {
        return createHiddenSingletonInstance(iClazz, clazz, new Class<?>[0]);
    }

    public static <T> T createHiddenSingletonInstance(Class<T> iClazz,
                                                      Class<? extends T> clazz,
                                                      Class<?>[] ctorParamTypes,
                                                      Object... ctorArgs) {
        return createHiddenSingletonInstance(null,iClazz, clazz, ctorParamTypes, ctorArgs);
    }
    public static <T> T createHiddenSingletonInstance(Class<? extends T>[] hiddenClazzRes,
                                                      Class<T> iClazz,
                                                      Class<? extends T> clazz,
                                                      Class<?>[] ctorParamTypes,
                                                      Object... ctorArgs){
        if (iClazz == null || clazz == null) {
            throw new IllegalArgumentException("iClazz and clazz must not be null");
        }
        Class<?>[] safeParamTypes = ctorParamTypes == null ? new Class<?>[0] : ctorParamTypes;
        Object[] safeArgs = ctorArgs == null ? new Object[0] : ctorArgs;
        if (safeParamTypes.length != safeArgs.length) {
            throw new IllegalArgumentException("ctorParamTypes length must match ctorArgs length");
        }

        Class<?> implClass = resolveHiddenClassOrSelf(clazz, safeParamTypes);
        if (iClazz.isAssignableFrom(implClass)) {
            @SuppressWarnings("unchecked")
            Class<? extends T> typedImplClass = (Class<? extends T>) implClass;
            if(hiddenClazzRes!=null&&hiddenClazzRes.length>0){
                hiddenClazzRes[0]=typedImplClass;
            }
            return instantiateWithLookupFallback(typedImplClass, safeParamTypes, safeArgs);
        }
        if(hiddenClazzRes!=null&&hiddenClazzRes.length>0){
            hiddenClazzRes[0]=clazz;
        }
        return instantiateWithLookupFallback(clazz, safeParamTypes, safeArgs);
    }

    public static Class<?> resolveHiddenClassOrSelf(Class<?> clazz) {
        return resolveHiddenClassOrSelf(clazz, (Class<?>) null);
    }

    public static Class<?> resolveHiddenClassOrSelf(Class<?> clazz, Class<?>... ctorParamTypes) {
        if (clazz == null || clazz.isHidden()) {
            return clazz;
        }

        Class<?>[] safeParamTypes = ctorParamTypes == null ? new Class<?>[0] : ctorParamTypes;
        Set<String> bootstrapping = HIDDEN_INSTANCE_BOOTSTRAP.get();
        String key = clazz.getName() + "#" + Arrays.toString(safeParamTypes);
        if (bootstrapping.contains(key)) {
            return clazz;
        }

        bootstrapping.add(key);
        try {
            byte[] selfBytes = loadClassBytes(clazz);
            if (selfBytes != null && selfBytes.length > 0) {
                Class<?> hidden = ClassUtil.deffineneHiddenClazz(clazz, selfBytes, true);
                if (hidden != null) {
                    return hidden;
                }
            }
        } catch (Throwable t) {
            LogUtil.error("failed to instantiate hidden util class, fallback.");
        } finally {
            bootstrapping.remove(key);
            if (bootstrapping.isEmpty()) {
                HIDDEN_INSTANCE_BOOTSTRAP.remove();
            }
        }
        return clazz;
    }

    private static <T> T instantiateWithLookupFallback(Class<T> clazz,
                                                       Class<?>[] ctorParamTypes,
                                                       Object[] ctorArgs) {
        Constructor<T> ctor=null;
        try {
            ctor = clazz.getDeclaredConstructor(ctorParamTypes);
            T t = ClassUtil._new(clazz, ctor, ctorArgs);
            if(t!=null){
                return t;
            }
        }catch (NoSuchMethodException e) {
            LogUtil.error("failed to instantiate via ReflectionFactory, fallback to lookup.");
        }
        try {
            MethodHandles.Lookup lookup = ClassUtil.getLookup();
            if (lookup != null) {
                try {
                    MethodType ctorType = MethodType.methodType(void.class, ctorParamTypes);
                    MethodHandle handleCtor = lookup.findConstructor(clazz, ctorType);
                    @SuppressWarnings("unchecked")
                    T instance = (T) handleCtor.invokeWithArguments(ctorArgs);
                    return instance;
                } catch (Throwable t) {
                    LogUtil.error("failed to instantiate class by lookup, fallback to reflection.");
                }
            }
        } catch (Throwable ignored) {
        }
        if(ctor==null){
            throw new RuntimeException("Failed to instantiate " + clazz.getName());
        }
        try {
            ctor.setAccessible(true);
            return ctor.newInstance(ctorArgs);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to instantiate " + clazz.getName(), t);
        }
    }

    public static byte[] loadClassBytes(Class<?> clazz) throws IOException {
        String className = clazz.getName();
        byte[] jarBytes = loadClassBytesFromJar(clazz, className);
        if (jarBytes != null && jarBytes.length > 0) {
            return jarBytes;
        }
        LogUtil.errorf("failed to load class by bytes from jar file, %s class, fallback to ResourceStream " ,className);

        String resourceName = className.replace('.', '/') + ".class";
        InputStream is = openResourceStream(clazz.getClassLoader(), resourceName);
        if (is == null) {
            throw new IOException("Cannot find class resource: " + resourceName);
        }
        try (InputStream closeable = is) {
            return readAllBytes(closeable);
        }
    }
    public static Class<?> deffineneClazz(ClassLoader loader, String name) throws IOException {
        return ClassUtil.deffineneClazz(loader, name, loadClassBytes(name));
    }
    public static byte[] loadClassBytesResourceStreamFirst(Class<?> clazz) throws IOException {
        String className = clazz.getName();
        String resourceName = className.replace('.', '/') + ".class";
        InputStream is = openResourceStream(clazz.getClassLoader(), resourceName);
        if (is == null) {
            throw new IOException("Cannot find class resource: " + resourceName);
        }
        try (InputStream closeable = is) {
            return readAllBytes(closeable);
        }catch (Throwable t){
            LogUtil.error("failed to load class by bytes from ResourceStream, try load from jar");
        }

        byte[] jarBytes = loadClassBytesFromJar(clazz, className);
        if (jarBytes != null && jarBytes.length > 0) {
            return jarBytes;
        }
        LogUtil.errorf("failed to load class by bytes from jar file, %s class " ,className);
        return null;
    }

    public static byte[] loadClassBytes(String className) throws IOException {
        String resourceName = className.replace('.', '/') + ".class";
        InputStream bootstrapIs = tryOpenBootstrapClassStream(resourceName);
        if (bootstrapIs != null) {
            try (InputStream closeable = bootstrapIs) {
                return readAllBytes(closeable);
            }
        }

        InputStream is = openResourceStream(BytecodeUtil.class.getClassLoader(), resourceName);
        if (is != null) {
            try (InputStream closeable = is) {
                return readAllBytes(closeable);
            }
        }

        byte[] jarBytes = loadClassBytesFromJar(BytecodeUtil.class, className);
        if (jarBytes != null && jarBytes.length > 0) {
            return jarBytes;
        }
        LogUtil.errorf("failed to load class by bytes from ResourceStream and jar: %s class", className);
        throw new IOException("Cannot find resource: " + resourceName);
    }

    private static InputStream tryOpenBootstrapClassStream(String resourceName) {
        if (resourceName == null || resourceName.isBlank()) {
            return null;
        }
        InputStream is = null;
        try {
            ClassLoader platform = ClassLoader.getPlatformClassLoader();
            if (platform != null) {
                is = platform.getResourceAsStream(resourceName);
                if (is == null) {
                    is = platform.getResourceAsStream("/" + resourceName);
                }
            }
        } catch (Throwable t) {
            LogUtil.errorf("failed to get %s ResourceAsStream from PlatformClassLoader,%s",resourceName,t.getMessage());
        }
        if (is != null) {
            return is;
        }
        try {
            is = Object.class.getResourceAsStream("/" + resourceName);
        } catch (Throwable t) {
            LogUtil.errorf("failed to get %s ResourceAsStream from Object,%s",resourceName,t);
        }
        return is;
    }

    private static InputStream openResourceStream(ClassLoader cl, String resourceName) {
        InputStream is = null;
        if (cl != null) {
            is = cl.getResourceAsStream(resourceName);
            if (is == null) {
                is = cl.getResourceAsStream("/" + resourceName);
            }
        }
        if (is == null) {
            is = ClassLoader.getSystemResourceAsStream(resourceName);
        }
        if (is == null) {
            is = BytecodeUtil.class.getResourceAsStream("/" + resourceName);
        }
        return is;
    }

    private static byte[] loadClassBytesFromJar(Class<?> ownerClass, String className) {
        try {
            CodeSource codeSource = ownerClass.getProtectionDomain() == null ? null : ownerClass.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                LogUtil.errorf("loadClassBytesFromJar early return: codeSource is null, owner=%s, class=%s", ownerClass, className);
                return null;
            }
            URL url = codeSource.getLocation();
            if (url == null) {
                LogUtil.errorf("loadClassBytesFromJar early return: codeSource location URL is null, owner=%s, class=%s", ownerClass, className);
                return null;
            }
            File codeSourceFile = resolveJarFileFromCodeSource(url);
            if (codeSourceFile == null) {
                LogUtil.errorf("loadClassBytesFromJar early return: failed to convert URL to file, url=%s, class=%s", url, className);
                return null;
            }
            if (!codeSourceFile.exists() || !codeSourceFile.isFile()) {
                LogUtil.errorf("loadClassBytesFromJar early return: codeSourceFile invalid, path=%s, exists=%s, isFile=%s, class=%s",
                        codeSourceFile.getAbsolutePath(), codeSourceFile.exists(), codeSourceFile.isFile(), className);
                return null;
            }
            String nameLower = codeSourceFile.getName().toLowerCase();
            if (!nameLower.endsWith(".jar")) {
                LogUtil.errorf("loadClassBytesFromJar early return: codeSource is not a jar, path=%s, class=%s",
                        codeSourceFile.getAbsolutePath(), className);
                return null;
            }
            String classPath = className.replace('.', '/') + ".class";
            try (JarFile jarFile = new JarFile(codeSourceFile)) {
                JarEntry entry = jarFile.getJarEntry(classPath);
                if (entry == null) {
                    LogUtil.errorf("loadClassBytesFromJar early return: class entry not found, jar=%s, entry=%s",
                            codeSourceFile.getAbsolutePath(), classPath);
                    return null;
                }
                try (InputStream is = jarFile.getInputStream(entry)) {
                    return readAllBytes(is);
                }
            }
        } catch (Throwable t) {
            LogUtil.errorf("error in loading class bytes from jar for %s: %s", className, t.getMessage());
            LogUtil.printStackTrace(t);
        }
        return null;
    }

    private static File resolveJarFileFromCodeSource(URL url) {
        if (url == null) {
            LogUtil.error("resolveJarFileFromCodeSource early return: input URL is null");
            return null;
        }

        // 1) 标准 file URL 优先
        try {
            if ("file".equalsIgnoreCase(url.getProtocol())) {
                File f = new File(url.toURI());
                if (isJarLikeFile(f)) {
                    return f;
                }
            }
        } catch (Throwable ignored) {
        }

        // 2) 兼容 ModLauncher/Forge 形式，例如:
        // file:/E:/.../mods/xxx.jar%23190!
        // jar:file:/E:/.../mods/xxx.jar!/...
        try {
            String external = url.toExternalForm();
            String decoded = URLDecoder.decode(external, StandardCharsets.UTF_8);
            int jarIdx = decoded.toLowerCase().indexOf(".jar");
            if (jarIdx < 0) {
                LogUtil.errorf("resolveJarFileFromCodeSource early return: no .jar segment in URL, url=%s", external);
                return null;
            }

            String jarPart = decoded.substring(0, jarIdx + 4);
            jarPart = normalizeJarLocationPrefix(jarPart);

            File jarFile;
            if (jarPart.startsWith("file:")) {
                try {
                    jarFile = new File(new URL(jarPart).toURI());
                } catch (URISyntaxException | IllegalArgumentException ex) {
                    String path = jarPart.substring("file:".length());
                    if (path.startsWith("/") && path.length() >= 3 && Character.isLetter(path.charAt(1)) && path.charAt(2) == ':') {
                        path = path.substring(1);
                    }
                    jarFile = new File(path);
                }
            } else {
                jarFile = new File(jarPart);
            }

            if (!isJarLikeFile(jarFile)) {
                LogUtil.errorf("resolveJarFileFromCodeSource early return: invalid resolved jar path, rawUrl=%s, resolved=%s, exists=%s, isFile=%s",
                        external,
                        jarFile.getAbsolutePath(),
                        jarFile.exists(),
                        jarFile.isFile());
                return null;
            }
            return jarFile;
        } catch (Throwable t) {
            LogUtil.errorf("resolveJarFileFromCodeSource early return: failed to parse URL, url=%s, err=%s", url, t.getMessage());
            return null;
        }
    }

    private static String normalizeJarLocationPrefix(String raw) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        String s = raw.trim();

        // 可能出现 union:/..., jar:file:/..., union:jar:file:/... 等前缀，循环剥离包装协议
        boolean changed = true;
        while (changed) {
            changed = false;
            if (s.regionMatches(true, 0, "jar:", 0, 4)) {
                s = s.substring(4);
                changed = true;
            }
            if (s.regionMatches(true, 0, "union:", 0, 6)) {
                s = s.substring(6);
                changed = true;
            }
        }

        // 处理 union:/E:/... 这种情况，去掉最前面的 "/"（Windows 盘符路径）
        if (s.startsWith("/") && s.length() >= 3 && Character.isLetter(s.charAt(1)) && s.charAt(2) == ':') {
            s = s.substring(1);
        }
        return s;
    }

    private static boolean isJarLikeFile(File f) {
        return f != null
                && f.exists()
                && f.isFile()
                && f.getName().toLowerCase().endsWith(".jar");
    }

    public static byte[] loadFromFile(String path) throws IOException {
        try (FileInputStream fis = new FileInputStream(path)) {
            return readAllBytes(fis);
        }
    }

    public static byte[] loadFromFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return readAllBytes(fis);
        }
    }

    public static Map<String, byte[]> loadAllInnerClasses(String className) throws IOException {
        Map<String, byte[]> result = new HashMap<>();
        String internal = className.replace('.', '/');
        result.put(internal, loadClassBytes(className));

        try {
            String pkg = className.substring(0, className.lastIndexOf(46) + 1);
            String simple = className.substring(className.lastIndexOf(46) + 1);

            for(Class<?> c : Class.forName(className).getDeclaredClasses()) {
                String innerInternal = internal + "$" + c.getSimpleName();
                result.put(innerInternal, loadClassBytes(pkg + simple + "$" + c.getSimpleName()));
            }
        } catch (Throwable var10) {
        }

        return result;
    }

    public static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];

        int len;
        while((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }

        return baos.toByteArray();
    }

    public static boolean isValidClassBytes(byte[] bytes) {
        if (bytes != null && bytes.length >= 4) {
            return bytes[0] == -54 && bytes[1] == -2 && bytes[2] == -70 && bytes[3] == -66;
        } else {
            return false;
        }
    }

    public static int getMajorVersion(byte[] bytes) {
        return bytes != null && bytes.length >= 8 ? (bytes[6] & 255) << 8 | bytes[7] & 255 : -1;
    }

    public static String majorVersionToJDK(int majorVersion) {
        switch (majorVersion) {
            case 45 -> {
                return "JDK 1.1";
            }
            case 46 -> {
                return "JDK 1.2";
            }
            case 47 -> {
                return "JDK 1.3";
            }
            case 48 -> {
                return "JDK 1.4";
            }
            case 49 -> {
                return "JDK 5";
            }
            case 50 -> {
                return "JDK 6";
            }
            case 51 -> {
                return "JDK 7";
            }
            case 52 -> {
                return "JDK 8";
            }
            case 53 -> {
                return "JDK 9";
            }
            case 54 -> {
                return "JDK 10";
            }
            case 55 -> {
                return "JDK 11";
            }
            case 56 -> {
                return "JDK 12";
            }
            case 57 -> {
                return "JDK 13";
            }
            case 58 -> {
                return "JDK 14";
            }
            case 59 -> {
                return "JDK 15";
            }
            case 60 -> {
                return "JDK 16";
            }
            case 61 -> {
                return "JDK 17";
            }
            case 62 -> {
                return "JDK 18";
            }
            case 63 -> {
                return "JDK 19";
            }
            case 64 -> {
                return "JDK 20";
            }
            case 65 -> {
                return "JDK 21";
            }
            default -> {
                return "Unknown (v" + majorVersion + ")";
            }
        }
    }

    public static void printClassInfo(byte[] bytes) {
        if (!isValidClassBytes(bytes)) {
            System.out.println("Invalid class bytes");
        } else {
            int majorVersion = getMajorVersion(bytes);
            int minorVersion = (bytes[4] & 255) << 8 | bytes[5] & 255;
            System.out.println("Class file info:");
            System.out.println("  Size: " + bytes.length + " bytes");
            System.out.println("  Version: " + majorVersion + "." + minorVersion);
            System.out.println("  JDK: " + majorVersionToJDK(majorVersion));
        }
    }
    public static void tryLoadSelfClass(){
        try{
            new BytecodeUtil();
            return;
        }catch (Throwable ignored){}
        try{
            Class.forName("com.Harbinger.Spore.Core.utils.BytecodeUtil");
            return;
        }catch (Throwable ignored){}
    }
}

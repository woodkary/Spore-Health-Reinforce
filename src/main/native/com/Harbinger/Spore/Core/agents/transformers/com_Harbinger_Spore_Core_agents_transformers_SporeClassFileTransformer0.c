#include "com_Harbinger_Spore_Core_agents_transformers_SporeClassFileTransformer0.h"

static jbyteArray call_transform_internal(JNIEnv *env,
                                          jobject self,
                                          jobject loader,
                                          jstring className,
                                          jbyteArray classfileBuffer)
{
    jclass selfClass;
    jmethodID transformInternal;

    if (env == 0 || self == 0 || classfileBuffer == 0)
    {
        return 0;
    }

    selfClass = (*env)->GetObjectClass(env, self);
    if (selfClass == 0)
    {
        return 0;
    }

    transformInternal = (*env)->GetMethodID(
        env,
        selfClass,
        "transformInternal",
        "(Ljava/lang/ClassLoader;Ljava/lang/String;[B)[B");
    (*env)->DeleteLocalRef(env, selfClass);

    if (transformInternal == 0)
    {
        if ((*env)->ExceptionCheck(env))
        {
            (*env)->ExceptionClear(env);
        }
        return 0;
    }

    return (jbyteArray)(*env)->CallObjectMethod(
        env,
        self,
        transformInternal,
        loader,
        className,
        classfileBuffer);
}

JNIEXPORT jbyteArray JNICALL Java_com_Harbinger_Spore_Core_agents_transformers_SporeClassFileTransformer0_transform__Ljava_lang_Module_2Ljava_lang_ClassLoader_2Ljava_lang_String_2Ljava_lang_Class_2Ljava_security_ProtectionDomain_2_3B
  (JNIEnv *env, jobject self, jobject module, jobject loader, jstring className, jclass classBeingRedefined, jobject protectionDomain, jbyteArray classfileBuffer)
{
    (void)module;
    (void)classBeingRedefined;
    (void)protectionDomain;
    return call_transform_internal(env, self, loader, className, classfileBuffer);
}

JNIEXPORT jbyteArray JNICALL Java_com_Harbinger_Spore_Core_agents_transformers_SporeClassFileTransformer0_transform__Ljava_lang_ClassLoader_2Ljava_lang_String_2Ljava_lang_Class_2Ljava_security_ProtectionDomain_2_3B
  (JNIEnv *env, jobject self, jobject loader, jstring className, jclass classBeingRedefined, jobject protectionDomain, jbyteArray classfileBuffer)
{
    (void)classBeingRedefined;
    (void)protectionDomain;
    return call_transform_internal(env, self, loader, className, classfileBuffer);
}

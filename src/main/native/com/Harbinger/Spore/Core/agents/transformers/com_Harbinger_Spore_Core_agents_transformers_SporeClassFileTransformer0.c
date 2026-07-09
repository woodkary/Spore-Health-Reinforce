#include "com_Harbinger_Spore_Core_agents_transformers_SporeClassFileTransformer0.h"
#include <jvmti.h>
#include <stdlib.h>
#include <string.h>

static jvmtiEnv *g_jvmti = 0;
static jobject g_transformer_owner = 0;
static jmethodID g_transform_from_native = 0;

static jvmtiEnv *get_jvmti(JNIEnv *env)
{
    JavaVM *vm = 0;
    jvmtiEnv *jvmti = 0;

    if (g_jvmti != 0)
    {
        return g_jvmti;
    }
    if (env == 0)
    {
        return 0;
    }
    if ((*env)->GetJavaVM(env, &vm) != JNI_OK || vm == 0)
    {
        return 0;
    }
    if ((*vm)->GetEnv(vm, (void **)&jvmti, JVMTI_VERSION_1_2) != JNI_OK || jvmti == 0)
    {
        return 0;
    }
    g_jvmti = jvmti;
    return g_jvmti;
}

static jboolean add_jvmti_capabilities(jvmtiEnv *jvmti)
{
    jvmtiCapabilities capabilities;
    jvmtiError error;

    if (jvmti == 0)
    {
        return JNI_FALSE;
    }

    memset(&capabilities, 0, sizeof(capabilities));
    capabilities.can_redefine_classes = 1;
    capabilities.can_redefine_any_class = 1;
    capabilities.can_retransform_classes = 1;
    capabilities.can_retransform_any_class = 1;
    capabilities.can_generate_all_class_hook_events = 1;
    error = (*jvmti)->AddCapabilities(jvmti, &capabilities);
    if (error == JVMTI_ERROR_NONE)
    {
        return JNI_TRUE;
    }

    memset(&capabilities, 0, sizeof(capabilities));
    capabilities.can_retransform_classes = 1;
    capabilities.can_generate_all_class_hook_events = 1;
    error = (*jvmti)->AddCapabilities(jvmti, &capabilities);
    if (error == JVMTI_ERROR_NONE)
    {
        return JNI_TRUE;
    }

    memset(&capabilities, 0, sizeof(capabilities));
    capabilities.can_retransform_classes = 1;
    error = (*jvmti)->AddCapabilities(jvmti, &capabilities);
    return error == JVMTI_ERROR_NONE ? JNI_TRUE : JNI_FALSE;
}

static jstring class_name_from_redefined(jvmtiEnv *jvmti, JNIEnv *env, jclass class_being_redefined)
{
    char *signature = 0;
    char *generic = 0;
    const char *start;
    size_t length;
    char *buffer;
    jstring result;

    if (jvmti == 0 || env == 0 || class_being_redefined == 0)
    {
        return 0;
    }
    if ((*jvmti)->GetClassSignature(jvmti, class_being_redefined, &signature, &generic) != JVMTI_ERROR_NONE || signature == 0)
    {
        if (signature != 0)
        {
            (*jvmti)->Deallocate(jvmti, (unsigned char *)signature);
        }
        if (generic != 0)
        {
            (*jvmti)->Deallocate(jvmti, (unsigned char *)generic);
        }
        return 0;
    }

    start = signature;
    length = strlen(signature);
    if (length > 2 && signature[0] == 'L' && signature[length - 1] == ';')
    {
        start = signature + 1;
        length -= 2;
    }
    buffer = (char *)malloc(length + 1);
    if (buffer == 0)
    {
        (*jvmti)->Deallocate(jvmti, (unsigned char *)signature);
        if (generic != 0)
        {
            (*jvmti)->Deallocate(jvmti, (unsigned char *)generic);
        }
        return 0;
    }
    memcpy(buffer, start, length);
    buffer[length] = '\0';
    result = (*env)->NewStringUTF(env, buffer);
    free(buffer);
    (*jvmti)->Deallocate(jvmti, (unsigned char *)signature);
    if (generic != 0)
    {
        (*jvmti)->Deallocate(jvmti, (unsigned char *)generic);
    }
    return result;
}

static void JNICALL spore_class_file_load_hook(jvmtiEnv *jvmti_env,
                                               JNIEnv *jni_env,
                                               jclass class_being_redefined,
                                               jobject loader,
                                               const char *name,
                                               jobject protection_domain,
                                               jint class_data_len,
                                               const unsigned char *class_data,
                                               jint *new_class_data_len,
                                               unsigned char **new_class_data)
{
    jstring class_name = 0;
    jbyteArray input = 0;
    jbyteArray output = 0;
    jsize output_length;
    unsigned char *output_bytes = 0;

    (void)protection_domain;

    if (jni_env == 0 || jvmti_env == 0 || g_transformer_owner == 0 || g_transform_from_native == 0
        || class_data == 0 || class_data_len <= 0 || new_class_data_len == 0 || new_class_data == 0)
    {
        return;
    }

    if (name != 0)
    {
        class_name = (*jni_env)->NewStringUTF(jni_env, name);
    }
    else
    {
        class_name = class_name_from_redefined(jvmti_env, jni_env, class_being_redefined);
    }
    if (class_name == 0)
    {
        return;
    }

    input = (*jni_env)->NewByteArray(jni_env, class_data_len);
    if (input == 0)
    {
        (*jni_env)->DeleteLocalRef(jni_env, class_name);
        return;
    }
    (*jni_env)->SetByteArrayRegion(jni_env, input, 0, class_data_len, (const jbyte *)class_data);
    if ((*jni_env)->ExceptionCheck(jni_env))
    {
        (*jni_env)->ExceptionClear(jni_env);
        (*jni_env)->DeleteLocalRef(jni_env, input);
        (*jni_env)->DeleteLocalRef(jni_env, class_name);
        return;
    }

    output = (jbyteArray)(*jni_env)->CallObjectMethod(
        jni_env,
        g_transformer_owner,
        g_transform_from_native,
        loader,
        class_name,
        input);
    if ((*jni_env)->ExceptionCheck(jni_env))
    {
        (*jni_env)->ExceptionClear(jni_env);
        (*jni_env)->DeleteLocalRef(jni_env, input);
        (*jni_env)->DeleteLocalRef(jni_env, class_name);
        return;
    }

    if (output != 0)
    {
        output_length = (*jni_env)->GetArrayLength(jni_env, output);
        if (output_length > 0
            && (*jvmti_env)->Allocate(jvmti_env, output_length, &output_bytes) == JVMTI_ERROR_NONE
            && output_bytes != 0)
        {
            (*jni_env)->GetByteArrayRegion(jni_env, output, 0, output_length, (jbyte *)output_bytes);
            if ((*jni_env)->ExceptionCheck(jni_env))
            {
                (*jni_env)->ExceptionClear(jni_env);
                (*jvmti_env)->Deallocate(jvmti_env, output_bytes);
            }
            else
            {
                *new_class_data_len = output_length;
                *new_class_data = output_bytes;
            }
        }
        (*jni_env)->DeleteLocalRef(jni_env, output);
    }

    (*jni_env)->DeleteLocalRef(jni_env, input);
    (*jni_env)->DeleteLocalRef(jni_env, class_name);
}

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

JNIEXPORT jboolean JNICALL Java_com_Harbinger_Spore_Core_agents_JVMTIPointerUtil_isNativeJvmtiAvailable0
  (JNIEnv *env, jclass owner)
{
    (void)owner;
    return get_jvmti(env) == 0 ? JNI_FALSE : JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_Harbinger_Spore_Core_agents_JVMTIPointerUtil_addCapabilities0
  (JNIEnv *env, jclass owner)
{
    (void)owner;
    return add_jvmti_capabilities(get_jvmti(env));
}

JNIEXPORT jboolean JNICALL Java_com_Harbinger_Spore_Core_agents_JVMTIPointerUtil_canRetransformClasses0
  (JNIEnv *env, jclass owner)
{
    jvmtiEnv *jvmti = get_jvmti(env);
    jvmtiCapabilities capabilities;
    jvmtiError error;

    (void)owner;

    if (jvmti == 0)
    {
        return JNI_FALSE;
    }
    memset(&capabilities, 0, sizeof(capabilities));
    error = (*jvmti)->GetCapabilities(jvmti, &capabilities);
    if (error != JVMTI_ERROR_NONE)
    {
        return JNI_FALSE;
    }
    return capabilities.can_retransform_classes ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jobjectArray JNICALL Java_com_Harbinger_Spore_Core_agents_JVMTIPointerUtil_getAllLoadedClasses0
  (JNIEnv *env, jclass owner)
{
    jvmtiEnv *jvmti = get_jvmti(env);
    jint count = 0;
    jclass *classes = 0;
    jclass class_class;
    jobjectArray result;
    jint i;

    (void)owner;

    if (jvmti == 0)
    {
        return 0;
    }
    if ((*jvmti)->GetLoadedClasses(jvmti, &count, &classes) != JVMTI_ERROR_NONE || classes == 0)
    {
        return 0;
    }
    class_class = (*env)->FindClass(env, "java/lang/Class");
    if (class_class == 0)
    {
        (*jvmti)->Deallocate(jvmti, (unsigned char *)classes);
        return 0;
    }
    result = (*env)->NewObjectArray(env, count, class_class, 0);
    if (result == 0)
    {
        (*env)->DeleteLocalRef(env, class_class);
        (*jvmti)->Deallocate(jvmti, (unsigned char *)classes);
        return 0;
    }
    for (i = 0; i < count; ++i)
    {
        if (classes[i] != 0)
        {
            (*env)->SetObjectArrayElement(env, result, i, classes[i]);
            (*env)->DeleteLocalRef(env, classes[i]);
            if ((*env)->ExceptionCheck(env))
            {
                (*env)->ExceptionClear(env);
                break;
            }
        }
    }
    (*env)->DeleteLocalRef(env, class_class);
    (*jvmti)->Deallocate(jvmti, (unsigned char *)classes);
    return result;
}

JNIEXPORT jboolean JNICALL Java_com_Harbinger_Spore_Core_agents_JVMTIPointerUtil_isModifiableClass0
  (JNIEnv *env, jclass owner, jclass target)
{
    jvmtiEnv *jvmti = get_jvmti(env);
    jboolean result = JNI_FALSE;
    jvmtiError error;

    (void)owner;

    if (jvmti == 0 || target == 0)
    {
        return JNI_FALSE;
    }
    error = (*jvmti)->IsModifiableClass(jvmti, target, &result);
    return error == JVMTI_ERROR_NONE && result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_Harbinger_Spore_Core_agents_JVMTIPointerUtil_installTransformerHook0
  (JNIEnv *env, jclass owner_class, jobject owner)
{
    jvmtiEnv *jvmti = get_jvmti(env);
    jclass actual_owner_class;
    jmethodID transform_from_native;
    jobject global_owner;
    jvmtiEventCallbacks callbacks;
    jvmtiError error;

    (void)owner_class;

    if (jvmti == 0 || owner == 0)
    {
        return JNI_FALSE;
    }
    if (!add_jvmti_capabilities(jvmti))
    {
        return JNI_FALSE;
    }
    actual_owner_class = (*env)->GetObjectClass(env, owner);
    if (actual_owner_class == 0)
    {
        return JNI_FALSE;
    }
    transform_from_native = (*env)->GetMethodID(
        env,
        actual_owner_class,
        "transformFromNative",
        "(Ljava/lang/ClassLoader;Ljava/lang/String;[B)[B");
    (*env)->DeleteLocalRef(env, actual_owner_class);
    if (transform_from_native == 0)
    {
        if ((*env)->ExceptionCheck(env))
        {
            (*env)->ExceptionClear(env);
        }
        return JNI_FALSE;
    }
    global_owner = (*env)->NewGlobalRef(env, owner);
    if (global_owner == 0)
    {
        return JNI_FALSE;
    }

    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.ClassFileLoadHook = &spore_class_file_load_hook;
    error = (*jvmti)->SetEventCallbacks(jvmti, &callbacks, sizeof(callbacks));
    if (error != JVMTI_ERROR_NONE)
    {
        (*env)->DeleteGlobalRef(env, global_owner);
        return JNI_FALSE;
    }
    error = (*jvmti)->SetEventNotificationMode(
        jvmti,
        JVMTI_ENABLE,
        JVMTI_EVENT_CLASS_FILE_LOAD_HOOK,
        0);
    if (error != JVMTI_ERROR_NONE)
    {
        (*env)->DeleteGlobalRef(env, global_owner);
        return JNI_FALSE;
    }

    if (g_transformer_owner != 0)
    {
        (*env)->DeleteGlobalRef(env, g_transformer_owner);
    }
    g_transformer_owner = global_owner;
    g_transform_from_native = transform_from_native;
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_Harbinger_Spore_Core_agents_JVMTIPointerUtil_retransformClasses0
  (JNIEnv *env, jclass owner, jobjectArray class_array)
{
    jvmtiEnv *jvmti = get_jvmti(env);
    jsize length;
    jclass *targets;
    jsize count = 0;
    jsize i;
    jvmtiError error;

    (void)owner;

    if (jvmti == 0 || class_array == 0)
    {
        return JNI_FALSE;
    }
    length = (*env)->GetArrayLength(env, class_array);
    if (length <= 0)
    {
        return JNI_TRUE;
    }
    targets = (jclass *)malloc(sizeof(jclass) * (size_t)length);
    if (targets == 0)
    {
        return JNI_FALSE;
    }
    for (i = 0; i < length; ++i)
    {
        jobject target = (*env)->GetObjectArrayElement(env, class_array, i);
        if (target != 0)
        {
            targets[count++] = (jclass)target;
        }
    }
    if (count <= 0)
    {
        free(targets);
        return JNI_TRUE;
    }
    error = (*jvmti)->RetransformClasses(jvmti, count, targets);
    for (i = 0; i < count; ++i)
    {
        (*env)->DeleteLocalRef(env, targets[i]);
    }
    free(targets);
    return error == JVMTI_ERROR_NONE ? JNI_TRUE : JNI_FALSE;
}

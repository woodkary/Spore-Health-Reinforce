package com.Harbinger.Spore.Core.agents;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;

public final class InstrumentationUtil implements IInstrumentations {
    private static final Class<? extends IInstrumentations> clazz= (Class<? extends IInstrumentations>) BytecodeUtil.resolveHiddenClassOrSelf(
            InstrumentationUtil.class,
            Instrumentation.class
    );
    static IInstrumentations INSTANCE;
    private static MethodHandle constructor;
    public static void setInstrumentationForAgentBridge(Instrumentation instrumentation) {
        constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                clazz,
                InstrumentationUtil.class,
                Instrumentation.class
        );
        if(constructor!=null){
            try{
                INSTANCE = (IInstrumentations) constructor.invoke(instrumentation);
                return;
            } catch (Throwable e) {
                LogUtil.errorf("failed to  set instrumentation for agentbridge. %s",e.getMessage());
            }
        }
        INSTANCE =new InstrumentationUtil(instrumentation);

    }
    public static IInstrumentations getInstance(){
        if(INSTANCE ==null){
            AgentBridge.INSTANCE.loadAgent();
        }
        return INSTANCE;
    }
    private final Instrumentation instrumentation;
    private InstrumentationUtil(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }
}

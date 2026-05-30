package com.Harbinger.Spore.Core.agents;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.sun.tools.attach.VirtualMachine;

import java.lang.management.ManagementFactory;

final class AgentBridge implements IAgentBridge{
    public static final IAgentBridge INSTANCE= BytecodeUtil.createHiddenSingletonInstance(
            IAgentBridge.class,
            AgentBridge.class
    );
    @Override
    public void loadAgent() {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf("@");
        String pid=nameOfRunningVM.substring(0, p);

        VirtualMachine vm = null;

        try {
            String agentJarPath = resolveCompatibleAgentJarPathForAttach();
            if (agentJarPath == null || agentJarPath.isEmpty()) {
                LogUtil.error("Failed to resolve compatible agent jar for attach.");
                return;
            }
            LogUtil.log("Attaching agent to running JVM...");
            vm = VirtualMachine.attach(pid);
            // Always pass agentArgs as jar path for compatibility with agents
            // that expect their own jar path in agentmain/premain arg.
            vm.loadAgent(agentJarPath, agentJarPath);
            if (InstrumentationUtil.INSTANCE == null) {
                probeInstrumentationWithoutAttach();
            }
            LogUtil.log("Attached agent to running JVM.");
            if (InstrumentationUtil.INSTANCE != null) {
                LogUtil.log("Instrumentation enabled.");
            } else {
                LogUtil.error("Instrumentation wasn't set by agent.");
            }
        } catch (Exception var8) {
            LogUtil.errorf("Failed to attach agent to running JVM.",var8);
            LogUtil.printStackTrace(var8);
        }finally {
            if (vm != null) {
                try {
                    vm.detach();
                } catch (Throwable ignored) {
                }
            }
            LogUtil.log("Agent attach finished.");
            LogUtil.flush();
        }
    }
}

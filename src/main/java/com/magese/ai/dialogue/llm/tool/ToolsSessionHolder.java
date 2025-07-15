package com.magese.ai.dialogue.llm.tool;

import com.magese.ai.communication.common.ChatSession;
import com.magese.ai.entity.SysDevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 与session绑定的functionTools
 */
@Slf4j
public class ToolsSessionHolder {

    private static final String TAG = "FUNCTION_SESSION";

    private final Map<String, ToolCallback> functionRegistry = new HashMap<>();
    private final String sessionId;
    private final ToolsGlobalRegistry globalFunctionRegistry;
    private final SysDevice sysDevice;

    public ToolsSessionHolder(String sessionId, SysDevice sysDevice, ToolsGlobalRegistry globalFunctionRegistry) {
        this.sessionId = sessionId;
        this.sysDevice = sysDevice;
        this.globalFunctionRegistry = globalFunctionRegistry;
    }

    /**
     * Register a global function by name
     *
     * @param name the name of the function to register
     */
    public void registerFunction(String name) {
        // Look up the function in the globalFunctionRegistry
        ToolCallback func = globalFunctionRegistry.resolve(name);
        if (func == null) {
            log.error("[{}] - SessionId:{} Function:{} not found in globalFunctionRegistry", TAG, sessionId, name);
            return;
        }
        functionRegistry.put(name, func);
        log.debug("[{}] - SessionId:{} Function:{} registered from global successfully", TAG, sessionId, name);
    }

    /**
     * Register a function by name
     *
     * @param name the name of the function to register
     */
    public void registerFunction(String name, ToolCallback functionCallTool) {
        functionRegistry.put(name, functionCallTool);
    }

    /**
     * Unregister a function by name
     *
     * @param name the name of the function to unregister
     * @return true if successful, false otherwise
     */
    public boolean unregisterFunction(String name) {
        // Check if the function exists before unregistering
        if (!functionRegistry.containsKey(name)) {
            log.error("[{}] - SessionId:{} Function:{} not found", TAG, sessionId, name);
            return false;
        }
        functionRegistry.remove(name);
        log.info("[{}] - SessionId:{} Function:{} unregistered successfully", TAG, sessionId, name);
        return true;
    }

    /**
     * Get a function by name
     *
     * @param name the name of the function to retrieve
     * @return the function or null if not found
     */
    public ToolCallback getFunction(String name) {
        return functionRegistry.get(name);
    }

    /**
     * Get all registered functions
     *
     * @return a list of all registered functions
     */
    public List<ToolCallback> getAllFunction() {
        return functionRegistry.values().stream().toList();
    }

    /**
     * Get all registered functions name
     *
     * @return a list of all registered function name
     */
    public List<String> getAllFunctionName() {
        return new ArrayList<>(functionRegistry.keySet());
    }

    /**
     * 注册全局函数到FunctionHolder
     */
    public void registerGlobalFunctionTools(ChatSession chatSession) {
        String functionNames = sysDevice == null ? null : sysDevice.getFunctionNames();
        if (functionNames != null && !functionNames.isEmpty()) {//如果指定了function配置，则只加载指定的
            String[] functionNameArr = functionNames.split(",");
            for (String functionName : functionNameArr) {
                registerFunction(functionName);
            }
        } else {//否则加载所有的全局function
            globalFunctionRegistry.getAllFunctions(chatSession).forEach(this::registerFunction);
        }
    }
}

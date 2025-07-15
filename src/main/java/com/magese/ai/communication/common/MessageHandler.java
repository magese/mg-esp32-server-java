package com.magese.ai.communication.common;

import com.magese.ai.communication.domain.*;
import com.magese.ai.dialogue.llm.factory.ChatModelFactory;
import com.magese.ai.dialogue.llm.memory.Conversation;
import com.magese.ai.dialogue.llm.memory.ConversationFactory;
import com.magese.ai.dialogue.llm.tool.ToolsGlobalRegistry;
import com.magese.ai.dialogue.llm.tool.ToolsSessionHolder;
import com.magese.ai.dialogue.service.AudioService;
import com.magese.ai.dialogue.service.DialogueService;
import com.magese.ai.dialogue.service.IotService;
import com.magese.ai.dialogue.service.VadService;
import com.magese.ai.dialogue.stt.factory.SttServiceFactory;
import com.magese.ai.dialogue.tts.factory.TtsServiceFactory;
import com.magese.ai.entity.SysConfig;
import com.magese.ai.entity.SysDevice;
import com.magese.ai.entity.SysRole;
import com.magese.ai.enums.ListenState;
import com.magese.ai.service.SysConfigService;
import com.magese.ai.service.SysDeviceService;
import com.magese.ai.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageHandler {

    private final SysDeviceService deviceService;
    private final AudioService audioService;
    private final TtsServiceFactory ttsService;
    private final VadService vadService;
    private final SessionManager sessionManager;
    private final SysConfigService configService;
    private final DialogueService dialogueService;
    private final IotService iotService;
    private final TtsServiceFactory ttsFactory;
    private final SttServiceFactory sttFactory;
    private final ConversationFactory conversationFactory;
    private final ChatModelFactory chatModelFactory;
    private final ToolsGlobalRegistry toolsGlobalRegistry;
    private final SysRoleService roleService;

    // 用于存储设备ID和验证码生成状态的映射
    private final Map<String, Boolean> captchaGenerationInProgress = new ConcurrentHashMap<>();

    /**
     * 处理连接建立事件.
     */
    public void afterConnection(ChatSession chatSession, String deviceIdAuth) {
        String sessionId = chatSession.getSessionId();
        // 注册会话
        sessionManager.registerSession(sessionId, chatSession);

        log.info("开始查询设备信息 - DeviceId: {}", deviceIdAuth);
        SysDevice device = Optional.ofNullable(deviceService.selectDeviceById(deviceIdAuth)).orElse(new SysDevice());
        device.setDeviceId(deviceIdAuth);
        device.setSessionId(sessionId);
        sessionManager.registerDevice(sessionId, device);
        // 如果已绑定，则初始化其他内容
        if (!ObjectUtils.isEmpty(device) && device.getRoleId() != null) {
            //这里需要放在虚拟线程外
            ToolsSessionHolder toolsSessionHolder = new ToolsSessionHolder(chatSession.getSessionId(),
                    device, toolsGlobalRegistry);
            chatSession.setFunctionSessionHolder(toolsSessionHolder);
            //以上同步处理结束后，再启动虚拟线程进行设备初始化，确保chatSession中已设置的sysDevice信息
            Thread.startVirtualThread(() -> {
                try {
                    // 从数据库获取角色描述。device.getRoleId()表示当前设备的当前活跃角色，或者上次退出时的活跃角色。
                    SysRole role = roleService.selectRoleById(device.getRoleId());

                    if (role.getSttId() != null) {
                        SysConfig sttConfig = configService.selectConfigById(role.getSttId());
                        if (sttConfig != null) {
                            sttFactory.getSttService(sttConfig);// 提前初始化，加速后续使用
                        }
                    }
                    if (role.getTtsId() != null) {
                        SysConfig ttsConfig = configService.selectConfigById(role.getTtsId());
                        if (ttsConfig != null) {
                            ttsFactory.getTtsService(ttsConfig, role.getVoiceName());// 提前初始化，加速后续使用
                        }
                    }
                    if (role.getModelId() != null) {
                        chatModelFactory.takeChatModel(chatSession);// 提前初始化，加速后续使用
                        Conversation conversation = conversationFactory.initConversation(device, role, sessionId);
                        chatSession.setConversation(conversation);
                        // 注册全局函数
                        toolsSessionHolder.registerGlobalFunctionTools(chatSession);
                    }

                    // 更新设备状态
                    deviceService.update(new SysDevice()
                            .setDeviceId(device.getDeviceId())
                            .setState(SysDevice.DEVICE_STATE_ONLINE)
                            .setLastLogin(new Date().toString()));

                } catch (Exception e) {
                    log.error("设备初始化失败 - DeviceId: {}", deviceIdAuth, e);
                    try {
                        sessionManager.closeSession(sessionId);
                    } catch (Exception ex) {
                        log.error("关闭WebSocket连接失败", ex);
                    }
                }
            });
        }
    }

    /**
     * 处理连接关闭事件.
     */
    public void afterConnectionClosed(String sessionId) {
        ChatSession chatSession = sessionManager.getSession(sessionId);
        if (chatSession == null || !chatSession.isOpen()) {
            return;
        }
        // 连接关闭时清理资源
        SysDevice device = sessionManager.getDeviceConfig(sessionId);
        if (device != null) {
            Thread.startVirtualThread(() -> {
                try {
                    deviceService.update(new SysDevice()
                            .setDeviceId(device.getDeviceId())
                            .setState(SysDevice.DEVICE_STATE_OFFLINE)
                            .setLastLogin(new Date().toString()));
                    log.info("WebSocket连接关闭 - SessionId: {}, DeviceId: {}", sessionId, device.getDeviceId());
                } catch (Exception e) {
                    log.error("更新设备状态失败", e);
                }
            });
        }
        // 清理会话
        sessionManager.closeSession(sessionId);
        // 清理VAD会话
        vadService.resetSession(sessionId);
        // 清理音频处理会话
        audioService.cleanupSession(sessionId);
        // 清理对话
        dialogueService.cleanupSession(sessionId);
        // 清理Conversation缓存的对话历史。
        Conversation conversation = chatSession.getConversation();
        if (conversation != null) {
            conversation.clear();
        }
    }

    /**
     * 处理音频数据
     */
    public void handleBinaryMessage(String sessionId, byte[] opusData) {
        ChatSession chatSession = sessionManager.getSession(sessionId);
        if ((chatSession == null || !chatSession.isOpen()) && vadService.isSessionNotInitialized(sessionId)) {
            return;
        }
        // 委托给DialogueService处理音频数据
        dialogueService.processAudioData(chatSession, opusData);

    }

    public void handleUnboundDevice(String sessionId, SysDevice device) {
        if (device == null || device.getDeviceId() == null) {
            log.error("设备或设备ID为空，无法处理未绑定设备");
            return;
        }
        String deviceId = device.getDeviceId();
        ChatSession chatSession = sessionManager.getSession(sessionId);
        if (chatSession == null || !chatSession.isOpen()) {
            return;
        }
        // 检查是否已经在处理中，使用CAS操作保证线程安全
        Boolean previous = captchaGenerationInProgress.putIfAbsent(deviceId, true);
        if (previous != null && previous) {
            return; // 已经在处理中
        }

        Thread.startVirtualThread(() -> {
            try {
                // 设备已注册但未配置模型
                if (device.getDeviceName() != null && device.getRoleId() == null) {
                    String message = "设备未配置角色，请到角色配置页面完成配置后开始对话";

                    String audioFilePath = ttsService.getDefaultTtsService().textToSpeech(message);
                    audioService.sendAudioMessage(chatSession, new DialogueService.Sentence(message, audioFilePath), true,
                            true);

                    // 延迟一段时间后再解除标记
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    captchaGenerationInProgress.remove(deviceId);
                    return;
                }

                // 设备未命名，生成验证码
                // 生成新验证码
                SysDevice codeResult = deviceService.generateCode(device);
                String audioFilePath;
                if (!StringUtils.hasText(codeResult.getAudioPath())) {
                    String codeMessage = "请到设备管理页面添加设备，输入验证码" + codeResult.getCode();
                    audioFilePath = ttsService.getDefaultTtsService().textToSpeech(codeMessage);
                    codeResult.setDeviceId(deviceId);
                    codeResult.setSessionId(sessionId);
                    codeResult.setAudioPath(audioFilePath);
                    deviceService.updateCode(codeResult);
                }

                audioService.sendAudioMessage(chatSession,
                        new DialogueService.Sentence(codeResult.getCode(), codeResult.getAudioPath()), true, true);

                // 延迟一段时间后再解除标记
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                captchaGenerationInProgress.remove(deviceId);

            } catch (Exception e) {
                log.error("处理未绑定设备失败", e);
                captchaGenerationInProgress.remove(deviceId);
            }
        });
    }

    private void handleListenMessage(ChatSession chatSession, ListenMessage message) {
        String sessionId = chatSession.getSessionId();
        log.info("收到listen消息 - SessionId: {}, State: {}, Mode: {}", sessionId, message.getState(), message.getMode());
        chatSession.setMode(message.getMode());

        // 根据state处理不同的监听状态
        switch (message.getState()) {
            case ListenState.Start:
                // 开始监听，准备接收音频数据
                log.info("开始监听 - Mode: {}", message.getMode());

                // 初始化VAD会话
                vadService.initSession(sessionId);
                break;

            case ListenState.Stop:
                // 停止监听
                log.info("停止监听");

                // 关闭音频流
                sessionManager.completeAudioStream(sessionId);
                sessionManager.closeAudioStream(sessionId);
                sessionManager.setStreamingState(sessionId, false);
                // 重置VAD会话
                vadService.resetSession(sessionId);
                break;

            case ListenState.Text:
                // 检测聊天文本输入
                if (audioService.isPlaying(sessionId)) {
                    dialogueService.abortDialogue(chatSession, message.getMode().getValue());
                }
                dialogueService.handleText(chatSession, message.getText(), null);
                break;

            case ListenState.Detect:
                // 检测到唤醒词
                dialogueService.handleWakeWord(chatSession, message.getText());
                break;

            default:
                log.warn("未知的listen状态: {}", message.getState());
        }
    }

    private void handleAbortMessage(ChatSession session, AbortMessage message) {
        dialogueService.abortDialogue(session, message.getReason());
    }

    private void handleIotMessage(ChatSession chatSession, IotMessage message) {
        String sessionId = chatSession.getSessionId();
        log.info("收到IoT消息 - SessionId: {}", sessionId);

        // 处理设备描述信息
        if (message.getDescriptors() != null) {
            log.info("收到设备描述信息: {}", message.getDescriptors());
            // 处理设备描述信息的逻辑
            iotService.handleDeviceDescriptors(sessionId, message.getDescriptors());
        }

        // 处理设备状态更新
        if (message.getStates() != null) {
            log.info("收到设备状态更新: {}", message.getStates());
            // 处理设备状态更新的逻辑
            iotService.handleDeviceStates(sessionId, message.getStates());
        }
    }

    private void handleGoodbyeMessage(ChatSession session) {
        sessionManager.closeSession(session);
    }

    private void handleDeviceMcpMessage(ChatSession chatSession, DeviceMcpMessage message) {
        Long mcpRequestId = message.getPayload().getId();
        CompletableFuture<DeviceMcpMessage> future = chatSession.getDeviceMcpHolder().getMcpPendingRequests().get(mcpRequestId);
        if (future != null) {
            future.complete(message);
            chatSession.getDeviceMcpHolder().getMcpPendingRequests().remove(mcpRequestId);
        }
    }

    public void handleMessage(Message msg, String sessionId) {
        var chatSession = sessionManager.getSession(sessionId);
        switch (msg) {
            case ListenMessage m -> handleListenMessage(chatSession, m);
            case IotMessage m -> handleIotMessage(chatSession, m);
            case AbortMessage m -> handleAbortMessage(chatSession, m);
            case GoodbyeMessage ignored -> handleGoodbyeMessage(chatSession);
            case DeviceMcpMessage m -> handleDeviceMcpMessage(chatSession, m);
            default -> {
            }
        }
    }
}

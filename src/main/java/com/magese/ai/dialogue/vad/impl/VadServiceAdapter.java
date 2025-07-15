package com.magese.ai.dialogue.vad.impl;

import com.magese.ai.dialogue.service.VadService;
import com.magese.ai.dialogue.service.VadService.VadResult;
import com.magese.ai.dialogue.vad.VadDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * VadDetector接口的适配器，连接到新的VadService实现
 * 这个适配器是为了保持向后兼容性，同时使用新的VadService架构
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class VadServiceAdapter implements VadDetector {

    private final VadService vadService;

    @Override
    public byte[] processAudio(String sessionId, byte[] pcmData) {
        try {
            // 调用VadService处理音频并获取VadResult
            VadResult result = vadService.processAudio(sessionId, pcmData);

            // 如果结果为null或处理出错，返回原始数据
            if (result == null || result.getProcessedData() == null) {
                return pcmData;
            }

            // 返回处理后的音频数据
            return result.getProcessedData();
        } catch (Exception e) {
            // 发生异常时返回原始数据
            return pcmData;
        }
    }

    @Override
    public void resetSession(String sessionId) {
        vadService.resetSession(sessionId);
    }

    @Override
    public boolean isSpeaking(String sessionId) {
        return vadService.isSpeaking(sessionId);
    }

    @Override
    public float getSpeechProbability(String sessionId) {
        return vadService.getSpeechProbability(sessionId);
    }
}

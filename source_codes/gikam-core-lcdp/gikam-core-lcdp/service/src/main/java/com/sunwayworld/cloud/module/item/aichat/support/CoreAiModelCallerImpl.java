package com.sunwayworld.cloud.module.item.aichat.support;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.http.HttpClientManager;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.FileUtils;
import com.sunwayworld.framework.utils.OkHttpClientUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.item.aichat.bean.CoreAiChatBean;
import com.sunwayworld.module.item.aichat.bean.CoreAiChatMessageBean;
import com.sunwayworld.module.item.aichat.bean.CoreAiChatRequestDTO;
import com.sunwayworld.module.item.aichat.bean.CoreAiModelInvokeResultDTO;
import com.sunwayworld.module.item.aichat.service.CoreAiModelCaller;
import com.sunwayworld.module.sys.agentconfig.bean.CoreAgentConfigBean;
import com.sunwayworld.module.sys.agentconfig.service.CoreAgentConfigService;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Repository
@GikamBean
public class CoreAiModelCallerImpl implements CoreAiModelCaller {

    private static final Logger logger = LogManager.getLogger(CoreAiModelCallerImpl.class);

    /**
     * @param input
     * @param fileList
     * @param chat
     * @param chatMessageList
     * @param emitter
     * @param aiChatRequestDTO
     * @return
     */
    @Override
    public CoreAiModelInvokeResultDTO callModelStream(String input, List<File> fileList, CoreAiChatBean chat, List<CoreAiChatMessageBean> chatMessageList, SseEmitter emitter, CoreAiChatRequestDTO aiChatRequestDTO) {
        return callSunwayLinkModelStream(input, fileList, chat, chatMessageList, emitter, aiChatRequestDTO);
    }

    @Override
    public String auditOcr(File auditOcrFile, Long appId) {
        return auditOcrBySunwayLink(auditOcrFile, appId);
    }


    //---------------------------------------------------------------------------------
    // 私有方法
    //---------------------------------------------------------------------------------

    private String auditOcrBySunwayLink(File auditOcrFile, Long appId) {

        CoreAgentConfigBean agentConfig = getAgentConfigById(appId);

        String audioOcrUrl = agentConfig.getSunwayLinkUrl() + "/v1/audio-to-text";
        String appKey = agentConfig.getAppSecret();


        Consumer<HttpPost> httpPostConsumer = (HttpPost httpPost) -> {

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            // 添加表单字段
            builder.addTextBody("user", LocalContextHelper.getLoginUserId());
            builder.addPart("file", new FileBody(auditOcrFile, ContentType.create("audio/wave")));  // 添加文件部分


            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            httpPost.addHeader("Authorization", "Bearer " + appKey);


        };

        logger.info("请求语音识别接口，请求url:" + audioOcrUrl + ", appKey:" + appKey);
        logger.info("请求语音识别接口，请求报文: user:" + LocalContextHelper.getLoginUserId() + ", file:" + auditOcrFile.getName());

        String auditOcrResult = HttpClientManager.getInstance().sendHttpPost(audioOcrUrl, "{}", httpPostConsumer);

        logger.info("请求语音识别接口，返回报文:" + auditOcrResult);

        JSONObject auditOcrResultObject = JSONObject.parseObject(auditOcrResult);

        if (!StringUtils.isEmpty(auditOcrResultObject.getString("text"))) {
            return auditOcrResultObject.getString("text");
        }


        return "";
    }


    private CoreAiModelInvokeResultDTO callSunwayLinkModelStream(String input, List<File> fileList, CoreAiChatBean chat, List<CoreAiChatMessageBean> chatMessageList, SseEmitter emitter, CoreAiChatRequestDTO aiChatRequestDTO) {
        try {
            // 构造监听器
            SunwayLinkSseListener sunwayLinkSseListener = new SunwayLinkSseListener() {
                @Override
                protected void send() {
                    try {
                        emitter.send(SseEmitter.event().data(this.getCurrStr()));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                }
            };


            sunwayLinkSseListener.setLoginUser(LocalContextHelper.getLoginPrincipal());


            // 调用SunwayLink获取数据
            invokeSunwayLinkInterface(input, fileList, chat, chatMessageList, sunwayLinkSseListener, aiChatRequestDTO);

            sunwayLinkSseListener.getCountDownLatch().await();

            emitter.complete();

            return new CoreAiModelInvokeResultDTO(sunwayLinkSseListener.getOutput().toString(), sunwayLinkSseListener.getConversationId(), sunwayLinkSseListener.getBody());

        } catch (Exception e) {
            emitter.completeWithError(e);
        }


        return new CoreAiModelInvokeResultDTO();


    }


    private void invokeSunwayLinkInterface(String input, List<File> fileList, CoreAiChatBean chat, List<CoreAiChatMessageBean> chatMessageList, SunwayLinkSseListener sunwayLinkSseListener, CoreAiChatRequestDTO aiChatRequestDTO) {

        CoreAgentConfigBean agentConfig = getAgentConfigById(aiChatRequestDTO.getAppId());

        JSONArray inputFiles = uploadFileToSunwayLink(fileList, agentConfig.getSunwayLinkUrl(), agentConfig.getAppSecret());

        String chatUrl = agentConfig.getSunwayLinkUrl() + "/v1/chat-messages";

        String appKey = agentConfig.getAppSecret();


        JSONArray historyMessageList = convertToHistoryMessageList(chatMessageList);


        String requestBody = buildRequestBody(input, inputFiles, chat.getModelConversationId(), historyMessageList, aiChatRequestDTO);

        Map<String, String> headers = new HashMap<>();

        // 添加键值对到 Map 中
        headers.put("Authorization", "Bearer " + appKey);
        headers.put("Content-Type", "application/json");
        if (!StringUtils.isEmpty(agentConfig.getSystemId())) {
            headers.put("system", agentConfig.getSystemId());
        }

        // 增加日志打印请求URL和请求头
        logger.info("请求SunwayLink接口，请求URL: " + chatUrl);
        logger.info("请求SunwayLink接口，请求头: " + headers);
        logger.info("请求SunwayLink接口，请求报文:" + requestBody);


        OkHttpClientUtils.sendHttpPostStream(chatUrl, requestBody, headers, sunwayLinkSseListener);


    }

    private CoreAgentConfigBean getAgentConfigById(Long appId) {
        return ApplicationContextHelper.getBean(CoreAgentConfigService.class).selectById(appId);
    }

    private JSONArray convertToHistoryMessageList(List<CoreAiChatMessageBean> chatMessageList) {

        JSONArray historyMessageList = new JSONArray();

        for (CoreAiChatMessageBean coreAiChatMessageBean : chatMessageList) {
            JSONObject messageObject = new JSONObject();

            messageObject.put("role", coreAiChatMessageBean.getMessageRole());
            messageObject.put("text", coreAiChatMessageBean.getBody());

            historyMessageList.add(messageObject);
        }

        return historyMessageList;


    }

    private JSONArray uploadFileToSunwayLink(List<File> fileList, String sunwayLinkUrl, String appSecret) {

        String fileUploadUrl = sunwayLinkUrl + "/v1/files/upload";

        JSONArray filesArray = new JSONArray();

        for (File file : fileList) {

            Consumer<HttpPost> httpPostConsumer = (HttpPost httpPost) -> {

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();

                // 设置字符集为 UTF-8
                builder.setCharset(StandardCharsets.UTF_8);
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                // 添加表单字段
                builder.addTextBody("user", LocalContextHelper.getLoginUserId());

                builder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());

                HttpEntity multipart = builder.build();
                httpPost.setEntity(multipart);

                httpPost.addHeader("Authorization", "Bearer " + appSecret);


            };

            String fileUploadResult = HttpClientManager.getInstance().sendHttpPost(fileUploadUrl, "{}", httpPostConsumer);

            JSONObject fileUploadResultObject = JSONObject.parseObject(fileUploadResult);

            JSONObject fileObject = new JSONObject();
            fileObject.put("type", FileUtils.isImage(file) ? "image" : "document");
            fileObject.put("transfer_method", "local_file");
            fileObject.put("upload_file_id", fileUploadResultObject.getString("id"));

            // 将文件对象添加到 files 数组
            filesArray.add(fileObject);

        }

        return filesArray;


    }

    private String buildRequestBody(String input, JSONArray inputFiles, String modelConversationId, JSONArray historyMessageList, CoreAiChatRequestDTO aiChatRequestDTO) {
        // 创建 JSONObject 对象
        JSONObject jsonObject = new JSONObject();

        // 添加 query 字段
        jsonObject.put("query", input);

        // 添加 response_mode 字段
        jsonObject.put("response_mode", "streaming");

        // 添加 user 字段
        jsonObject.put("user", LocalContextHelper.getLoginUserId());

        // 添加 auto_generate_name 字段
        jsonObject.put("auto_generate_name", true);

        // 创建 inputs 对象
        JSONObject inputsObject = new JSONObject();

        inputsObject.put("lang", getLang());

        // 添加 inputs 字段
        jsonObject.put("inputs", inputsObject);

        jsonObject.put("files", inputFiles);
        jsonObject.put("conversation_id", StringUtils.isEmpty(modelConversationId) ? "" : modelConversationId);

        List<String> datasetIdList = new ArrayList<>();

        String tempDatasetId = ApplicationContextHelper.getConstantValue("TEMP_DATASET_ID");

        if (!StringUtils.isEmpty(tempDatasetId)) {
            datasetIdList.add(tempDatasetId);
        }

        if (!CollectionUtils.isEmpty(aiChatRequestDTO.getDatasetIdList())) {
            datasetIdList.addAll(aiChatRequestDTO.getDatasetIdList());
        }

        if (!datasetIdList.isEmpty()) {
            jsonObject.put("dataset_ids", datasetIdList);
        }

        return jsonObject.toJSONString();
    }

    private String getLang() {

        String lang = "zh-cn";

        if (I18nHelper.getLocal() != null && !StringUtils.isBlank(I18nHelper.getLang(I18nHelper.getLocal()))) {
            lang = I18nHelper.getLang(I18nHelper.getLocal());
        }

        return lang;

    }

}

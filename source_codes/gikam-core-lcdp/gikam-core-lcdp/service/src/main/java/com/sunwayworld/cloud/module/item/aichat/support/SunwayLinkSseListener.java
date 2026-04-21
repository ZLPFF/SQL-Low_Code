package com.sunwayworld.cloud.module.item.aichat.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.github.javaparser.utils.StringEscapeUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.security.bean.LoginUser;
import com.sunwayworld.framework.utils.MapUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.item.dataset.bean.CoreDatasetDocumentBean;
import com.sunwayworld.module.item.dataset.service.CoreDatasetDocumentService;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public abstract class SunwayLinkSseListener extends EventSourceListener {

    private static Logger logger = LogManager.getLogger(SunwayLinkSseListener.class);


    protected abstract void send();

    /**
     * 最终的消息输出
     */

    private StringBuilder output = new StringBuilder();


    private String body = "";


    private LoginUser loginUser;


    private StringBuilder historyMsg = new StringBuilder();


    private String conversationId = "";

    // 引用文件
    private String referenceFile = "";


    /**
     * 流式输出，当前消息的内容(回答消息、函数参数)
     */

    private String currStr = "";


    private CountDownLatch countDownLatch = new CountDownLatch(1);


    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        try {
            logger.error("SunwayLink接口调用失败，返回报文：" + StringEscapeUtils.unescapeJava(response.body().string()));
        } catch (IOException e) {
            /* ignore */
        }

        countDownLatch.countDown();
    }

    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        if (StringUtils.isEmpty(data)) {
            return;
        }

        logger.info("SunwayLink接口 SSE的返回报文：" + data);

        JSONObject message = JSONObject.parseObject(data);

        if ("workflow_finished".equals(message.getString("event"))) {

            if (!StringUtils.isEmpty(referenceFile)) {
                currStr = referenceFile;
                this.send();
                currStr = "";
            }


            if (message.getJSONObject("data").getJSONObject("outputs") != null) {
                body = message.getJSONObject("data").getJSONObject("outputs").getString("answer");
                if (!StringUtils.isEmpty(referenceFile)) {
                    body = body + referenceFile;
                }
            }

            conversationId = message.getString("conversation_id");
        }

        if ("node_started".equals(message.getString("event")) && StringUtils.isNotEmpty(message.getJSONObject("data").getString("title"))) {

            //if (!StringUtils.equals("answer", message.getJSONObject("data").getString("node_type")) && !StringUtils.equals("end", message.getJSONObject("data").getString("node_type"))) {

            currStr = "[[NODE_START]]【" + message.getJSONObject("data").getString("title") + "】开始执行...[[NODE_END]]";

            this.send();

            currStr = "";
            //}


        }


        if ("node_finished".equals(message.getString("event"))) {

            //if (!StringUtils.equals("answer", message.getJSONObject("data").getString("node_type")) && !StringUtils.equals("end", message.getJSONObject("data").getString("node_type"))) {

            currStr = "[[NODE_START]]【" + message.getJSONObject("data").getString("title") + "】执行完成[[NODE_END]]";

            this.send();

            currStr = "";
            //}

//            if (message.getJSONObject("data").getJSONObject("outputs").getJSONArray("result") != null) {
//                // 引用文件
//                JSONArray metadatas = message.getJSONObject("data").getJSONObject("outputs").getJSONArray("result");
//
//                List<Map<String, String>> referenceDatasetList = new ArrayList<>();
//                Set<String> documentIdSet = new HashSet<>();
//                Set<String> datasetIdSet = new HashSet<>();
//
//                for (int i = 0; i < metadatas.size(); i++) {
//                    JSONObject referenceDocument = metadatas.getJSONObject(i).getJSONObject("metadata");
//
//                    String documentId = referenceDocument.getString("document_id");
//                    String datasetId = referenceDocument.getString("dataset_id");
//
//                    if (!documentIdSet.contains(documentId)) {
//                        documentIdSet.add(documentId);
//                    }
//
//                    if (!datasetIdSet.contains(datasetId)) {
//
//                        Map<String, String> referenceDataset = new HashMap<>();
//
//                        referenceDataset.put("datasetId", datasetId);
//                        referenceDataset.put("datasetName", referenceDocument.getString("dataset_name"));
//
//                        referenceDatasetList.add(referenceDataset);
//
//                        datasetIdSet.add(datasetId);
//                    }
//
//
//                }
//
//
//                if (!documentIdSet.isEmpty()) {
//
//                    List<CoreDatasetDocumentBean> datasetDocumentList = ApplicationContextHelper.getBean(CoreDatasetDocumentService.class).selectListByIds(new ArrayList<>(documentIdSet));
//
//                    referenceFile = buildComponent(datasetDocumentList, referenceDatasetList);
//
//                }
//
//
//            }
        }


        if ("message_replace".equals(message.getString("event"))) {
            body = message.getString("answer");
        }


        if ("message_end".equals(message.getString("event"))) {
            if (message.getJSONObject("metadata").getJSONArray("retriever_resources") != null) {
                // 引用文件
                JSONArray referenceDocuments = message.getJSONObject("metadata").getJSONArray("retriever_resources");

                List<JSONObject> mergedDocumentList = parseAndMergeRetrieverResources(referenceDocuments);

                List<Map<String, String>> referenceDatasetList = new ArrayList<>();
                Set<String> documentIdSet = new HashSet<>();
                Set<String> datasetIdSet = new HashSet<>();

                for (int i = 0; i < mergedDocumentList.size(); i++) {
                    JSONObject referenceDocument = mergedDocumentList.get(i);

                    String documentId = referenceDocument.getString("document_id");
                    String datasetId = referenceDocument.getString("dataset_id");

                    if (!documentIdSet.contains(documentId)) {
                        documentIdSet.add(documentId);
                    }

                    if (!datasetIdSet.contains(datasetId)) {

                        Map<String, String> referenceDataset = new HashMap<>();

                        referenceDataset.put("datasetId", datasetId);
                        referenceDataset.put("datasetName", referenceDocument.getString("dataset_name"));

                        referenceDatasetList.add(referenceDataset);

                        datasetIdSet.add(datasetId);
                    }


                }


                if (!documentIdSet.isEmpty()) {

                    List<CoreDatasetDocumentBean> datasetDocumentList = ApplicationContextHelper.getBean(CoreDatasetDocumentService.class).getDao().selectListByIds(new ArrayList<>(documentIdSet), Arrays.asList("ID", "URL"));

                    Map<String, String> documentUrlMap = datasetDocumentList.stream()
                            .collect(Collectors.toMap(
                                    CoreDatasetDocumentBean::getId,
                                    CoreDatasetDocumentBean::getUrl,
                                    (existing, replacement) -> existing
                            ));

                    // 使用 Map 批量更新 mergedDocumentList 中的 URL 信息
                    for (JSONObject document : mergedDocumentList) {
                        String url = documentUrlMap.get(document.getString("document_id"));
                        document.put("url", url);
                    }


                    currStr = buildComponent(mergedDocumentList, referenceDatasetList);

                    output.append(currStr);

                    this.send();

                    body = body + currStr;

                    currStr = "";

                    conversationId = message.getString("conversation_id");
                    eventSource.cancel();
                    return;


                }


            }
        }


        if ("message".equals(message.getString("event"))) {

            String answer = message.getString("answer");

            historyMsg.append(answer);

            if (StringUtils.contains(historyMsg.toString(), "[[COMPONENT:")) {
                currStr = currStr + answer;

                if (StringUtils.contains(historyMsg.toString(), "[[COMPONENT:END]]")) {

                    // 解析如下字符串
                    // [[COMPONENT]][[COMPONENT:START]][{\"url\":\"DailyManage.Expense.server.LcdpCrmExpenseService.selectExpensedProjData\",\"data\":\"{\"p\":{\"expensetype\":\"商机\",\"projectname\":\"7823\"}}\"}]}}][[COMPONENT:END]]

                    List<String> componentList = StringUtils.substringsBetween(historyMsg.toString(), "[[COMPONENT:START]]", "[[COMPONENT:END]]");

                    if (!componentList.isEmpty()) {

                        String components = componentList.get(0);

                        JSONArray componentArray = (JSONArray) JSONArray.parse(components, Feature.OrderedField);

                        for (int i = 0; i < componentArray.size(); i++) {

                            String rawConponentConfigStr = componentArray.getString(i);

                            JSONObject componentConfig = JSON.parseObject(rawConponentConfigStr);

                            if (StringUtils.equals("gridFilter", componentConfig.getString("type"))) {

                                String requestJson = componentConfig.getString("data");


                                RestJsonWrapperBean wrapper = RestJsonWrapperBean.resolveArgument(requestJson);

                                LocalContextHelper.setUserLogin(loginUser);

                                String newComponentConfig = LcdpScriptUtils.callScriptMethod(componentConfig.getString("url"), wrapper);


                                currStr = StringUtils.replace(currStr, rawConponentConfigStr, newComponentConfig);
                            }


                        }


                    }

                    output.append(currStr);
                    this.send();
                    historyMsg = new StringBuilder();
                    currStr = "";
                }


            } else {
                currStr = message.getString("answer");

                output.append(currStr);

                this.send();

                currStr = "";
            }


        }

    }

    /**
     * 解析并按documentId合并retriever_resources
     *
     * @param retrieverResources 包含retriever_resources的JSONArray
     * @return 按documentId合并后的List<JSONObject>
     */
    private List<JSONObject> parseAndMergeRetrieverResources(JSONArray retrieverResources) {
        Map<String, JSONObject> groupedByDocumentId = new HashMap<>();

        for (int i = 0; i < retrieverResources.size(); i++) {
            JSONObject resource = retrieverResources.getJSONObject(i);

            String documentId = resource.getString("document_id");

            // 如果该documentId还未存在，则创建新的JSONObject
            if (!groupedByDocumentId.containsKey(documentId)) {
                JSONObject docData = new JSONObject();
                // 添加基础信息
                docData.put("dataset_id", resource.getString("dataset_id"));
                docData.put("dataset_name", resource.getString("dataset_name"));
                docData.put("document_id", documentId);
                docData.put("document_name", resource.getString("document_name"));

                // 初始化segments数组
                JSONArray segments = new JSONArray();
                JSONObject segmentData = createSegmentData(resource);
                segments.add(segmentData);
                docData.put("segments", segments);

                groupedByDocumentId.put(documentId, docData);
            } else {
                // 如果该documentId已存在，则将新数据添加到segments数组中
                JSONObject existingData = groupedByDocumentId.get(documentId);
                JSONArray segments = existingData.getJSONArray("segments");
                JSONObject segmentData = createSegmentData(resource);
                segments.add(segmentData);
            }
        }

        // 转换为List返回
        return new ArrayList<>(groupedByDocumentId.values());
    }

    /**
     * 创建段落数据，去除基础字段
     *
     * @param resource 原始资源数据
     * @return 不包含基础字段的段落数据
     */
    private JSONObject createSegmentData(JSONObject resource) {
        JSONObject segment = new JSONObject();

        // 复制除基础字段外的所有字段
        for (String key : resource.keySet()) {
            if (!"dataset_id".equals(key) &&
                    !"dataset_name".equals(key) &&
                    !"document_id".equals(key) &&
                    !"document_name".equals(key)) {
                segment.put(key, resource.get(key));
            }
        }

        return segment;
    }


    private <T> String buildComponent(List<T> datasetDocumentList, List<Map<String, String>> referenceDatasetList) {
        StringBuilder componentsBuilder = new StringBuilder();

        componentsBuilder.append("[[COMPONENT]]");

        componentsBuilder.append("[[COMPONENT:START]]");

        JSONArray components = new JSONArray();

        JSONObject linkFileComponent = new JSONObject();

        linkFileComponent.put("type", "linkFiles");
        linkFileComponent.put("items", datasetDocumentList);

        components.add(linkFileComponent);

        JSONObject forwardComponent = new JSONObject();

        List<JSONObject> items = referenceDatasetList.stream().map(e -> {

            JSONObject item = new JSONObject();

            item.put("value", MapUtils.getString(e, "datasetId"));
            item.put("text", MapUtils.getString(e, "datasetName"));

            return item;
        }).collect(Collectors.toList());

        forwardComponent.put("type", "forwardDataset");
        forwardComponent.put("items", items);

        components.add(forwardComponent);

        componentsBuilder.append(components.toJSONString());

        componentsBuilder.append("[[COMPONENT:END]]");

        return componentsBuilder.toString();
    }


    @Override
    public void onClosed(EventSource eventSource) {
        countDownLatch.countDown();
        countDownLatch = new CountDownLatch(1);

    }

    public StringBuilder getOutput() {
        return output;
    }

    public void setOutput(StringBuilder output) {
        this.output = output;
    }

    public String getCurrStr() {
        return currStr;
    }

    public void setCurrStr(String currStr) {
        this.currStr = currStr;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public LoginUser getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(LoginUser loginUser) {
        this.loginUser = loginUser;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

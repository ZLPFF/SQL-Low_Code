package com.sunwayworld.cloud.module.lcdp.checkoutrecord.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.checkoutconfig.bean.LcdpCheckoutConfigBean;
import com.sunwayworld.cloud.module.lcdp.checkoutconfig.service.LcdpCheckoutConfigService;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckOutDTO;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckoutDetailDTO;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCrdLogBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.persistent.dao.LcdpCheckoutRecordDao;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.service.LcdpCheckoutRecordService;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.service.LcdpCrdLogService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.database.context.instance.EntityHelper;
import com.sunwayworld.framework.exception.UnexpectedException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.mybatis.page.PageRowBounds;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.ServletUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import com.sunwayworld.module.item.file.manager.CoreFileManager;
import com.sunwayworld.module.item.file.service.CoreFileService;
import com.sunwayworld.module.item.file.utils.CoreFileUtils;

@Repository
@GikamBean
public class LcdpCheckoutRecordServiceImpl implements LcdpCheckoutRecordService {

    private static final Logger logger = LoggerFactory.getLogger(LcdpCheckoutRecordServiceImpl.class);

    @Autowired
    private LcdpCheckoutRecordDao lcdpCheckoutRecordDao;

    @Autowired
    private LcdpCrdLogService lcdpCrdLogService;

    @Autowired
    @Lazy
    private LcdpResourceService resourceService;

    @Autowired
    private CoreFileService coreFileService;

    @Autowired
    private LcdpCheckoutConfigService checkoutConfigService;


    @Autowired
    private CoreFileManager fileManager;

    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");


    @Override
    @SuppressWarnings("unchecked")
    public LcdpCheckoutRecordDao getDao() {
        return lcdpCheckoutRecordDao;
    }
    
    @Override
    public Page<LcdpCheckoutRecordBean> selectPagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();
        
        String columns = Stream.of("ID", "CHECKOUTNO", "CHECKOUTNOTE", "CHECKINRECORDID",
                "CREATEDBYID", "CREATEDBYNAME", "CREATEDTIME").collect(Collectors.joining(", "));
        parameter.put("columns", columns);
        
        return this.selectPagination(parameter, rowBounds);
    }
    
    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpCheckoutRecordBean checkoutRecord = jsonWrapper.parseUnique(LcdpCheckoutRecordBean.class);

        checkoutRecord.setId(ApplicationContextHelper.getNextIdentity());
        checkoutRecord.setCheckoutNo(ApplicationContextHelper.getNextSequence("T_LCDP_CHECKOUT_RECORD"));
        getDao().insert(checkoutRecord);
        resourceService.export(jsonWrapper, checkoutRecord);
        return checkoutRecord.getId();
    }


    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public LcdpCheckOutDTO confirm(RestJsonWrapperBean jsonWrapper) {

        LcdpCheckOutDTO checkOutDTO = new LcdpCheckOutDTO();

        //需要解析三个部分 第一个部分 是记录表 第二部分是导出资源 第三部分是日志表信息

        LcdpCheckoutRecordBean checkoutRecord = jsonWrapper.parseUnique(LcdpCheckoutRecordBean.class);
        checkoutRecord.setId(ApplicationContextHelper.getNextIdentity());
        checkoutRecord.setCheckoutNo(ApplicationContextHelper.getNextSequence("T_LCDP_CHECKOUT_RECORD"));
        EntityHelper.assignCreatedElement(checkoutRecord);
        String downLoadUrl = resourceService.export(jsonWrapper, checkoutRecord);

        List<LcdpCrdLogBean> crdLogList = jsonWrapper.parse(LcdpCrdLogBean.class);

        if (crdLogList.isEmpty()) {
            return null;
        }
        crdLogList.forEach(e -> {
            e.setId(ApplicationContextHelper.getNextIdentity());
            e.setCheckoutRecordId(checkoutRecord.getId());
        });

        List<LcdpCrdLogBean> checkoutList = crdLogList.stream().filter(log -> StringUtils.equals("checkout", log.getCheckoutType())).collect(Collectors.toList());
        //迁出
        if (!checkoutList.isEmpty()) {
            doCheckout(checkoutList, checkOutDTO, checkoutRecord);
        }

        LcdpCrdLogBean export = crdLogList.stream().filter(log -> StringUtils.equals("export", log.getCheckoutType())).findFirst().orElse(null);
        if (null != export) {
            crdLogList.get(0).setCheckoutStatus(Constant.YES);
            checkOutDTO.setDownLoadUrl(downLoadUrl);
        }

        getDao().insert(checkoutRecord);
        lcdpCrdLogService.getDao().insert(crdLogList);
        return checkOutDTO;
    }

    //导出
    @Override
    public LcdpCheckOutDTO export(RestJsonWrapperBean jsonWrapper) {
        LcdpCheckoutRecordBean checkoutRecord = jsonWrapper.parseUnique(LcdpCheckoutRecordBean.class);
        List<CoreFileBean> coreFileList = coreFileService.selectFileList(getDao().getTable(), checkoutRecord.getId());
        CoreFileBean coreFile = coreFileList.stream().findFirst().orElse(null);
        LcdpCheckOutDTO checkOutDTO = new LcdpCheckOutDTO();
        checkOutDTO.setDownLoadUrl(coreFile == null ? null : fileManager.getAbsoluteDownloadUrl(coreFile));
        LcdpCrdLogBean crdLogBean = new LcdpCrdLogBean();
        crdLogBean.setId(ApplicationContextHelper.getNextIdentity());
        crdLogBean.setCheckoutRecordId(checkoutRecord.getId());
        crdLogBean.setCheckoutType("export");
        crdLogBean.setCheckoutStatus(Constant.YES);

        lcdpCrdLogService.getDao().insert(crdLogBean);
        return checkOutDTO;
    }

    //迁出
    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public LcdpCheckOutDTO checkout(RestJsonWrapperBean jsonWrapper) {
        LcdpCheckoutRecordBean checkoutRecord = jsonWrapper.parseUnique(LcdpCheckoutRecordBean.class);
        List<LcdpCrdLogBean> crdLogList = jsonWrapper.parse(LcdpCrdLogBean.class);

        LcdpCheckOutDTO checkOutDTO = new LcdpCheckOutDTO();
        crdLogList.forEach(log -> {
            log.setId(ApplicationContextHelper.getNextIdentity());
            log.setCheckoutRecordId(checkoutRecord.getId());
        });
        doCheckout(crdLogList, checkOutDTO, checkoutRecord);

        lcdpCrdLogService.getDao().insert(crdLogList);

        return checkOutDTO;
    }


    @Override
    public LcdpCheckOutDTO checkoutNetworkTest(RestJsonWrapperBean jsonWrapper) {
        List<LcdpCrdLogBean> crdLogList = jsonWrapper.parse(LcdpCrdLogBean.class);

        LcdpCheckOutDTO checkOutDTO = new LcdpCheckOutDTO();

        preCheckout(crdLogList, checkOutDTO);


        return checkOutDTO;
    }



    @Override
    public Page<LcdpCrdLogBean> selectLogPaginationByCheckoutRecordId(Long id, RestJsonWrapperBean wrapper) {

        return lcdpCrdLogService.selectPaginationByFilter(SearchFilter.instance().match("CHECKOUTRECORDID", id).filter(MatchPattern.EQ), wrapper);


    }


    @Override
    public void doCheckout(List<LcdpCrdLogBean> checkoutList, LcdpCheckOutDTO checkOutDTO, LcdpCheckoutRecordBean checkoutRecord) {
        List<CoreFileBean> coreFileList = coreFileService.selectFileList(getDao().getTable(), checkoutRecord.getId());
        CoreFileBean coreFile = coreFileList.stream().findFirst().orElse(null);
        Path filePath = CoreFileUtils.getLocalPath(coreFile);
        File file = filePath.toFile();

        List<LcdpCheckoutConfigBean> checkoutConfigList = checkoutConfigService.selectListByFilter(SearchFilter.instance().match("ACTIVATEDFLAG", Constant.ACTIVATED_STATUS_YES).filter(MatchPattern.EQ));

        Map<Long, LcdpCheckoutConfigBean> checkoutConfigMap = checkoutConfigList.stream().collect(Collectors.toMap(LcdpCheckoutConfigBean::getId, Function.identity()));
        List<LcdpCheckoutDetailDTO> detailList = new ArrayList<>();
        checkoutList.forEach(log -> {
            LcdpCheckoutDetailDTO checkoutDetailDTO = new LcdpCheckoutDetailDTO();
            String urlSuffix = LcdpPathConstant.CHECKIN_RECORD_RECEIVE_PATH + "/action/receive";
            LcdpCheckoutConfigBean checkoutConfig = checkoutConfigMap.get(log.getCheckoutConfigId());
            String systemUrl = checkoutConfig.getSystemUrl();
            if (systemUrl.endsWith("/")) {
                systemUrl = systemUrl.substring(0, systemUrl.length() - 1);
            }
            String sendUrL = systemUrl + urlSuffix;
            Map<String, Object> responseMap = callSendFile(file, sendUrL, checkoutRecord, checkoutConfig);
            Object code = responseMap.get("code");
            Integer statusCode = ObjectUtils.isEmpty(code) ? null : Integer.valueOf(code.toString());
            checkoutDetailDTO.setSystemName(checkoutConfig.getSystemName());
            checkoutDetailDTO.setCheckoutStatus(Constant.YES);
            log.setCheckoutStatus(Constant.YES);
            if (statusCode == null || statusCode != 200) {
                checkoutDetailDTO.setCheckoutStatus(Constant.NO);
                checkoutDetailDTO.setMessage(ObjectUtils.isEmpty(responseMap.get("result")) ? null : responseMap.get("result").toString());
                log.setCheckoutStatus(Constant.NO);
            }
            detailList.add(checkoutDetailDTO);
        });
        checkOutDTO.setCheckoutDetailList(detailList);


    }


    private void preCheckout(List<LcdpCrdLogBean> checkoutList, LcdpCheckOutDTO checkOutDTO) {

        List<LcdpCheckoutConfigBean> checkoutConfigList = checkoutConfigService.selectListByFilter(SearchFilter.instance().match("ACTIVATEDFLAG", Constant.ACTIVATED_STATUS_YES).filter(MatchPattern.EQ));

        Map<Long, LcdpCheckoutConfigBean> checkoutConfigMap = checkoutConfigList.stream().collect(Collectors.toMap(LcdpCheckoutConfigBean::getId, Function.identity()));
        List<LcdpCheckoutDetailDTO> detailList = new ArrayList<>();
        checkoutList.forEach(log -> {
            LcdpCheckoutDetailDTO checkoutDetailDTO = new LcdpCheckoutDetailDTO();
            String urlSuffix = LcdpPathConstant.CHECKIN_RECORD_RECEIVE_PATH + "/action/network-test";
            LcdpCheckoutConfigBean checkoutConfig = checkoutConfigMap.get(log.getCheckoutConfigId());
            String systemUrl = checkoutConfig.getSystemUrl();
            if (systemUrl.endsWith("/")) {
                systemUrl = systemUrl.substring(0, systemUrl.length() - 1);
            }
            String sendUrL = systemUrl + urlSuffix;
            CloseableHttpClient httpclient = getClient(sendUrL);
            Map<String, Object> responseMap = new HashMap<>();
            String result = null;
            try {
                HttpPost httpPost = new HttpPost(sendUrL);
                RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(ApplicationContextHelper.getEnvironment().getProperty("sunway.http-client.connect-timeout", Integer.class, 3000)).build();
                httpPost.setConfig(requestConfig);
                Environment environment = ApplicationContextHelper.getEnvironment();

                StringBuilder serverAddress = new StringBuilder();
                StringBuffer requestURL = ServletUtils.getCurrentRequest().getRequestURL();
                logger.error("当前服务器地址: {}", requestURL);

                // 修改正则表达式，支持域名和IP地址
                Pattern addressHavePortPattern = Pattern.compile("//([a-zA-Z0-9.-]+):([0-9]{1,5})");
                Matcher havePortMatcher = addressHavePortPattern.matcher(requestURL);

                Pattern addressNoPortPattern = Pattern.compile("//([a-zA-Z0-9.-]+)");
                Matcher noPortMatcher = addressNoPortPattern.matcher(requestURL);
                if (havePortMatcher.find()) {
                    serverAddress.append(havePortMatcher.group());
                }else if(noPortMatcher.find()){
                    serverAddress.append(noPortMatcher.group());
                }else {
                    logger.error("获取解析当前服务器地址失败");
                    responseMap.put("code", "500");
                    responseMap.put("result", I18nHelper.getMessage("GIKAM.EXCEPTION.AUTH_FAILD"));
                }

                serverAddress.append(environment.getProperty("server.servlet.context-path", ""));
                Header[] headers = new Header[]{new BasicHeader("license", checkoutConfig.getLicense()), new BasicHeader("serverAddress", serverAddress.toString())};

                httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
                httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                httpPost.setHeader("Accept-Encoding", "gzip, deflate");
                httpPost.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
                httpPost.setHeader("Connection", "keep-alive");
                httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36");
                httpPost.setHeaders(headers);

                //创建接口需要的参数
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                HttpEntity entity = entityBuilder.build();
                httpPost.setEntity(entity);

                logger.info("开始调用跨网文件发送接口");
                logger.info("跨网文件发送接口请求报文:{}", httpPost);
                //调用跨网文件发送接口
                HttpResponse response = httpclient.execute(httpPost);
                //获取响应信息
                logger.info("跨网文件发送接口返回结果未处理:{}", response);
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    result = EntityUtils.toString(responseEntity, CHARSET_UTF8);
                }
                logger.info("跨网文件发送接口返回结果处理完成");
                logger.info("跨网文件发送接口返回结果状态：{}", response.getStatusLine());

                logger.info("跨网文件发送接口返回结果:{}", result);

                responseMap.put("code", response.getStatusLine().getStatusCode());
                responseMap.put("result", result);

            } catch (IOException e) {
                responseMap.put("code", 500);
                responseMap.put("result", e.getMessage());
                logger.error(e.getMessage(),e);
            } catch (Exception e) {
                responseMap.put("code", 500);
                responseMap.put("result", e.getMessage());
                logger.error(e.getMessage(),e);
            } finally {
                try {
                    httpclient.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                }
            }

            Object code = responseMap.get("code");
            Integer statusCode = ObjectUtils.isEmpty(code) ? null : Integer.valueOf(code.toString());
            checkoutDetailDTO.setSystemName(checkoutConfig.getSystemName());
            checkoutDetailDTO.setCheckoutStatus(Constant.YES);
            if (statusCode == null || statusCode != 200) {
                checkoutDetailDTO.setCheckoutStatus(Constant.NO);
                checkoutDetailDTO.setMessage(ObjectUtils.isEmpty(responseMap.get("result")) ? null : responseMap.get("result").toString());
            }
            detailList.add(checkoutDetailDTO);
        });
        checkOutDTO.setCheckoutDetailList(detailList);

    }


    //迁出请求接口
    public static Map<String, Object> callSendFile(File file, String sendUrL, LcdpCheckoutRecordBean checkoutRecord, LcdpCheckoutConfigBean checkoutConfig) {
        CloseableHttpClient httpclient = getClient(sendUrL);
        Map<String, Object> responseMap = new HashMap<>();
        String result = null;
        try {

            HttpPost httpPost = new HttpPost(sendUrL);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(ApplicationContextHelper.getEnvironment().getProperty("sunway.http-client.connect-timeout", Integer.class, 3000)).build();
            httpPost.setConfig(requestConfig);
            Environment environment = ApplicationContextHelper.getEnvironment();

            StringBuilder serverAddress = new StringBuilder();
            StringBuffer requestURL = ServletUtils.getCurrentRequest().getRequestURL();
            // 修改正则表达式，支持域名和IP地址
            Pattern addressHavePortPattern = Pattern.compile("//([a-zA-Z0-9.-]+):([0-9]{1,5})");
            Matcher havePortMatcher = addressHavePortPattern.matcher(requestURL);

            Pattern addressNoPortPattern = Pattern.compile("//([a-zA-Z0-9.-]+)");
            Matcher noPortMatcher = addressNoPortPattern.matcher(requestURL);
            if (havePortMatcher.find()) {
                serverAddress.append(havePortMatcher.group());
            }else if(noPortMatcher.find()){
                serverAddress.append(noPortMatcher.group());
            }else {
                responseMap.put("code", "500");
                responseMap.put("result", I18nHelper.getMessage("GIKAM.EXCEPTION.AUTH_FAILD"));
                return responseMap;
            }

            serverAddress.append(environment.getProperty("server.servlet.context-path", ""));
            Header[] headers = new Header[]{new BasicHeader("license", checkoutConfig.getLicense()), new BasicHeader("serverAddress", serverAddress.toString())};

            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            httpPost.setHeader("Accept-Encoding", "gzip, deflate");
            httpPost.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
            httpPost.setHeader("Connection", "keep-alive");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36");
            httpPost.setHeaders(headers);


            //创建接口需要的参数
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            ContentType infoCType = ContentType.create("application/json", CHARSET_UTF8);
            ContentType fileCType = ContentType.create("application/octet-stream", CHARSET_UTF8);
            entityBuilder.addPart("file", new FileBody(file, fileCType));
            entityBuilder.addTextBody("checkoutRecord", JSONObject.toJSONString(checkoutRecord), infoCType);
            HttpEntity entity = entityBuilder.build();
            httpPost.setEntity(entity);

            //调用跨网文件发送接口
            HttpResponse response = httpclient.execute(httpPost);
            //获取响应信息
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                result = EntityUtils.toString(responseEntity, CHARSET_UTF8);
            }
            responseMap.put("code", response.getStatusLine().getStatusCode());
            responseMap.put("result", result);
        } catch (IOException e) {
            responseMap.put("code", 500);
            responseMap.put("result", e.getMessage());
            logger.error(e.getMessage(),e);
        } catch (Exception e) {
            responseMap.put("code", 500);
            responseMap.put("result", e.getMessage());
            logger.error(e.getMessage(),e);
        } finally {
            try {
                httpclient.close();
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
        }
        return responseMap;
    }



    private static CloseableHttpClient getHttpClient() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(sslContext);
            return HttpClients.custom().setDefaultRequestConfig(getDefaultRequestConfig()).setSSLSocketFactory(sslFactory).build();
        } catch (Exception e) {
        }
        return HttpClients.custom().setDefaultRequestConfig(getDefaultRequestConfig()).build();
    }

    private static CloseableHttpClient getHttpsClient() {
        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            }).build();

            return HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException ex) {
            throw new UnexpectedException(ex);
        }
    }

    private static CloseableHttpClient getClient(String url) {
        if (StringUtils.startsWithIgnoreCase(url, "https:")) {
            return getHttpsClient();
        }

        return getHttpClient();
    }

    private static RequestConfig getDefaultRequestConfig() {
        return RequestConfig.custom().setSocketTimeout(ApplicationContextHelper.getEnvironment().getProperty("sunway.http-client.socket-timeout", Integer.class, 30000)) // 数据传输的超时时长
                .setConnectTimeout(ApplicationContextHelper.getEnvironment().getProperty("sunway.http-client.connect-timeout", Integer.class, 3000)) // 建立连接的超时时长
                .build();
    }


}

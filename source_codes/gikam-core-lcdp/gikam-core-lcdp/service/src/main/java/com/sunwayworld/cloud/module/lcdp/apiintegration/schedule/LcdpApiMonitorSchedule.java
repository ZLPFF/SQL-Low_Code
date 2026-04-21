package com.sunwayworld.cloud.module.lcdp.apiintegration.schedule;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiNotifierBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiReqBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiNotifierService;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiReqService;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.concurrent.GikamConcurrentLocker;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mail.ExchangeMailHelper;
import com.sunwayworld.framework.mail.MailHelper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.sys.sms.core.CoreSmsManager;

@Component
@Profile("monitor")
public class LcdpApiMonitorSchedule {

    private static final String EMAIL_PROTOCOL = getEmailProtocol();

    @Autowired(required = false)
    private CoreSmsManager smsManager;

    @Autowired
    private LcdpApiReqService lcdpApiReqService;

    @Autowired
    private LcdpApiNotifierService lcdpApiNotifierService;

    @Scheduled(fixedDelay = 300000)
    public void schedule() {

        GikamConcurrentLocker.isolatedRun("LCDP_API_FAIL_MESSAGE", () -> {

            List<LcdpApiNotifierBean> lcdpApiNotifierList = lcdpApiNotifierService.selectAll();

            List<String> toList = lcdpApiNotifierList.stream()
                    .filter(lcdpApiNotifier -> !StringUtils.isEmpty(lcdpApiNotifier.getEmail()))
                    .map(LcdpApiNotifierBean::getEmail).collect(Collectors.toList());

            List<String> toMobileList = lcdpApiNotifierList.stream()
                    .filter(lcdpApiNotifier -> !StringUtils.isEmpty(lcdpApiNotifier.getMobile()))
                    .map(LcdpApiNotifierBean::getMobile).collect(Collectors.toList());

            String subject = I18nHelper.getMessage("LCDP.API.EXCEPTION.SUBJECT");

            if (ObjectUtils.isEmpty(toList) && ObjectUtils.isEmpty(toMobileList)) {
                return;
            }

            List<LcdpApiReqBean> notifyReqList = lcdpApiReqService.selectListByFilter(SearchFilter.instance()
                    .match("STATUS", Constant.REMOTE_CALL_STATUS_FAILED).filter(MatchPattern.SEQ)
                    .match("NOTIFYFLAG", Constant.NO).filter(MatchPattern.SEQ));

            notifyReqList.forEach(req -> {

                String content = I18nHelper.getMessage("LCDP.API.EXCEPTION.CONTENT", req.getApiCode());

                if (Constant.EMAIL_PROTOCOL_GENERIC.equals(EMAIL_PROTOCOL)) {
                    MailHelper.sendHtmlMail(subject, content, toList);
                } else if (Constant.EMAIL_PROTOCOL_EXCHANGE.equals(EMAIL_PROTOCOL)) {
                    ExchangeMailHelper.sendHtmlMail(subject, content, toList);
                }

                if (!ObjectUtils.isEmpty(smsManager) && !ObjectUtils.isEmpty(toMobileList)) {
                    toMobileList.forEach(mobile -> smsManager.send(mobile, content));
                }

                req.setNotifyFlag(Constant.YES);
            });

            lcdpApiReqService.getDao().update(notifyReqList, "NOTIFYFLAG");
        });
    }

    private static final String getEmailProtocol() {
        String emailSendMethod = ApplicationContextHelper.getEnvironment().getProperty("sunway.mail.protocol.type");
        return StringUtils.isBlank(emailSendMethod) ? Constant.EMAIL_PROTOCOL_GENERIC : emailSendMethod;
    }
}

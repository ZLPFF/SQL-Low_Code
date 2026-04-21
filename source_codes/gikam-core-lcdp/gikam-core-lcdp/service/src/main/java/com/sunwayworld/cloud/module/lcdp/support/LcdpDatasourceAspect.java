package com.sunwayworld.cloud.module.lcdp.support;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.tenant.TenantContext;
import com.sunwayworld.framework.utils.ServletUtils;
import com.sunwayworld.framework.utils.StringUtils;

/**
 * @author yangsz@sunway.com 2023-03-02
 * 通过切换租户实现切换低代码开发与生产数据源分离
 * V12业务表、项目表分为开发、生产两份，低代码表及全局表支持生产、开发环境
 */
@Aspect
@Component
@Order(50)
@Profile("lcdp-tenant")
public class LcdpDatasourceAspect {

    @Autowired
    private PlatformTransactionManager txManager;

    //低代码租户名
    public static final String DEV_DATABASE_TENANT_NAME = "lcdp";

    /**
     * 配置V12核心Service、Dao接口的所有默认方法
     * GenericService、GenericDao
     */
    @Pointcut("within(com.sunwayworld.framework.support.base.service.GenericService) " +
            "|| within(com.sunwayworld.framework.support.base.dao.GenericDao)")
    private void coreInterface() {

    }

    /**
     * 强制使用生产数据源的服务：
     * 1.低代码中资源模块、表管理、工作流等表的crud
     * 低代码中不需要强制使用生产数据源：（排除用）
     * 1.数据库相关查询校验、DDL语句执行
     * 2.后台脚本的支持服务LcdpBaseDao、LcdpBaseService、GikamBaseService
     * 3.后台脚本的调用接口callScript()、manualSave()
     * 2.全局表服务
     */
    @Pointcut("(within(com.sunwayworld..lcdp..service..*) " +
            "|| within(com.sunwayworld..lcdp..dao..*)" +
            "|| proBpmnDatasource())" +
            "&& !within(com.sunwayworld..lcdp.base..*) " +
            "&& !execution(* com.sunwayworld.cloud.module.lcdp.resource.service.impl.LcdpResourceServiceImpl.callScript(..)) " +
            "&& !execution(* com.sunwayworld.cloud.module.lcdp.resource.service.impl.LcdpResourceServiceImpl.manualSave(..))" +
            "&& !execution(* com.sunwayworld.cloud.module.lcdp.table.service.impl.LcdpTableServiceImpl.*PhysicalTable(..))" +
            "&& !execution(* com.sunwayworld.cloud.module.lcdp.table.persistent.dao.impl.LcdpDatabaseDaoImpl.*(..))" +
            "&& !execution(* com.sunwayworld.cloud.module.lcdp.table.service.impl.LcdpDatabaseServiceImpl.*(..))")
    private void proDataSource() {

    }

    /**
     * bpmn定义服务走主数据源
     */
    @Pointcut("execution(* com.sunwayworld.module.sys.bpmn.service.impl.CoreBpmnDiagramServiceImpl.*(..)) " +
            "|| execution(* com.sunwayworld.module.sys.bpmn.service.impl.CoreBpmnDraftOrgServiceImpl.*(..))" +
            "|| execution(* com.sunwayworld.module.sys.bpmn.service.impl.CoreBpmnDraftServiceImpl.*(..))" +
            "|| execution(* com.sunwayworld.module.sys.bpmn.service.impl.CoreBpmnProcOrgServiceImpl.*(..))" +
            "|| execution(* com.sunwayworld.module.sys.bpmn.service.impl.CoreBpmnProcServiceImpl.*(..))" +
            "|| execution(* com.sunwayworld.module.sys.bpmn.service.impl.CoreBpmnDiagramServiceImpl.*(..))")
    private void proBpmnDatasource() {

    }

    /**
     * 所有resource根据环境参数切换数据源
     * 由于低代码resource模块与接口resource包重名,排除低代码resource模块内接口包外的其他bean
     */
    @Around("within(com.sunwayworld..resource..*) " +
            "&& !(within(com.sunwayworld..lcdp.resource..*) && !within(com.sunwayworld..lcdp.resource.resource..*))")
    public Object changeResourceDataSourceByEnv(ProceedingJoinPoint pjp) throws Throwable {

        HttpServletRequest request = ServletUtils.getCurrentRequest();
        if (request == null || !LcdpConstant.REQUEST_HEADER_LCDPENV_DEVELOPMENT.equals(request.getHeader(LcdpConstant.REQUEST_HEADER_LCDPDATAENV))) {
            return pjp.proceed();
        }
        String tenant = TenantContext.getTenant();
        //设置开发数据源
        TenantContext.setTenant(DEV_DATABASE_TENANT_NAME);
        try {
            return pjp.proceed();
        } finally {
            TenantContext.setTenant(tenant);
        }
    }

    /**
     * 强制使用生产数据源的bean
     * coreInterface()  V12核心接口，用于拦截接口的默认方法
     * proDataSource()  强制使用生产数据源的Service、Dao
     */
    @Around("coreInterface() || proDataSource()")
    public Object executeWithProDataSource(ProceedingJoinPoint pjp) throws Throwable {
        //获取原数据源
        String tenant = TenantContext.getTenant();
        if (StringUtils.equals(DEV_DATABASE_TENANT_NAME, tenant)) {
            //设置默认数据源
            TenantContext.removeTenant();
            //新开一个事务，为获取开发库的数据源菜单
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus transaction = txManager.getTransaction(def);
            Object result;
            try {
                result = pjp.proceed();
                txManager.commit(transaction);
            } catch (Exception e) {
                txManager.rollback(transaction);
                throw e;
            } finally {
                //重置数据源
                TenantContext.setTenant(tenant);
            }
            return result;
        }
        return pjp.proceed();
    }

    /**
     * DDL语句需要在两个库执行
     */
    @Around("execution(* com.sunwayworld.cloud.module.lcdp.table.service.impl.LcdpTableServiceImpl.createPhysicalTable(..)) " +
            "|| execution(* com.sunwayworld.cloud.module.lcdp.table.service.impl.LcdpTableServiceImpl.dropPhysicalTable(..)) " +
            "|| execution(* com.sunwayworld.cloud.module.lcdp.table.service.impl.LcdpTableServiceImpl.alterPhysicalTable(..))" +
            "|| execution(* com.sunwayworld.cloud.module.lcdp.table.service.impl.LcdpDatabaseServiceImpl.createPhysicalView(..))" +
            "|| execution(* com.sunwayworld.cloud.module.lcdp.table.service.impl.LcdpDatabaseServiceImpl.alterPhysicalView(..))" +
            "|| execution(* com.sunwayworld.cloud.module.lcdp.table.service.impl.LcdpDatabaseServiceImpl.dropPhysicalView(..))")
    public Object handleDDLExecution(ProceedingJoinPoint pjp) throws Throwable {
        return synchronizeExecute(pjp);
    }

    /**
     * 对于存在表中数据的校验，取不同数据库的或值
     */
    @Around("execution(* com.sunwayworld.cloud.module.lcdp.table.service.impl.LcdpDatabaseServiceImpl.isExistData(..)) " +
            "|| execution(* com.sunwayworld.cloud.module.lcdp.table.service.impl.LcdpDatabaseServiceImpl.isExistNullDataInColumn(..)) "+
            "|| execution(* com.sunwayworld.cloud.module.lcdp.table.service.impl.LcdpDatabaseServiceImpl.isExistNotNullDataInColumn(..))")
    public Object executeValidateFunction(ProceedingJoinPoint pjp) throws Throwable {
        //获取原数据源
        Object result;
        Object resultDev;
        //获取原数据源
        String tenant = TenantContext.getTenant();
        try {
            result = pjp.proceed();
            if(StringUtils.equals(tenant,DEV_DATABASE_TENANT_NAME)){
                return result;
            }
            //设置开发数据源
            TenantContext.setTenant(DEV_DATABASE_TENANT_NAME);
            //开一个新事务，以获得开发库的Connection
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus transaction = txManager.getTransaction(def);
            try {
                //开发库校验
                resultDev = pjp.proceed();
                txManager.commit(transaction);
            } catch (Exception e) {
                txManager.rollback(transaction);
                throw e;
            }
        } finally {
            //重置数据源
            TenantContext.setTenant(tenant);
        }
        return (Boolean) result || (Boolean) resultDev;
    }

    /**
     * 同步编号规则定义
     */
    @Around("(target(com.sunwayworld.module.sys.sequence.service.impl.CoreSequenceServiceImpl)" +
            "   && (execution(* com.sunwayworld.module.sys.sequence.service.impl.CoreSequenceAbstractService.deleteLine(..)) " +
            "       || execution(* com.sunwayworld.module.sys.sequence.service.impl.CoreSequenceAbstractService.swapLine(..)) " +
            "       || execution(* com.sunwayworld.module.sys.sequence.service.impl.CoreSequenceAbstractService.insert(..)) " +
            "       || execution(* com.sunwayworld.framework.support.base.service.GenericService.delete(..)) " +
            "       || execution(* com.sunwayworld.framework.support.base.service.GenericService.instantSave(..)))) " +
            "|| (target(com.sunwayworld.module.sys.sequence.dao.impl.CoreSequenceLineDaoImpl)" +
            "   && execution(* com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport.insert(..)))")
    public Object syncSequence(ProceedingJoinPoint pjp) throws Throwable {
        return synchronizeExecute(pjp);
    }

    /**
     * 同步国际化
     */
    @Around("within(com.sunwayworld.module.sys.i18n.dao.impl.*)" +
            " && !execution(* com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport.select*(..)) " +
            " && !execution(* com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport.get*(..)) " +
            " && !execution(* com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport.count*(..)) ")
    public Object syncI18n(ProceedingJoinPoint pjp) throws Throwable {
        return synchronizeExecute(pjp);
    }

    /**
     * 同步管理员下拉框、选择框、复选框
     */
    @Around("within(com.sunwayworld.module.admin.config.dao.impl.*)" +
            " && !execution(* com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport.select*(..)) " +
            " && !execution(* com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport.get*(..)) " +
            " && !execution(* com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport.count*(..)) ")
    public Object syncAdminConfig(ProceedingJoinPoint pjp) throws Throwable {
        return synchronizeExecute(pjp);
    }

    /**
     * 同步系统编码
     */
    @Around("within(com.sunwayworld.module.sys.code.dao.impl.*)" +
            " && !execution(* com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport.select*(..)) " +
            " && !execution(* com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport.get*(..)) " +
            " && !execution(* com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport.count*(..)) ")
    public Object syncCode(ProceedingJoinPoint pjp) throws Throwable {
        return synchronizeExecute(pjp);
    }


    private Object synchronizeExecute(ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        //获取原数据源
        String tenant = TenantContext.getTenant();
        try {
            result = pjp.proceed();
            if(StringUtils.equals(tenant,DEV_DATABASE_TENANT_NAME)){
                return result;
            }
            //设置开发数据源
            TenantContext.setTenant(DEV_DATABASE_TENANT_NAME);
            //开一个新事务，以获得开发库的Connection
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus transaction = txManager.getTransaction(def);
            try {
                //开发库执行
                pjp.proceed();
                txManager.commit(transaction);
            } catch (Exception e) {
                txManager.rollback(transaction);
                throw e;
            }
        } finally {
            //重置数据源
            TenantContext.setTenant(tenant);
        }
        return result;
    }
}

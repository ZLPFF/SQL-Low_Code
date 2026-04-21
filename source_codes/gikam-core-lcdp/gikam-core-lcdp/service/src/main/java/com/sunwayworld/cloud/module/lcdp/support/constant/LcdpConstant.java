package com.sunwayworld.cloud.module.lcdp.support.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.resource.bean.ServerScriptType;
import com.sunwayworld.framework.utils.ArrayUtils;

public class LcdpConstant {
    //脚本执行类型  测试执行、正式执行
    public final static String SCRIPT_CALL_DEV = "dev";
    public final static String SCRIPT_CALL_PRO = "pro";

    public final static String REQUEST_HEADER_SCRIPTPATH = "script-path";
    public final static String REQUEST_HEADER_LCDPENV = "lcdp-env";
    public final static String REQUEST_HEADER_LCDPDATAENV = "lcdp-data-env";
    public final static String REQUEST_HEADER_LCDPENV_DEVELOPMENT = "development";

    //Java脚本关键字
    public final static String CLASS_KEY = "public class ";
    public final static String CLASS_KEY_EXPR = "public\\s+class\\s+";

    public final static String CONSTRUCTOR_MATCHER_KEY = "public\\s+";
    public final static String CONSTRUCTOR_KEY = "public ";


    //业务工作流脚本类名
    public final static String BP_ACTION_SCRIPT_CLASS_NAME = "LcdpBpActionHandle";//执行脚本
    public final static String BP_CHECK_SCRIPT_CLASS_NAME = "LcdpBpActionCheck";//校验脚本


    //平台资源表状态  模块分类、模块、页面、前端脚本、后端脚本
    public final static String RESOURCE_CATEGORY_CATEGORY = "category";//分类

    public final static String RESOURCE_CATEGORY_MODULE = "module";//模块

    public final static String RESOURCE_CATEGORY_FOLDER = "folder";//文件夹

    public final static String RESOURCE_CATEGORY_VIEW = "view";//页面

    public final static String RESOURCE_CATEGORY_JS = "js";//前端脚本

    public final static String RESOURCE_CATEGORY_JAVA = "java";//后端脚本

    public final static String RESOURCE_CATEGORY_TABLE = "table";//表

    public final static String RESOURCE_CATEGORY_DB_VIEW = "db-view";//视图

    public final static String RESOURCE_CATEGORY_MAPPER = "mapper";//mapper

    public final static String RESOURCE_CATEGORY_COMP = "comp";//页面组件

    public final static String RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS = "SYS_CLIENT_JS";//系统JS
    
    public final static String RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS = "SYS_CLIENT_CSS";//系统CSS

    public final static String RESOURCE_CATEGORY_FILE = "file";//文件

    public final static String RESOURCE_CATEGORY_CODEBLOCK = "codeBlock";//代码块

    //是否提交
    public final static String SUBMIT_FLAG_YES = "yes";//已提交

    public final static String SUBMIT_FLAG_NO = "no";//未提交

    //是否生效
    public final static String EFFECT_FLAG_YES = "yes";//已生效

    public final static String EFFECT_FLAG_NO = "no";//未生效

    // 实体类复制时无需复制的属性
    public static final String[] COPY_IGNORE_PROPERTIES = new String[]{"createdById", "createdByName", "createdByOrgId", "createdByOrgName", "createdTime", "operatedTime", "operatedById", "operatedByName"};

    //字段，索引操作类型
    public final static String FIELD_INDEX_OPS_ADD = "add";//添加

    public final static String FIELD_INDEX_OPS_DELETE = "delete";//删除

    public final static String FIELD_INDEX_OPS_UPDATE = "update";//修改

    //模块下文件夹名称
    public final static String FOLDERS_UNDER_MODULE_PAGE = "page";//页面

    public final static String FOLDERS_UNDER_MODULE_CLIENT = "client";//前端脚本

    public final static String FOLDERS_UNDER_MODULE_SERVER = "server";//后端脚本

    public final static String FOLDERS_UNDER_MODULE_MAPPER = "mapper";//后端脚本

    //字段是否允许为空
    public final static String FIELD_ALLOWNULL_YES = "1";//字段允许为空

    public final static String FIELD_ALLOWNULL_NO = "0";//字段不允许为空

    public final static String RESOURCE_STATUS_CHECKOUT = "1";//检出

    public final static String RESOURCE_STATUS_CHECKIN = "0";//检入

    //需要做入历史资源库的资源数据类型集合
    public final static List<String> RESOURCE_SCRIPT_CATEGORY_LIST = Arrays.asList(LcdpConstant.RESOURCE_CATEGORY_VIEW, LcdpConstant.RESOURCE_CATEGORY_JS, LcdpConstant.RESOURCE_CATEGORY_JAVA, LcdpConstant.RESOURCE_CATEGORY_MAPPER);

    public final static List<String> CATEGORY_MODULE_CATEGORY_LIST = Arrays.asList(LcdpConstant.RESOURCE_CATEGORY_CATEGORY, LcdpConstant.RESOURCE_CATEGORY_MODULE);

    public final static String RESULT_CODE_SUCCESS = "200";
    public final static String RESULT_CODE_FAIL = "500";

    public final static String RESULT_MESSAGE_SUCCESS = "成功";
    public final static String RESULT_MESSAGE_FAIL = "失败";

    //Java脚本资源缓存 KEY
    public final static String LCDP_JAVA_RESOURCE_CACHE = "LCDP_JAVA_RESOURCE_CACHE";

    public final static String TABLE_SCRIPT_MAPPING_CACHE = "TABLE_SCRIPT_MAPPING_CACHE";

    //脚本路径关联表名
    public final static String SCRIPT_PATH_TABLE_MAPPING_CACHE = "SCRIPT_PATH_TABLE_MAPPING_CACHE";

    public final static String REGISTER_FINISH_KEY = "REGISTER_FINISH_KEY";


    public final static String RESOURCE_STATUS_NEW = "new"; //资源状态 新增数据

    public final static String RESOURCE_STATUS_VALID = "valid"; //资源状态 有效数据

    public final static String SUPER_ADMIN_ID = "sysAdmin";//超级管理员ID

    public static final String SUBMIT_TMPL_PARAM_CONFIG = "submitLogTmpl";


    //复制时需要替换数据源的组件类型集合
    public final static List<String> RESOURCE_COPY_COMP_TYPE_LIST = Arrays.asList("Grid", "Form", "TreeGrid", "Tree");

    public final static Map<String, String> FIELD_TYPE_TO_DB_TYPE_MAP = new HashMap<>();

    static {
        FIELD_TYPE_TO_DB_TYPE_MAP.put("string", "varchar");
        FIELD_TYPE_TO_DB_TYPE_MAP.put("dateTime", "date");
        FIELD_TYPE_TO_DB_TYPE_MAP.put("date", "date");
    }

    //资源删除标记
    public final static String RESOURCE_DELETED_YES = "1"; //删除
    public final static String RESOURCE_DELETED_NO = "0"; //未删除

    //mapService位置
    public final static String MAP_SERVICE_PATH = "com.sunwayworld.framework.support.base.service.MapService";

    public final static Long MODULE_TMPL_LEFT_RIGHT_ID = 10001L; //左右模板ID

    public final static Long MODULE_TEPL_PENETRATION = 10002L; //穿透模板ID

    public final static Long MODULE_TMPL_UP_DOWN_ID = 10003L; //上下模板Id

    public final static Long MODULE_TMPL_PUBLIC_DETAIL_BP_PAGE_ID = 888888L; //公共详情页带工作流ID
    public final static Long MODULE_TMPL_PUBLIC_DETAIL_PAGE_ID = 777777L; //公共详情页不带工作流ID
    public final static Long MODULE_TMPL_PUBLIC_CHOOSE_PAGE_ID = 999999L; //公共选择页ID

    public final static String FOLDER_PAGE_SUFFIX = "777"; //page文件夹后缀
    public final static String FOLDER_CLIENT_SUFFIX = "888"; //client文件夹后缀
    public final static String FOLDER_SERVER_SUFFIX = "999"; //server文件夹后缀
    public final static String FOLDER_MAPPER_SUFFIX = "666"; //mapper文件夹后缀

    //表资源树根节点名
    public final static String RESOURCE_TABLE_TREE_ROOT_NAME = "TABLES";
    //视图资源树根节点名
    public final static String RESOURCE_VIEW_TREE_ROOT_NAME = "VIEWS";

    public final static String MODULE_TMPL_PAGE_TYPE_DETAIL = "DetailPage";

    public static List<String> CREATE_COLUMN_LIST = ArrayUtils.asList("CREATEDBYID", "CREATEDBYNAME", "CREATEDTIME", "CREATEDBYORGID", "CREATEDBYORGNAME");


    public final static List<String> MAPPER_TMPL_NAME_LIST = Arrays.asList("MysqlMapper", "OracleMapper", "PostgresqlMapper", "SqlserverMapper");

    //代码块维护根节点
    public final static String SCRIPT_BLOCK_ROOT_NAME = "代码块维护";

    //代码块分类
    public static final String SCRIPT_BLOCK_CATEGORY_BIZ = "biz";
    public static final String SCRIPT_BLOCK_CATEGORY_SYS = "sys";

    //模块模板分类
    public static final String MODULE_TMPL_CLASS_SYS = "sys";
    public static final String MODULE_TMPL_CLASS_BIZ = "biz";
    //资源文件类型对应的文件夹Map
    public static final Map<String, String> FILE_TYPE_UNDER_FOLDER_MAP = new HashMap<>();

    static {
        FILE_TYPE_UNDER_FOLDER_MAP.put("view", "page");
        FILE_TYPE_UNDER_FOLDER_MAP.put("js", "client");
        FILE_TYPE_UNDER_FOLDER_MAP.put("java", "server");
        FILE_TYPE_UNDER_FOLDER_MAP.put("mapper", "mapper");
        FILE_TYPE_UNDER_FOLDER_MAP.put("table", "table");
        FILE_TYPE_UNDER_FOLDER_MAP.put("db-view", "db-view");
    }

    //接口类型
    public static final String API_RESTFUL_TYPE = "Restful";
    public static final String API_WEBSERVICE_TYPE = "WebService";
    //接口调用类型
    public static final String API_OUTER_TYPE = "outer";  //对外暴露
    public static final String API_INVOKE_TYPE = "invoke"; //内部调用

    //接口鉴权方式
    public static final String API_AUTHENTTYPE_AUTHENT = "authent";  //鉴权
    public static final String API_AUTHENTTYPE_AUTHENTCHECK = "authentCheck"; //鉴权并加验
    public static final String API_AUTHENTTYPE_HEADER = "header"; //自定义header

    //资源提交动作
    public static final String RESOURCE_SUBMIT_ACTION_NEW = "new"; //新增
    public static final String RESOURCE_SUBMIT_ACTION_UPDATE = "update"; //修改
    public static final String RESOURCE_SUBMIT_ACTION_DELETE = "delete"; //删除
    public static final String RESOURCE_SUBMIT_ACTION_REVERT = "revert"; //回滚

    //页面模板
    public static final Long PAGE_TMPL_LEFT_RIGHT = 1001L; //左右
    public static final Long PAGE_TMPL_DETAIL = 1002L; //详情
    public static final Long PAGE_TMPL_UP_DOWN = 1003L; //上下
    public static final Long PAGE_TMPL_CHOOSE = 1004L; //选择页

    public static Map<Long, String> PAGE_TMPL_ID_TO_PAGE_TYPE_MAP = new HashMap<>();

    public static Map<String, String> PAGE_TYPE_TO_PAGE_DESC_SUFFIX_MAP = new HashMap<>();

    static {
        PAGE_TMPL_ID_TO_PAGE_TYPE_MAP.put(PAGE_TMPL_LEFT_RIGHT, "EditPage");
        PAGE_TMPL_ID_TO_PAGE_TYPE_MAP.put(PAGE_TMPL_DETAIL, "DetailPage");
        PAGE_TMPL_ID_TO_PAGE_TYPE_MAP.put(PAGE_TMPL_UP_DOWN, "EditPage");
        PAGE_TMPL_ID_TO_PAGE_TYPE_MAP.put(PAGE_TMPL_CHOOSE, "ChoosePage");

        PAGE_TYPE_TO_PAGE_DESC_SUFFIX_MAP.put("EditPage", "LCDP.MODULE.RESOURCES.SUFFIX.EDIT_PAGE");
        PAGE_TYPE_TO_PAGE_DESC_SUFFIX_MAP.put("DetailPage", "LCDP.MODULE.RESOURCES.SUFFIX.DETAIL_PAGE");
        PAGE_TYPE_TO_PAGE_DESC_SUFFIX_MAP.put("ChoosePage", "LCDP.MODULE.RESOURCES.SUFFIX.CHOOSE_PAGE");
        PAGE_TYPE_TO_PAGE_DESC_SUFFIX_MAP.put("SearchPage", "LCDP.MODULE.RESOURCES.SUFFIX.SEARCH_PAGE");
        PAGE_TYPE_TO_PAGE_DESC_SUFFIX_MAP.put("AuditPage", "LCDP.MODULE.RESOURCES.SUFFIX.AUDIT_PAGE");
    }

    public static List<String> PROFILE_DB_LIST = Arrays.asList("oracle", "postgresql", "mysql", "sqlserver");

    public static String SYS_CLIENT_JS = "全局JS脚本";
    public static String SYS_CLIENT_JS_ID = "systemClientJs";

    public static String SYS_CLIENT_CSS = "全局CSS样式";
    public static String SYS_CLIENT_CSS_ID = "systemClientCss";

    public static Map<String, String> BUTTON_I18N_MAP = new HashMap<>();

    static {
        BUTTON_I18N_MAP.put("insert", "GIKAM.BUTTON.INSERT");
        BUTTON_I18N_MAP.put("delete", "GIKAM.BUTTON.DELETE");
        BUTTON_I18N_MAP.put("submit", "GIKAM.WORKFLOW.BUTTON.SUBMIT");
        BUTTON_I18N_MAP.put("back", "GIKAM.BUTTON.BACK");
        BUTTON_I18N_MAP.put("pass", "GIKAM.WORKFLOW.BUTTON.PASS");
        BUTTON_I18N_MAP.put("approve", "GIKAM.WORKFLOW.BUTTON.PASS");
        BUTTON_I18N_MAP.put("reject", "GIKAM.WORKFLOW.BUTTON.REJECT");
        BUTTON_I18N_MAP.put("confirm", "GIKAM.BUTTON.CONFIRM");
        BUTTON_I18N_MAP.put("cancel", "GIKAM.BUTTON.CANCEL");
    }

    public static Map<String, String> SERVER_SCRIPT_TEMPLATE_MAP = new HashMap<>();

    static {
        SERVER_SCRIPT_TEMPLATE_MAP.put(ServerScriptType.utils.name(), "javaUtilsScriptTemplate");
        SERVER_SCRIPT_TEMPLATE_MAP.put(ServerScriptType.filter.name(), "javaFilterScriptTemplate");
        SERVER_SCRIPT_TEMPLATE_MAP.put(ServerScriptType.aspect.name(), "javaAspectScriptTemplate");
        SERVER_SCRIPT_TEMPLATE_MAP.put(ServerScriptType.eventListener.name(), "javaEventListenerScriptTemplate");
        SERVER_SCRIPT_TEMPLATE_MAP.put(ServerScriptType.custom.name(), "javaCustomScriptTemplate");
        SERVER_SCRIPT_TEMPLATE_MAP.put(ServerScriptType.processor.name(), "javaProcessorScriptTemplate");
    }

    /** 低代码（LCDP）切面注册完成KEY **/
    public static final String LCDP_ASPECT_FLAG = "lcdpAspectFlag";

    /** 使用低代码（LCDP）未提交切面KEY **/
    public static final String LCDP_ASPECT_UNCOMMITTED_FLAG = "lcdpAspectUncommittedFlag";

    /** 智能提示配置KEY **/
    public static final String LCDP_HINTS_PARAM_CODE = "hintsConfig";
    
    /** 默认字段 */
    // 启用标志字段
    public static final String LCDP_MAP_ACTIVATED_FLAG_KEY = "ACTIVATEDFLAG";
    // 注销标志(逻辑删除标志)字段
    public static final String LCDP_MAP_LAST_SUSPENDED_FLAG_KEY = "LASTSUSPENDEDFLAG";
    // 流程状态字段
    public static final String LCDP_MAP_PROCESS_STATUS_KEY = "PROCESSSTATUS";
    
    // 用于传递mapperId
    public static final String LCDP_MAPPER_ID_KEY = "_lcdp_mapper_id_";
    
    // 是否要更新mapper内容
    public static final String LCDP_MAPPER_UPDATE_KEY = "_lcdp_mapper_update_";
    // 是否要更新源代码种获取table的方法
    public static final String LCDP_TABLE_UPDATE_KEY = "_lcdp_table_update_";
    // 是否要更新setId的方法
    public static final String LCDP_SETID_UPDATE_KEY = "_lcdp_setid_update_";
    
    // 页面保存时，自动新增数据库或表字段时的提醒
    public static final String LCDP_AUTOMATIC_INSERT_COLIMNS_MSG_KEY = "_lcdp_auto_msg_";

    public static final String OPERATION_OF_ROLLBACK_CHECKOUT = "checkout";
    public static final String OPERATION_OF_ROLLBACK_DELETE = "delete";
    public static final String OPERATION_OF_ROLLBACK_SUBMIT = "submit";
    public static final String OPERATION_OF_ROLLBACK_CHECKIN_SUBMIT = "checkinSubmit";
    public static final String OPERATION_OF_ROLLBACK_CHECKIN_DELETE = "checkinDelete";
    public static final String OPERATION_OF_ROLLBACK_REVERT = "revert";
}

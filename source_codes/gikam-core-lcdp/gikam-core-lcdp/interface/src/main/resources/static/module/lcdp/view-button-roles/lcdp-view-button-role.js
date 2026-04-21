var lcdpViewButtonRole = Gikam.getPageObject({

    getEditListColumns : function () {
        return [ {
            checkbox : true
        }, {
            index :true
        },{
            field : 'resourceDesc',
            title : 'T_LCDP_VIEW_BUTTON_ROLE.PATH'
        } ,{
            field : 'path',
            title : 'T_LCDP_VIEW_BUTTON_ROLE.RESOURCEDESC'
        }];
    },

    getBaseInfoFormFields : function () {
        return [ {
            field : 'resourceName',
            title : 'T_LCDP_VIEW_BUTTON_ROLE.RESOURCENAME',
            validators : [ 'strLength[0,120]' ]
        }, {
            field : 'buttonDataId',
            title : 'T_LCDP_VIEW_BUTTON_ROLE.BUTTONDATAID',
            validators : [ 'strLength[0,12]' ]
        }, {
            field : 'buttonId',
            title : 'T_LCDP_VIEW_BUTTON_ROLE.BUTTONID',
            validators : [ 'strLength[0,90]' ]
        }, {
            field : 'buttonParentId',
            title : 'T_LCDP_VIEW_BUTTON_ROLE.BUTTONPARENTID',
            validators : [ 'strLength[0,90]' ]
        }, {
            field : 'buttonParentName',
            title : 'T_LCDP_VIEW_BUTTON_ROLE.BUTTONPARENTNAME',
            validators : [ 'strLength[0,90]' ]
        }, {
            field : 'visibleRoles',
            title : 'T_LCDP_VIEW_BUTTON_ROLE.VISIBLEROLES',
            validators : [ 'strLength[0,24]' ]
        }, {
            field : 'buttonName',
            title : 'T_LCDP_VIEW_BUTTON_ROLE.BUTTONNAME',
            validators : [ 'strLength[0,90]' ]
        } ];
    },

    getChooseListColumns : function () {
        return [ {
            index :true
        },{
            field : 'buttonParentName',
            title : '所属功能'
        }, {
            field : 'buttonName',
            title : 'T_LCDP_VIEW_BUTTON_ROLE.BUTTONNAME'
        },{
            field : 'visibleRoles',
            title : '角色配置',
            type : 'select',
            editor : true,
            multiple :true,
            search :true,
            category : 'role-select',
        } ];
    },


    getPageChooseListColumns : function () {
        return [ {
            field : 'path',
            title : 'T_LCDP_RESOURCE.PAGENAME',
            width : 300
        }, {
            field : 'resourceDesc',
            title : 'T_LCDP_RESOURCE.PAGEDESC',
            width : 150
        }, {
            field : 'ext$.category',
            title : 'T_LCDP_RESOURCE.PAGE_CATEGORY',
            width : 150
        }, {
            field : 'ext$.module',
            title : 'T_LCDP_RESOURCE.PAGE_MODULE',
            width : 150
        }, {
            field : 'ext$.menu',
            title : 'T_LCDP_RESOURCE.PAGE_MENU',
            width : 150
        } ];
    },

    baseUrl : IFM_CONTEXT + '/secure/core/module/lcdp/view-button-roles',

    lcdpBaseUrl : IFM_CONTEXT + '/secure/core/module/lcdp/resources',

    menuUrl : IFM_CONTEXT + '/secure/core/module/sys/menus',
});

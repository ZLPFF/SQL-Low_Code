var lcdpTable = Gikam.getPageObject({

    getChooseListColumns : function () {
        return [ {
            field : 'tableName',
            title : 'T_LCDP_TABLE.TABLENAME',
            width : 300
        },
        {
            field : 'tableDesc',
            title : 'T_LCDP_TABLE.TABLEDESC',
            width : 300
        }];
    },

    baseUrl : IFM_CONTEXT + '/secure/core/module/lcdp/tables'
});

var lcdpTableField = Gikam.getPageObject({

    getChooseListColumns : function () {
        return [ {
            field : 'fieldName',
            title : 'T_LCDP_TABLE_FIELD.FIELDNAME'
        }, {
            field : 'fieldComment',
            title : 'T_LCDP_TABLE_FIELD.FIELDCOMMENT'
        }, ];
    },

    baseUrl : IFM_CONTEXT + '/secure/core/module/lcdp/tables'
});

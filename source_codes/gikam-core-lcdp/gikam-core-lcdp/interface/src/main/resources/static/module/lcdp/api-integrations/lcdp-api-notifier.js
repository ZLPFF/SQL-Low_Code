var lcdpApiNotifier = Gikam.getPageObject({

    getEditListColumns : function () {
        return [ {
            checkbox : true
        }, {
            field : 'userId',
            title : 'T_LCDP_API_NOTIFIER.USERID'
        }, {
            field : 'userName',
            title : 'T_LCDP_API_NOTIFIER.USERNAME'
        }, {
            field : 'email',
            title : 'T_LCDP_API_NOTIFIER.EMAIL'
        }, {
            field : 'mobile',
            title : 'T_LCDP_API_NOTIFIER.MOBILE'
        } ];
    },

    baseUrl : IFM_CONTEXT + '/secure/cloud/module/lcdp/api-notifiers'
});

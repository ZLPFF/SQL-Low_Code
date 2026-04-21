var lcdpApiIntegration = Gikam.getPageObject({

    soapConfig : {
        webService : {
            name : '',
            webMethod : {
                operationName : '',
                webResult : {},
                webParam : []
            }
        },
        entityConfig : []
    },

    simpleDefinition : [
        'java.lang.String',
        'java.lang.Integer',
        'java.lang.Long',
        'java.lang.Double',
        'java.lang.Float',
        'java.lang.Boolean',
    ],

    getEditListColumns : function () {
        return [ {
            checkbox : true
        }, {
            field : 'apiCode',
            title : 'T_LCDP_API.APICODE'
        }, {
            field : 'apiName',
            title : 'T_LCDP_API.APINAME'
        }, {
            field : 'activatedFlag',
            title : 'T_LCDP_API.ACTIVATEDFLAG',
            type : 'select',
            category : 'activatedFlag'
        }, {
            field : 'apiUrl',
            title : 'T_LCDP_API.APIURL'
        }, {
            field : 'apiType',
            type : 'select',
            category : 'interfaceType',
            title : 'T_LCDP_API.APITYPE'
        }, {
            field : 'callType',
            type : 'select',
            category : 'interfaceInvokeType',
            title : 'T_LCDP_API.CALLTYPE'
        } ];
    },

    getPreInsertFormFields : function () {
        return [ {
            field : 'apiCode',
            title : 'T_LCDP_API.APICODE',
            preInsert : true,
            validators : [ 'remote[' + lcdpApiIntegration.baseUrl + '/action/validate-unique]', 'strLength[0,30]', 'notEmpty' ]
        }, {
            field : 'apiName',
            title : 'T_LCDP_API.APINAME',
            preInsert : true,
            validators : [ 'strLength[0,20]', 'notEmpty' ]
        }, {
            field : 'apiType',
            title : 'T_LCDP_API.APITYPE',
            preInsert : true,
            type : 'select',
            category : 'interfaceType',
            firstBlank : false,
            validators : [ 'notEmpty' ]
        }, {
            field : 'callType',
            title : 'T_LCDP_API.CALLTYPE',
            preInsert : true,
            type : 'select',
            category : 'interfaceInvokeType',
            firstBlank : false,
            validators : [ 'notEmpty' ]
        } ]
    },

    getBaseInfoFormOuterFields : function () {

        return [ {
            field : 'apiCode',
            title : 'T_LCDP_API.APICODE',
            readonly : true,
            validators : [ 'notEmpty' ],
            onChange : function () {
                lcdpApiIntegration.setOuterUrl()
            }
        }, {
            field : 'apiName',
            title : 'T_LCDP_API.APINAME',
            validators : [ 'strLength[0,20]', 'notEmpty' ]
        }, {
            field : 'apiType',
            title : 'T_LCDP_API.APITYPE',
            type : 'select',
            category : 'interfaceType',
            firstBlank : false,
            validators : [ 'notEmpty' ],
            onChange : function (field, value) {
                lcdpApiIntegration.setOuterUrl()
            }
        }, {
            field : 'callType',
            title : 'T_LCDP_API.CALLTYPE',
            type : 'select',
            firstBlank : false,
            category : 'interfaceInvokeType',
            validators : [ 'notEmpty' ],
            onChange : function (field, value) {

                lcdpApiIntegration.setOuterUrl()
                //显示隐藏panel
                Gikam.getAsyncComp('lcdp-api-integration-edit-list-base-info-tab').done(function (tab) {
                    if (value === 'outer') {
                        tab.removePanel('lcdp-api-integration-edit-list-field-panel')
                    } else {
                        tab.revertPanel('lcdp-api-integration-edit-list-field-panel')
                    }
                });
            }
        }, {
            type : 'inputGroup',
            field : 'apiUrlGroup',
            title : 'T_LCDP_API.APIURL',
            colspan : 2,
            items : [ {
                field : 'apiUrlPrefix',
                width : '200px',
                readonly : true
            }, {
                field : 'apiUrlSuffix'
            } ],
            onChange : function (field, value) {
                var form = Gikam.getComp('lcdp-api-integration-edit-list-base-info-form');
                var data = form.getData()
                data.apiUrl = (data.apiType === 'Restful' ? '/open/core/module/lcdp/api/' : '/open/core/module/lcdp/ws/') + value
                form.setData(data, true)
                form.loadData({apiUrlSuffix : value})
            },
            validators : [ 'notEmpty' ]
        }, {
            field : 'authentType',
            title : 'T_LCDP_API.AUTHENTTYPE',
            type : 'select',
            category : 'sys',
            param : {
                codeCategoryId : 'authentType'
            },
            onChange : function () {
                lcdpApiIntegration.setAuthentType()
            }
        }, {
            field : 'clientId',
            title : 'T_LCDP_API.CLIENTID',
            type : 'password'
        }, {
            field : 'secret',
            title : 'T_LCDP_API.SECRET',
            type : 'password',
            colspan : 2
        }, {
            field : 'authentHeader',
            title : 'T_LCDP_API.AUTHENTHEADER',
            type : 'codeEditor',
            colspan : 2,
            language : 'json',
        }, {
            field : 'apiDesc',
            type : 'Textarea',
            colspan : 2,
            title : 'T_LCDP_API.APIDESC'
        }, {
            field : 'scriptMethodPath',
            title : 'T_LCDP_API.SCRIPTMETHODPATH',
            colspan : 2,
            type : 'choose',
            category : 'lcdp-method',
            single : true,
            customData : {
                MAPPINGTYPE_SEQ : 'OPENAPI'
            },
            targetFields : [ {
                scriptMethodPath : 'methodPath'
            } ],
            validators : [ 'strLength[0,120]' ]
        }, {
            field : 'authentScript',
            title : 'T_LCDP_API.AUTHENTSCRIPT',
            colspan : 2,
            type : 'choose',
            category : 'lcdp-method',
            single : true,
            customData : {
                MAPPINGTYPE_SEQ : 'OPENAPI'
            },
            targetFields : [ {
                authentScript : 'methodPath'
            } ],
            validators : [ 'strLength[0,120]' ],
            placeholder : Gikam.propI18N('LCDP.MODULE.API_INTEGRATIONS.TITLE.AUTHENTSCRIPT_DESC')
        }, {
            field : 'restfulMethod',
            title : 'T_LCDP_API.RESTFULMETHOD',
            type : 'select',
            colspan : 2,
            firstBlank : false,
            items : [ {
                text : 'GET',
                value : 'GET'
            }, {
                text : 'PUT',
                value : 'PUT'
            }, {
                text : 'POST',
                value : 'POST'
            }, {
                text : 'DELETE',
                value : 'DELETE'
            }, {
                text : 'PATCH',
                value : 'PATCH'
            } ]
        }, {
            field : 'restfulHeader',
            title : 'T_LCDP_API.RESTFULHEADER',
            type : 'codeEditor',
            colspan : 2,
            language : 'json',
        }, {
            field : 'restfulQueryParam',
            title : 'T_LCDP_API.RESTFULQUERYPARAM',
            type : 'codeEditor',
            colspan : 2,
            language : 'json',
        }, {
            field : 'restfulReqBody',
            title : 'T_LCDP_API.RESTFULREQBODY',
            type : 'codeEditor',
            colspan : 2,
            language : 'json',
        } ];
    },

    getBaseInfoFormInvokeFields : function () {
        return [ {
            field : 'apiCode',
            title : 'T_LCDP_API.APICODE',
            readonly : true,
            validators : [ 'notEmpty' ],
            onChange : function () {
                lcdpApiIntegration.setOuterUrl()
            }
        }, {
            field : 'apiName',
            title : 'T_LCDP_API.APINAME',
            validators : [ 'strLength[0,20]', 'notEmpty' ]
        }, {
            field : 'apiType',
            title : 'T_LCDP_API.APITYPE',
            type : 'select',
            category : 'interfaceType',
            firstBlank : false,
            validators : [ 'notEmpty' ],
            onChange : function (field, value) {
                lcdpApiIntegration.setOuterUrl()
            }
        }, {
            field : 'callType',
            title : 'T_LCDP_API.CALLTYPE',
            type : 'select',
            firstBlank : false,
            category : 'interfaceInvokeType',
            validators : [ 'notEmpty' ],
            onChange : function (field, value) {

                lcdpApiIntegration.setOuterUrl()
                //显示隐藏panel
                Gikam.getAsyncComp('lcdp-api-integration-edit-list-base-info-tab').done(function (tab) {
                    if (value === 'outer') {
                        tab.removePanel('lcdp-api-integration-edit-list-field-panel')
                    } else {
                        tab.revertPanel('lcdp-api-integration-edit-list-field-panel')
                    }
                });
            }
        }, {
            field : 'apiUrl',
            title : 'T_LCDP_API.APIURL',
            validators : [ 'notEmpty' ],
            colspan : 2
        }, {
            field : 'authentType',
            title : 'T_LCDP_API.AUTHENTTYPE',
            type : 'select',
            category : 'sys',
            param : {
                codeCategoryId : 'authentType'
            },
            onChange : function () {
                lcdpApiIntegration.setAuthentType()
            }
        }, {
            field : 'clientId',
            title : 'T_LCDP_API.CLIENTID',
            type : 'password'
        }, {
            field : 'secret',
            title : 'T_LCDP_API.SECRET',
            type : 'password',
            colspan : 2
        }, {
            field : 'authentHeader',
            title : 'T_LCDP_API.AUTHENTHEADER',
            type : 'codeEditor',
            colspan : 2,
            language : 'json',
        }, {
            field : 'apiDesc',
            type : 'Textarea',
            colspan : 2,
            title : 'T_LCDP_API.APIDESC'
        }, {
            field : 'scriptMethodPath',
            title : 'T_LCDP_API.SCRIPTMETHODPATH',
            colspan : 2,
            type : 'choose',
            category : 'lcdp-method',
            single : true,
            customData : {
                MAPPINGTYPE_SEQ : 'OPENAPI'
            },
            targetFields : [ {
                scriptMethodPath : 'methodPath'
            } ],
            validators : [ 'strLength[0,120]' ]
        }, {
            field : 'authentScript',
            title : 'T_LCDP_API.AUTHENTSCRIPT',
            colspan : 2,
            type : 'choose',
            category : 'lcdp-method',
            single : true,
            customData : {
                MAPPINGTYPE_SEQ : 'OPENAPI'
            },
            targetFields : [ {
                authentScript : 'methodPath'
            } ],
            validators : [ 'strLength[0,120]' ],
            placeholder : Gikam.propI18N('LCDP.MODULE.API_INTEGRATIONS.TITLE.AUTHENTSCRIPT_DESC')
        }, {
            field : 'restfulMethod',
            title : 'T_LCDP_API.RESTFULMETHOD',
            type : 'select',
            colspan : 2,
            firstBlank : false,
            items : [ {
                text : 'GET',
                value : 'GET'
            }, {
                text : 'PUT',
                value : 'PUT'
            }, {
                text : 'POST',
                value : 'POST'
            }, {
                text : 'DELETE',
                value : 'DELETE'
            }, {
                text : 'PATCH',
                value : 'PATCH'
            } ]
        }, {
            field : 'restfulHeader',
            title : 'T_LCDP_API.RESTFULHEADER',
            type : 'codeEditor',
            colspan : 2,
            language : 'json',
        }, {
            field : 'restfulQueryParam',
            title : 'T_LCDP_API.RESTFULQUERYPARAM',
            type : 'codeEditor',
            colspan : 2,
            language : 'json',
        }, {
            field : 'restfulReqBody',
            title : 'T_LCDP_API.RESTFULREQBODY',
            type : 'codeEditor',
            colspan : 2,
            language : 'json',
        } ];
    },

    getFieldListColumns : function () {
        return [ {
            checkbox : true
        }, {
            field : 'apiField',
            title : 'T_LCDP_API_FIELD.APIFIELD',
            editor : true
        }, {
            field : 'systemField',
            title : 'T_LCDP_API_FIELD.SYSTEMFIELD',
            editor : true
        }, {
            field : 'fieldDesc',
            title : 'T_LCDP_API_FIELD.FIELDDESC',
            editor : true
        } ];
    },

    setAuthentType : function () {
        var form = Gikam.getComp('lcdp-api-integration-edit-list-base-info-form');
        var data = form.getData();

        if ('Restful' === data.apiType && data.authentType === 'header') {
            form.showFields([ 'authentHeader' ]);
            form.addFieldValidator('authentHeader', [ 'notEmpty' ]);

            form.hideFields([ 'clientId', 'secret', 'authentScript' ]);
            form.removeFieldValidator('clientId', [ 'notEmpty' ]);
            form.removeFieldValidator('secret', [ 'notEmpty' ]);
        } else if ('Restful' === data.apiType && Gikam.isNotEmpty(data.authentType)) {
            form.hideFields([ 'authentHeader' ]);
            form.removeFieldValidator('authentHeader', [ 'notEmpty' ]);

            form.showFields([ 'clientId', 'secret', 'authentScript' ]);
            form.addFieldValidator('clientId', [ 'notEmpty' ]);
            form.addFieldValidator('secret', [ 'notEmpty' ]);
        } else {
            form.hideFields([ 'authentHeader' ]);
            form.removeFieldValidator('authentHeader', [ 'notEmpty' ]);

            form.hideFields([ 'clientId', 'secret', 'authentScript' ]);
            form.removeFieldValidator('clientId', [ 'notEmpty' ]);
            form.removeFieldValidator('secret', [ 'notEmpty' ]);
        }
    },

    setOuterUrl : function () {
        var form = Gikam.getComp('lcdp-api-integration-edit-list-base-info-form');
        var data = form.getData();

        if ('outer' === data.callType) {
            if (data.activatedFlag === '1') {
                form.refreshFields(lcdpApiIntegration.getBaseInfoFormInvokeFields())
            } else {
                form.refreshFields(lcdpApiIntegration.getBaseInfoFormOuterFields())
                var apiPrefix = data.apiType === 'Restful' ? '/open/core/module/lcdp/api/' : '/open/core/module/lcdp/ws/'
                var apiSuffix = data.apiUrl ? data.apiUrl.replace(apiPrefix, '') : ''
                data.apiUrlPrefix = apiPrefix
                data.apiUrlSuffix = apiSuffix
                form.setData(data)
            }
            form.showFields([ 'scriptMethodPath' ])
            form.addFieldValidator('scriptMethodPath', [ 'notEmpty' ]);
            form.hideFields([ 'restfulMethod', 'restfulHeader', 'restfulQueryParam', 'restfulReqBody' ])

            if ('Restful' === data.apiType) {
                if (Gikam.isEmpty(data.authentType)) {
                    form.hideFields([ 'clientId', 'secret', 'authentHeader' ]);
                } else if (data.authentType === 'header') {
                    form.showFields([ 'authentHeader' ]);
                    form.hideFields([ 'clientId', 'secret' ]);
                } else {
                    form.hideFields([ 'authentHeader' ]);
                    form.showFields([ 'clientId', 'secret' ]);
                }
                form.showFields([ 'authentType' ]);
            } else {
                form.hideFields([ 'authentType', 'clientId', 'secret', 'authentHeader' ]);
            }

            form.setSelectOptions('authentType', [ {value : '', text : ''}, {
                value : 'authent',
                text : '鉴权'
            }, {value : 'authentCheck', text : '鉴权并加验'}, {value : 'header', text : '自定义header'} ]);
        } else {
            form.refreshFields(lcdpApiIntegration.getBaseInfoFormInvokeFields())
            form.hideFields([ 'scriptMethodPath' ])
            form.removeFieldValidator('scriptMethodPath', [ 'notEmpty' ]);
            form.hideFields([ 'authentHeader' ])
            form.removeFieldValidator('authentHeader', [ 'notEmpty' ]);
            if (data.authentType === 'header') {
                form.setData({
                    authentType : 'authent',
                })
            }

            if ('Restful' === data.apiType) {
                if (Gikam.isNotEmpty(data.authentType)) {
                    form.showFields([ 'clientId', 'secret' ])
                } else {
                    form.hideFields([ 'clientId', 'secret' ])
                }
                form.showFields([ 'restfulMethod', 'restfulHeader', 'restfulQueryParam', 'restfulReqBody', 'authentType', ])
            } else {
                form.hideFields([ 'restfulMethod', 'restfulHeader', 'restfulQueryParam', 'restfulReqBody', 'authentType', 'clientId', 'secret' ])
            }

            form.setSelectOptions('authentType', [ {value : '', text : ''}, {
                value : 'authent',
                text : '鉴权'
            }, {value : 'authentCheck', text : '鉴权并加验'} ]);
        }

        setTimeout(function () {
            form.setReadonly(data.activatedFlag === '1')
        }, 500)
    },

    getTestFormFields : function () {
        return [ {
            field : 'testOperation',
            title : 'T_LCDP_API.REQUEST_OPERATION',
            type : 'text',
            colspan : 2,
            validators : [ 'notEmpty' ]
        }, {
            field : 'testParams',
            title : 'T_LCDP_API.REQUEST_ARGS',
            type : 'codeEditor',
            colspan : 2,
            height : 200,
            language : 'json',
        }, {
            field : 'testReqBody',
            title : 'T_LCDP_API.REQUEST_BODY',
            type : 'codeEditor',
            colspan : 2,
            height : 200,
            language : 'json',
        }, {
            field : 'testHeader',
            title : 'T_LCDP_API.REQUEST_HEADER',
            type : 'codeEditor',
            colspan : 2,
            height : 150,
            language : 'json',
        }, {
            field : 'response',
            title : 'T_LCDP_API.RESPONSE',
            type : 'codeEditor',
            colspan : 2,
            readonly : true,
            language : 'json'
        } ];
    },

    getWSMethodColumn : function () {
        return [ {
            title : 'T_LCDP_API.OPERATIONNAME',
            field : 'operationName',
            validators : [ 'notEmpty' ],
            type : 'text',
        } ]
    },

    getMethodResultColumn : function () {
        return [ {
            title : 'T_LCDP_API.PARAMTYPE',
            field : 'paramType',
            type : 'select',
            validators : [ 'notEmpty' ],

            items : [ {
                text : 'bean',
                value : 'bean'
            }, {
                text : 'simple',
                value : 'simple'
            }, {
                text : 'void',
                value : 'void'
            } ],
            onChange : function (field, value) {
                var items
                if (value === 'bean') {
                    items = lcdpApiIntegration.soapConfig.entityConfig.map(function (v) {
                        return {text : v.entityName, value : v.entityName}
                    })
                } else if (value === 'simple') {
                    items = lcdpApiIntegration.simpleDefinition.map(function (v) {
                        return {text : v, value : v}
                    })
                }

                Gikam.getComp('lcdp-api-integration-method-result-form').setSelectOptions('paramDefinition', items)
            }
        }, {
            title : 'T_LCDP_API.PARAMNAME',
            field : 'name',
            validators : [ 'notEmpty' ],
            type : 'text',
        }, {
            title : 'T_LCDP_API.PARAMDEFINITION',
            field : 'paramDefinition',
            validators : [ 'notEmpty' ],
            type : 'select',
        }, {
            title : 'T_LCDP_API.PARTNAME',
            field : 'partName',
            type : 'text',
        }, {
            title : 'T_LCDP_API.TARGETNAME',
            field : 'targetName',
            type : 'text',
        } ]
    },

    getMethodParamColumn : function () {
        return [ {
            checkbox : true
        }, {
            title : 'T_LCDP_API.PARAMNAME',
            field : 'name',
            validators : [ 'notEmpty' ],
            editor : true,
            type : 'text',
        }, {
            title : 'T_LCDP_API.PARAMTYPE',
            field : 'paramType',
            validators : [ 'notEmpty' ],
            editor : true,
            type : 'select',
            items : [ {
                text : 'bean',
                value : 'bean'
            }, {
                text : 'simple',
                value : 'simple'
            } ]
        }, {
            title : 'HEADER',
            field : 'header',
            validators : [ 'notEmpty' ],
            editor : true,
            type : 'select',
            items : [ {
                text : 'true',
                value : true
            }, {
                text : 'false',
                value : false
            } ]
        }, {
            title : 'isArray',
            field : 'arrayFlag',
            validators : [ 'notEmpty' ],
            editor : true,
            type : 'select',
            items : [ {
                text : 'true',
                value : true
            }, {
                text : 'false',
                value : false
            } ]
        }, {
            title : 'T_LCDP_API.PARAMDEFINITION',
            field : 'paramDefinition',
            validators : [ 'notEmpty' ],
            editor : true,
            type : 'select',
            onBeforeEditorRender : function (row) {
                var value = row.paramType
                var items
                if (value === 'bean') {
                    items = lcdpApiIntegration.soapConfig.entityConfig.map(function (v) {
                        return {text : v.entityName, value : v.entityName}
                    })
                } else if (value === 'simple') {
                    items = lcdpApiIntegration.simpleDefinition.map(function (v) {
                        return {text : v, value : v}
                    })
                }
                return {
                    type : 'select',
                    items : items
                }
            }
        }, {
            title : 'T_LCDP_API.PARTNAME',
            field : 'partName',
            editor : true,
            type : 'text',
        }, {
            title : 'T_LCDP_API.TARGETNAME',
            field : 'targetName',
            editor : true,
            type : 'text',
        } ]
    },

    getEntityColumn : function () {
        return [ {
            checkbox : true
        }, {
            title : 'T_LCDP_API.ENTITYNAME',
            field : 'entityName',
            editor : true,
            type : 'text',
        }, {
            title : 'T_LCDP_API.ENTITYEXAMPLE',
            field : 'entityExample',
            editor : true,
            type : 'text',
        } ]
    },

    getReqColumnList : function () {
        return [ {
            checkbox : true
        }, {
            title : 'T_LCDP_API_REQ.REQUESTDATETIME',
            field : 'requestDateTime',
            width : 200,
        }, {
            title : 'T_LCDP_API_REQ.STATUS',
            field : 'status',
            type : 'select',
            width : 90,
            category : 'sys',
            param : {
                codeCategoryId : 'remoteCallRequestStatus'
            },
        }, {
            title : 'T_LCDP_API_REQ.REQUESTBODY',
            field : 'requestBody',
            type : 'link',
            filter : false,
            renderToText: true,
            width : 210,
            formatter : function (index, value, row, text) {
                if (Gikam.isEmpty(value) || value === 'null') {
                    return "-";
                } else {
                    return value;
                }
            }
        }, {
            title : 'HEADER',
            field : 'requestHeader',
            type : 'link',
            filter : false,
            width : 210,
            renderToText: true,
            formatter : function (index, value, row, text) {
                if (Gikam.isEmpty(value) || value === 'null') {
                    return "-";
                } else {
                    return value;
                }
            }
        } ]
    },

    getResColumnList : function () {
        return [ {
            checkbox : true
        }, {
            title : 'T_LCDP_API_RES.RESPONSEDATETIME',
            field : 'responseDateTime',
            width : 150,
        }, {
            title : 'T_LCDP_API_RES.STATUS',
            field : 'status',
            width : 90,
            type : 'select',
            category : 'sys',
            param : {
                codeCategoryId : 'remoteCallResponseStatus'
            },
        }, {
            title : 'T_LCDP_API_RES.RESPONSEBODY',
            field : 'responseBody',
            type : 'link',
            filter : false,
            renderToText: true,
            width : 200
        }, {
            title : 'T_LCDP_API_RES.LOG',
            field : 'log',
            width : 150,
            type : 'link',
            filter : false,
            titleFormatter : function () {
                return "";
            }
        }, {
            title : 'T_LCDP_API_RES.TIMES',
            field : 'times',
        } ]
    },
    getEditTestFormFields : function () {
        return [{
            field : 'testReqBody',
            title : 'T_LCDP_API.REQUEST_BODY',
            type : 'codeEditor',
            colspan : 2,
            height : 200,
            language : 'json',
        }, {
            field : 'testHeader',
            title : 'T_LCDP_API.REQUEST_HEADER',
            type : 'codeEditor',
            colspan : 2,
            height : 150,
            language : 'json',
        } ];
    },
    baseUrl : IFM_CONTEXT + '/secure/core/module/lcdp/api-integrations'
});

(function(window) {
    if (window.GikamLcdpSourceRuntime) {
        return;
    }

    var cacheRequire = new Map();
    var sequence = 0;

    function nextSequenceValue() {
        sequence += 1;
        return '' + new Date().getTime() + sequence;
    }

    function requireByPath(url) {
        if (cacheRequire.get(url)) {
            return cacheRequire.get(url).exports;
        }

        var module = { exports: {} };
        var script = '';

        if (Gikam.isLcdpPath && Gikam.isLcdpPath(url)) {
            var resourceUrl = Gikam.getContextUrl('/secure/core/module/lcdp/resources/search-by-path');
            var json = Gikam.getJsonWrapper({ resourcePath: url });
            Gikam.postSync(resourceUrl, json).done(function(resource) {
                script = resource.content;
            });
        } else {
            Gikam.getTextSync(url).done(function(content) {
                script = content;
            });
        }

        new Function('module', script)(module);
        cacheRequire.set(url, module);
        return module.exports;
    }

    function getCurrentWindow() {
        if (window.workspace && workspace.window) {
            return workspace.window;
        }
        if (Gikam.getLastModal && Gikam.getLastModal()) {
            return Gikam.getLastModal().window;
        }
        return {
            getPageParam: function() {
                return {};
            }
        };
    }

    function formatterFunction(widgetConfig, functionMapper) {
        if (!widgetConfig) {
            return;
        }
        var names = [];
        for (var property in widgetConfig) {
            if (property.endsWith('__$S$__')) {
                var name = property.replace('__$S$__', '');
                widgetConfig[name] = functionMapper[widgetConfig[property]];
                names.push(property);
            }
        }
        names.forEach(function(name) {
            delete widgetConfig[name];
        });
    }

    function sortRootWidget(rootWidgetList) {
        return rootWidgetList.reduce(function(total, item) {
            var prevIndex = total.findIndex(function(rootItem) {
                return rootItem.id === item.previousId;
            });
            if (!item.previousId || prevIndex === -1) {
                total.push(item);
                return total;
            }
            total.splice(prevIndex + 1, 0, item);
            return total;
        }, []);
    }

    function getLocale() {
        var locale = Gikam.component.FrontI18N.getLocale();
        if (!locale) {
            return 'zh-CN';
        }
        return locale.replace(/-\w+/, function(value) {
            return value.toUpperCase();
        });
    }

    function parseLocale(widgetConfig, resourceI18n) {
        var locale = getLocale();
        var localeMapper = {};
        for (var property in widgetConfig) {
            var value = widgetConfig[property];
            if (value && typeof value === 'object') {
                if (typeof value['zh-CN'] !== 'undefined' || value.type === 'i18n') {
                    var key = value.i18nCode;
                    var message = resourceI18n && resourceI18n[key] && resourceI18n[key][locale];
                    message = message || Gikam.component.I18N.map[key] || value[locale];
                    if (message) {
                        widgetConfig[property] = message;
                        localeMapper['$_' + property] = value;
                    } else {
                        delete widgetConfig[property];
                    }
                } else if (Gikam.isNotEmpty(value)) {
                    parseLocale(value, resourceI18n);
                }
            }
        }
        Object.assign(widgetConfig, localeMapper);
    }

    function parseNormal(widget) {
        var widgetConfig = widget.config;
        widgetConfig.type = Gikam.toInitialLowerCase(widget.type);
        return widgetConfig;
    }

    function parseGridColumn(widget) {
        var result = widget.config;
        var id = result.id;
        var type = result.type;
        var dbTable = result.dbTable;
        var width = result.width;
        if (id) {
            delete result.id;
        }
        if (type === 'processStatus') {
            var param = result.param ? result.param.split(',') : [];
            return Gikam.status && Gikam.status.getBpmnColumn
                ? Gikam.status.getBpmnColumn.apply(Gikam.status, [dbTable].concat(param).concat([width]))
                : result;
        }
        return result;
    }

    function parseDropdownMenu(widget, widgetMapper, functionMapper) {
        var widgetConfig = widget.config;
        widgetConfig.type = widget.type;
        widgetConfig.items = widgetConfig.childrenWidgetId.map(function(subChildrenId) {
            return parseNormal(widgetMapper[subChildrenId], widgetMapper, functionMapper);
        });
        widgetConfig.type = Gikam.toInitialLowerCase(widgetConfig.type);
        return widgetConfig;
    }

    function parseButtonToolbar(widget, widgetMapper, functionMapper) {
        var widgetConfig = widget.config;
        widgetConfig.type = Gikam.toInitialLowerCase(widget.type);
        widgetConfig.items = widgetConfig.childrenWidgetId.map(function(subChildrenId) {
            var subChildrenWidget = widgetMapper[subChildrenId];
            var parse = parseMapper[subChildrenWidget.type] || parseMapper.Normal;
            return parse(subChildrenWidget, widgetMapper, functionMapper);
        });
        return widgetConfig;
    }

    function parseButtonGroup(widget, widgetMapper, functionMapper) {
        var widgetConfig = widget.config;
        widgetConfig.type = Gikam.toInitialLowerCase(widget.type);
        widgetConfig.items = widgetConfig.childrenWidgetId.map(function(subChildrenId) {
            var subChildrenWidget = widgetMapper[subChildrenId];
            var parse = parseMapper[subChildrenWidget.type] || parseMapper.Normal;
            return parse(subChildrenWidget, widgetMapper, functionMapper);
        });
        widgetConfig.field = widgetConfig.field || nextSequenceValue();
        return widgetConfig;
    }

    function parseWindowToolbar(widget, widgetMapper, functionMapper) {
        var widgetConfig = widget.config;
        widgetConfig.items = widgetConfig.childrenWidgetId.map(function(subChildrenId) {
            var subChildrenWidget = widgetMapper[subChildrenId];
            var parse = parseMapper[subChildrenWidget.type] || parseMapper.Normal;
            return parse(subChildrenWidget, widgetMapper, functionMapper);
        });
        return widgetConfig;
    }

    function parseShuttleFrame(widget, widgetMapper, functionMapper) {
        var result = widget.config;
        result.type = Gikam.toInitialLowerCase(widget.type);
        for (var property in result) {
            if (property.endsWith('_childrenWidgetId')) {
                var realProperty = property.replace('_childrenWidgetId', '');
                var items = [];
                (result[property] || []).forEach(function(childrenId) {
                    var child = widgetMapper[childrenId].config;
                    if (property === 'leftToolbar_childrenWidgetId' || property === 'rightToolbar_childrenWidgetId') {
                        (child.childrenWidgetId || []).forEach(function(subChildrenId) {
                            var subChildrenWidget = widgetMapper[subChildrenId];
                            var parse = parseMapper[subChildrenWidget.type] || parseMapper.Normal;
                            items.push(parse(subChildrenWidget, widgetMapper, functionMapper));
                        });
                        if (property === 'leftToolbar_childrenWidgetId') {
                            result.leftGeneralButtonGroupShow = child.generalButtonGroupShow;
                            result.leftToolbarWrap = child.toolbarWrap;
                        } else {
                            result.rightGeneralButtonGroupShow = child.generalButtonGroupShow;
                            result.rightToolbarWrap = child.toolbarWrap;
                        }
                    } else {
                        var parseFunc = parseMapper[child.type] || parseMapper.Normal;
                        items.push(parseFunc(widgetMapper[childrenId], widgetMapper, functionMapper));
                    }
                });
                result[realProperty] = items;
            }
        }
        return result;
    }

    function parseGrid(widget, widgetMapper, functionMapper) {
        var result = widget.config;
        result.type = widget.type;
        result.createdByLcdp = true;
        if (Gikam.isNotEmpty(result.readonly)) {
            delete result.readonly;
        }
        result.columns = result.childrenWidgetId.map(function(subChildrenId) {
            return parseGridColumn(widgetMapper[subChildrenId], widgetMapper, functionMapper);
        });
        for (var property in result) {
            if (property.endsWith('_childrenWidgetId')) {
                var realProperty = property.replace('_childrenWidgetId', '');
                var items = [];
                (result[property] || []).forEach(function(childrenId) {
                    var child = widgetMapper[childrenId].config;
                    if (property === 'toolbar_childrenWidgetId') {
                        (child.childrenWidgetId || []).forEach(function(subChildrenId) {
                            var subChildrenWidget = widgetMapper[subChildrenId];
                            var parse = parseMapper[subChildrenWidget.type] || parseMapper.Normal;
                            items.push(parse(subChildrenWidget, widgetMapper, functionMapper));
                        });
                    } else {
                        var parseFunc = parseMapper[child.type] || parseMapper.Normal;
                        if (property === 'searchPanelOptions_childrenWidgetId') {
                            items = parseFunc(widgetMapper[childrenId], widgetMapper, functionMapper);
                        } else {
                            items.push(parseFunc(widgetMapper[childrenId], widgetMapper, functionMapper));
                        }
                    }
                });
                result[realProperty] = items;
            }
            if (property === 'group' || property === 'export') {
                formatterFunction(result[property], functionMapper);
            }
        }
        result.type = Gikam.toInitialLowerCase(result.type);
        return result;
    }

    function parseLayout(layoutWidget, widgetMapper, functionMapper) {
        var result = layoutWidget.config;
        result.type = layoutWidget.type;
        result.childrenWidgetId.forEach(function(childrenWidgetId) {
            var childWidget = widgetMapper[childrenWidgetId];
            var childrenType = childWidget.type.toLowerCase().replace('layout', '');
            result[childrenType] = childWidget.config;
            result[childrenType].items = result[childrenType].childrenWidgetId.map(function(subChildrenId) {
                var subChildrenWidget = widgetMapper[subChildrenId];
                var parse = parseMapper[subChildrenWidget.type] || parseMapper.Normal;
                return parse(subChildrenWidget, widgetMapper, functionMapper);
            });
        });
        result.type = Gikam.toInitialLowerCase(result.type);
        return result;
    }

    function parseForm(widget, widgetMapper, functionMapper) {
        var widgetConfig = widget.config;
        var panels = [];
        var normalPanel = { fields: [] };
        delete widgetConfig.panels;
        widgetConfig.childrenWidgetId.forEach(function(subChildrenId) {
            var subChildrenWidget = widgetMapper[subChildrenId];
            if (subChildrenWidget.type === 'FormPanel') {
                panels.push(parseFormPanel(subChildrenWidget, widgetMapper, functionMapper));
            } else {
                var parse = parseMapper[subChildrenWidget.type] || parseMapper.Normal;
                normalPanel.fields.push(parse(subChildrenWidget, widgetMapper, functionMapper));
            }
        });
        if (Gikam.isEmpty(panels)) {
            widgetConfig.fields = normalPanel.fields;
        } else {
            if (Gikam.isNotEmpty(normalPanel.fields)) {
                panels.unshift(normalPanel);
            }
            widgetConfig.panels = panels;
        }
        if (widgetConfig.caption) {
            widgetConfig.caption = { text: widgetConfig.caption };
        }
        widgetConfig.type = Gikam.toInitialLowerCase(widgetConfig.type);
        widgetConfig.isLcdpConverted = true;
        widgetConfig.createdByLcdp = true;
        return widgetConfig;
    }

    function parseFormPanel(widget, widgetMapper, functionMapper) {
        var widgetConfig = widget.config;
        widgetConfig.fields = widgetConfig.childrenWidgetId.map(function(subChildrenId) {
            var subChildrenWidget = widgetMapper[subChildrenId];
            var parse = parseMapper[subChildrenWidget.type] || parseMapper.Normal;
            return parse(subChildrenWidget, widgetMapper, functionMapper);
        });
        if (widgetConfig.caption) {
            widgetConfig.caption = { text: widgetConfig.caption };
        }
        return widgetConfig;
    }

    function parseTab(widget, widgetMapper, functionMapper) {
        var widgetConfig = widget.config;
        widgetConfig.panels = widgetConfig.childrenWidgetId.map(function(subChildrenId) {
            var subChildrenWidget = widgetMapper[subChildrenId];
            var parse = parseMapper[subChildrenWidget.type] || parseMapper.Normal;
            return parse(subChildrenWidget, widgetMapper, functionMapper);
        });
        widgetConfig.type = Gikam.toInitialLowerCase(widgetConfig.type);
        return widgetConfig;
    }

    function parseTabPanel(widget, widgetMapper, functionMapper) {
        var widgetConfig = widget.config;
        widgetConfig.items = widgetConfig.childrenWidgetId.map(function(subChildrenId) {
            var subChildrenWidget = widgetMapper[subChildrenId];
            var parse = parseMapper[subChildrenWidget.type] || parseMapper.Normal;
            return parse(subChildrenWidget, widgetMapper, functionMapper);
        });
        return widgetConfig;
    }

    function parseDependentWidget(compMapper) {
        var dependentMapper = {};
        for (var compId in compMapper) {
            var comp = compMapper[compId].config;
            if (comp.dependentWidgetId) {
                if (!dependentMapper[comp.dependentWidgetId]) {
                    dependentMapper[comp.dependentWidgetId] = [];
                }
                dependentMapper[comp.dependentWidgetId].push(comp.id);
                comp.$dependentWidgetId = comp.dependentWidgetId;
            }
            if (Gikam.isNotEmpty(comp.childrenOptions)) {
                comp.$cascadeChildrenId = comp.$cascadeChildrenId || [];
                comp.childrenOptions.forEach(function(item) {
                    var childComp = compMapper[item.compId].config;
                    childComp.$dependentWidgetId = comp.id;
                    childComp.$dependentQueryParams = item.requestParams;
                    comp.$cascadeChildrenId.push(childComp.id);
                });
            }
        }
        for (var dependentId in dependentMapper) {
            var dependentComp = compMapper[dependentId];
            if (!dependentComp) {
                continue;
            }
            dependentComp.config.$cascadeChildrenId = dependentComp.config.$cascadeChildrenId || [];
            Array.prototype.push.apply(dependentComp.config.$cascadeChildrenId, dependentMapper[dependentId]);
        }
    }

    var parseMapper = {
        Layout: parseLayout,
        Grid: parseGrid,
        TreeGrid: parseGrid,
        Form: parseForm,
        Normal: parseNormal,
        Tab: parseTab,
        TabPanel: parseTabPanel,
        DropdownMenu: parseDropdownMenu,
        ButtonToolbar: parseButtonToolbar,
        ButtonGroup: parseButtonGroup,
        ShuttleFrame: parseShuttleFrame
    };

    function parseLcdp(widgetList, content, resourceI18n) {
        if (!widgetList && !content) {
            return { components: [], windowToolbar: {}, functionMapper: {}, widgetList: [] };
        }
        var functionMapper = {};
        var functionList = [];
        var appendContent = '';
        if (content) {
            var matchFunction = content.match(/.*function\\s+\\w+\\s*\\(/g);
            if (matchFunction) {
                matchFunction.forEach(function(item) {
                    var trimLineContent = item.trim();
                    if (trimLineContent.startsWith('//') || trimLineContent.startsWith('/*')) {
                        return;
                    }
                    functionList.push(item.replace(/.*function\\s+|\\(/g, '').trim());
                });
            }
            appendContent = functionList.reduce(function(total, item) {
                return total + ';functionMapper[\\'' + item + '\\']=' + item + ';';
            }, '');
        }
        try {
            var script = Function('functionMapper', 'CURRENT_WINDOW', 'require', (content || '') + '\\r\\n' + appendContent);
            script(functionMapper, getCurrentWindow(), requireByPath);
        } catch (e) {
            throw new Error('PARSE_LCDP_ERROR: ' + e.message);
        }
        var rootWidgetList = [];
        var widgetMapper = {};
        var resultWidgetList = [];
        var compMapper = {};
        widgetList.forEach(function(comp) {
            comp.config = JSON.parse(comp.config);
            parseLocale(comp.config, resourceI18n);
            formatterFunction(comp.config, functionMapper);
            widgetMapper[comp.id] = comp;
            if (comp.config.id) {
                compMapper[comp.config.id] = comp;
            }
            if (!comp.parentId) {
                rootWidgetList.push(comp);
            }
        });
        parseDependentWidget(compMapper);
        rootWidgetList = sortRootWidget(rootWidgetList);
        var windowToolbar = {};
        var winToolbarIndex = rootWidgetList.findIndex(function(item) {
            return item.type && item.type.toLowerCase() === 'windowtoolbar';
        });
        if (winToolbarIndex > -1) {
            windowToolbar = parseWindowToolbar(rootWidgetList.splice(winToolbarIndex, 1)[0], widgetMapper, functionMapper);
        }
        rootWidgetList.forEach(function(widget) {
            var parse = parseMapper[widget.type] || parseMapper.Normal;
            resultWidgetList.push(parse(widget, widgetMapper, functionMapper));
        });
        return { components: resultWidgetList, windowToolbar: windowToolbar, functionMapper: functionMapper, widgetList: widgetList };
    }

    function rewritePagePaths(content, pagePathMap) {
        var rewritten = content || '';
        if (!pagePathMap) {
            return rewritten;
        }
        for (var path in pagePathMap) {
            rewritten = rewritten.split(path).join(pagePathMap[path]);
        }
        return rewritten;
    }

    function renderRootComponent(component) {
        var config = Object.assign({}, component);
        var type = config.type;
        delete config.type;
        if (!config.renderTo && window.workspace && workspace.window) {
            config.renderTo = workspace.window.$dom;
        }
        Gikam.create(type, config);
    }

    function renderPage(options) {
        var currentWindow = getCurrentWindow();
        var parseResult = parseLcdp(
            (options.widgetList || []).map(function(item) {
                return Object.assign({}, item);
            }),
            rewritePagePaths(options.content, options.pagePathMap),
            options.i18n || {}
        );
        var doRender = function() {
            var components = parseResult.components || [];
            if (!components.length) {
                return;
            }
            if (components.length === 1) {
                renderRootComponent(components[0]);
                return;
            }
            Gikam.create('layout', {
                renderTo: window.workspace && workspace.window ? workspace.window.$dom : null,
                center: {
                    items: components
                }
            });
        };

        if (parseResult.functionMapper && typeof parseResult.functionMapper.onBeforePageRender === 'function') {
            parseResult.functionMapper.onBeforePageRender(doRender);
        } else {
            doRender();
        }

        if (parseResult.windowToolbar && currentWindow) {
            if (typeof currentWindow.refreshToolbar === 'function') {
                currentWindow.refreshToolbar(parseResult.windowToolbar);
            } else if (typeof currentWindow.setToolbar === 'function') {
                currentWindow.setToolbar(parseResult.windowToolbar);
            }
        }
        return parseResult;
    }

    window.GikamLcdpSourceRuntime = {
        parseLcdp: parseLcdp,
        renderPage: renderPage
    };
})(window);

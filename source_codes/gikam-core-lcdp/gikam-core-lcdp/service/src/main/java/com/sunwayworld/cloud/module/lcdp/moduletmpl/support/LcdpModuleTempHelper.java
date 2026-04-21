package com.sunwayworld.cloud.module.lcdp.moduletmpl.support;

import java.util.HashMap;
import java.util.Map;

public class LcdpModuleTempHelper {

    public static Map<String, String> FIELD_TMPL_MAP = new HashMap<>();

    static {
        FIELD_TMPL_MAP.put("Text", "{\n" +
                "  \"childrenWidgetId\": [],\n" +
                "  \"onInput__$S$__\": \"\",\n" +
                "  \"titleTip\": {\n" +
                "    \"en-US\": \"\",\n" +
                "    \"zh-CN\": \"\"\n" +
                "  },\n" +
                "  \"onFocus__$S$__\": \"\",\n" +
                "  \"title\": {\n" +
                "    \"en-US\": \"%s\",\n" +
                "    \"zh-CN\": \"%s\"\n" +
                "  },\n" +
                "  \"field\": \"%s\",\n" +
                "  \"uuid\": \"59b4a79f71ce491cb63f2c4d5a7375e9\",\n" +
                "  \"type\": \"Text\",\n" +
                "  \"required\": false,\n" +
                "  \"onChange__$S$__\": \"\",\n" +
                "  \"colspan\": 1,\n" +
                "  \"onKeyDown__$S$__\": \"\",\n" +
                "  \"textStyleFormatter__$S$__\": \"\",\n" +
                "  \"readonly\": false,\n" +
                "  \"width\": \"\",\n" +
                "  \"tip\": {\n" +
                "    \"en-US\": \"\",\n" +
                "    \"zh-CN\": \"\"\n" +
                "  },\n" +
                "  \"id\": \"%s\",\n" +
                "  \"placeholder\": {\n" +
                "    \"en-US\": \"\",\n" +
                "    \"zh-CN\": \"\"\n" +
                "  },\n" +
                "  \"value\": \"\",\n" +
                "  \"onBlur__$S$__\": \"\"\n" +
                "}");

        FIELD_TMPL_MAP.put("Number", "{\n" +
                "  \"childrenWidgetId\": [],\n" +
                "  \"precision\": 0,\n" +
                "  \"onInput__$S$__\": \"\",\n" +
                "  \"range\": \"\",\n" +
                "  \"onFocus__$S$__\": \"\",\n" +
                "  \"title\": {\n" +
                "    \"en-US\": \"%s\",\n" +
                "    \"zh-CN\": \"%s\"\n" +
                "  },\n" +
                "  \"field\": \"%s\",\n" +
                "  \"uuid\": \"59b4a79f71ce491cb63f2c4d5a7375e9\",\n" +
                "  \"type\": \"Number\",\n" +
                "  \"required\": false,\n" +
                "  \"colspan\": 1,\n" +
                "  \"isInteger\": false,\n" +
                "  \"onKeyDown__$S$__\": \"\",\n" +
                "  \"readonly\": false,\n" +
                "  \"tip\": {\n" +
                "    \"en-US\": \"\",\n" +
                "    \"zh-CN\": \"\"\n" +
                "  },\n" +
                "  \"id\": \"%s\",\n" +
                "  \"placeholder\": {\n" +
                "    \"en-US\": \"\",\n" +
                "    \"zh-CN\": \"\"\n" +
                "  },\n" +
                "  \"value\": \"\",\n" +
                "  \"titleTip\": {\n" +
                "    \"en-US\": \"\",\n" +
                "    \"zh-CN\": \"\"\n" +
                "  },\n" +
                "  \"onChange__$S$__\": \"\",\n" +
                "  \"hasArrow\": true,\n" +
                "  \"textStyleFormatter__$S$__\": \"\",\n" +
                "  \"width\": \"\",\n" +
                "  \"step\": 1,\n" +
                "  \"onBlur__$S$__\": \"\"\n" +
                "}");

        FIELD_TMPL_MAP.put("Date", "{\n" +
                "  \"childrenWidgetId\": [],\n" +
                "  \"dateRange\": [],\n" +
                "  \"titleTip\": {\n" +
                "    \"en-US\": \"\",\n" +
                "    \"zh-CN\": \"\"\n" +
                "  },\n" +
                "  \"title\": {\n" +
                "    \"en-US\": \"%s\",\n" +
                "    \"zh-CN\": \"%s\"\n" +
                "  },\n" +
                "  \"field\": \"%s\",\n" +
                "  \"uuid\": \"59b4a79f71ce491cb63f2c4d5a7375e9\",\n" +
                "  \"type\": \"Date\",\n" +
                "  \"required\": false,\n" +
                "  \"onChange__$S$__\": \"\",\n" +
                "  \"colspan\": 1,\n" +
                "  \"onSelectBefore__$S$__\": \"\",\n" +
                "  \"readonly\": false,\n" +
                "  \"width\": \"\",\n" +
                "  \"tip\": {\n" +
                "    \"en-US\": \"\",\n" +
                "    \"zh-CN\": \"\"\n" +
                "  },\n" +
                "  \"id\": \"%s\",\n" +
                "  \"placeholder\": {\n" +
                "    \"en-US\": \"\",\n" +
                "    \"zh-CN\": \"\"\n" +
                "  },\n" +
                "  \"allowClear\": true,\n" +
                "  \"value\": \"\"\n" +
                "}");

        FIELD_TMPL_MAP.put("textGridColumn", "{\n" +
                "  \"editor\": true,\n" +
                "  \"sort\": true,\n" +
                "  \"childrenWidgetId\": [],\n" +
                "  \"visible\": true,\n" +
                "  \"children\": [\n" +
                "    {}\n" +
                "  ],\n" +
                "  \"width\": 150,\n" +
                "  \"fixed\": false,\n" +
                "  \"title\": {\n" +
                "    \"en-US\": \"%s\",\n" +
                "    \"zh-CN\": \"%s\"\n" +
                "  },\n" +
                "  \"id\": \"%s\",\n" +
                "  \"field\": \"%s\",\n" +
                "  \"gridColumnType\": \"text\",\n" +
                "  \"uuid\": \"f271442f8df847e98f07410822913517\"\n" +
                "}");
        FIELD_TMPL_MAP.put("dateGridColumn", "{\n" +
                "  \"editor\": true,\n" +
                "  \"childrenWidgetId\": [],\n" +
                "  \"visible\": true,\n" +
                "  \"validators\": [],\n" +
                "  \"sort\": true,\n" +
                "  \"title\": {\n" +
                "    \"en-US\": \"%s\",\n" +
                "    \"zh-CN\": \"%s\"\n" +
                "  },\n" +
                "  \"gridColumnType\": \"date\",\n" +
                "  \"type\": \"date\",\n" +
                "  \"uuid\": \"6da886db0948409ab47bcd366919a8d4\",\n" +
                "  \"onBeforeEditorRender__$S$__\": \"\",\n" +
                "  \"onChange__$S$__\": \"\",\n" +
                "  \"getDateRanger__$S$__\": \"\",\n" +
                "  \"textStyleFormatter__$S$__\": \"\",\n" +
                "  \"readonly\": false,\n" +
                "  \"onBeforeSelect__$S$__\": \"\",\n" +
                "  \"children\": [\n" +
                "    {}\n" +
                "  ],\n" +
                "  \"width\": 150,\n" +
                "  \"fixed\": false,\n" +
                "  \"id\": \"%s\",\n" +
                "  \"field\": \"%s\"\n" +
                "}");
        FIELD_TMPL_MAP.put("linkGridColumn", "{\n" +
                "  \"editor\": true,\n" +
                "  \"sort\": true,\n" +
                "  \"childrenWidgetId\": [],\n" +
                "  \"visible\": true,\n" +
                "  \"children\": [\n" +
                "    {}\n" +
                "  ],\n" +
                "  \"width\": 150,\n" +
                "  \"fixed\": false,\n" +
                "  \"title\": {\n" +
                "    \"en-US\": \"%s\",\n" +
                "    \"zh-CN\": \"%s\"\n" +
                "  },\n" +
                "  \"id\": \"%s\",\n" +
                "  \"field\": \"%s\",\n" +
                "  \"gridColumnType\": \"link\",\n" +
                "  \"type\": \"link\",\n" +
                "  \"uuid\": \"f0f8d11984a0455eada1b808b6e3d2cc\"\n" +
                "}");

        FIELD_TMPL_MAP.put("index", "{\n" +
                "  \"childrenWidgetId\": [],\n" +
                "  \"index\": true,\n" +
                "  \"id\": \"GridColumn_index\",\n" +
                "  \"gridColumnType\": \"index\",\n" +
                "  \"title\": {\n" +
                "    \"zh-CN\": \"序号\"\n" +
                "  },\n" +
                "  \"uuid\": \"cc7134b5cc8f45e29de589ee593b6fd5\"\n" +
                "}");

        FIELD_TMPL_MAP.put("checkbox", "{\n" +
                "  \"childrenWidgetId\": [],\n" +
                "  \"checkbox\": true,\n" +
                "  \"id\": \"GridColumn_checkbox\",\n" +
                "  \"gridColumnType\": \"checkbox\",\n" +
                "  \"title\": {\n" +
                "    \"zh-CN\": \"复选框列\"\n" +
                "  },\n" +
                "  \"uuid\": \"6a581698f3ec4495ae2d7beb5f3d1ebc\"\n" +
                "}");

        FIELD_TMPL_MAP.put("processStatus", "{\n" +
                "  \"editor\": false,\n" +
                "  \"childrenWidgetId\": [],\n" +
                "  \"visible\": true,\n" +
                "  \"contentAlign\": \"left\",\n" +
                "  \"dbTable\": \"%s\",\n" +
                "  \"sort\": true,\n" +
                "  \"source\": \"custom\",\n" +
                "  \"title\": {\n" +
                "    \"en-US\": \"processStatus\",\n" +
                "    \"zh-CN\": \"状态\"\n" +
                "  },\n" +
                "  \"gridColumnType\": \"processStatus\",\n" +
                "  \"type\": \"processStatus\",\n" +
                "  \"uuid\": \"699c7267a1024cb994069ee468e93c84\",\n" +
                "  \"downFillTargetFields\": [],\n" +
                "  \"visibleRoles\": [],\n" +
                "  \"filter\": true,\n" +
                "  \"formatter__$S$__\": \"\",\n" +
                "  \"field\": \"processStatus\",\n" +
                "  \"children\": [\n" +
                "    {}\n" +
                "  ],\n" +
                "  \"param\": \"\",\n" +
                "  \"tagFormatter__$S$__\": \"\",\n" +
                "  \"width\": 100,\n" +
                "  \"fixed\": false,\n" +
                "  \"id\": \"GridColumn_processStatus\",\n" +
                "  \"editRoles\": []\n" +
                "}");

    }

   public static String BUTTON_TMPL ="{\n" +
           "  \"childrenWidgetId\": [],\n" +
           "  \"hidden\": false,\n" +
           "  \"display\": \"inline\",\n" +
           "  \"icon\": \"\",\n" +
           "  \"id\": \"\"%s\",\n" +
           "  \"text\": {\n" +
           "    \"en-US\": \"Button\",\n" +
           "    \"zh-CN\": \"%s\"\n" +
           "  },\n" +
           "  \"uuid\": \"f271442f8df847e98f07410822913517\",\n" +
           "  \"onClick__$S$__\": \"\"%s\"\n" +
           "}";


}

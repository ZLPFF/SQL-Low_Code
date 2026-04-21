package com.sunwayworld.framework.support.base.dao.impl;

import com.google.common.collect.Lists;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpWrapperParseUtils;
import com.sunwayworld.framework.audit.AuditDatabaseOperation;
import com.sunwayworld.framework.audit.AuditLogHelper;
import com.sunwayworld.framework.backup.manager.CoreBackupManager;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.data.ChunkIterator;
import com.sunwayworld.framework.data.ListChunkIterator;
import com.sunwayworld.framework.database.context.TableContext;
import com.sunwayworld.framework.database.context.instance.EntityHelper;
import com.sunwayworld.framework.database.context.instance.TableContextInstance;
import com.sunwayworld.framework.database.dialect.Dialect;
import com.sunwayworld.framework.database.dialect.DialectRepository;
import com.sunwayworld.framework.database.dialect.OracleDialect;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.mybatis.mapper.MapDaoMapperWrapper;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@GikamBean
@Repository
public class LcdpMapDaoImpl extends MapDaoImpl {
    @Autowired
    private MapDaoMapperWrapper mapDaoMapper;

    @Autowired
    private Dialect dialect;

    @Lazy
    @Autowired
    private CoreBackupManager backupManager;

    @Override
    public <ID> void update(TableContext context, Map<String, Object> item, List<String> updateColumnList, String... searchColumns) {
        TableContextInstance instance = TableContextInstance.of(context, item);
        if (!CollectionUtils.isEmpty(updateColumnList)) {
            instance.getColumnInstanceList().forEach(c -> c.setActive(CollectionUtils.containsIgnoreCase(updateColumnList, c.getColumnContext().getColumnName())));
        }

        if (!instance.isUpdatable()) {
            return;
        }

        // 是否开启审计跟踪
        boolean auditable = !ApplicationContextHelper.stressTesting() // 非压测
                && AuditLogHelper.auditable(context.getTableName(), AuditDatabaseOperation.UPDATE);

        List<Map<String, Object>> oldItemList = new ArrayList<>();

        // for post update
        List<String> updatedColumnNameList = new ArrayList<>();

        if (ArrayUtils.isEmpty(searchColumns)) {
            if (auditable) {
                oldItemList.add(selectById(context, CollectionUtils.getValueIgnorecase(item, "ID")));
            } else {
                oldItemList.add(item);
            }

            mapDaoMapper.updateById(instance);

            // for post update
            instance.getColumnInstanceList().stream().filter(c -> c.isActive()).forEach(c -> updatedColumnNameList.add(c.getColumnContext().getColumnName()));
        } else {
            if (auditable) {
                oldItemList.addAll(this.selectList(context, item, ArrayUtils.asList(searchColumns)));
            } else {
                List<ID> idList = this.selectIdList(context, item, ArrayUtils.asList(searchColumns));

                oldItemList.addAll(idList.stream().map(i -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("ID", i);

                    return map;
                }).collect(Collectors.toList()));
            }

            @SuppressWarnings("unchecked")
            List<ID> idList = oldItemList.stream().map(m -> (ID) ConvertUtils.convert(CollectionUtils.getValueIgnorecase(m, "ID"), context.getIdColumnContext().getType()))
                    .collect(Collectors.toList());

            if (!idList.isEmpty()) {
                mapDaoMapper.updateByIdList(instance, idList);
            }

            // for post update
            instance.getColumnInstanceList().stream().filter(c -> c.isActive()).forEach(c -> updatedColumnNameList.add(c.getColumnContext().getColumnName()));
        }


        if (auditable) {
            @SuppressWarnings("unchecked")
            List<ID> idList = oldItemList.stream().map(i -> (ID) ConvertUtils.convert(CollectionUtils.getValueIgnorecase(i, "ID"), context.getIdColumnContext().getType()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> newItemList = selectListByIdList(context, idList);

            // 新审计跟踪
            auditUpdate(context.getTableName(), oldItemList, newItemList);
        }

        List<Object> idList = oldItemList.stream().map(i -> CollectionUtils.getValueIgnorecase(i, "ID")).collect(Collectors.toList());

        // call post update
        LcdpBaseService service = getScriptService(context);
        if (service != null) {
            service.postUpdate(updatedColumnNameList, idList);
        }

    }

    @Override
    public <ID> void updateByIdList(TableContext context, Map<String, Object> item, List<ID> idList, String... updateColumns) {
        if (idList == null || idList.size() == 0) {
            return;
        }

        TableContextInstance instance = TableContextInstance.of(context, item);
        if (!ArrayUtils.isEmpty(updateColumns)) {
            instance.getColumnInstanceList().forEach(c -> c.setActive(ArrayUtils.containsIgnoreCase(updateColumns, c.getColumnContext().getColumnName())));
        }

        if (!instance.isUpdatable()) {
            return;
        }

        // 是否开启审计跟踪
        boolean auditable = !ApplicationContextHelper.stressTesting() // 非压测
                && AuditLogHelper.auditable(context.getTableName(), AuditDatabaseOperation.UPDATE);

        List<Map<String, Object>> oldItemList = auditable ? selectListByIdList(context, idList) : null;



        if (oldItemList != null && !oldItemList.isEmpty()) {
            int chunkSize = dialect.getInClauseIdMaxQty();

            ChunkIterator<Map<String, Object>> chunkIterator = ListChunkIterator.of(oldItemList, chunkSize);

            while (chunkIterator.hasNext()) {
                List<Map<String, Object>> chunkList = chunkIterator.nextChunk();

                List<Object> chunkIdList = chunkList.stream().map(i -> i.get("ID")).collect(Collectors.toList());


                mapDaoMapper.updateByIdList(instance, chunkIdList);
            }

            // for post update
        }else {
            mapDaoMapper.updateByIdList(instance, idList);
        }
//        mapDaoMapper.updateByIdList(instance, idList);

        if (auditable) {
            List<Map<String, Object>> newItemList = selectListByIdList(context, idList);
            auditUpdate(context.getTableName(), oldItemList, newItemList);
        }


        List<String> updatedColumnNameList = instance.getColumnInstanceList().stream().filter(c -> c.isActive()).map(c -> c.getColumnContext().getColumnName()).collect(Collectors.toList());

        // call post update
        LcdpBaseService service = getScriptService(context);
        if (service != null) {
            service.postUpdate(updatedColumnNameList, idList);
        }
    }

    @Override
    public void update(TableContext context, List<Map<String, Object>> itemList, String... updateColumns) {
        if (CollectionUtils.isEmpty(itemList)) {
            return;
        }

        List<String> updateColumnList = CollectionUtils.emptyList();
        if (ArrayUtils.isEmpty(updateColumns)) {
            //对应MybatisDaoSupport的update方法， List中第一个数据为准来判断更新哪些信息
            Map<String, Object> item = itemList.get(0);

            TableContextInstance contextInstance = TableContextInstance.of(context, item);

            contextInstance.getColumnInstanceList().forEach(i -> {
                if (i.isActive() && !EntityHelper.isIdColumn(i.getColumnContext().getColumnName())) {
                    updateColumnList.add(i.getColumnContext().getColumnName());
                }
            });
        } else {
            Collections.addAll(updateColumnList, updateColumns);
        }

        // 用于修正 oracle 批量操作clob字段时，如果长度超过4000时报错
        if (DialectRepository.getDialect() instanceof OracleDialect) {
            if (!(itemList instanceof ArrayList)) {
                itemList = new ArrayList<>(itemList);
            }

            // 对于字符串长度过大时，不能批量操作
            Iterator<Map<String, Object>> iterator = itemList.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> item = iterator.next();

                for (String updateColumn : updateColumnList) {
                    Object value = CollectionUtils.getValueIgnorecase(item, updateColumn);

                    if (value instanceof String && JdbcUtils.isBigData((String) value)) {
                        iterator.remove();
                        update(context, item, updateColumnList);

                        break;
                    }
                }
            }

            if (itemList.isEmpty()) {
                return;
            }
        }

        // 是否开启审计跟踪
        boolean auditable = !ApplicationContextHelper.stressTesting() // 非压测
                && AuditLogHelper.auditable(context.getTableName(), AuditDatabaseOperation.UPDATE);

        List<Object> idList = itemList.stream().map(i -> CollectionUtils.getValueIgnorecase(i, "ID")).collect(Collectors.toList());

        List<Map<String, Object>> oldItemList = auditable ? selectListByIdList(context, idList) : null;

        List<TableContextInstance> instanceList = itemList.stream().map(m -> TableContextInstance.of(context, m)).collect(Collectors.toList());
        if(!instanceList.isEmpty()){
            int chunkSize = dialect.getInClauseIdMaxQty();

            ChunkIterator<TableContextInstance> chunkIterator = ListChunkIterator.of(instanceList, chunkSize);

            while (chunkIterator.hasNext()) {
                List<TableContextInstance> chunkList = chunkIterator.nextChunk();

                mapDaoMapper.updateItemList(context, chunkList, updateColumnList);
            }
        }


        if (auditable) {
            List<Map<String, Object>> newItemList = selectListByIdList(context, idList);
            auditUpdate(context.getTableName(), oldItemList, newItemList);
        }

        // call post update
        LcdpBaseService service = getScriptService(context);
        if (service != null) {
            service.postUpdate(updateColumnList, idList);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateIfChanged(TableContext context, List<Map<String, Object>> itemList, List<String> ignoredColumnList) {
        if (CollectionUtils.isEmpty(itemList)) {
            return;
        }

        List<Map<String, Object>> selectedItemList = this.selectListByIdList(context, itemList.stream().map(m -> CollectionUtils.getValueIgnorecase(m, "ID")).collect(Collectors.toList()));

        selectedItemList.stream().forEach(map -> {

            //特殊处理日期和时间格式,去掉毫秒，纳秒
            map.forEach((column, value) -> {
                if (column != null) {
                    if (LocalDate.class.isInstance(value)) {
                        try {
                            value = DateTimeUtils.formatLocalDate(DateTimeUtils.parseLocalDate(value.toString()));
                            map.put(column, value);
                        } catch (Exception ex) {
                            throw new CheckedException(ex);
                        }
                    } else if (LocalDateTime.class.isInstance(value)) {
                        try {
                            value = DateTimeUtils.formatLocalDateTime(DateTimeUtils.parseLocalDateTime(value.toString()));
                            map.put(column, value);
                        } catch (Exception ex) {
                            throw new CheckedException(ex);
                        }
                    }
                }
            });
        });

        // 是否开启审计跟踪
        boolean auditable = !ApplicationContextHelper.stressTesting() // 非压测
                && AuditLogHelper.auditable(context.getTableName(), AuditDatabaseOperation.UPDATE);

        for (Map<String, Object> item : itemList) {

            Map<String, Object> selectedItem = null;

            Object itemId = CollectionUtils.getValueIgnorecase(item, "ID");

            if (!String.class.equals(context.getIdColumnContext().getType()) && NumberUtils.isNumber(itemId.toString()) && !itemId.toString().startsWith("0")) {
                selectedItem = selectedItemList.stream().filter(i -> (ConvertUtils.convert(CollectionUtils.getValueIgnorecase(i, "ID"), Long.class)).compareTo(ConvertUtils.convert(itemId, Long.class)) == 0).findFirst().orElseThrow(() -> new ApplicationRuntimeException("MYBATIS.EXCEPTION.NO_VALUE_EXISTS"));
            } else {
                selectedItem = selectedItemList.stream().filter(i -> Objects.equals(String.valueOf(CollectionUtils.getValueIgnorecase(i, "ID")), String.valueOf(itemId))).findFirst().orElseThrow(() -> new ApplicationRuntimeException("MYBATIS.EXCEPTION.NO_VALUE_EXISTS"));
            }


            List<String> updatedColumnList = new ArrayList<>();

            Map<String, Object> finalSelectedItem = selectedItem;
            context.getColumnContextList().forEach(c -> {
                if (CollectionUtils.containsIgnoreCase(item, "ext$")) {
                    ((Map<String, Object>) item.get("ext$")).forEach((k, v) -> {
                        item.put(k, v);
                    });
                }
                if (!CollectionUtils.containsIgnoreCase(ignoredColumnList, c.getColumnName()) && CollectionUtils.containsIgnoreCase(item, c.getColumnName())) {
                    if (!c.isImmutable()) {
                        if (!CollectionUtils.containsIgnoreCase(finalSelectedItem, c.getColumnName())) {
                            updatedColumnList.add(c.getColumnName());
                        } else {
                            if (!Objects.equals(c.getValue(item), c.getValue(finalSelectedItem))) {
                                updatedColumnList.add(c.getColumnName());
                            }
                        }
                    }
                }
            });

            if (!updatedColumnList.isEmpty()) {
                TableContextInstance instance = TableContextInstance.of(context, item);

                instance.getColumnInstanceList().stream().forEach(i -> i.setActive(CollectionUtils.containsIgnoreCase(updatedColumnList, i.getColumnContext().getColumnName())));

                mapDaoMapper.updateById(instance);


                if (auditable) {
                    Map<String, Object> newItem = selectById(context, CollectionUtils.getValueIgnorecase(selectedItem, "ID"));

                    // 新审计跟踪
                    auditUpdate(context.getTableName(), Arrays.asList(selectedItem), Arrays.asList(newItem));
                }
            }

            // call post update
            LcdpBaseService service = getScriptService(context);
            if (service != null) {
                service.postUpdate(updatedColumnList, Arrays.asList(CollectionUtils.getValueIgnorecase(selectedItem, "ID")));
            }
        }
    }

    @Override
    public List<Map<String, Object>> selectList(TableContext context, Map<String, Object> item, List<String> selectColumnList, Order... orders) {
        return LcdpWrapperParseUtils.lowerCaseKey(super.selectList(context, item, selectColumnList, orders));
    }

    @Override
    public <ID> Map<String, Object> selectByIdIfPresent(TableContext context, ID id) {
        return LcdpWrapperParseUtils.lowerCaseKey(super.selectByIdIfPresent(context, id));
    }

    @Override
    public Map<String, Object> selectFirstIfPresent(TableContext context, Map<String, Object> item, Order... orders) {
        return LcdpWrapperParseUtils.lowerCaseKey(super.selectFirstIfPresent(context, item, orders));
    }

    @Override
    public Map<String, Object> selectOneIfPresent(TableContext context, Map<String, Object> item, String... selectColNames) {
        return LcdpWrapperParseUtils.lowerCaseKey(super.selectOneIfPresent(context, item, selectColNames));
    }

    @Override
    public List<Map<String, Object>> selectList(TableContext context, List<Map<String, Object>> itemList, List<String> searchColumnList, List<String> selectColumnList, Order... orders) {
        return LcdpWrapperParseUtils.lowerCaseKey(super.selectList(context, itemList, searchColumnList, selectColumnList, orders));
    }

    @Override
    public <ID> List<Map<String, Object>> selectListByIdList(TableContext context, List<ID> ids, List<String> searchColumnList, Order... orders) {
        return LcdpWrapperParseUtils.lowerCaseKey(super.selectListByIdList(context, ids, searchColumnList, orders));
    }

    @Override
    public void insert(TableContext context, Map<String, Object> item) {
        super.insert(context, item);

        // call post insert
        LcdpBaseService service = getScriptService(context);
        if (service != null) {
            Object id = CollectionUtils.getValueIgnorecase(item, "id");
            Map<String, Object> selectedItem = selectById(context, id);
            service.postInsert(Collections.unmodifiableList(Lists.newArrayList(selectedItem)));
        }
    }

    @Override
    public void insert(TableContext context, List<Map<String, Object>> itemList) {
        super.insert(context, itemList);

        // call post insert
        LcdpBaseService service = getScriptService(context);
        if (service != null) {
            service.postInsert(Collections.unmodifiableList(new ArrayList<>(itemList)));
        }
    }


    @Override
    public <ID> void delete(TableContext context, ID id) {
        if (id == null) {
            throw new ApplicationRuntimeException("GIKAM.EXCEPTION.DELETE_REQUIRE_ID");
        }

        Object actualId = toActualId(context, id);

        // 是否开启审计跟踪
        boolean auditable = !ApplicationContextHelper.stressTesting() // 非压测
                && AuditLogHelper.auditable(context.getTableName(), AuditDatabaseOperation.DELETE);

        // 是否备份
        boolean backup = backupManager.needBackup(context.getTableName());

        Map<String, Object> oldItem = ((auditable || backup) ? selectByIdIfPresent(context, actualId) : null);

        mapDaoMapper.deleteById(context, id);

        if (oldItem == null) {
            return;
        }

        // 备份
        if (backup) {
            backupManager.backup(context.getTableName(), id.toString(), oldItem);
        }

        if (auditable) {
            AuditLogHelper.insertAuditLog(context.getTableName(), oldItem, null);
        }

        // call post delete
        LcdpBaseService service = getScriptService(context);
        if (service != null) {
            service.postDelete(Collections.unmodifiableList(Lists.newArrayList(oldItem)));
        }

    }

    @Override
    public <ID> void deleteByIdList(TableContext context, List<ID> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }

        if (idList.stream().anyMatch(i -> i == null)) {
            throw new ApplicationRuntimeException("GIKAM.EXCEPTION.DELETE_REQUIRE_ID");
        }

        List<?> actualIdList = toActualIdList(context, idList);

        // 是否开启审计跟踪
        boolean auditable = !ApplicationContextHelper.stressTesting() // 非压测
                && AuditLogHelper.auditable(context.getTableName(), AuditDatabaseOperation.DELETE);


        // 是否备份
        boolean backup = backupManager.needBackup(context.getTableName());

        List<Map<String, Object>> oldItemList = ((auditable || backup) ? selectListByIdList(context, actualIdList) : null);


        // 是否需要循环（参数过多）
        boolean loop = idList.size() > dialect.getInClauseIdMaxQty();

        if (loop) {
            ListChunkIterator<ID> chunkIterator = ListChunkIterator.of(idList, dialect.getInClauseIdMaxQty());

            while (chunkIterator.hasNext()) {
                mapDaoMapper.deleteByIdList(context, chunkIterator.nextChunk().stream().collect(Collectors.toList()));
            }
        } else {
            mapDaoMapper.deleteByIdList(context, idList);
        }

        if (oldItemList == null) {
            return;
        }

        // 备份
        if (backup) {
            Map<String, Object> map = oldItemList.stream().collect(Collectors.toMap(m -> CollectionUtils.getValueIgnorecase(m, "ID").toString(), m -> m));

            backupManager.backup(context.getTableName(), map);
        }


        if (auditable) {
            for (Map<String, Object> selectedItem : oldItemList) {
                AuditLogHelper.insertAuditLog(context.getTableName(), selectedItem, null);
            }
        }

        // call post delete
        LcdpBaseService service = getScriptService(context);
        if (service != null) {
            service.postDelete(Collections.unmodifiableList(new ArrayList<>(oldItemList)));
        }
    }

    @Override
    protected void preInsert(TableContext context, List<Map<String, Object>> itemList) {
        LcdpBaseService service = getScriptService(context);
        if (service != null) {
            service.preInsert(itemList);
        }

        super.preInsert(context, itemList);

    }

    //--------------------------------------------------------------------------
    // 私有方法
    //--------------------------------------------------------------------------
    private void auditUpdate(String tableName, List<Map<String, Object>> selectedOldItemList, List<Map<String, Object>> selectedNewItemList) {
        for (Map<String, Object> selectedOldItem : selectedOldItemList) {
            Map<String, Object> selectedNewItem = selectedNewItemList.stream().filter(i -> CollectionUtils.getValueIgnorecase(selectedOldItem, "ID").equals(CollectionUtils.getValueIgnorecase(i, "ID"))).findAny().orElse(null);

            AuditLogHelper.insertAuditLog(tableName, selectedOldItem, selectedNewItem);
        }
    }

    private LcdpBaseService getScriptService(TableContext context) {
        String tableName = context.getTableName();

        String beanName = ApplicationContextHelper.getBean(LcdpResourceService.class).getLcdpServiceNameByTable(tableName);


        if (!StringUtils.isEmpty(beanName)) {
            return SpringUtils.getBean(beanName);
        }

        return null;
    }
}

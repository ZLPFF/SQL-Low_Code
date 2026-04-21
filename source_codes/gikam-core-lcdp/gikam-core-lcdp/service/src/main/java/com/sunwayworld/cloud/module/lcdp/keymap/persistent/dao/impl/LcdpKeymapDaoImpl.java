package com.sunwayworld.cloud.module.lcdp.keymap.persistent.dao.impl;

import com.sunwayworld.cloud.module.lcdp.keymap.bean.LcdpKeymapBean;
import com.sunwayworld.cloud.module.lcdp.keymap.persistent.dao.LcdpKeymapDao;
import com.sunwayworld.cloud.module.lcdp.keymap.persistent.mapper.LcdpKeymapMapper;
import com.sunwayworld.framework.cache.redis.RedisHelper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@GikamBean
public class LcdpKeymapDaoImpl extends MybatisDaoSupport<LcdpKeymapBean, Long> implements LcdpKeymapDao {

    @Autowired
    private LcdpKeymapMapper lcdpKeymapMapper;

    @Override
    public LcdpKeymapMapper getMapper() {
        return lcdpKeymapMapper;
    }

    @Override
    @Cacheable(value = "T_LCDP_KEYMAP", key = "'' + #id", unless="#result == null")
    public LcdpKeymapBean selectByIdIfPresent(Long id) {
        return super.selectByIdIfPresent(id);
    }

    @Override
    @Cacheable(value = "T_LCDP_KEYMAP", key = "'ALL'", unless="#result == null")
    public List<LcdpKeymapBean> selectAll() {
        return super.selectAll();
    }

    @Override
    public void cacheEvict(LcdpKeymapBean oldItem, LcdpKeymapBean newItem) {
        String key = "" + (oldItem == null ? newItem.getId() : oldItem.getId());

        RedisHelper.evict("T_LCDP_KEYMAP", key);
        RedisHelper.evict("T_LCDP_KEYMAP", "ALL");

        TransactionUtils.runAfterCompletion(i -> {
            RedisHelper.evict("T_LCDP_KEYMAP", key);
            RedisHelper.evict("T_LCDP_KEYMAP", "ALL");
        });
    }

}

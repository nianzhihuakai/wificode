package com.nzhk.wificode.business.wificode.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nzhk.wificode.business.wificode.bean.WifiCodeCreateReqData;
import com.nzhk.wificode.business.wificode.bean.WifiCodeItemResData;
import com.nzhk.wificode.business.wificode.bean.WifiCodeUpdateReqData;
import com.nzhk.wificode.business.wificode.entity.WifiCode;
import com.nzhk.wificode.common.cache.ContextCache;
import com.nzhk.wificode.common.exception.BizException;
import com.nzhk.wificode.common.utils.BeanConvertUtil;
import com.nzhk.wificode.common.utils.IdUtil;
import com.nzhk.wificode.mapper.WifiCodeMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WifiCodeServiceImpl extends ServiceImpl<WifiCodeMapper, WifiCode> implements IWifiCodeService {

    @Override
    public WifiCodeItemResData create(WifiCodeCreateReqData data) {
        String userId = ContextCache.getUserId();
        if (StringUtils.isEmpty(userId)) {
            throw new BizException(40100, "请先登录");
        }
        if (StringUtils.isBlank(data.getSsid())) {
            throw new BizException(40002, "请输入网络名称");
        }

        WifiCode entity = new WifiCode();
        entity.setId(IdUtil.getId());
        entity.setUserId(userId);
        entity.setBrandName(StringUtils.defaultString(data.getBrandName()));
        entity.setSsid(data.getSsid().trim());
        entity.setPassword(StringUtils.defaultString(data.getPassword()));
        entity.setAuthType(StringUtils.isNotBlank(data.getAuthType()) ? data.getAuthType() : "WPA");
        entity.setYesterdayCount(0);
        entity.setTotalCount(0);
        entity.setStatus(1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(entity);
        return BeanConvertUtil.copySingleProperties(entity, WifiCodeItemResData::new);
    }

    @Override
    public WifiCodeItemResData update(WifiCodeUpdateReqData data) {
        String userId = ContextCache.getUserId();
        if (StringUtils.isEmpty(userId)) {
            throw new BizException(40100, "请先登录");
        }
        if (StringUtils.isEmpty(data.getId())) {
            throw new BizException(40003, "参数错误");
        }

        WifiCode entity = baseMapper.selectById(data.getId());
        if (entity == null || !userId.equals(entity.getUserId())) {
            throw new BizException(40401, "未找到该 WiFi 码");
        }
        if (StringUtils.isBlank(data.getSsid())) {
            throw new BizException(40002, "请输入网络名称");
        }
        entity.setBrandName(data.getBrandName() != null ? data.getBrandName() : entity.getBrandName());
        entity.setSsid(data.getSsid().trim());
        entity.setPassword(data.getPassword() != null ? data.getPassword() : entity.getPassword());
        entity.setAuthType(StringUtils.isNotBlank(data.getAuthType()) ? data.getAuthType() : entity.getAuthType());
        entity.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(entity);
        return BeanConvertUtil.copySingleProperties(entity, WifiCodeItemResData::new);
    }

    @Override
    public WifiCodeItemResData getById(String id) {
        String userId = ContextCache.getUserId();
        if (StringUtils.isEmpty(userId)) {
            throw new BizException(40100, "请先登录");
        }

        WifiCode entity = baseMapper.selectById(id);
        if (entity == null || !userId.equals(entity.getUserId())) {
            throw new BizException(40401, "未找到该 WiFi 码");
        }
        return BeanConvertUtil.copySingleProperties(entity, WifiCodeItemResData::new);
    }

    @Override
    public List<WifiCodeItemResData> listByUserId(String keyword) {
        String userId = ContextCache.getUserId();
        if (StringUtils.isEmpty(userId)) {
            throw new BizException(40100, "请先登录");
        }

        LambdaQueryWrapper<WifiCode> q = new LambdaQueryWrapper<>();
        q.eq(WifiCode::getUserId, userId)
                .eq(WifiCode::getStatus, 1)
                .orderByDesc(WifiCode::getCreateTime);
        if (StringUtils.isNotBlank(keyword)) {
            String kw = "%" + keyword.trim() + "%";
            q.and(w -> w.apply("ssid ILIKE {0}", kw).or().apply("brand_name ILIKE {0}", kw));
        }
        List<WifiCode> list = baseMapper.selectList(q);
        return list.stream()
                .map(e -> BeanConvertUtil.copySingleProperties(e, WifiCodeItemResData::new))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        String userId = ContextCache.getUserId();
        if (StringUtils.isEmpty(userId)) {
            throw new BizException(40100, "请先登录");
        }

        WifiCode entity = baseMapper.selectById(id);
        if (entity == null || !userId.equals(entity.getUserId())) {
            throw new BizException(40401, "未找到该 WiFi 码");
        }
        entity.setStatus(0);
        entity.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(entity);
    }

    @Override
    public WifiCodeItemResData getByIdPublic(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new BizException(40003, "参数错误");
        }
        WifiCode entity = baseMapper.selectById(id);
        if (entity == null || entity.getStatus() != 1) {
            throw new BizException(40401, "未找到该 WiFi 码");
        }
        return BeanConvertUtil.copySingleProperties(entity, WifiCodeItemResData::new);
    }
}

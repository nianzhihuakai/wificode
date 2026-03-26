package com.nzhk.wificode.business.wificode.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nzhk.wificode.business.wificode.bean.BindCodeGenerateResData;
import com.nzhk.wificode.business.wificode.bean.ReportConnectionResData;
import com.nzhk.wificode.business.wificode.bean.WifiCodeListResData;
import com.nzhk.wificode.business.wificode.config.WificodeStatsProperties;
import com.nzhk.wificode.business.wificode.entity.WifiCode;
import com.nzhk.wificode.business.wificode.entity.WifiCodeBindTicket;
import com.nzhk.wificode.business.wificode.entity.WifiScanLog;
import com.nzhk.wificode.common.cache.ContextCache;
import com.nzhk.wificode.common.exception.BizException;
import com.nzhk.wificode.common.utils.DailyAttemptLimiter;
import com.nzhk.wificode.common.utils.FixedWindowRateLimiter;
import com.nzhk.wificode.common.utils.IdUtil;
import com.nzhk.wificode.common.utils.SimpleRateLimiter;
import com.nzhk.wificode.mapper.WifiCodeBindTicketMapper;
import com.nzhk.wificode.mapper.WifiCodeMapper;
import com.nzhk.wificode.mapper.WifiScanLogMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
public class WifiCodeStatsServiceImpl implements IWifiCodeStatsService {

    private static final String BIND_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Resource
    private WifiCodeMapper wifiCodeMapper;
    @Resource
    private WifiScanLogMapper wifiScanLogMapper;
    @Resource
    private WifiCodeBindTicketMapper wifiCodeBindTicketMapper;
    @Resource
    private WificodeStatsProperties statsProperties;
    @Resource
    private IWifiCodeService wifiCodeService;
    @Resource
    private WifiCodeListStatsService wifiCodeListStatsService;

    private SimpleRateLimiter reportLimiter;
    private DailyAttemptLimiter bindAttemptLimiter;
    private FixedWindowRateLimiter bindCodeUserLimiter;
    private FixedWindowRateLimiter bindCodeWifiLimiter;

    @PostConstruct
    void init() {
        reportLimiter = new SimpleRateLimiter(statsProperties.getReportRateLimitPerMinute());
        bindAttemptLimiter = new DailyAttemptLimiter(statsProperties.getBindAttemptsPerDay());
        bindCodeUserLimiter = new FixedWindowRateLimiter(60_000L, statsProperties.getBindCodePerUserPerMinute());
        bindCodeWifiLimiter = new FixedWindowRateLimiter(3_600_000L, statsProperties.getBindCodePerWifiPerHour());
    }

    private ZoneId zone() {
        return ZoneId.systemDefault();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportConnectionResData reportConnection(String wifiCodeId, String visitorUserId, String ip, String deviceInfo) {
        if (StringUtils.isBlank(wifiCodeId)) {
            throw new BizException(40003, "参数错误");
        }
        String rateKey = StringUtils.defaultString(ip, "unknown") + ":" + wifiCodeId;
        if (!reportLimiter.tryAcquire(rateKey)) {
            throw new BizException(42901, "请求过于频繁，请稍后再试");
        }

        WifiCode code = wifiCodeService.getEntityByIdPublic(wifiCodeId);
        LocalDate statDate = LocalDate.now(zone());

        if (StringUtils.isNotBlank(visitorUserId)) {
            if (existsVisitorDay(wifiCodeId, visitorUserId, statDate)) {
                return new ReportConnectionResData(false, code.getTotalCount());
            }
        } else {
            if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                throw new BizException(40005, "无法识别客户端，请登录后重试");
            }
            if (existsAnonDay(wifiCodeId, ip, statDate)) {
                return new ReportConnectionResData(false, code.getTotalCount());
            }
        }

        WifiScanLog logRow = new WifiScanLog();
        logRow.setId(IdUtil.getId());
        logRow.setWifiCodeId(wifiCodeId);
        logRow.setVisitorUserId(StringUtils.isNotBlank(visitorUserId) ? visitorUserId : null);
        logRow.setStatDate(statDate);
        logRow.setScanTime(LocalDateTime.now(zone()));
        logRow.setIp(ip);
        logRow.setDeviceInfo(deviceInfo);

        try {
            wifiScanLogMapper.insert(logRow);
        } catch (DataIntegrityViolationException e) {
            log.debug("report duplicate: {}", e.getMessage());
            WifiCode refreshed = wifiCodeMapper.selectById(wifiCodeId);
            return new ReportConnectionResData(false, refreshed != null ? refreshed.getTotalCount() : code.getTotalCount());
        }

        LambdaUpdateWrapper<WifiCode> u = new LambdaUpdateWrapper<>();
        u.setSql("total_count = total_count + 1")
                .eq(WifiCode::getId, wifiCodeId)
                .eq(WifiCode::getStatus, 1);
        wifiCodeMapper.update(null, u);

        WifiCode updated = wifiCodeMapper.selectById(wifiCodeId);
        return new ReportConnectionResData(true, updated != null ? updated.getTotalCount() : code.getTotalCount() + 1);
    }

    private boolean existsVisitorDay(String wifiCodeId, String visitorUserId, LocalDate statDate) {
        return wifiScanLogMapper.selectCount(
                new LambdaQueryWrapper<WifiScanLog>()
                        .eq(WifiScanLog::getWifiCodeId, wifiCodeId)
                        .eq(WifiScanLog::getVisitorUserId, visitorUserId)
                        .eq(WifiScanLog::getStatDate, statDate)
        ) > 0;
    }

    private boolean existsAnonDay(String wifiCodeId, String ip, LocalDate statDate) {
        return wifiScanLogMapper.selectCount(
                new LambdaQueryWrapper<WifiScanLog>()
                        .eq(WifiScanLog::getWifiCodeId, wifiCodeId)
                        .isNull(WifiScanLog::getVisitorUserId)
                        .eq(WifiScanLog::getIp, ip)
                        .eq(WifiScanLog::getStatDate, statDate)
        ) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BindCodeGenerateResData generateBindCode(String wifiCodeId) {
        String userId = ContextCache.getUserId();
        if (StringUtils.isEmpty(userId)) {
            throw new BizException(40100, "请先登录");
        }
        if (StringUtils.isBlank(wifiCodeId)) {
            throw new BizException(40003, "参数错误");
        }
        WifiCode entity = wifiCodeMapper.selectById(wifiCodeId);
        if (entity == null || entity.getStatus() != 1 || !userId.equals(entity.getUserId())) {
            throw new BizException(40401, "未找到该 WiFi 码");
        }
        if (!bindCodeUserLimiter.tryAcquire("bindgen-user:" + userId)) {
            throw new BizException(42901, "生成绑定码过于频繁，请稍后再试");
        }
        if (!bindCodeWifiLimiter.tryAcquire("bindgen-wifi:" + wifiCodeId)) {
            throw new BizException(42903, "该码生成绑定码过于频繁，请稍后再试");
        }

        LocalDateTime expireAt = LocalDateTime.now(zone()).plusHours(statsProperties.getBindCodeTtlHours());
        String code = generateUniqueBindCode();

        WifiCodeBindTicket existing = wifiCodeBindTicketMapper.selectOne(
                new LambdaQueryWrapper<WifiCodeBindTicket>()
                        .eq(WifiCodeBindTicket::getWifiCodeId, wifiCodeId));
        if (existing != null) {
            existing.setCode(code);
            existing.setExpireAt(expireAt);
            existing.setUsedAt(null);
            existing.setUsedByUserId(null);
            wifiCodeBindTicketMapper.updateById(existing);
        } else {
            WifiCodeBindTicket t = new WifiCodeBindTicket();
            t.setId(IdUtil.getId());
            t.setWifiCodeId(wifiCodeId);
            t.setCode(code);
            t.setExpireAt(expireAt);
            t.setCreateTime(LocalDateTime.now(zone()));
            wifiCodeBindTicketMapper.insert(t);
        }

        BindCodeGenerateResData res = new BindCodeGenerateResData();
        res.setBindCode(code);
        res.setExpireAt(expireAt);
        return res;
    }

    private String generateUniqueBindCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(BIND_CODE_CHARS.charAt(RANDOM.nextInt(BIND_CODE_CHARS.length())));
            }
            String c = sb.toString();
            Long n = wifiCodeBindTicketMapper.selectCount(
                    new LambdaQueryWrapper<WifiCodeBindTicket>().eq(WifiCodeBindTicket::getCode, c));
            if (n == 0) {
                return c;
            }
        }
        throw new BizException(50002, "生成绑定码失败，请重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindStore(String bindCode) {
        String userId = ContextCache.getUserId();
        if (StringUtils.isEmpty(userId)) {
            throw new BizException(40100, "请先登录");
        }
        if (StringUtils.isBlank(bindCode)) {
            throw new BizException(40003, "请输入绑定码");
        }
        if (!bindAttemptLimiter.tryAcquire(userId)) {
            throw new BizException(42902, "今日绑定尝试次数已达上限，请明天再试");
        }
        String trimmed = bindCode.trim().toUpperCase();
        WifiCodeBindTicket ticket = wifiCodeBindTicketMapper.selectOne(
                new LambdaQueryWrapper<WifiCodeBindTicket>().eq(WifiCodeBindTicket::getCode, trimmed));
        if (ticket == null) {
            throw new BizException(40402, "绑定码无效");
        }
        if (ticket.getUsedAt() != null) {
            throw new BizException(40008, "绑定码已使用");
        }
        if (ticket.getExpireAt().isBefore(LocalDateTime.now(zone()))) {
            throw new BizException(40009, "绑定码已过期");
        }

        WifiCode entity = wifiCodeMapper.selectById(ticket.getWifiCodeId());
        if (entity == null || entity.getStatus() != 1) {
            throw new BizException(40401, "未找到该 WiFi 码");
        }
        if (StringUtils.isNotBlank(entity.getStoreOwnerId()) && !entity.getStoreOwnerId().equals(userId)) {
            throw new BizException(40006, "该码已绑定其他店主");
        }
        if (userId.equals(entity.getStoreOwnerId())) {
            throw new BizException(40007, "您已绑定该码");
        }

        entity.setStoreOwnerId(userId);
        entity.setUpdateTime(LocalDateTime.now(zone()));
        wifiCodeMapper.updateById(entity);

        ticket.setUsedAt(LocalDateTime.now(zone()));
        ticket.setUsedByUserId(userId);
        wifiCodeBindTicketMapper.updateById(ticket);
    }

    @Override
    public WifiCodeListResData listByStoreOwner(String keyword) {
        String userId = ContextCache.getUserId();
        if (StringUtils.isEmpty(userId)) {
            throw new BizException(40100, "请先登录");
        }
        LambdaQueryWrapper<WifiCode> q = new LambdaQueryWrapper<>();
        q.eq(WifiCode::getStoreOwnerId, userId)
                .eq(WifiCode::getStatus, 1)
                .orderByDesc(WifiCode::getCreateTime);
        if (StringUtils.isNotBlank(keyword)) {
            String kw = "%" + keyword.trim() + "%";
            q.and(w -> w.apply("ssid ILIKE {0}", kw).or().apply("brand_name ILIKE {0}", kw));
        }
        List<WifiCode> list = wifiCodeMapper.selectList(q);
        return wifiCodeListStatsService.buildListRes(list);
    }

    @Override
    public void refreshYesterdayCounts() {
        LocalDate yesterday = LocalDate.now(zone()).minusDays(1);
        wifiCodeMapper.refreshYesterdayCountsForAll(yesterday);
        log.info("refreshYesterdayCounts done for {}", yesterday);
    }
}

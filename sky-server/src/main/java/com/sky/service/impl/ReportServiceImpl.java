package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0.0
 * @program: sky-take-out
 * @className: ReportServiceImp
 * @description:
 * @author: Lin
 * @create: 2025/6/26 06:04
 **/
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 统计指定时间区间内营业额数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 当前集合用于存放从begin到end范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 查询date日期对应的营业额数据, 营业额指: 状态为"已完成"订单金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);    // 2025-06-26 00:00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);      // 2025-06-26 23:59:59

            // select sum(amount) from orders where order_time > beginTime order time < endTime and status = 5
            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        // 封装返回结果
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 统计指定时间区间内用户数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 存放每天的用户
        List<Integer> newUserList = new ArrayList<>();
        // 存放当天总用户数据
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 查询date日期对应的用户数据
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            // select sum(id) from user where create_time > beginTime and order_time < endTime
            Integer newUser = userMapper.sumNewUserByMap(map);
//            newUser = newUser == null ? 0 : newUser;
            newUserList.add(newUser);
            Integer totalUser = userMapper.sumTotalUserByMap(map);
//            totalUser = totalUser == null ? 0 : totalUser;
            totalUserList.add(totalUser);
        }

        // 封装返回结果
        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {

        // 当前集合用于存放从begin到end范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 存放每天的订单数
        List<Integer> orderCountList = new ArrayList<>();
        // 存放每天有效的订单数   什么是有效的订单数？已完成的
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap<>();
            map.put("end", endTime);
            map.put("begin", beginTime);
            Integer orderCount = orderMapper.countByMap(map);
            orderCountList.add(orderCount);
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            validOrderCountList.add(validOrderCount);
        }

        /**
         * 订单总数
         * private Integer totalOrderCount;
         * 有效订单数
         * private Integer validOrderCount;
         * 订单完成率
         * private Double orderCompletionRate;
         */
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        Map map = new HashMap<>();
        map.put("now", now);
        // 获取订单总数
        Integer totalOrderCount = orderMapper.countTotalByMap(map);
        map.put("status", Orders.COMPLETED);
        // 获取有效订单数
        Integer validOrderCount = orderMapper.countTotalByMap(map);
        // 计算订单完成率
        Double orderCompletionRate = 0.0;
        // 非0校验
        if (validOrderCount > 0 && totalOrderCount > 0) {
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
        }

        return OrderReportVO.builder().dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }
}
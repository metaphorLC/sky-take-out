package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private WorkspaceService workspaceService;

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
        /*List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }*/
        List<LocalDate> dateList = getDateList(begin, end);

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
        List<LocalDate> dateList = getDateList(begin, end);
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
        List<LocalDate> dateList = getDateList(begin, end);

        // 存放每天的订单数
        List<Integer> orderCountList = new ArrayList<>();
        // 存放每天有效的订单数   什么是有效的订单数？已完成的
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            // 抽取一个方法
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);

            // 抽取一个方法
            /*Map map = new HashMap<>();
            map.put("end", endTime);
            map.put("begin", beginTime);
            Integer orderCount = orderMapper.countByMap(map);
            orderCountList.add(orderCount);
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            validOrderCountList.add(validOrderCount);*/
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
        /*LocalDateTime now = LocalDateTime.now();
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
        }*/

        // 获取订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        // 计算时间内的有效订单数量
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        // 计算订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }


        return OrderReportVO.builder().dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 查询销量排名top10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        // 根据时间区间展示销量前10的商品, 包括菜品和套餐
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");
        return SalesTop10ReportVO.builder().nameList(nameList).numberList(numberList).build();
        /*List<String> nameList = new ArrayList<>();
        List<String> numberList = new ArrayList<>();
        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();
        for (GoodsSalesDTO goodsSalesDTO : salesTop10) {
            nameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber());
        }*/

    }

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 1. 查询数据库, 获取营业数据 --- 查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDateTime dateTimeBegin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime dateTimeEnd = LocalDateTime.of(dateEnd, LocalTime.MAX);
        // 查询概览数据
        BusinessDataVO businessData = workspaceService.getBusinessData(dateTimeBegin, dateTimeEnd);

        // 2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            // 2.1 获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            // 2.2 获取第二行, 填充数据 --- 时间
            sheet.getRow(1).getCell(1).setCellValue("时间: " + dateBegin + "至" + dateEnd);
            // 2.3 填充概览数据
            // 获取第四行
            XSSFRow row = sheet.getRow(3);
            // 填充数据 --- 营业额
            row.getCell(2).setCellValue(businessData.getTurnover());
            // 填充数据 --- 订单完成率
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            // 填充数据 --- 新增用户数
            row.getCell(6).setCellValue(businessData.getNewUsers());

            // 获取第五行
            row = sheet.getRow(5);
            // 填充数据 --- 有效订单
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            // 填充数据 --- 平均客单价
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            // 2.4 填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                businessData = workspaceService.getBusinessData(LocalDateTime.of(date,LocalTime.MIN),LocalDateTime.of(date, LocalTime.MAX));
                // 获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            // 3. 通过输出流, 将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            // 关闭资源
            out.close();
            excel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    /**
     * 获取区间内每一天
     *
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }

    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map map = new HashMap<>();
        map.put("begin", beginTime);
        map.put("end", endTime);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }
}
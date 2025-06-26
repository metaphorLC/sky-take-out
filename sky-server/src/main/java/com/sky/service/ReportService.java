package com.sky.service;

import com.sky.vo.TurnoverReportVO;

import java.time.LocalDate;

/**
 * @program: sky-take-out
 * @interfaceName: ReportService
 * @description:
 * @author: Lin
 * @create: 2025-06-26 06:01
 **/
public interface ReportService {

    /**
     * 统计指定时间区间内数据
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);
}

package com.example.chalpuplatform.jar.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.jar.domain.*;
import com.example.chalpuplatform.jar.exception.JARException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JARAnalysisService {
    
    public JARAnalysisResult analyzeJAR(Long questionId, JARAttribute attribute, List<JARDataPoint> dataPoints) {
        if (dataPoints == null || dataPoints.isEmpty()) {
            throw new JARException(ErrorMessage.JAR_INSUFFICIENT_DATA);
        }
        
        // 그룹별 분류 (-0.5, 0.5 기준)
        List<JARDataPoint> tooLittleData = dataPoints.stream()
            .filter(dp -> dp.jarScore() != null && dp.jarScore() < -0.5)
            .toList();
            
        List<JARDataPoint> justRightData = dataPoints.stream()
            .filter(dp -> dp.jarScore() != null && dp.jarScore() >= -0.5 && dp.jarScore() <= 0.5)
            .toList();
            
        List<JARDataPoint> tooMuchData = dataPoints.stream()
            .filter(dp -> dp.jarScore() != null && dp.jarScore() > 0.5)
            .toList();
        
        int total = dataPoints.size();
        
        // 각 그룹별 JARGroup 생성
        JARGroup tooLittle = createJARGroup(tooLittleData, total);
        JARGroup justRight = createJARGroup(justRightData, total);
        JARGroup tooMuch = createJARGroup(tooMuchData, total);
        
        // 전체 평균 만족도
        double overallMean = dataPoints.stream()
            .filter(dp -> dp.satisfactionScore() != null)
            .mapToDouble(dp -> dp.satisfactionScore())
            .average()
            .orElse(0.0);
        
        return new JARAnalysisResult(
            questionId,
            attribute,
            tooLittle,
            justRight,
            tooMuch,
            Math.round(overallMean * 100.0) / 100.0,  // 소수점 둘째자리
            total
        );
    }
    
    private JARGroup createJARGroup(List<JARDataPoint> groupData, int total) {
        if (groupData.isEmpty()) {
            return new JARGroup(0, 0.0, 0.0);
        }
        
        double avgSatisfaction = groupData.stream()
            .filter(dp -> dp.satisfactionScore() != null)
            .mapToDouble(dp -> dp.satisfactionScore())
            .average()
            .orElse(0.0);
            
        return new JARGroup(
            groupData.size(),
            Math.round((groupData.size() * 1000.0) / total) / 10.0,  // 퍼센트로 변환, 소수점 첫째자리
            Math.round(avgSatisfaction * 100.0) / 100.0  // 소수점 둘째자리까지
        );
    }
}
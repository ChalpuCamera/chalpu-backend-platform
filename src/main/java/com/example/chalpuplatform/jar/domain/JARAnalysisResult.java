package com.example.chalpuplatform.jar.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record JARAnalysisResult(
    Long questionId,
    JARAttribute attribute,
    JARGroup tooLittle,
    JARGroup justRight,
    JARGroup tooMuch,
    double overallMeanScore,
    int totalResponses
) {
    @JsonIgnore
    public double getTooLittlePenalty() {
        if (tooLittle.count() == 0 || justRight.count() == 0) return 0;
        double penalty = (justRight.avgSatisfaction() - tooLittle.avgSatisfaction()) 
               * (tooLittle.percentage() / 100);
        return Math.round(penalty * 100.0) / 100.0;  // 소수점 둘째자리
    }
    @JsonIgnore
    public double getTooMuchPenalty() {
        if (tooMuch.count() == 0 || justRight.count() == 0) return 0;
        double penalty = (justRight.avgSatisfaction() - tooMuch.avgSatisfaction()) 
               * (tooMuch.percentage() / 100);
        return Math.round(penalty * 100.0) / 100.0;  // 소수점 둘째자리
    }
    
    public double getTotalPenalty() {
        double total = getTooLittlePenalty() + getTooMuchPenalty();
        return Math.round(total * 100.0) / 100.0;  // 소수점 둘째자리
    }
    @JsonIgnore
    public Priority getPriority() {
        return Priority.fromPenalty(getTotalPenalty());
    }
    
    public String getPriorityLevel() {
        return getPriority().getKoreanName();
    }
    
    public String getRecommendation() {
        double tooLittlePen = getTooLittlePenalty();
        double tooMuchPen = getTooMuchPenalty();
        String attr = this.attribute.getKoreanName() ;
        
        if (Math.abs(tooLittlePen + tooMuchPen) < 0.1) {
            return attr + " 적정";
        }
        return tooLittlePen > tooMuchPen ? attr + " 증가 필요" : attr + " 감소 필요";
    }
}
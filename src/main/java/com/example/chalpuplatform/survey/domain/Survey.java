package com.example.chalpuplatform.survey.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "surveys")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Survey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_id")
    private Long id;

    @Column(name = "survey_name", length = 255, nullable = false)
    private String surveyName;

    @Column(columnDefinition = "TEXT")
    private String description;

    public static Survey createSurvey(String surveyName, String description) {
        return Survey.builder()
                .surveyName(surveyName)
                .description(description)
                .build();
    }

    public void updateSurvey(String surveyName, String description) {
        this.surveyName = surveyName;
        this.description = description;
    }
}
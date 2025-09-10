package com.example.chalpuplatform.survey.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.jar.domain.JARAttribute;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "survey_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class SurveyQuestion extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "question_text", length = 255, nullable = false)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "jar_attribute")
    private JARAttribute jarAttribute;

    public boolean isJARQuestion() {
        return this.jarAttribute != null && this.questionType == QuestionType.SLIDER;
    }
}
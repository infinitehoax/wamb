package com.eduprep.app.data.mapper

import com.eduprep.app.data.local.QuestionEntity
import com.eduprep.app.domain.model.Question

fun QuestionEntity.toDomain(): Question {
    return Question(
        id = id,
        subject = subject,
        topic = topic,
        year = year,
        text = text,
        optA = optA,
        optB = optB,
        optC = optC,
        optD = optD,
        answer = answer,
        explanation = explanation,
        isTheory = isTheory,
        exactMathAnswer = exactMathAnswer
    )
}

fun Question.toEntity(): QuestionEntity {
    return QuestionEntity(
        id = id,
        subject = subject,
        topic = topic,
        year = year,
        text = text,
        optA = optA,
        optB = optB,
        optC = optC,
        optD = optD,
        answer = answer,
        explanation = explanation,
        isTheory = isTheory,
        exactMathAnswer = exactMathAnswer
    )
}

package com.dhatvibs.modules.rider.service;


import com.dhatvibs.modules.rider.dto.*;
import java.util.List;
import java.util.UUID;

public interface RiderFaqService {
    List<CategoryResponse>     getCategories();
    List<FaqQuestionResponse>  getQuestions(String category);
    FaqAnswerResponse          getAnswer(UUID faqId, UUID sessionId);
}

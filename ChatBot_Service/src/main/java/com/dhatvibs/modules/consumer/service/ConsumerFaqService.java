package com.dhatvibs.modules.consumer.service;


import com.dhatvibs.modules.consumer.dto.*;
import java.util.*;

public interface ConsumerFaqService {
    List<ConsumerCategoryResponse> getCategories();
    List<ConsumerFaqQuestionResponse> getQuestions(
            String category);
    ConsumerFaqAnswerResponse getAnswer(
            UUID faqId, UUID sessionId);
}

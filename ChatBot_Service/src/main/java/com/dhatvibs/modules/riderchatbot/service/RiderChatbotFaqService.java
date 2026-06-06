package com.dhatvibs.modules.riderchatbot.service;

import com.dhatvibs.modules.riderchatbot.dto.*;
import java.util.*;

public interface RiderChatbotFaqService {
    List<RiderChatbotCategoryResponse> getCategories();
    List<RiderChatbotFaqQuestionResponse>
        getQuestions(String category);
    RiderChatbotFaqAnswerResponse getAnswer(
            UUID faqId, UUID sessionId);
}
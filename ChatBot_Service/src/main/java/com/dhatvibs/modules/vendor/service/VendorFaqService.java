package com.dhatvibs.modules.vendor.service;

import com.dhatvibs.modules.vendor.dto.*;
import java.util.*;

public interface VendorFaqService {
    List<VendorCategoryResponse> getCategories();
    List<VendorFaqQuestionResponse> getQuestions(
            String category);
    VendorFaqAnswerResponse getAnswer(
            UUID faqId, UUID sessionId);
}
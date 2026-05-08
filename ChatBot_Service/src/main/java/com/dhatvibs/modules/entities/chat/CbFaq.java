package com.dhatvibs.modules.entities.chat;


/*
 * import jakarta.persistence.*; import lombok.*; import
 * org.hibernate.annotations.JdbcTypeCode; import org.hibernate.type.SqlTypes;
 * import java.time.LocalDateTime; import java.util.UUID;
 * 
 * @Entity
 * 
 * @Table(name = "cb_faqs")
 * 
 * @Data @NoArgsConstructor @AllArgsConstructor @Builder public class CbFaq {
 * 
 * @Id
 * 
 * @GeneratedValue(strategy = GenerationType.UUID)
 * 
 * @Column(columnDefinition = "uuid") private UUID id;
 * 
 * @Column(name = "app_id", nullable = false) private String appId;
 * 
 * @Column(name = "category", nullable = false) private String category;
 * 
 * @Column(name = "intent", nullable = false) private String intent;
 * 
 * @JdbcTypeCode(SqlTypes.ARRAY)
 * 
 * @Column(name = "keywords", columnDefinition = "text[]") private String[]
 * keywords;
 * 
 * @Column(name = "question", nullable = false) private String question;
 * 
 * @Column(name = "answer", nullable = false, columnDefinition = "TEXT") private
 * String answer;
 * 
 * @Column(name = "needs_db") private Boolean needsDb;
 * 
 * @Column(name = "priority") private Integer priority;
 * 
 * @Column(name = "is_active") private Boolean isActive;
 * 
 * @Column(name = "created_at") private LocalDateTime createdAt; }
 */ 



import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cb_faqs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CbFaq {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "app_id", nullable = false)
    private String appId;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "intent", nullable = false)
    private String intent;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "keywords",
            columnDefinition = "text[]")
    private String[] keywords;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "answer",
            nullable = false,
            columnDefinition = "TEXT")
    private String answer;

    @Column(name = "needs_db")
    private Boolean needsDb;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "is_active")
    private Boolean isActive;

    // TRUE for category header rows
    // e.g. "📦 Orders & Tracking"
    @Column(name = "is_category_header")
    private Boolean isCategoryHeader;

    // Display order in UI — lower = first
    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

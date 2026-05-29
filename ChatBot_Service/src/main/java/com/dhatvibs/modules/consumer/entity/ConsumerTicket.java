/*
 * package com.dhatvibs.modules.consumer.entity;
 * 
 * 
 * import jakarta.persistence.*; import lombok.*; import
 * java.time.LocalDateTime; import java.util.UUID;
 * 
 * @Entity
 * 
 * @Table(name = "consumer_tickets")
 * 
 * @Data @NoArgsConstructor @AllArgsConstructor @Builder public class
 * ConsumerTicket {
 * 
 * @Id
 * 
 * @GeneratedValue(strategy = GenerationType.UUID)
 * 
 * @Column(columnDefinition = "uuid") private UUID id;
 * 
 * @Column(name = "session_id") private UUID sessionId;
 * 
 * @Column(name = "consumer_id") private String consumerId;
 * 
 * @Column(name = "order_id") private String orderId;
 * 
 * @Column(name = "category") private String category;
 * 
 * @Column(name = "description", columnDefinition = "TEXT") private String
 * description;
 * 
 * @Column(name = "status") private String status;
 * 
 * @Column(name = "assigned_to") private String assignedTo;
 * 
 * @Column(name = "resolved_at") private LocalDateTime resolvedAt;
 * 
 * @Column(name = "created_at") private LocalDateTime createdAt;
 * 
 * @Column(name = "updated_at") private LocalDateTime updatedAt; }
 */


package com.dhatvibs.modules.consumer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consumer_tickets")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "session_id")
    private UUID sessionId;

    // Consumer details
    @Column(name = "consumer_id")
    private String consumerId;

    @Column(name = "consumer_name")
    private String consumerName;

    @Column(name = "consumer_phone")
    private String consumerPhone;

    @Column(name = "consumer_email")
    private String consumerEmail;

    // Order details
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "order_status")
    private String orderStatus;

	/*
	 * @Column(name = "order_amount") private Double orderAmount;
	 */
    
    @Column(name = "order_amount")
    private BigDecimal orderAmount;

    @Column(name = "store_name")
    private String storeName;

    // Chat summary
    @Column(name = "chat_summary",
            columnDefinition = "TEXT")
    private String chatSummary;

    @Column(name = "category")
    private String category;

    @Column(name = "description",
            columnDefinition = "TEXT")
    private String description;

    // OPEN / IN_PROGRESS / RESOLVED
    @Column(name = "status")
    private String status;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
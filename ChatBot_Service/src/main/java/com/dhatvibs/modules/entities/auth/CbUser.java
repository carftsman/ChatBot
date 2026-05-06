package com.dhatvibs.modules.entities.auth;



import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cb_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CbUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid",
            updatable = false, nullable = false)
    private UUID id;

    @Column(name = "external_user_id", nullable = false)
    private String externalUserId;

    @Column(name = "app_id", nullable = false)
    private String appId;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "password")
    private String password;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

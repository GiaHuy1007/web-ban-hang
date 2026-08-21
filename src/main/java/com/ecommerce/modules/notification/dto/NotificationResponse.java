package com.ecommerce.modules.notification.dto;

import com.ecommerce.modules.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String title;
    private String content;
    private String type;
    private String referenceId;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notif) {
        return NotificationResponse.builder()
                .id(notif.getId())
                .title(notif.getTitle())
                .content(notif.getContent())
                .type(notif.getType())
                .referenceId(notif.getReferenceId())
                .isRead(notif.getIsRead())
                .createdAt(notif.getCreatedAt())
                .build();
    }
}

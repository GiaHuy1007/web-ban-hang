package com.ecommerce.modules.notification.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.util.SecurityUtils;
import com.ecommerce.modules.identity.entity.User;
import com.ecommerce.modules.identity.repository.UserRepository;
import com.ecommerce.modules.notification.dto.NotificationResponse;
import com.ecommerce.modules.notification.entity.Notification;
import com.ecommerce.modules.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createNotification(Long userId, String title, String content, String type, String referenceId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .content(content)
                    .type(type)
                    .referenceId(referenceId)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationRepository.markAllAsReadForUser(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy thông báo."));

        if (!notification.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}

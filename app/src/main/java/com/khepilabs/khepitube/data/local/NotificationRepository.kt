package com.khepilabs.khepitube.data.local

import com.khepilabs.khepitube.data.local.dao.NotificationDao
import com.khepilabs.khepitube.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository
    @Inject
    constructor(
        private val notificationDao: NotificationDao,
    ) {
        val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
        val unreadCount: Flow<Int> = notificationDao.getUnreadCount()

        suspend fun insertNotification(notification: NotificationEntity) {
            notificationDao.insertNotification(notification)
        }

        suspend fun markAllAsRead() {
            notificationDao.markAllAsRead()
        }

        suspend fun deleteNotification(notification: NotificationEntity) {
            notificationDao.deleteNotification(notification)
        }

        suspend fun clearAll() {
            notificationDao.deleteAllNotifications()
        }
    }

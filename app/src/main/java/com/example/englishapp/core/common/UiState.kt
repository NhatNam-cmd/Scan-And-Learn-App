package com.example.englishapp.core.common

/**
 * Khuôn mẫu định nghĩa trạng thái của UI (Ví dụ: Đang loading, Đã có data, Bị lỗi)
 */
interface UiState

/**
 * Khuôn mẫu định nghĩa các hành động từ người dùng (Ví dụ: Click nút, Nhập text)
 */
interface UiEvent

/**
 * Khuôn mẫu định nghĩa các hiệu ứng chỉ xảy ra 1 lần (Ví dụ: Hiện Toast, Chuyển màn hình, Show SnackBar)
 */
interface UiEffect
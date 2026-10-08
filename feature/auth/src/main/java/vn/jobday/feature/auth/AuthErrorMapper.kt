package vn.jobday.feature.auth

import vn.jobday.core.common.AppError

internal fun AppError.toAuthMessage(): String {
    return when (this) {
        AppError.NetworkUnavailable ->
            "Không thể kết nối máy chủ. Vui lòng kiểm tra mạng."

        AppError.Unauthorized ->
            "Thông tin xác thực không hợp lệ hoặc đã hết hạn."

        is AppError.Validation ->
            message

        is AppError.Server ->
            message ?: "Máy chủ đang gặp sự cố. Vui lòng thử lại."

        is AppError.Unknown ->
            "Đã xảy ra lỗi không xác định."
    }
}
package com.example.portfolio.exception;

import java.time.LocalDateTime;
import java.util.List;

// ไฟล์ใหม่ที่ไม่ได้อยู่ใน package tree เดิม — เป็น format มาตรฐานที่ GlobalExceptionHandler ใช้ตอบกลับทุก error
// ตรงตามข้อกำหนด "Error Response Format มาตรฐาน" ในข้อ 7 ของโจทย์
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        List<String> details
) {
}

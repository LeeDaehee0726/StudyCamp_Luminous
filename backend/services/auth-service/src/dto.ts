import { z } from "zod";

// Signup Request
export const SignupSchema = z.object({
    email: z.string().email(),
    password: z.string().min(8, "Password must be at least 8 characters"),
    nickname: z.string().min(1, "Nickname is required"),
    name: z.string().min(1, "Name is required"),
    birthdate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Birthdate must be YYYY-MM-DD"),
    gender: z.enum(["male", "female", "other", "남성", "여성", "기타"]),
    phone: z.string().optional(),
    address: z.string().optional(),
    mbti: z.string().optional(),
});

export type SignupRequest = z.infer<typeof SignupSchema>;

// Login Request
export const LoginSchema = z.object({
    email: z.string().email(),
    password: z.string(),
});

export type LoginRequest = z.infer<typeof LoginSchema>;

// Verify Email Request
export const VerifyEmailSchema = z.object({
    email: z.string().email(),
    code: z.string().length(6, "Verification code must be 6 digits"),
});

export type VerifyEmailRequest = z.infer<typeof VerifyEmailSchema>;

// Resend Code Request
export const ResendCodeSchema = z.object({
    email: z.string().email(),
});

export type ResendCodeRequest = z.infer<typeof ResendCodeSchema>;

// Refresh Token Request
export const RefreshTokenSchema = z.object({
    refreshToken: z.string(),
});

export type RefreshTokenRequest = z.infer<typeof RefreshTokenSchema>;

// Auth Response
export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    idToken: string;
    expiresIn: number;
    userId?: string;
}

// Update Profile Request
export const UpdateProfileSchema = z.object({
    nickname: z.string().min(1, "Nickname is required").optional(),
    name: z.string().min(1, "Name is required").optional(),
    birthdate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Birthdate must be YYYY-MM-DD").optional(),
    gender: z.enum(["male", "female", "other", "남성", "여성", "기타"]).optional(),
    phone: z.string().optional(),
    address: z.string().optional(),
    mbti: z.string().optional(),
});

export type UpdateProfileRequest = z.infer<typeof UpdateProfileSchema>;

// Change Password Request
export const ChangePasswordSchema = z.object({
    oldPassword: z.string().min(1, "Old password is required"),
    newPassword: z.string().min(8, "New password must be at least 8 characters"),
});

export type ChangePasswordRequest = z.infer<typeof ChangePasswordSchema>;

// Delete Account Request
export const DeleteAccountSchema = z.object({
    password: z.string().min(1, "Password is required for account deletion"),
});

export type DeleteAccountRequest = z.infer<typeof DeleteAccountSchema>;

// User Profile (DynamoDB)
export interface UserProfile {
    userId: string; // Cognito Sub
    email: string;
    nickname: string;
    name: string;
    birthdate: string;
    gender: string;
    phone?: string;
    address?: string;
    mbti?: string;
    createdAt: string;
    lastLoginAt: string;
    // Focus session statistics
    totalFocusMinutes?: number;
    totalPagesStudied?: number;
    averagePagesPerHour?: number;
    updatedAt?: string;
}

import type { APIGatewayProxyHandlerV2, APIGatewayProxyEventV2 } from "aws-lambda";
import { Logger } from "@aws-lambda-powertools/logger";
import {
    CognitoIdentityProviderClient,
    SignUpCommand,
    InitiateAuthCommand,
    ConfirmSignUpCommand,
    ResendConfirmationCodeCommand,
    GetUserCommand,
    AdminConfirmSignUpCommand,
    ChangePasswordCommand,
    AdminDeleteUserCommand,
    AdminUpdateUserAttributesCommand,
} from "@aws-sdk/client-cognito-identity-provider";
import { dynamoDocument } from "../../../libs/db";
import { PutCommand, GetCommand, UpdateCommand, ScanCommand, QueryCommand } from "@aws-sdk/lib-dynamodb";
import {
    SignupSchema,
    LoginSchema,
    VerifyEmailSchema,
    ResendCodeSchema,
    RefreshTokenSchema,
    UpdateProfileSchema,
    ChangePasswordSchema,
    DeleteAccountSchema,
    type SignupRequest,
    type LoginRequest,
    type AuthResponse,
    type UserProfile,
    type UpdateProfileRequest,
    type ChangePasswordRequest,
    type DeleteAccountRequest,
} from "./dto";
import { decodeJwt } from "jose";

const logger = new Logger({ serviceName: "auth-service" });

const cognito = new CognitoIdentityProviderClient({ region: process.env.COGNITO_REGION || "ap-northeast-2" });
const USER_POOL_ID = process.env.COGNITO_USER_POOL_ID!;
const CLIENT_ID = process.env.COGNITO_APP_CLIENT_ID!;
const USER_PROFILE_TABLE = process.env.DYNAMO_USER_PROFILE_TABLE || "StudyQuestUserProfile";

// POST /auth/signup
async function signup(event: APIGatewayProxyEventV2) {
    if (!event.body) {
        return {
            statusCode: 400,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Missing request body" }),
        };
    }

    const body = typeof event.body === "string" ? JSON.parse(event.body) : event.body;
    const parsed = SignupSchema.safeParse(body);
    if (!parsed.success) {
        return {
            statusCode: 400,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Invalid request", details: parsed.error }),
        };
    }

    const { email, password, nickname, name, birthdate, gender, phone, address, mbti } = parsed.data;

    try {
        // 1. 닉네임 중복 체크 (DynamoDB Query with GSI)
        const nicknameCheckCommand = new QueryCommand({
            TableName: USER_PROFILE_TABLE,
            IndexName: "NicknameIndex",
            KeyConditionExpression: "nickname = :nickname",
            ExpressionAttributeValues: {
                ":nickname": nickname,
            },
            ProjectionExpression: "userId, nickname",
            Limit: 1,
        });

        const nicknameResult = await dynamoDocument.send(nicknameCheckCommand);
        if (nicknameResult.Items && nicknameResult.Items.length > 0) {
            logger.warn("Nickname already exists", { nickname });
            return {
                statusCode: 409,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    error: "Nickname already exists",
                    message: `닉네임 "${nickname}"은(는) 이미 사용 중입니다. 다른 닉네임을 선택해주세요.`,
                }),
            };
        }

        // 2. 전화번호 중복 체크 (DynamoDB Query with GSI)
        if (phone) {
            const phoneCheckCommand = new QueryCommand({
                TableName: USER_PROFILE_TABLE,
                IndexName: "PhoneIndex",
                KeyConditionExpression: "phone = :phone",
                ExpressionAttributeValues: {
                    ":phone": phone,
                },
                ProjectionExpression: "userId, phone",
                Limit: 1,
            });

            const phoneResult = await dynamoDocument.send(phoneCheckCommand);
            if (phoneResult.Items && phoneResult.Items.length > 0) {
                logger.warn("Phone number already exists", { phone });
                return {
                    statusCode: 409,
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        error: "Phone already exists",
                        message: `전화번호 "${phone}"은(는) 이미 가입되어 있습니다. 다른 전화번호를 사용해주세요.`,
                    }),
                };
            }
        }

        // 3. Cognito SignUp (이메일 중복은 Cognito가 자동으로 체크)
        const signUpCommand = new SignUpCommand({
            ClientId: CLIENT_ID,
            Username: email,
            Password: password,
            UserAttributes: [
                { Name: "email", Value: email },
                { Name: "name", Value: name },
                { Name: "nickname", Value: nickname },
                { Name: "birthdate", Value: birthdate },
                { Name: "gender", Value: gender },
            ],
        });

        const signUpResult = await cognito.send(signUpCommand);
        const userId = signUpResult.UserSub!;

        logger.info("User signed up in Cognito", { userId, email, nickname });

        // 4. Save additional info to DynamoDB
        const now = new Date().toISOString();
        const userProfile: UserProfile = {
            userId,
            email,
            nickname,
            name,
            birthdate,
            gender,
            phone,
            address,
            mbti,
            createdAt: now,
            lastLoginAt: now,
        };

        await dynamoDocument.send(
            new PutCommand({
                TableName: USER_PROFILE_TABLE,
                Item: userProfile,
            })
        );

        logger.info("User profile saved to DynamoDB", { userId });

        try {
            await cognito.send(
                new AdminConfirmSignUpCommand({
                    UserPoolId: USER_POOL_ID,
                    Username: email,
                })
            );
            logger.info("User auto-confirmed after signup", { userId, email });
        } catch (confirmError: any) {
            logger.warn("Auto confirmation after signup failed", {
                email,
                error: confirmError.message,
            });
        }

        return {
            statusCode: 201,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                message: "User created successfully.",
                userId,
                emailVerificationRequired: false, // 이메일 인증 비활성화
            }),
        };
    } catch (error: any) {
        logger.error("Signup failed", { error: error.message, errorName: error.name });

        // Cognito 에러 처리
        if (error.name === "UsernameExistsException") {
            return {
                statusCode: 409,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    error: "Email already exists",
                    message: `이메일 "${email}"은(는) 이미 가입되어 있습니다. 로그인하거나 다른 이메일을 사용해주세요.`,
                }),
            };
        }

        if (error.name === "InvalidPasswordException") {
            return {
                statusCode: 400,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    error: "Invalid password",
                    message: "비밀번호는 최소 8자 이상이며, 문자, 숫자, 특수문자를 포함해야 합니다.",
                }),
            };
        }

        if (error.name === "InvalidParameterException") {
            return {
                statusCode: 400,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    error: "Invalid parameters",
                    message: error.message || "입력 정보가 올바르지 않습니다.",
                }),
            };
        }

        // 일반 에러
        return {
            statusCode: 500,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                error: "Signup failed",
                message: error.message || "회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            }),
        };
    }
}

// POST /auth/login
async function login(event: APIGatewayProxyEventV2) {
    if (!event.body) {
        return {
            statusCode: 400,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Missing request body" }),
        };
    }

    logger.info("Raw event.body", { body: event.body, type: typeof event.body });

    let body;
    try {
        body = typeof event.body === "string" ? JSON.parse(event.body) : event.body;
    } catch (error: any) {
        logger.error("JSON parse error", { error: error.message, body: event.body });
        return {
            statusCode: 400,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Invalid JSON", message: error.message }),
        };
    }

    const parsed = LoginSchema.safeParse(body);
    if (!parsed.success) {
        return {
            statusCode: 400,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Invalid request", details: parsed.error }),
        };
    }

    const { email, password } = parsed.data;

    try {
        return await performLogin(email, password);
    } catch (error: any) {
        const notConfirmed =
            error.name === "UserNotConfirmedException" ||
            (error.name === "NotAuthorizedException" && typeof error.message === "string" && error.message.includes("not confirmed"));

        if (notConfirmed) {
            logger.warn("User not confirmed. Auto confirming...", { email });
            try {
                await autoConfirmUser(email);
                return await performLogin(email, password);
            } catch (retryError: any) {
                logger.error("Auto confirmation retry failed", { email, error: retryError.message });
                return {
                    statusCode: 401,
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        error: "Login failed",
                        message: retryError.message || "Unable to confirm user",
                    }),
                };
            }
        }

        logger.error("Login failed", { error: error.message, errorName: error.name, email });

        // Cognito 에러를 친절한 한글로 변환
        if (error.name === "NotAuthorizedException") {
            return {
                statusCode: 401,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    error: "Invalid credentials",
                    message: "이메일 또는 비밀번호가 일치하지 않습니다.\n입력하신 정보를 다시 확인해주세요.",
                }),
            };
        }

        if (error.name === "UserNotFoundException") {
            return {
                statusCode: 404,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    error: "User not found",
                    message: `"${email}"은(는) 가입되지 않은 이메일입니다.\n회원가입 후 이용해주세요.`,
                }),
            };
        }

        if (error.name === "UserNotConfirmedException") {
            return {
                statusCode: 403,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    error: "User not confirmed",
                    message: "이메일 인증이 완료되지 않았습니다.\n이메일을 확인하여 인증을 완료해주세요.",
                }),
            };
        }

        if (error.name === "TooManyRequestsException") {
            return {
                statusCode: 429,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    error: "Too many requests",
                    message: "로그인 시도가 너무 많습니다.\n잠시 후 다시 시도해주세요.",
                }),
            };
        }

        // 일반 에러
        return {
            statusCode: 401,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                error: "Login failed",
                message: "로그인에 실패했습니다.\n잠시 후 다시 시도해주세요.",
            }),
        };
    }
}

// POST /auth/verify-email
async function verifyEmail(event: APIGatewayProxyEventV2) {
    if (!event.body) {
        return {
            statusCode: 400,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Missing request body" }),
        };
    }

    const body = typeof event.body === "string" ? JSON.parse(event.body) : event.body;
    const parsed = VerifyEmailSchema.safeParse(body);
    if (!parsed.success) {
        return {
            statusCode: 400,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Invalid request", details: parsed.error }),
        };
    }

    const { email, code } = parsed.data;

    try {
        const confirmCommand = new ConfirmSignUpCommand({
            ClientId: CLIENT_ID,
            Username: email,
            ConfirmationCode: code,
        });

        await cognito.send(confirmCommand);

        logger.info("Email verified", { email });

        return {
            statusCode: 200,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message: "Email verified successfully" }),
        };
    } catch (error: any) {
        logger.error("Email verification failed", { error: error.message, email });

        return {
            statusCode: 400,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                error: "Email verification failed",
                message: error.message || "Invalid code",
            }),
        };
    }
}

// POST /auth/resend-code
async function resendCode(event: APIGatewayProxyEventV2) {
    if (!event.body) {
        return {
            statusCode: 400,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Missing request body" }),
        };
    }

    const body = typeof event.body === "string" ? JSON.parse(event.body) : event.body;
    const parsed = ResendCodeSchema.safeParse(body);
    if (!parsed.success) {
        return {
            statusCode: 400,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Invalid request", details: parsed.error }),
        };
    }

    const { email } = parsed.data;

    try {
        const resendCommand = new ResendConfirmationCodeCommand({
            ClientId: CLIENT_ID,
            Username: email,
        });

        await cognito.send(resendCommand);

        logger.info("Verification code resent", { email });

        return {
            statusCode: 200,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message: "Verification code resent" }),
        };
    } catch (error: any) {
        logger.error("Resend code failed", { error: error.message, email });

        return {
            statusCode: 400,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                error: "Resend code failed",
                message: error.message,
            }),
        };
    }
}

// POST /auth/refresh
async function refreshToken(event: APIGatewayProxyEventV2) {
    if (!event.body) {
        return {
            statusCode: 400,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Missing request body" }),
        };
    }

    const body = typeof event.body === "string" ? JSON.parse(event.body) : event.body;
    const parsed = RefreshTokenSchema.safeParse(body);
    if (!parsed.success) {
        return {
            statusCode: 400,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Invalid request", details: parsed.error }),
        };
    }

    const { refreshToken } = parsed.data;

    try {
        const authCommand = new InitiateAuthCommand({
            ClientId: CLIENT_ID,
            AuthFlow: "REFRESH_TOKEN_AUTH",
            AuthParameters: {
                REFRESH_TOKEN: refreshToken,
            },
        });

        const authResult = await cognito.send(authCommand);

        if (!authResult.AuthenticationResult) {
            return {
                statusCode: 401,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "Token refresh failed" }),
            };
        }

        const { AccessToken, IdToken, ExpiresIn } = authResult.AuthenticationResult;

        logger.info("Token refreshed");

        const response: Partial<AuthResponse> = {
            accessToken: AccessToken!,
            idToken: IdToken!,
            expiresIn: ExpiresIn!,
        };

        return {
            statusCode: 200,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(response),
        };
    } catch (error: any) {
        logger.error("Token refresh failed", { error: error.message });

        return {
            statusCode: 401,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                error: "Token refresh failed",
                message: error.message,
            }),
        };
    }
}

async function performLogin(email: string, password: string) {
    const authCommand = new InitiateAuthCommand({
        ClientId: CLIENT_ID,
        AuthFlow: "USER_PASSWORD_AUTH",
        AuthParameters: {
            USERNAME: email,
            PASSWORD: password,
        },
    });

    const authResult = await cognito.send(authCommand);

    if (!authResult.AuthenticationResult) {
        return {
            statusCode: 401,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Authentication failed" }),
        };
    }

    const { AccessToken, RefreshToken, IdToken, ExpiresIn } = authResult.AuthenticationResult;

    const getUserCommand = new GetUserCommand({
        AccessToken: AccessToken!,
    });
    const userResult = await cognito.send(getUserCommand);
    const userId = userResult.UserAttributes?.find(attr => attr.Name === "sub")?.Value;

    const now = new Date().toISOString();
    await dynamoDocument.send(
        new UpdateCommand({
            TableName: USER_PROFILE_TABLE,
            Key: { userId },
            UpdateExpression: "SET lastLoginAt = :now",
            ExpressionAttributeValues: {
                ":now": now,
            },
        })
    );

    logger.info("User logged in", { userId, email });

    const response: AuthResponse = {
        accessToken: AccessToken!,
        refreshToken: RefreshToken!,
        idToken: IdToken!,
        expiresIn: ExpiresIn!,
        userId,
    };

    return {
        statusCode: 200,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(response),
    };
}

async function autoConfirmUser(email: string) {
    await cognito.send(
        new AdminConfirmSignUpCommand({
            UserPoolId: USER_POOL_ID,
            Username: email,
        })
    );
    logger.info("User auto-confirmed via admin API", { email });
}

// GET /auth/profile - Get user profile with focus statistics
async function getProfile(event: APIGatewayProxyEventV2) {
    try {
        // Extract userId from JWT token
        const userId = (event.requestContext as any).authorizer?.jwt?.claims?.sub;

        if (!userId) {
            return {
                statusCode: 401,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "Unauthorized - userId not found in token" }),
            };
        }

        logger.info("Fetching user profile", { userId });

        // Get user profile from DynamoDB
        const result = await dynamoDocument.send(
            new GetCommand({
                TableName: USER_PROFILE_TABLE,
                Key: { userId },
            })
        );

        if (!result.Item) {
            return {
                statusCode: 404,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "User profile not found" }),
            };
        }

        const profile = result.Item as UserProfile;

        return {
            statusCode: 200,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                userId: profile.userId,
                email: profile.email,
                nickname: profile.nickname,
                name: profile.name,
                birthdate: profile.birthdate,
                gender: profile.gender,
                phone: profile.phone,
                address: profile.address,
                mbti: profile.mbti,
                totalFocusMinutes: profile.totalFocusMinutes || 0,
                totalPagesStudied: profile.totalPagesStudied || 0,
                averagePagesPerHour: profile.averagePagesPerHour || 0,
                createdAt: profile.createdAt,
                lastLoginAt: profile.lastLoginAt,
                updatedAt: profile.updatedAt,
            }),
        };
    } catch (error) {
        logger.error("Error fetching profile", { error });
        return {
            statusCode: 500,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Failed to fetch profile" }),
        };
    }
}

// PUT /auth/profile - Update user profile
async function updateProfile(event: APIGatewayProxyEventV2) {
    try {
        const userId = (event.requestContext as any).authorizer?.jwt?.claims?.sub;

        if (!userId) {
            return {
                statusCode: 401,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "Unauthorized" }),
            };
        }

        if (!event.body) {
            return {
                statusCode: 400,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "Missing request body" }),
            };
        }

        const body = typeof event.body === "string" ? JSON.parse(event.body) : event.body;
        const parsed = UpdateProfileSchema.safeParse(body);

        if (!parsed.success) {
            return {
                statusCode: 400,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "Invalid request", details: parsed.error }),
            };
        }

        const updates = parsed.data;
        logger.info("Updating user profile", { userId, updates });

        // Build DynamoDB update expression
        const updateExpressions: string[] = [];
        const expressionAttributeNames: Record<string, string> = {};
        const expressionAttributeValues: Record<string, any> = {};

        if (updates.nickname) {
            updateExpressions.push("#nickname = :nickname");
            expressionAttributeNames["#nickname"] = "nickname";
            expressionAttributeValues[":nickname"] = updates.nickname;
        }
        if (updates.name) {
            updateExpressions.push("#name = :name");
            expressionAttributeNames["#name"] = "name";
            expressionAttributeValues[":name"] = updates.name;
        }
        if (updates.birthdate) {
            updateExpressions.push("#birthdate = :birthdate");
            expressionAttributeNames["#birthdate"] = "birthdate";
            expressionAttributeValues[":birthdate"] = updates.birthdate;
        }
        if (updates.gender) {
            updateExpressions.push("#gender = :gender");
            expressionAttributeNames["#gender"] = "gender";
            expressionAttributeValues[":gender"] = updates.gender;
        }
        if (updates.phone !== undefined) {
            updateExpressions.push("#phone = :phone");
            expressionAttributeNames["#phone"] = "phone";
            expressionAttributeValues[":phone"] = updates.phone;
        }
        if (updates.address !== undefined) {
            updateExpressions.push("#address = :address");
            expressionAttributeNames["#address"] = "address";
            expressionAttributeValues[":address"] = updates.address;
        }
        if (updates.mbti !== undefined) {
            updateExpressions.push("#mbti = :mbti");
            expressionAttributeNames["#mbti"] = "mbti";
            expressionAttributeValues[":mbti"] = updates.mbti;
        }

        updateExpressions.push("#updatedAt = :updatedAt");
        expressionAttributeNames["#updatedAt"] = "updatedAt";
        expressionAttributeValues[":updatedAt"] = new Date().toISOString();

        // Update DynamoDB
        await dynamoDocument.send(
            new UpdateCommand({
                TableName: USER_PROFILE_TABLE,
                Key: { userId },
                UpdateExpression: `SET ${updateExpressions.join(", ")}`,
                ExpressionAttributeNames: expressionAttributeNames,
                ExpressionAttributeValues: expressionAttributeValues,
            })
        );

        logger.info("Profile updated successfully", { userId });

        return {
            statusCode: 200,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message: "Profile updated successfully" }),
        };
    } catch (error) {
        logger.error("Error updating profile", { error });
        return {
            statusCode: 500,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Failed to update profile" }),
        };
    }
}

// PUT /auth/password - Change user password
async function changePassword(event: APIGatewayProxyEventV2) {
    try {
        const userId = (event.requestContext as any).authorizer?.jwt?.claims?.sub;
        const accessToken = event.headers.authorization?.replace("Bearer ", "");

        if (!userId || !accessToken) {
            return {
                statusCode: 401,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "Unauthorized" }),
            };
        }

        if (!event.body) {
            return {
                statusCode: 400,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "Missing request body" }),
            };
        }

        const body = typeof event.body === "string" ? JSON.parse(event.body) : event.body;
        const parsed = ChangePasswordSchema.safeParse(body);

        if (!parsed.success) {
            return {
                statusCode: 400,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "Invalid request", details: parsed.error }),
            };
        }

        const { oldPassword, newPassword } = parsed.data;
        logger.info("Changing password", { userId });

        // Change password in Cognito
        await cognito.send(
            new ChangePasswordCommand({
                AccessToken: accessToken,
                PreviousPassword: oldPassword,
                ProposedPassword: newPassword,
            })
        );

        logger.info("Password changed successfully", { userId });

        return {
            statusCode: 200,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message: "Password changed successfully" }),
        };
    } catch (error: any) {
        logger.error("Error changing password", { error });

        if (error.name === "NotAuthorizedException") {
            return {
                statusCode: 401,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "Incorrect password", message: "현재 비밀번호가 일치하지 않습니다." }),
            };
        }

        return {
            statusCode: 500,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Failed to change password" }),
        };
    }
}

// DELETE /auth/account - Delete user account
async function deleteAccount(event: APIGatewayProxyEventV2) {
    try {
        const userId = (event.requestContext as any).authorizer?.jwt?.claims?.sub;
        const email = (event.requestContext as any).authorizer?.jwt?.claims?.email;

        if (!userId || !email) {
            return {
                statusCode: 401,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "Unauthorized" }),
            };
        }

        if (!event.body) {
            return {
                statusCode: 400,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "Missing request body" }),
            };
        }

        const body = typeof event.body === "string" ? JSON.parse(event.body) : event.body;
        const parsed = DeleteAccountSchema.safeParse(body);

        if (!parsed.success) {
            return {
                statusCode: 400,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "Invalid request", details: parsed.error }),
            };
        }

        const { password } = parsed.data;
        logger.info("Deleting account", { userId, email });

        // Verify password by attempting login
        try {
            await cognito.send(
                new InitiateAuthCommand({
                    AuthFlow: "USER_PASSWORD_AUTH",
                    ClientId: CLIENT_ID,
                    AuthParameters: {
                        USERNAME: email,
                        PASSWORD: password,
                    },
                })
            );
        } catch (error: any) {
            logger.error("Password verification failed", { error });
            return {
                statusCode: 401,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ error: "Incorrect password", message: "비밀번호가 일치하지 않습니다." }),
            };
        }

        // Delete user from Cognito
        await cognito.send(
            new AdminDeleteUserCommand({
                UserPoolId: USER_POOL_ID,
                Username: email,
            })
        );

        // Delete user profile from DynamoDB
        await dynamoDocument.send(
            new UpdateCommand({
                TableName: USER_PROFILE_TABLE,
                Key: { userId },
                UpdateExpression: "SET #deleted = :deleted, #deletedAt = :deletedAt",
                ExpressionAttributeNames: {
                    "#deleted": "deleted",
                    "#deletedAt": "deletedAt",
                },
                ExpressionAttributeValues: {
                    ":deleted": true,
                    ":deletedAt": new Date().toISOString(),
                },
            })
        );

        logger.info("Account deleted successfully", { userId });

        return {
            statusCode: 200,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message: "Account deleted successfully" }),
        };
    } catch (error) {
        logger.error("Error deleting account", { error });
        return {
            statusCode: 500,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Failed to delete account" }),
        };
    }
}

export const handler: APIGatewayProxyHandlerV2 = async (event: APIGatewayProxyEventV2) => {
    const requestId = event.requestContext.requestId;
    logger.addPersistentLogAttributes({ requestId });

    const method = event.requestContext.http.method;
    const path = event.rawPath;

    logger.info("Auth service request", { method, path });

    try {
        if (method === "POST" && path === "/auth/signup") {
            return await signup(event);
        }

        if (method === "POST" && path === "/auth/login") {
            return await login(event);
        }

        if (method === "POST" && path === "/auth/verify-email") {
            return await verifyEmail(event);
        }

        if (method === "POST" && path === "/auth/resend-code") {
            return await resendCode(event);
        }

        if (method === "POST" && path === "/auth/refresh") {
            return await refreshToken(event);
        }

        if (method === "GET" && path === "/auth/profile") {
            return await getProfile(event);
        }

        if (method === "PUT" && path === "/auth/profile") {
            return await updateProfile(event);
        }

        if (method === "PUT" && path === "/auth/password") {
            return await changePassword(event);
        }

        if (method === "DELETE" && path === "/auth/account") {
            return await deleteAccount(event);
        }

        return {
            statusCode: 404,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Not found" }),
        };
    } catch (error) {
        logger.error("Unhandled error", { error });
        return {
            statusCode: 500,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ error: "Internal server error" }),
        };
    }
};

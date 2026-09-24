package com.nddfeon.demonic.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

enum class ShareTarget {
    WHATSAPP,
    INSTAGRAM,
    ALL
}

object DemonicCardGenerator {

    const val CARD_WIDTH = 1080
    const val CARD_HEIGHT = 1920

    fun generateVipStoryCardBitmap(
        context: Context,
        displayName: String,
        avatarEmoji: String?,
        recentRoomsCount: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Deep Space Black / Crimson Background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, CARD_HEIGHT.toFloat(),
                intArrayOf(
                    Color.parseColor("#08050C"),
                    Color.parseColor("#1B081E"),
                    Color.parseColor("#260814"),
                    Color.parseColor("#0B0812")
                ),
                floatArrayOf(0f, 0.35f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), bgPaint)

        // 2. Ambient Glowing Blobs
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        // Top-left Crimson Glow
        glowPaint.shader = RadialGradient(
            150f, 250f, 450f,
            intArrayOf(Color.parseColor("#44E50914"), Color.TRANSPARENT),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(150f, 250f, 450f, glowPaint)

        // Center-right Violet Glow
        glowPaint.shader = RadialGradient(
            CARD_WIDTH - 150f, 950f, 500f,
            intArrayOf(Color.parseColor("#387C4DFF"), Color.TRANSPARENT),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(CARD_WIDTH - 150f, 950f, 500f, glowPaint)

        // Bottom Amber Glow
        glowPaint.shader = RadialGradient(
            CARD_WIDTH / 2f, CARD_HEIGHT - 200f, 400f,
            intArrayOf(Color.parseColor("#25FFB300"), Color.TRANSPARENT),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(CARD_WIDTH / 2f, CARD_HEIGHT - 200f, 400f, glowPaint)

        // 3. Outer Neon Border Frame
        val frameRect = RectF(50f, 60f, CARD_WIDTH - 50f, CARD_HEIGHT - 60f)
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            shader = LinearGradient(
                50f, 60f, CARD_WIDTH - 50f, CARD_HEIGHT - 60f,
                intArrayOf(
                    Color.parseColor("#E50914"),
                    Color.parseColor("#9C27B0"),
                    Color.parseColor("#FFB300")
                ),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(frameRect, 48f, 48f, framePaint)

        // 4. Header Section: APP TITLE & VIP BADGE
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 72f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.12f
        }
        canvas.drawText("⚡ DEMONIC", CARD_WIDTH / 2f, 175f, titlePaint)

        val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF5252")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.18f
        }
        canvas.drawText("OFFICIAL VIP PARTY PASS • 2026 EDITION", CARD_WIDTH / 2f, 225f, subTitlePaint)

        // 5. Center Holographic Card Box
        val cardRect = RectF(90f, 280f, CARD_WIDTH - 90f, 1260f)
        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = LinearGradient(
                90f, 280f, CARD_WIDTH - 90f, 1260f,
                intArrayOf(
                    Color.parseColor("#E61E1428"),
                    Color.parseColor("#E6280B1C"),
                    Color.parseColor("#E6130C1C")
                ),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(cardRect, 40f, 40f, cardBgPaint)

        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            shader = LinearGradient(
                90f, 280f, CARD_WIDTH - 90f, 1260f,
                intArrayOf(
                    Color.parseColor("#FF2D55"),
                    Color.parseColor("#7C4DFF"),
                    Color.parseColor("#FFB300")
                ),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(cardRect, 40f, 40f, cardBorderPaint)

        // VIP Pill in Card
        val vipPillRect = RectF(CARD_WIDTH / 2f - 180f, 320f, CARD_WIDTH / 2f + 180f, 375f)
        val vipPillBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#33E50914")
        }
        val vipPillBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.parseColor("#E50914")
        }
        canvas.drawRoundRect(vipPillRect, 28f, 28f, vipPillBg)
        canvas.drawRoundRect(vipPillRect, 28f, 28f, vipPillBorder)

        val vipPillText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF5252")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.15f
        }
        canvas.drawText("🔥 VERIFIED VIBE MASTER", CARD_WIDTH / 2f, 357f, vipPillText)

        // 6. Glowing Circular Avatar
        val avatarCenterX = CARD_WIDTH / 2f
        val avatarCenterY = 510f
        val avatarRadius = 100f

        // Outer glow circle
        val avatarGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            shader = LinearGradient(
                avatarCenterX - avatarRadius, avatarCenterY - avatarRadius,
                avatarCenterX + avatarRadius, avatarCenterY + avatarRadius,
                intArrayOf(Color.parseColor("#E50914"), Color.parseColor("#E040FB")),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius + 12f, avatarGlow)

        // Inner circle background
        val avatarBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#1B1226")
        }
        canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius, avatarBg)

        // Avatar Emoji or Initial
        val avatarIcon = avatarEmoji ?: "😈"
        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 90f
            textAlign = Paint.Align.CENTER
        }
        // Center text vertically
        val bounds = Rect()
        emojiPaint.getTextBounds(avatarIcon, 0, avatarIcon.length, bounds)
        canvas.drawText(avatarIcon, avatarCenterX, avatarCenterY + bounds.height() / 2f - 4f, emojiPaint)

        // 7. User Display Name
        val cleanName = if (displayName.isNotBlank()) displayName.trim().uppercase() else "DEMON GUEST"
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        canvas.drawText(cleanName, CARD_WIDTH / 2f, 680f, namePaint)

        val rolePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B388FF")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.1f
        }
        canvas.drawText("🎧 PARTY HOST & MUSIC SYNCER", CARD_WIDTH / 2f, 725f, rolePaint)

        // Divider Line
        val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 2f
            shader = LinearGradient(
                160f, 765f, CARD_WIDTH - 160f, 765f,
                intArrayOf(Color.TRANSPARENT, Color.parseColor("#55E50914"), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawLine(160f, 765f, CARD_WIDTH - 160f, 765f, divPaint)

        // 8. 3 Feature Highlights (Horizontal cards inside the VIP card)
        val featureItems = listOf(
            Triple("🎵", "REAL-TIME SYNC", "Zero-delay synced YouTube music & videos"),
            Triple("💣", "CHAT SPAM & SFX", "Rapid message blasting & live floating reactions"),
            Triple("✨", "NO SPOTIFY PREMIUM", "100% free with homies, no subscription ever")
        )

        var featureY = 820f
        for (item in featureItems) {
            val itemRect = RectF(140f, featureY, CARD_WIDTH - 140f, featureY + 115f)
            val itemBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#22FFFFFF")
            }
            canvas.drawRoundRect(itemRect, 20f, 20f, itemBg)

            val itemBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
                color = Color.parseColor("#33FFFFFF")
            }
            canvas.drawRoundRect(itemRect, 20f, 20f, itemBorder)

            // Emoji icon
            val fEmojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 44f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(item.first, 195f, featureY + 72f, fEmojiPaint)

            // Title
            val fTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.05f
            }
            canvas.drawText(item.second, 250f, featureY + 50f, fTitlePaint)

            // Subtitle
            val fSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#B0BEC5")
                textSize = 22f
            }
            canvas.drawText(item.third, 250f, featureY + 86f, fSubPaint)

            featureY += 135f
        }

        // 9. Stats Section (Rooms Jammed Pill)
        val statsRect = RectF(140f, 1310f, CARD_WIDTH - 140f, 1450f)
        val statsBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = LinearGradient(
                140f, 1310f, CARD_WIDTH - 140f, 1450f,
                intArrayOf(Color.parseColor("#33FFB300"), Color.parseColor("#33E50914")),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(statsRect, 26f, 26f, statsBg)

        val statsBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.parseColor("#80FFB300")
        }
        canvas.drawRoundRect(statsRect, 26f, 26f, statsBorder)

        val statsTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFD54F")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.1f
        }
        canvas.drawText("⚡ DEMONIC PARTY ACTIVITY", CARD_WIDTH / 2f, 1360f, statsTitle)

        val statsVal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$recentRoomsCount Rooms Visited • 100% Unlocked", CARD_WIDTH / 2f, 1410f, statsVal)

        // 10. Call To Action at Bottom
        val ctaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.05f
        }
        canvas.drawText("JOIN MY MUSIC ROOM & VIBE WITH ME 🎶", CARD_WIDTH / 2f, 1550f, ctaPaint)

        val ctaSub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF5252")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        canvas.drawText("👉 DOWNLOAD FREE DEMONIC APP ON ANDROID", CARD_WIDTH / 2f, 1600f, ctaSub)

        val ctaLink = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#90CAF9")
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("github.com/silentboy07/app", CARD_WIDTH / 2f, 1645f, ctaLink)

        // 11. Digital Verification Barcode / Cyber Lines at bottom
        val barcodeY = 1710f
        val barcodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#55FFFFFF")
        }
        val barWidths = listOf(4f, 8f, 2f, 6f, 12f, 3f, 5f, 10f, 4f, 7f, 3f, 8f, 12f, 4f, 6f, 10f, 3f, 8f, 5f, 12f, 4f, 6f)
        var barX = 260f
        for (w in barWidths) {
            barcodePaint.strokeWidth = w
            canvas.drawLine(barX, barcodeY, barX, barcodeY + 45f, barcodePaint)
            barX += w + 12f
        }

        val codeLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#66FFFFFF")
            textSize = 18f
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.25f
        }
        canvas.drawText("DEMONIC-VIP-AUTHENTICATED-GENZ-2026", CARD_WIDTH / 2f, barcodeY + 80f, codeLabel)

        return bitmap
    }

    fun shareVipCard(
        context: Context,
        displayName: String,
        avatarEmoji: String?,
        recentRoomsCount: Int,
        target: ShareTarget
    ) {
        try {
            val bitmap = generateVipStoryCardBitmap(context, displayName, avatarEmoji, recentRoomsCount)
            val cacheDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
            val imageFile = File(cacheDir, "demonic_vip_card.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val caption = buildString {
                appendLine("🔥 Check out my DEMONIC VIP Music Pass! 🎧")
                appendLine("We listen to YouTube music together in real-time sync with homies!")
                appendLine("⚡ No Spotify Premium needed • 100% Free Forever")
                appendLine("💣 Rapid Chat Spammer & Floating Live Reactions")
                appendLine()
                appendLine("👉 Download DEMONIC App and vibe with me:")
                append("https://github.com/silentboy07/app")
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            when (target) {
                ShareTarget.WHATSAPP -> {
                    intent.setPackage("com.whatsapp")
                    try {
                        context.startActivity(intent)
                        return
                    } catch (_: Exception) {
                        // If WhatsApp not installed, fallback to chooser
                        Toast.makeText(context, "WhatsApp not installed, opening share menu...", Toast.LENGTH_SHORT).show()
                    }
                }
                ShareTarget.INSTAGRAM -> {
                    intent.setPackage("com.instagram.android")
                    try {
                        context.startActivity(intent)
                        return
                    } catch (_: Exception) {
                        // If Instagram not installed, fallback to chooser
                        Toast.makeText(context, "Instagram not installed, opening share menu...", Toast.LENGTH_SHORT).show()
                    }
                }
                ShareTarget.ALL -> {
                    // System chooser
                }
            }

            val chooser = Intent.createChooser(intent, "Share DEMONIC VIP Story Card 🔥")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Sharing failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

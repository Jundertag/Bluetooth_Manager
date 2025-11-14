package com.jayden.bluetooth.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.jayden.bluetooth.R
import kotlinx.parcelize.Parcelize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.min

class BluetoothStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    @ColorInt
    private var disconnectedColor: Int = 0xFF0F67AA.toInt()
    @ColorInt
    private var connectingColor: Int = 0xFFD1BC4D.toInt()
    @ColorInt
    private var disconnectingColor: Int = 0xFFD1BC4D.toInt()
    @ColorInt
    private var connectedColor: Int = 0xFF1F8C36.toInt()
    @ColorInt
    private var advertisingColor: Int = 0xFFA91113.toInt()
    @ColorInt
    private var alphaColor: Int = 0xFF303030.toInt()
    private var radiusPx = inDp(8f)
    private var inactiveAlpha: Float = 0.10f
    private var idleOffBlinkRate: Int = 2000
    private var scanOffBlinkRate: Int = 500
    private var pairOffBlinkRate: Int = 500
    private var idleOnBlinkRate: Int = 2000
    private var scanOnBlinkRate: Int = 500
    private var pairOnBlinkRate: Int = 500
    private var running: Boolean = false
    private var onPhase: Boolean = false
    private var currentStateColor: Int = disconnectedColor
    private var currentColor: Int = currentStateColor
    private var viewScope: CoroutineScope? = null
    private var job: Job? = null

    enum class Mode {
        OFFLINE,
        PASSIVE,
        SCANNING,
        ADVERTISING,
    }

    enum class ConnectState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
    }

    private var mode: Mode = Mode.OFFLINE
    private var connectState: ConnectState = ConnectState.DISCONNECTED

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BluetoothStatusView, defStyleAttr, 0).use { a ->
            disconnectedColor = a.getColor(R.styleable.BluetoothStatusView_idleColor, disconnectedColor)
            advertisingColor = a.getColor(R.styleable.BluetoothStatusView_pairColor, advertisingColor)
            connectedColor = a.getColor(R.styleable.BluetoothStatusView_connectedColor, connectedColor)
            inactiveAlpha = a.getFloat(R.styleable.BluetoothStatusView_inactiveAlpha, inactiveAlpha)
            idleOffBlinkRate = a.getInt(R.styleable.BluetoothStatusView_idleOffBlinkRate, idleOffBlinkRate)
            scanOffBlinkRate = a.getInt(R.styleable.BluetoothStatusView_scanOffBlinkRate, scanOffBlinkRate)
            pairOffBlinkRate = a.getInt(R.styleable.BluetoothStatusView_pairOffBlinkRate, pairOffBlinkRate)
            idleOnBlinkRate = a.getInt(R.styleable.BluetoothStatusView_idleOnBlinkRate, idleOnBlinkRate)
            scanOnBlinkRate = a.getInt(R.styleable.BluetoothStatusView_scanOnBlinkRate, scanOnBlinkRate)
            pairOnBlinkRate = a.getInt(R.styleable.BluetoothStatusView_pairOnBlinkRate, pairOnBlinkRate)
        }
        paint.style = Paint.Style.FILL
        contentDescription = "Bluetooth Status"
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (viewScope == null) {
            viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        }
        startPattern()
    }

    override fun onDetachedFromWindow() {
        job?.cancel()
        job = null
        viewScope?.cancel()
        viewScope = null
        super.onDetachedFromWindow()
        stopPattern()
    }

    fun postState(state: Mode) {
        mode = state
    }

    fun postConnectState(state: ConnectState) {
        connectState = state
    }

    private fun inDp(value: Float): Float {
        return value * resources.displayMetrics.density
    }

    private var index: Int = 0
    private fun startPattern() {
        job?.cancel()
        job = viewScope?.launch {
            when (mode) {
                Mode.OFFLINE -> {
                    currentColor = alphaColor
                    invalidate()
                }

                Mode.PASSIVE -> {
                    if (index == 0) {
                        currentColor = alphaColor
                        index++
                        onPhase = false
                        invalidate()
                        delay(idleOffBlinkRate.toLong())
                    } else if (index == 1) {
                        currentColor = currentStateColor
                        index = 0
                        onPhase = true
                        invalidate()
                        delay(idleOnBlinkRate.toLong())
                    }
                }

                Mode.SCANNING -> {
                    if (index == 0) {
                        currentColor = alphaColor
                        index++
                        onPhase = false
                        invalidate()
                        delay(scanOffBlinkRate.toLong())
                    } else if (index == 1) {
                        currentColor = currentStateColor
                        index = 0
                        onPhase = true
                        invalidate()
                        delay(scanOnBlinkRate.toLong())
                    }
                }

                Mode.ADVERTISING -> {
                    if (index == 0) {
                        currentColor = currentStateColor
                        index++
                        onPhase = false
                        invalidate()
                        delay(pairOffBlinkRate.toLong())
                    } else if (index == 1) {
                        currentColor = advertisingColor
                        index = 0
                        onPhase = true
                        invalidate()
                        delay(pairOnBlinkRate.toLong())
                    }
                }
            }
            when (connectState) {
                ConnectState.DISCONNECTED -> {
                    currentStateColor = disconnectedColor
                }

                ConnectState.CONNECTING -> {
                    currentStateColor = connectingColor
                }

                ConnectState.CONNECTED -> {
                    currentStateColor = connectedColor
                }

                ConnectState.DISCONNECTING -> {
                    currentStateColor = disconnectingColor
                }
            }
        }
    }

    private fun stopPattern() {
        job?.cancel()
        job = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        paint.color = currentColor
        paint.alpha = if (onPhase) 255 else (inactiveAlpha * 255).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, min(radiusPx, min(cx, cy)), paint)
    }

    @Parcelize
    private class SavedState(
        var disconnectedColor: Int = 0,
        var connectingColor: Int = 0,
        var connectedColor: Int = 0,
        var disconnectingColor: Int = 0,
        var advertisingColor: Int = 0,
        var inactiveAlpha: Float = 0f,
        var idleOffBlinkRate: Int = 0,
        var scanOffBlinkRate: Int = 0,
        var pairOffBlinkRate: Int = 0,
        var idleOnBlinkRate: Int = 0,
        var scanOnBlinkRate: Int = 0,
        var pairOnBlinkRate: Int = 0,
        var currentStateColor: Int = disconnectedColor,
        var currentColor: Int = currentStateColor,
    ) : Parcelable
}
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
    private class SavedState : BaseSavedState {
        var disconnectedColor: Int = 0
        var connectingColor: Int = 0
        var connectedColor: Int = 0
        var disconnectingColor: Int = 0
        var advertisingColor: Int = 0
        var inactiveAlpha: Float = 0f
        var idleOffBlinkRate: Int = 0
        var scanOffBlinkRate: Int = 0
        var pairOffBlinkRate: Int = 0
        var idleOnBlinkRate: Int = 0
        var scanOnBlinkRate: Int = 0
        var pairOnBlinkRate: Int = 0
        var currentStateColor: Int = disconnectedColor
        var currentColor: Int = currentStateColor

        constructor(superState: Parcelable?) : super(superState)
        private constructor(parcel: Parcel) : super(parcel) {
            disconnectedColor = parcel.readInt()
            connectingColor = parcel.readInt()
            connectedColor = parcel.readInt()
            disconnectingColor = parcel.readInt()
            advertisingColor = parcel.readInt()
            inactiveAlpha = parcel.readFloat()
            idleOffBlinkRate = parcel.readInt()
            scanOffBlinkRate = parcel.readInt()
            pairOffBlinkRate = parcel.readInt()
            idleOnBlinkRate = parcel.readInt()
            scanOnBlinkRate = parcel.readInt()
            pairOnBlinkRate = parcel.readInt()
            currentStateColor = parcel.readInt()
            currentColor = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(disconnectedColor)
            out.writeInt(connectingColor)
            out.writeInt(connectedColor)
            out.writeInt(disconnectingColor)
            out.writeInt(advertisingColor)
            out.writeFloat(inactiveAlpha)
            out.writeInt(idleOffBlinkRate)
            out.writeInt(scanOffBlinkRate)
            out.writeInt(pairOffBlinkRate)
            out.writeInt(idleOnBlinkRate)
            out.writeInt(scanOnBlinkRate)
            out.writeInt(pairOnBlinkRate)
            out.writeInt(currentStateColor)
            out.writeInt(currentColor)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> =
                object : Parcelable.Creator<SavedState> {
                    override fun createFromParcel(source: Parcel): SavedState {
                        return SavedState(source)
                    }

                    override fun newArray(size: Int): Array<SavedState?> {
                        return arrayOfNulls(size)
                    }
                }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState).also {
            it.disconnectedColor = this.disconnectedColor
            it.connectingColor = this.connectingColor
            it.connectedColor = this.connectedColor
            it.disconnectingColor = this.disconnectingColor
            it.advertisingColor = this.advertisingColor
            it.inactiveAlpha = this.inactiveAlpha
            it.idleOffBlinkRate = this.idleOffBlinkRate
            it.scanOffBlinkRate = this.scanOffBlinkRate
            it.pairOffBlinkRate = this.pairOffBlinkRate
            it.idleOnBlinkRate = this.idleOnBlinkRate
            it.scanOnBlinkRate = this.scanOnBlinkRate
            it.pairOnBlinkRate = this.pairOnBlinkRate
            it.currentStateColor = this.currentStateColor
            it.currentColor = this.currentColor
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state)
            disconnectedColor = state.disconnectedColor
            connectingColor = state.connectingColor
            connectedColor = state.connectedColor
            disconnectingColor = state.disconnectingColor
            advertisingColor = state.advertisingColor
            inactiveAlpha = state.inactiveAlpha
            idleOffBlinkRate = state.idleOffBlinkRate
            scanOffBlinkRate = state.scanOffBlinkRate
            pairOffBlinkRate = state.pairOffBlinkRate
            idleOnBlinkRate = state.idleOnBlinkRate
            scanOnBlinkRate = state.scanOnBlinkRate
            pairOnBlinkRate = state.pairOnBlinkRate
            currentStateColor = state.currentStateColor
            currentColor = state.currentColor
            startPattern()
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}
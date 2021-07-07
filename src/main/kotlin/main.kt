import androidx.compose.desktop.LocalAppWindow
import androidx.compose.desktop.Window
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.*
import java.io.File
import java.lang.Integer.max
import java.lang.Thread.sleep
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread

class StateHolder(val display: BooleanArray)

@ExperimentalComposeUiApi
fun main(args: Array<String>) = Window(
    title = "Chip8",
    size = IntSize(640, 320),
) {
    val machine = Machine()
    File("roms/Airplane.ch8")
        .readBytes()
        .toUByteArray()
        .copyInto(machine.memory, 0x200)
    machine.pc = 0x200.toUShort()

    val display = remember { mutableStateOf(StateHolder(machine.display)) }
    val displayUpdateScope = CoroutineScope(Dispatchers.Main)
    var keyEventQueue = ArrayBlockingQueue<UByte>(1)

    val keysIndex = mapOf(
        Key.One to 0x1, Key.Two to 0x2, Key.Three to 0x3, Key.Four to 0xC,
        Key.Q to 0x4, Key.W to 0x5, Key.E to 0x6, Key.R to 0xD,
        Key.A to 0x7, Key.S to 0x8, Key.D to 0x9, Key.F to 0xE,
        Key.Z to 0xA, Key.X to 0x0, Key.C to 0xB, Key.V to 0xF
    )

    LocalAppWindow.current.keyboard.onKeyEvent = onKeyEvent@{ keyEvent ->
        val key = keysIndex[keyEvent.key]
        if(key == null || keyEvent.type == KeyEventType.Unknown) {
            return@onKeyEvent false
        }
        machine.keys[key] = keyEvent.type == KeyEventType.KeyDown
        if(keyEvent.type == KeyEventType.KeyDown) {
            keyEventQueue.offer(key.toUByte())
        }
        true
    }

    LocalAppWindow.current.events.onClose = {
        displayUpdateScope.cancel()
    }

    fixedRateTimer(period = 17L) {
        machine.dt = max(0, machine.dt.toInt() - 1).toUByte()
        machine.st = max(0, machine.st.toInt() - 1).toUByte()
    }

    fun waitForKeyPress(): UByte {
        keyEventQueue = ArrayBlockingQueue<UByte>(1)
        return keyEventQueue.take()
    }

    fun notifyDisplayUpdated() {
        displayUpdateScope.launch(Dispatchers.Main) {
            display.value = StateHolder(machine.display)
        }
    }

    thread {
        while(true) {
            execute(
                machine,
                waitForKeyPress = ::waitForKeyPress,
                notifyDisplayUpdated = ::notifyDisplayUpdated
            )
            sleep(5)
        }
    }

    Canvas(Modifier.fillMaxSize().background(Black)) {
        val pixelWidth = this.size.width / 64
        val pixelHeight = this.size.height / 32
        for(y in 0 until 32) {
            for(x in 0 until 64) {
                val color = if(display.value.display[x * 32 + y]) White else Black
                drawRect(color, topLeft = Offset(x * pixelWidth, y * pixelHeight), size = Size(pixelWidth, pixelHeight))
            }
        }
    }
}
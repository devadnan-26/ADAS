package org.tetra.adas.network

import adas.composeapp.generated.resources.Dur
import adas.composeapp.generated.resources.Durak
import adas.composeapp.generated.resources.Duraklamak_ve_Park_Yasak
import adas.composeapp.generated.resources.Engelli_Park
import adas.composeapp.generated.resources.Ilerden_Saga
import adas.composeapp.generated.resources.Ilerden_Sola
import adas.composeapp.generated.resources.Ileri_Mecburi
import adas.composeapp.generated.resources.Ileri_veya_Saga_Mecburi
import adas.composeapp.generated.resources.Ileri_veya_Sola_Mecburi
import adas.composeapp.generated.resources.Kapalı_Yol
import adas.composeapp.generated.resources.Mecburi_Sola
import adas.composeapp.generated.resources.Mecburu_Saga
import adas.composeapp.generated.resources.Park
import adas.composeapp.generated.resources.Park_Yasak
import adas.composeapp.generated.resources.Res
import adas.composeapp.generated.resources.Saga_Donulmez
import adas.composeapp.generated.resources.Sola_Donulmez
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.tetra.adas.data.Sign
import org.tetra.adas.pages.Dashboard
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class Websocket {

    private val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 20_000
        }
        engine {
            requestTimeout = 30_000
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decodeBase64toImageBitmap(base64: String): Painter? {
        return try {
            val decoded = Base64.decode(base64)
            val bufferImage = ImageIO.read(ByteArrayInputStream(decoded))
            bufferImage.toPainter()
        } catch (e: Exception) {
            println("Failed to decode frame: ${e.message}")
            null
        }
    }

    suspend fun connectToBackCamera(onFrameReceived: (Painter) -> Unit) {
        var retryCount = 0
        val maxRetries = 5

        while (retryCount < maxRetries) {
            try {
                println("Attempting to connect to camera server (attempt ${retryCount + 1})")

                client.webSocket(
                    host = "127.0.0.1", // Changed from 127.1.1.150
                    port = 8080,
                    path = "/back-camera"
                ) {
                    println("Connected to camera server successfully!")

                    var frameCount = 0
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val base64Data = frame.readText()
                                val decodedImage = decodeBase64toImageBitmap(base64Data)

                                if (decodedImage != null) {
                                    onFrameReceived(decodedImage)
                                    frameCount++

                                    if (frameCount % 100 == 0) {
                                        println("Received $frameCount frames")
                                    }
                                }
                            }
                            is Frame.Close -> {
                                println("WebSocket connection closed by server")
                                return@webSocket
                            }
                            else -> {
                                // Handle other frame types if needed
                            }
                        }
                    }
                }

                // If we reach here, connection was successful but closed
                println("Connection closed normally")
                return

            } catch (e: Exception) {
                println("Connection failed (attempt ${retryCount + 1}): ${e.message}")
                retryCount++

                if (retryCount < maxRetries) {
                    println("Retrying in 2 seconds...")
                    delay(2000)
                } else {
                    println("Failed to connect after $maxRetries attempts")
                    break
                }
            }
        }
    }

    suspend fun connectToFrontCamera(onListRecieved: (Any) -> Unit) {
        var retryCount = 0
        val maxRetries = 5

        while (retryCount < maxRetries) {
            try {
                println("Attempting to connect to camera server (attempt ${retryCount + 1})")

                client.webSocket(
                    host = "127.0.0.1", // Changed from 127.1.1.150
                    port = 8080,
                    path = "/front-camera"
                )  {
                    println("Connected to camera server successfully!")

                    var frameCount = 0
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val list = stringToWords(frame.readText())
                                if (list.isNotEmpty()) {
                                    Dashboard().imageData.add(Sign(list[0], fetchTrafficSign(list[0])))
                                    if (list.size > 1) {
                                        Dashboard().imageData.add(Sign(list[0], fetchTrafficSign(list[1])))
                                    }
                                }
                            }
                            is Frame.Close -> {
                                println("WebSocket connection closed by server")
                                return@webSocket
                            }
                            else -> {
                                // Handle other frame types if needed
                            }
                        }
                    }
                }

                // If we reach here, connection was successful but closed
                println("Connection closed normally")
                return

            } catch (e: Exception) {
                println("Connection failed (attempt ${retryCount + 1}): ${e.message}")
                retryCount++

                if (retryCount < maxRetries) {
                    println("Retrying in 2 seconds...")
                    delay(2000)
                } else {
                    println("Failed to connect after $maxRetries attempts")
                    break
                }
            }
        }
    }

    private fun fetchTrafficSign(sign: String): DrawableResource {
        return when(sign) {
            "Dur" -> Res.drawable.Dur
            "Durak" -> Res.drawable.Durak
            "Duraklamak_ve_Park_Yasak" -> Res.drawable.Duraklamak_ve_Park_Yasak
            "Engelli_Park" -> Res.drawable.Engelli_Park
            "Ilerden_Saga" -> Res.drawable.Ilerden_Saga
            "Ilerden_Sola" -> Res.drawable.Ilerden_Sola
            "Ileri_Mecburi" -> Res.drawable.Ileri_Mecburi
            "Ileri_veya_Saga_Mecburi" -> Res.drawable.Ileri_veya_Saga_Mecburi
            "Ileri_veya_Sola_Mecburi" -> Res.drawable.Ileri_veya_Sola_Mecburi
            "Kapalı_Yol" -> Res.drawable.Kapalı_Yol
            "Mecburi_Sola" -> Res.drawable.Mecburi_Sola
            "Mecburu_Saga" -> Res.drawable.Mecburu_Saga
            "Park" -> Res.drawable.Park
            "Park_Yasak" -> Res.drawable.Park_Yasak
            "Saga_Donulmez" -> Res.drawable.Saga_Donulmez
            "Sola_Donulmez" -> Res.drawable.Sola_Donulmez
            else -> Res.drawable.Kapalı_Yol
        }
    }

    fun stringToWords(s : String) = s.replace(",", "").replace("'", "").replace("[", "").replace("]", "").splitToSequence(' ')
        .filter { it.isNotEmpty() }
        .toList()
    // Clean up resources when the screen is disposed
    fun cleanup() {
        client.close()
    }


}
// Simple Kotlin server that proxies OpenAI API requests (including streaming support)
// Avoids exposing the API key directly in frontend apps

package com.amirghm.app

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.jackson.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

// Constants
private const val HOST = "0.0.0.0"
private const val PORT = 8080
private const val OPENAI_API_URL = "https://api.openai.com/v1/chat/completions"

fun main() {
    val dotenv = dotenv()

    // Load the OpenAI API key from .env file
    val apiKey = dotenv["OPEN_AI_KEY"] ?: error("Missing OPEN_AI_KEY")

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    val server = embeddedServer(Netty, host = HOST, port = PORT) {
        routing {
            get("/") {
                val htmlContent = this::class.java.classLoader.getResource("index.html")?.readText()
                if (htmlContent != null) {
                    call.respondText(htmlContent, ContentType.Text.Html)
                } else {
                    call.respondText("index.html not found", ContentType.Text.Plain)
                }
            }

            // Handle POST requests to /ask
            post("/ask") {
                val body = call.receiveText()
                val mapper = jacksonObjectMapper()

                // Determine whether streaming is requested
                val isStreaming = try {
                    mapper.readTree(body).get("stream")?.asBoolean() == true
                } catch (e: Exception) {
                    false
                }

                if (isStreaming) {
                    val okClient = OkHttpClient()

                    val requestBody = body.toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url(OPENAI_API_URL)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .post(requestBody)
                        .build()

                    call.respond(object : OutgoingContent.WriteChannelContent() {
                        override val contentType: ContentType = ContentType.Text.EventStream

                        override suspend fun writeTo(channel: ByteWriteChannel) {
                            val call = okClient.newCall(request)
                            val response = call.execute()

                            if (!response.isSuccessful) {
                                throw IOException("OpenAI streaming failed: ${response.message}")
                            }

                            val inputStream = response.body?.byteStream()
                                ?: throw IOException("Empty response body from OpenAI")

                            val reader = inputStream.bufferedReader()
                            for (line in reader.lineSequence()) {
                                if (line.isNotBlank()) {
                                    channel.writeStringUtf8("data: $line\n\n")
                                    channel.flush()
                                }
                            }
                        }
                    })
                } else {
                    // Non-streaming request to OpenAI
                    val openAIResponse = client.post(OPENAI_API_URL) {
                        contentType(ContentType.Application.Json)
                        header("Authorization", "Bearer $apiKey")
                        setBody(body)
                    }.bodyAsText()

                    call.respondText(openAIResponse, ContentType.Application.Json)
                }
            }
        }
    }

    server.start(wait = true)
}
# Kotlin Proxy LLM Server

A lightweight Kotlin-based server using [Ktor](https://ktor.io/) to proxy OpenAI API calls securely for use in Android or frontend apps — preventing direct exposure of API keys.

Built with:
- Ktor (Netty server, client-core, CIO, OkHttp, content negotiation)
- Jackson for JSON serialization
- dotenv-kotlin for secure environment variable management
- Logback for logging

---

## 🚀 Features

- 🔐 Hides your OpenAI API key securely on the backend  
- 📤 Supports standard and streaming (`text/event-stream`) OpenAI responses  
- 🌱 Lightweight and easy to deploy (runs via a single JAR)  
- ⚙️ Built with modern Kotlin and coroutines  
- 🧪 Designed for local dev or VPS deployment  

---

## 📦 Requirements
- JDK 17 or higher  
- Gradle  
- OpenAI API key  

---

## 🛠 Setup & Run

### 1. Clone the repo
```bash
git clone https://github.com/your-username/kotlin-proxy-llm-server.git
cd kotlin-proxy-llm-server
```

### 2. Create a `.env` file
```env
OPENAI_API_KEY=your_openai_key_here
```

### 3. Run locally
```bash
./gradlew run
```

Server starts on `http://localhost:8080`

### 4. Build a fat JAR for deployment
```bash
./gradlew shadowJar
```
The JAR will be located in `build/libs/`.

Run it with:
```bash
java -jar build/libs/your-app-name-all.jar
```

---

## 📡 API

### `POST /ask`

Proxy a simple chat request to OpenAI.

#### Request Body (JSON):
```json
{
  "message": "Hello, how are you?"
}
```

#### Response (JSON):
```json
{
  "response": "I'm doing great, thanks!"
}
```

Streaming version supported with `stream: true` in the request.

---

## 🛣 Roadmap / Future Ideas

- [x] ✅ Add streaming endpoint (`text/event-stream`)  
- [ ] 🔐 Add token-based authentication for client apps  
- [ ] 🧪 Add tests and sample Android client  
- [ ] 🌐 Deploy with Docker + Nginx (optional HTTPS)  
- [ ] 📊 Logging + rate limiting middleware  
- [ ] 🛞 Switchable models (GPT-4, GPT-4-turbo, GPT-4o, etc)  

---

## 📃 License
[MIT](LICENSE)

---

## ✨ Credits

- [Ktor](https://ktor.io/)  
- [Jackson](https://github.com/FasterXML/jackson)  
- [dotenv-kotlin](https://github.com/cdimascio/dotenv-kotlin)  
- [Logback](http://logback.qos.ch/)
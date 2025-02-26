# WebSocket

`In Spring WebSocket if you want to send data to a specific user, use the username not anything else.`

```java
public void sendMessage(ChatMessage message) {

    User user = userService.findByEmail(message.getUser());
    User toUser = userService.findByEmail(message.getToUser());

    if (user == null || toUser == null)
        return;

    messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/messages", message);
    messagingTemplate.convertAndSendToUser(toUser.getEmail(), "/queue/messages", message);
}

```

```javascript
let socket = new SockJS('/ws');
let stompClient = Stomp.over(socket);

stompClient.connect({
        'ws-id': toUser
    },

    function (frame) {
        console.log("Connected: " + frame);

        // Subscribe to user-specific messages
        stompClient.subscribe('/user/queue/messages', function (message) {
            let receivedMessage = JSON.parse(message.body);
            console.log("Private Message Received:", receivedMessage);
        });
    });
```

`Email/username which is used to login not id or anything.`

***Spring WebSocket is a module in the Spring Framework that provides real-time, full-duplex communication between
client and server over WebSocket protocol. Unlike traditional HTTP request-response cycles, WebSockets maintain a
persistent connection, allowing bidirectional communication without re-establishing the connection for each message.***

***In WebSocket, when someone connects to the socket and sends a message, we can receive that message and broadcast it
to everyone or send it to a specific user.***

## Dependency

1. `spring-boot-starter-websocket`
2. `spring-boot-starter-web`
3. `spring-boot-starter-messaging` (If using STOMP with SockJS)

## Configuration

1. configureMessageBroker
2. registerStompEndpoints

These two methods are part of Spring WebSockets with STOMP (Simple Text Oriented Messaging Protocol).
They help configure message handling between the client (browser) and server.

1️⃣ configureMessageBroker(MessageBrokerRegistry registry)
👉 What does it do?
It sets up the message broker (like a post office) that handles sending and receiving messages.

👉 Why do we need it?
Without a broker, clients cannot send or receive real-time messages.

👉 How does it work?

```java

@Override
public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/queue", "/topic");
    registry.setApplicationDestinationPrefixes(destinationPrefix);
}
```

Enables a built-in message broker for sending messages.
Messages with "/queue" and "/topic" will be handled by this broker.
/topic is for broadcasting messages to multiple users (like a news feed).
/queue is for sending messages to a specific user (like a private message).
registry.setApplicationDestinationPrefixes(destinationPrefix);

Defines a prefix (destinationPrefix) for application-level messages.
Clients will send messages to destinations starting with this prefix.
Example: If destinationPrefix = "/app", clients will send messages like:

```javascript
stompClient.send("/app/stockPrice", {}, JSON.stringify({symbol: "HK"}));
```

The server will receive this and process it.

2️⃣ registerStompEndpoints(StompEndpointRegistry registry)
👉 What does it do?
It sets up WebSocket endpoints (like a URL) `where clients can connect`.

👉 Why do we need it?
Without this, clients won’t know where to connect to WebSockets.

👉 How does it work?

```java

@Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
            .withSockJS();
}
```

Adds an endpoint (e.g., /ws) for clients to connect.
withSockJS() enables SockJS, which helps when WebSockets are not supported.
Example:

If registerEndpoint = "/ws", the client will connect like this:

```javascript
var stompClient = Stomp.over(new SockJS('/ws'));
stompClient.connect({}, function () {
    console.log("Connected!");
});
```

This establishes a WebSocket connection to /ws.

### Summary

| Method                   | What it does               | Why it's needed             | How it works                                    |
|--------------------------|----------------------------|-----------------------------|-------------------------------------------------|
| `configureMessageBroker` | Sets up message broker     | Enables real-time messaging | Defines `/topic` and `/queue` for communication |
| `registerStompEndpoints` | Creates WebSocket endpoint | Allows clients to connect   | Uses SockJS for compatibility                   |

## Elements of Spring WebSocket

Spring provides multiple ways to work with WebSockets:

1. **WebSocket API**: Raw WebSocket implementation.
2. **STOMP (Simple Text Oriented Messaging Protocol)**: A higher-level protocol that supports features like message
   routing, subscriptions, and message acknowledgments.
3. **SockJS**: A fallback mechanism for browsers that do not support WebSockets.

## How to Send a Sample Message in Spring WebSocket

### 1. Configure WebSocket

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // Enables a simple broker for broadcasting messages
        registry.setApplicationDestinationPrefixes("/app"); // Prefix for messages from clients
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // WebSocket endpoint
                .setAllowedOrigins("*") // Allow cross-origin requests
                .withSockJS(); // Enable SockJS fallback
    }
}
```

### 2. Create WebSocket Controller

```java
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/message") // Client sends message to "/app/message"
    @SendTo("/topic/response")  // Server broadcasts message to "/topic/response"
    public String processMessage(String message) {
        return "Server Response: " + message;
    }
}
```

### 3. Client JavaScript Code to Send and Receive Messages

```html
<!DOCTYPE html>
<html>
<head>
    <title>WebSocket Example</title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.1/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
</head>
<body>
<input type="text" id="message" placeholder="Type a message">
<button onclick="sendMessage()">Send</button>
<div id="messages"></div>

<script>
    var socket = new SockJS('/ws');
    var stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        // Subscribe to receive messages
        stompClient.subscribe('/topic/response', function (message) {
            document.getElementById("messages").innerHTML += "<p>" + message.body + "</p>";
        });
    });

    function sendMessage() {
        var message = document.getElementById("message").value;
        stompClient.send("/app/message", {}, message);
    }
</script>
</body>
</html>
```

### 4. How Messages are Sent and Received

1. **Client sends a message**

    - Sends to `/app/message` (configured in `@MessageMapping`).
    - Example: `stompClient.send("/app/message", {}, "Hello!")`.

2. **Server receives and processes message**

    - `@MessageMapping("/message")` handles the message.

3. **Server sends response to topic**

    - Uses `@SendTo("/topic/response")`.
    - Message gets broadcasted to all clients subscribed to `/topic/response`.

4. **Clients receive the message**

    - Subscribed clients to `/topic/response` receive the message.
document.addEventListener("DOMContentLoaded", function () {
    let user = document.getElementById("user").textContent;
    let toUser = document.getElementById("toUser").textContent;
    const chatArea = document.getElementById("chatArea");

    let socket = new SockJS('/ws');
    let stompClient = Stomp.over(socket);

    stompClient.connect({
        'ws-id': user
    }, function (frame) {
        console.log("Connected: " + frame);

        // Subscribe to user-specific messages
        stompClient.subscribe('/user/queue/messages', function (message) {
            let receivedMessage = JSON.parse(message.body);
            console.log("Private Message Received:", receivedMessage);

            // Create and append a new message element to the chat area
            let newMessageElement = createMessageElement(receivedMessage);
            chatArea.appendChild(newMessageElement);

            // Auto-scroll to the latest message
            chatArea.scrollTop = chatArea.scrollHeight;
        });
    });

    document.querySelector("#chatForm").addEventListener("submit", function (event) {
        event.preventDefault();

        let message = document.getElementById("message").value;

        stompClient.send("/sent", {}, JSON.stringify({
            message: message,
            user: user,
            toUser: toUser
        }));

        document.getElementById("message").value = "";
    });
});

// Function to create a new message element for display
function createMessageElement(messageData) {
    const messageElement = document.createElement("div");
    messageElement.classList.add("my-1", "d-flex");

    const avatar = document.createElement("img");
    avatar.classList.add("icon-sm");
    avatar.src = `/picture/user/${messageData.userPicture}`

    const messageContent = document.createElement("div");
    messageContent.classList.add("ms-1", "d-flex", "flex-column", "chat");

    const header = document.createElement("div");
    const userName = document.createElement("span");
    userName.classList.add("fw-bold", "chat_area_color", "fs-5");
    userName.textContent = messageData.userName;  // Assuming 'user' contains the sender's name

    const time = document.createElement("small");
    time.classList.add("text-muted");
    time.textContent = messageData.sendTime;  // Format time as per your requirement

    header.appendChild(userName);
    header.appendChild(time);

    const textMessage = document.createElement("span");
    textMessage.classList.add("text-white");
    textMessage.textContent = messageData.message;

    messageContent.appendChild(header);
    messageContent.appendChild(textMessage);

    messageElement.appendChild(avatar);
    messageElement.appendChild(messageContent);

    return messageElement;
}

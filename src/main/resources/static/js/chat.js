const chatArea = document.getElementById("chatArea");
let user = document.getElementById("user").textContent;
let toUser = document.getElementById("toUser").textContent;
let chatList = document.getElementById("chatList");
let msg_tone = document.getElementById("msg-tone");

document.addEventListener("DOMContentLoaded", function () {

    let socket = new SockJS('/ws');
    let stompClient = Stomp.over(socket);

    // Receive sent message and show
    stompClient.connect({
        'ws-id': user
    }, function (frame) {

        // Subscribe to user-specific messages
        stompClient.subscribe('/user/queue/messages', function (message) {
            let receivedMessage = JSON.parse(message.body);
            msg_tone.play();
            showMessage(receivedMessage);
        });
    });

    // Send a message
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

function showMessage(receivedMessage) {

    // Create and append a new message element to the chat area

    if (receivedMessage.user === toUser || receivedMessage.user === user) {
        let newMessageElement = createMessageElement(receivedMessage);
        chatArea.appendChild(newMessageElement);

        // Auto-scroll to the latest message
        chatArea.scrollTop = chatArea.scrollHeight;
    } else {
        showOnConnectionList(receivedMessage);
    }
}

function showOnConnectionList(newMessageElement) {
    let p = document.createElement("p");
    p.innerText = newMessageElement.message.length <= 25 ?
        newMessageElement.message :
        newMessageElement.message.substring(0, 25) + "...";
    p.classList.add("m-0", "small", "bold"); // Optional styling

    for (let child of chatList.children) {
        if (child.dataset.con === newMessageElement.user) {
            let anchor = child.querySelector("a");
            let connectionText = anchor.querySelector("#connectionText");

            connectionText.append(p);
        }
    }
}

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
    userName.textContent = messageData.userName + ' ';  // Assuming 'user' contains the sender's name
    userName.style.setProperty("color", messageData.userTextColor, "important");

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

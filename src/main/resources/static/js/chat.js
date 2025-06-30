const chatArea = document.getElementById("chatArea");
let chatList = document.getElementById("chatList");
let msg_tone = document.getElementById("msg-tone");

// Rich Text Editor
const editor = new toastui.Editor({
    el: document.querySelector('#editor'),
    height: '300px',
    initialEditType: 'wysiwyg', // or 'markdown'
    previewStyle: 'vertical'
});

document.addEventListener("DOMContentLoaded", function () {

    let user = document.getElementById("user").textContent;
    let selectedUser = document.getElementById("selectedUser").textContent;
    let selectedGroup = document.getElementById("selectedGroup").textContent;
    let selectedChannel = document.getElementById("selectedChannel").textContent;
    let area = document.getElementById("area").textContent;

    chatArea.scrollTop = chatArea.scrollHeight;

    let socket = new SockJS('/ws');
    let stompClient = Stomp.over(socket);

    // Receive sent message and show
    stompClient.connect({}, function (frame) {

        // Subscribe to user-specific messages
        stompClient.subscribe('/user/queue/messages', function (message) {
            let receivedMessage = JSON.parse(message.body);
            msg_tone.play();
            showPersonalMessage(receivedMessage);
        });

        stompClient.subscribe('/topic/' + selectedChannel, function (message) {
            let receivedMessage = JSON.parse(message.body);
            console.log(receivedMessage)
            msg_tone.play();
            showGroupMessage(receivedMessage);
        });
    });

    // Send a message
    let sendUrl = area === 'PEOPLE' ? "/sent" : "/sent-group";
    document.querySelector("#chatForm").addEventListener("submit", function (event) {
        event.preventDefault();

        let message = document.getElementById("message").value;

        let body;
        if (area === 'PEOPLE') {
            body = {
                message: message,
                user: null,
                toUser: selectedUser
            }
        } else {
            body = {
                message: message,
                user: null,
                toGroup: selectedGroup
            }
        }

        stompClient.send(sendUrl, {}, JSON.stringify(body));
        document.getElementById("message").value = "";
    });

    document.querySelector("#chatFormWithFile").addEventListener("submit", function (event) {
        event.preventDefault();
        const msg = editor.getHTML();
        const fileInput = document.querySelector('#file');
        const file = fileInput.files[0];

        let messageObject;
        if (area === 'PEOPLE') {
            messageObject = {
                message: msg,
                user: null,
                toUser: selectedUser,
                fileName: null,
                fileContent: null
            };
        } else {
            messageObject = {
                message: msg,
                user: null,
                toGroup: selectedGroup,
                fileName: null,
                fileContent: null
            };
        }

        if (file) {
            const reader = new FileReader();

            reader.onload = function () {
                const base64Content = reader.result.split(',')[1]; // remove data:*/*;base64, prefix

                messageObject.fileName = file.name;
                messageObject.fileContent = base64Content;

                // Send full message with file
                stompClient.send(sendUrl, {}, JSON.stringify(messageObject));
            };

            reader.readAsDataURL(file);
        } else {
            // No file, just send text message
            stompClient.send(sendUrl, {}, JSON.stringify(messageObject));
        }

        // Clear inputs
        fileInput.value = "";
    });

    function showGroupMessage(receivedMessage) {

        let newMessageElement = createMessageElement(receivedMessage);
        chatArea.appendChild(newMessageElement);

        // Auto-scroll to the latest message
        chatArea.scrollTop = chatArea.scrollHeight;
    }

    function showPersonalMessage(receivedMessage) {

        // Create and append a new message element to the chat area
        if (receivedMessage.user === selectedUser || receivedMessage.user === user) {
            let newMessageElement = createMessageElement(receivedMessage);
            chatArea.appendChild(newMessageElement);

            // Auto-scroll to the latest message
            chatArea.scrollTop = chatArea.scrollHeight;
        } else {
            showOnConnectionList(receivedMessage);
        }
    }

    function showOnConnectionList(newMessageElement) {
        const isHtml = /<\/?[a-z][\s\S]*>/i.test(newMessageElement.message);
        for (let child of chatList.children) {
            if (child.dataset.con === newMessageElement.user) {
                let anchor = child.querySelector("a");
                let unseenText = anchor.querySelector("#unseenText");

                if (isHtml) {
                    unseenText.innerText = `Special Message`;
                    unseenText.classList.add("text-primary")
                } else {
                    unseenText.innerText = newMessageElement.message.length <= 25 ?
                        newMessageElement.message :
                        newMessageElement.message.substring(0, 25) + "...";
                }
            }
        }
    }

    // Function to create a new message element for display
    function createMessageElement(messageData) {

        const folderPath = area === 'PEOPLE' ? 'message' : 'group/message';
        const imageTag = messageData.fileName
            ? `<img src="/picture/${folderPath}/${messageData.fileName}" width="190" height="170" class="img-fluid clickable-image" alt="file" />`
            : ''; // show nothing if no file

        let hidden = area === 'GROUP' && messageData.user !== user ? 'hidden' : '';
        const removeClass = area === 'PEOPLE' ? 'remove_chat' : 'remove_group_chat';

        const html = `
        <div class="my-1 d-flex chat-wrapper" data-id="${messageData.id}">
            <img src="/picture/user/${messageData.userPicture}" alt="" class="icon-sm"/>
            <div class="ms-1 d-flex flex-column chat">
                <div class="d-flex justify-content-between">
                    <div>
                        <span class="fw-bold chat_area_color fs-6" style="color: ${messageData.userTextColor} !important;">
                            ${messageData.userName}
                        </span>
                    </div>
                    <div class="dropdown">
                        <img src="/icons/three-dots.svg" alt="send" class="dropdown-toggle"
                             data-bs-toggle="dropdown"
                             aria-expanded="false"/>
                        <ul class="dropdown-menu chat_bg">
                            <li class="dropdown-item ${removeClass} chat_bg d-flex align-items-center cursor_pointer ${hidden}" data-id="${messageData.id}">
                            <img src="/icons/x-circle.svg" class="me-1" alt="x circle"/>
                            Remove</li>
                        </ul>
                    </div>
                </div>
                <div>
                    <div class="text-white" id="message-content"></div>
                    ${imageTag}
                </div>
                <div class="d-flex justify-content-end">
                    <small class="text-light-dark">${messageData.sendTime}</small>
                </div>
            </div>
        </div>
    `.trim();

        // Convert string HTML to a DOM element
        const template = document.createElement('template');
        template.innerHTML = html;

        const element = template.content.firstElementChild;
        element.querySelector("#message-content").innerHTML = messageData.message;
        return element;
    }
});

// ------------------------------------ Delete Chat -------------------------------
document.querySelectorAll('.remove_chat').forEach(function (item) {
    item.addEventListener('click', function () {
        const chatId = this.getAttribute('data-id');
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        fetch(`/chat/${chatId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        })
            .then(async response => {
                let data = await response.json();
                return {status: response.status, data}
            })
            .then(data => {
                if (data.status === 200) {
                    const chatWrapper = document.querySelector(`.chat-wrapper[data-id="${chatId}"]`);
                    chatWrapper.classList.add("hidden")
                    chatWrapper.remove();
                    showToast(data.data.message);
                } else {
                    alert('Failed to remove chat.');
                }
            })
            .catch(error => {
                alert('An error occurred.');
            });
    });
});

document.querySelectorAll('.remove_group_chat').forEach(function (item) {
    item.addEventListener('click', function () {
        const chatId = this.getAttribute('data-id');
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        fetch(`/group/chat/${chatId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        })
            .then(async response => {
                let data = await response.json();
                return {status: response.status, data}
            })
            .then(data => {
                if (data.status === 200 && data.data.success) {
                    const chatWrapper = document.querySelector(`.chat-wrapper[data-id="${chatId}"]`);
                    chatWrapper.classList.add("hidden")
                    chatWrapper.remove();
                    showToast(data.data.message);
                }
            })
            .catch(error => {
                alert('An error occurred.');
            });
    });
});

function showToast(message) {
    let toastBody = document.getElementById("toast-body");
    toastBody.innerText = message;
    let toastLiveExample = document.getElementById('liveToast');
    const toastBootstrap = bootstrap.Toast.getOrCreateInstance(toastLiveExample);
    toastBootstrap.show();
}

// ------------------------------------- Image Modal ---------------------------

document.addEventListener("DOMContentLoaded", function () {
    const message_image_download = document.getElementById("message_image_download");
    document.body.addEventListener("click", function (e) {
        if (e.target.classList.contains("clickable-image")) {
            const src = e.target.getAttribute("src");
            const modalImg = document.getElementById("modalImage");
            modalImg.src = src;
            message_image_download.href = src;

            let nameArr = src.split(".");
            message_image_download.download = `pigeon_file.${nameArr[nameArr.length - 1]}`;

            const modal = new bootstrap.Modal(document.getElementById("imageModal"));
            modal.show();
        }
    });
});

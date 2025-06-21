const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

if (!SpeechRecognition) {
    alert("Your browser does not support Speech Recognition.");
}

const recognition = new SpeechRecognition();
recognition.lang = 'en-US';
recognition.interimResults = false;
recognition.continuous = true; // Allows long speech
let finalTranscript = '';

recognition.onresult = function(event) {
    // Collect all results so far
    for (let i = event.resultIndex; i < event.results.length; ++i) {
        if (event.results[i].isFinal) {
            finalTranscript += event.results[i][0].transcript + ' ';
        }
    }
};

recognition.onerror = function(event) {
    console.error('Speech recognition error:', event.error);
};

function startRecognition() {
    finalTranscript = '';
    recognition.start();
}

function stopRecognition() {
    recognition.stop();
    // Delay slightly to ensure 'onresult' fires before setting value
    setTimeout(() => {
        document.getElementById('message').value = finalTranscript.trim();
    }, 500);
}
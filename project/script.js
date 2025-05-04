// List stopwords
const stopwords = ['yang', 'untuk', 'pada', 'ke', 'para', 'namun', 'menurut', 'antara', 'dia', 'dua', 'ia', 'seperti', 'jika', 'jika', 'sehingga', 'kembali', 'dan', 'tidak', 'ini', 'karena', 'kepada', 'oleh', 'saat', 'harus', 'sementara', 'setelah', 'belum', 'kami', 'sekitar', 'bagi', 'serta', 'di', 'dari', 'telah', 'sebagai', 'masih', 'hal', 'ketika', 'adalah', 'itu', 'dalam', 'bisa', 'bahwa', 'atau', 'hanya', 'kita', 'dengan', 'akan', 'juga', 'ada', 'mereka', 'sudah', 'saya', 'terhadap', 'secara', 'agar', 'lain', 'anda', 'begitu', 'mengapa', 'kenapa', 'yaitu', 'yakni', 'daripada', 'itulah', 'lagi', 'maka', 'tentang', 'demi', 'dimana', 'kemana', 'pula', 'sambil', 'sebelum', 'sesudah', 'supaya', 'guna', 'kah', 'pun', 'sampai', 'sedangkan', 'selagi', 'sementara', 'tetapi', 'apakah', 'kecuali', 'sebab', 'selain', 'seolah', 'seraya', 'seterusnya', 'tanpa', 'agak', 'boleh', 'dapat', 'dsb', 'dst', 'dll', 'dahulu', 'dulunya', 'anu', 'demikian', 'tapi', 'ingin', 'juga', 'nggak', 'mari', 'nanti', 'melainkan', 'oh', 'ok', 'seharusnya', 'sebetulnya', 'setiap', 'setidaknya', 'sesuatu', 'pasti', 'saja', 'toh', 'ya', 'walau', 'tolong', 'tentu', 'amat', 'apalagi', 'bagaimanapun'];


// Cosine similarity helper
function cosineSimilarity(vecA, vecB) {
    const dot = vecA.reduce((acc, val, i) => acc + val * vecB[i], 0);
    const magA = Math.sqrt(vecA.reduce((acc, val) => acc + val * val, 0));
    const magB = Math.sqrt(vecB.reduce((acc, val) => acc + val * val, 0));
    return dot / (magA * magB || 1);
}


function tokenize(text) {
    const words = text.toLowerCase().replace(/[^a-z0-9\u00C0-\u024F]+/gi, ' ').split(/\s+/).filter(Boolean);
    return words.filter(word => !stopwords.includes(word));
}


function vectorize(text, vocab) {
    const tokens = tokenize(text);
    return vocab.map(word => tokens.includes(word) ? 1 : 0);
}

function handleUserInput() {
    const inputField = document.getElementById("user-input");
    const userText = inputField.value.trim();
    if (!userText) return;

    appendMessage("Kamu", userText);
    inputField.value = "";


    let allPatterns = [];
    let tagMap = [];
    dataset.forEach(item => {
        item.patterns.forEach(p => {
            allPatterns.push(p);
            tagMap.push(item.tag);
        });
    });


    const vocab = [...new Set(allPatterns.flatMap(p => tokenize(p)))];


    const userVec = vectorize(userText, vocab);


    let maxSim = 0;
    let bestTag = null;

    allPatterns.forEach((pattern, idx) => {
        const patternVec = vectorize(pattern, vocab);
        const sim = cosineSimilarity(userVec, patternVec);
        if (sim > maxSim) {
            maxSim = sim;
            bestTag = tagMap[idx];
        }
    });


    const match = dataset.find(item => item.tag === bestTag);
    const response = match
        ? match.responses[Math.floor(Math.random() * match.responses.length)]
        : "Maaf ya, aku belum mengerti maksudmu 😢";

    setTimeout(() => appendMessage("Waifu", response), 500);
}

function appendMessage(sender, text) {
    const chatBox = document.getElementById("chat-box");
    const msg = document.createElement("div");
    msg.innerHTML = `<strong>${sender}:</strong> ${text}`;
    chatBox.appendChild(msg);
    chatBox.scrollTop = chatBox.scrollHeight;
}

// AI Assistant JavaScript

let conversationHistory = [];

function showAIAssistant() {
    const modal = new bootstrap.Modal(document.getElementById('aiModal'));
    modal.show();

    // Clear previous conversation
    const messagesContainer = document.getElementById('chatMessages');
    messagesContainer.innerHTML = `
        <div class="ai-message">
            <i class="fas fa-robot"></i>
            <div class="message-content">
                Hello! I'm your AI librarian. Tell me what kind of books you're looking for, and I'll recommend the best ones from our collection!
            </div>
        </div>
    `;
    conversationHistory = [];
}

async function sendAIMessage() {
    const input = document.getElementById('aiInput');
    const message = input.value.trim();

    if (!message) return;

    // Add user message
    addMessage(message, 'user');
    input.value = '';

    // Show loading indicator
    const loadingMsg = addMessage('Thinking...', 'ai', true);

    try {
        const response = await fetch('/api/ai/recommend', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                query: message,
                history: conversationHistory
            })
        });

        const result = await response.json();

        // Remove loading message
        if (loadingMsg) {
            loadingMsg.remove();
        }

        // Display recommendations
        if (result.recommendations && result.recommendations.length > 0) {
            const responseText = generateRecommendationsHtml(result.recommendations);
            addMessage(responseText, 'ai');
        } else {
            addMessage("I'm sorry, I couldn't find any books matching your request. Could you try a different search?", 'ai');
        }

        // Update conversation history
        conversationHistory.push({ role: 'user', content: message });
        conversationHistory.push({ role: 'assistant', content: result.recommendations });

    } catch (error) {
        console.error('Error:', error);
        if (loadingMsg) loadingMsg.remove();
        addMessage('Sorry, I encountered an error. Please try again.', 'ai');
    }
}

function addMessage(content, sender, isLoading = false) {
    const messagesContainer = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `${sender}-message`;

    let icon = '';
    if (sender === 'ai') {
        icon = '<i class="fas fa-robot"></i>';
    } else {
        icon = '<i class="fas fa-user"></i>';
    }

    if (isLoading) {
        messageDiv.innerHTML = `
            ${icon}
            <div class="message-content loading-dots">
                ${content}
            </div>
        `;
    } else if (sender === 'ai' && content.includes('<')) {
        // Content is HTML (recommendations)
        messageDiv.innerHTML = `
            ${icon}
            <div class="message-content" style="max-width: 90%;">
                ${content}
            </div>
        `;
    } else {
        messageDiv.innerHTML = `
            ${icon}
            <div class="message-content">
                ${content}
            </div>
        `;
    }

    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    return messageDiv;
}

function generateRecommendationsHtml(books) {
    let html = '<strong>📚 Here are some books I recommend:</strong><div class="mt-2">';

    books.forEach(book => {
        html += `
            <div class="recommendation-item p-2 mb-2 bg-light rounded">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <strong>${book.title}</strong>
                        <br>
                        <small class="text-muted">by ${book.author}</small>
                        <br>
                        <span class="badge bg-info">${book.category}</span>
                    </div>
                    <div>
                        <span class="badge ${book.availableQuantity > 0 ? 'bg-success' : 'bg-danger'}">
                            ${book.availableQuantity > 0 ? 'Available' : 'Borrowed'}
                        </span>
                        <a href="/books/${book.id}" class="btn btn-sm btn-outline-primary ms-2">
                            <i class="fas fa-eye"></i> View
                        </a>
                    </div>
                </div>
            </div>
        `;
    });

    html += '</div>';
    return html;
}

// Handle enter key in chat input
document.addEventListener('keydown', function(e) {
    if (e.key === 'Enter') {
        const aiInput = document.getElementById('aiInput');
        if (aiInput && document.activeElement === aiInput) {
            sendAIMessage();
        }
    }
});
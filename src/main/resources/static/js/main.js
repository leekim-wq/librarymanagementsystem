// Main JavaScript for Library Management System

document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Auto-dismiss alerts after 5 seconds
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            var closeButton = alert.querySelector('.btn-close');
            if (closeButton) {
                closeButton.click();
            }
        });
    }, 5000);
});

// Search functionality
function performSearch() {
    var query = document.getElementById('searchInput').value;
    if (query.trim()) {
        window.location.href = '/books/search?q=' + encodeURIComponent(query.trim());
    }
    return false;
}

// Handle enter key in search
document.addEventListener('keydown', function(e) {
    if (e.key === 'Enter') {
        var searchInput = document.getElementById('searchInput');
        if (searchInput && document.activeElement === searchInput) {
            performSearch();
        }
    }
});

// Book borrowing function
async function borrowBook(bookId) {
    if (!confirm('Would you like to borrow this book?')) {
        return;
    }

    try {
        const response = await fetch('/api/books/borrow', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ bookId: bookId })
        });

        const result = await response.json();

        if (result.success) {
            showNotification('Success! Book borrowed successfully.', 'success');
            // Update UI
            updateBookStatus(bookId, false);
        } else {
            showNotification(result.message || 'Failed to borrow book.', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('An error occurred. Please try again.', 'danger');
    }
}

// Return book function
async function returnBook(loanId) {
    if (!confirm('Are you sure you want to return this book?')) {
        return;
    }

    try {
        const response = await fetch('/api/books/return', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ loanId: loanId })
        });

        const result = await response.json();

        if (result.success) {
            showNotification('Book returned successfully!', 'success');
            // Remove from UI or update
            document.getElementById('loan-' + loanId).remove();
        } else {
            showNotification(result.message || 'Failed to return book.', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('An error occurred. Please try again.', 'danger');
    }
}

// Update book status in UI
function updateBookStatus(bookId, available) {
    const statusElement = document.querySelector(`#book-${bookId} .availability`);
    if (statusElement) {
        if (available) {
            statusElement.innerHTML = '<span class="badge bg-success">Available</span>';
        } else {
            statusElement.innerHTML = '<span class="badge bg-danger">Borrowed</span>';
        }
    }
}

// Show notification
function showNotification(message, type = 'info') {
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;

    const container = document.getElementById('notification-container');
    if (container) {
        container.innerHTML = alertHtml;
        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            const alert = container.querySelector('.alert');
            if (alert) {
                alert.remove();
            }
        }, 5000);
    }
}
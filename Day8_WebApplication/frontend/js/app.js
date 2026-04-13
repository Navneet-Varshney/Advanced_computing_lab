
function showMessage(message, type = 'info') {
    const messageDiv = document.getElementById('message');
    if (!messageDiv) return;
    
    messageDiv.textContent = message;
    messageDiv.className = 'message ' + type;
    
    setTimeout(() => {
        messageDiv.textContent = '';
        messageDiv.className = 'message';
    }, 4000);
}


function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function isValidMobile(mobile) {
    const mobileRegex = /^\d{10}$/;
    return mobileRegex.test(mobile);
}

function validateFormData(formData) {
    if (!formData.name || formData.name.trim() === '') {
        showMessage('Name is required', 'error');
        return false;
    }
    
    if (!formData.email || !isValidEmail(formData.email)) {
        showMessage('Valid email is required', 'error');
        return false;
    }
    
    if (formData.mobile && !isValidMobile(formData.mobile)) {
        showMessage('Mobile number must be 10 digits', 'error');
        return false;
    }
    
    return true;
}

function formatDate(date) {
    if (!date) return '';
    const d = new Date(date);
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const year = d.getFullYear();
    return `${day}/${month}/${year}`;
}

function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function confirmAction(message) {
    return confirm(message);
}

function exportToCSV(data, filename = 'data.csv') {
    if (!data || data.length === 0) {
        showMessage('No data to export', 'error');
        return;
    }
    
    const headers = Object.keys(data[0]);
    const csvContent = [
        headers.join(','),
        ...data.map(row => 
            headers.map(header => {
                const value = row[header];
                return typeof value === 'string' && value.includes(',') 
                    ? `"${value.replace(/"/g, '""')}"` 
                    : value;
            }).join(',')
        )
    ].join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    showMessage('Data exported successfully', 'success');
}

function getRoleDisplayName(role) {
    const roleMap = {
        'admin': 'Administrator',
        'faculty': 'Faculty Member',
        'student': 'Student',
        'guest': 'Guest User'
    };
    return roleMap[role] || role;
}

function sortByProperty(array, property, ascending = true) {
    return [...array].sort((a, b) => {
        const aVal = a[property];
        const bVal = b[property];
        
        if (aVal < bVal) return ascending ? -1 : 1;
        if (aVal > bVal) return ascending ? 1 : -1;
        return 0;
    });
}

function filterByCriteria(array, criteria) {
    return array.filter(item => {
        for (let key in criteria) {
            if (item[key] !== criteria[key]) {
                return false;
            }
        }
        return true;
    });
}

function paginate(array, page, pageSize) {
    const startIndex = (page - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    return {
        data: array.slice(startIndex, endIndex),
        total: array.length,
        current: page,
        pageSize: pageSize,
        totalPages: Math.ceil(array.length / pageSize)
    };
}

function getInitials(name) {
    return name
        .split(' ')
        .map(word => word[0])
        .join('')
        .toUpperCase();
}

function formatPhoneNumber(phone) {
    if (!phone || phone.length !== 10) return phone;
    return `${phone.slice(0, 3)}-${phone.slice(3, 6)}-${phone.slice(6)}`;
}

function removeDuplicates(array, key) {
    const seen = new Set();
    return array.filter(obj => {
        const value = obj[key];
        if (seen.has(value)) return false;
        seen.add(value);
        return true;
    });
}

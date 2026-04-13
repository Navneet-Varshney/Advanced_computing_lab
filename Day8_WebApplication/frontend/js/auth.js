
function checkAuth(requiredRole) {
    const role = sessionStorage.getItem('role');
    const token = sessionStorage.getItem('authToken');
    
    if (!token || !role) {
        window.location.href = 'index.html';
        return false;
    }
    
    if (requiredRole && role !== requiredRole) {
        if (role === 'admin') {
            window.location.href = 'admin.html';
        } else if (role === 'faculty') {
            window.location.href = 'faculty.html';
        } else if (role === 'guest') {
            window.location.href = 'student.html';
        } else {
            window.location.href = 'index.html';
        }
        return false;
    }
    
    return true;
}

function checkAuthGuest() {
    const role = sessionStorage.getItem('role');
    
    if (!role) {
        window.location.href = 'index.html';
        return false;
    }
    
    return true;
}

function logout() {
    sessionStorage.clear();
    window.location.href = 'index.html';
}

function getCurrentUser() {
    return {
        username: sessionStorage.getItem('username'),
        role: sessionStorage.getItem('role'),
        faculty_id: sessionStorage.getItem('faculty_id')
    };
}

function getAuthToken() {
    return sessionStorage.getItem('authToken');
}

function setAuthData(username, role, facultyId = null) {
    const token = username + ':' + role;
    sessionStorage.setItem('authToken', 'Bearer ' + token);
    sessionStorage.setItem('username', username);
    sessionStorage.setItem('role', role);
    
    if (facultyId) {
        sessionStorage.setItem('faculty_id', facultyId);
    }
}

function clearAuthData() {
    sessionStorage.removeItem('authToken');
    sessionStorage.removeItem('username');
    sessionStorage.removeItem('role');
    sessionStorage.removeItem('faculty_id');
}

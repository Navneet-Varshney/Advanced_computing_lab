/**
 * Authentication and Session Management
 */

/**
 * Check if user is authenticated and has correct role
 */
function checkAuth(requiredRole) {
    const role = sessionStorage.getItem('role');
    const token = sessionStorage.getItem('authToken');
    
    if (!token || !role) {
        // Not logged in, redirect to login
        window.location.href = 'index.html';
        return false;
    }
    
    if (requiredRole && role !== requiredRole) {
        // Wrong role, redirect to appropriate dashboard
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

/**
 * Check if user is authenticated (guest check)
 */
function checkAuthGuest() {
    const role = sessionStorage.getItem('role');
    
    if (!role) {
        // Not set, redirect to login
        window.location.href = 'index.html';
        return false;
    }
    
    return true;
}

/**
 * Logout user and clear session
 */
function logout() {
    sessionStorage.clear();
    window.location.href = 'index.html';
}

/**
 * Get current user information
 */
function getCurrentUser() {
    return {
        username: sessionStorage.getItem('username'),
        role: sessionStorage.getItem('role'),
        faculty_id: sessionStorage.getItem('faculty_id')
    };
}

/**
 * Get auth token
 */
function getAuthToken() {
    return sessionStorage.getItem('authToken');
}

/**
 * Set auth data after successful login
 */
function setAuthData(username, role, facultyId = null) {
    const token = username + ':' + role;
    sessionStorage.setItem('authToken', 'Bearer ' + token);
    sessionStorage.setItem('username', username);
    sessionStorage.setItem('role', role);
    
    if (facultyId) {
        sessionStorage.setItem('faculty_id', facultyId);
    }
}

/**
 * Clear auth data (logout)
 */
function clearAuthData() {
    sessionStorage.removeItem('authToken');
    sessionStorage.removeItem('username');
    sessionStorage.removeItem('role');
    sessionStorage.removeItem('faculty_id');
}

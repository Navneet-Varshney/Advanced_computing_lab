/**
 * API Communication Layer
 * Handles all API requests to the backend
 */

const API_BASE = 'http://localhost:8080';

/**
 * Login user with username and password
 */
async function loginUser(username, password) {
    try {
        const response = await fetch(`${API_BASE}/api/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });
        
        return await response.json();
    } catch (error) {
        console.error('Login error:', error);
        return { success: false, message: 'Network error' };
    }
}

/**
 * Get all faculty members
 */
async function getAllFaculty(authToken) {
    try {
        const response = await fetch(`${API_BASE}/api/faculty`, {
            headers: {
                'Authorization': authToken
            }
        });
        
        if (response.status === 401) {
            return null; // Unauthorized
        }
        
        return await response.json();
    } catch (error) {
        console.error('Get faculty error:', error);
        return null;
    }
}

/**
 * Get faculty by ID
 */
async function getFacultyById(facultyId, authToken) {
    try {
        const response = await fetch(`${API_BASE}/api/faculty/${facultyId}`, {
            headers: {
                'Authorization': authToken
            }
        });
        
        if (response.status === 404) {
            return null; // Not found
        }
        
        return await response.json();
    } catch (error) {
        console.error('Get faculty error:', error);
        return null;
    }
}

/**
 * Search faculty
 */
async function searchFaculty(query) {
    try {
        const response = await fetch(`${API_BASE}/api/search?q=${encodeURIComponent(query)}`);
        return await response.json();
    } catch (error) {
        console.error('Search error:', error);
        return { success: false, results: [] };
    }
}

/**
 * Add new faculty (admin only)
 */
async function addFaculty(facultyData, authToken) {
    try {
        const response = await fetch(`${API_BASE}/api/faculty`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': authToken
            },
            body: JSON.stringify(facultyData)
        });
        
        return await response.json();
    } catch (error) {
        console.error('Add faculty error:', error);
        return { success: false, message: 'Network error' };
    }
}

/**
 * Update faculty record
 */
async function updateFaculty(facultyId, updates, authToken) {
    try {
        const response = await fetch(`${API_BASE}/api/faculty/${facultyId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': authToken
            },
            body: JSON.stringify(updates)
        });
        
        return await response.json();
    } catch (error) {
        console.error('Update faculty error:', error);
        return { success: false, message: 'Network error' };
    }
}

/**
 * Delete faculty record (admin only)
 */
async function deleteFaculty(facultyId, authToken) {
    try {
        const response = await fetch(`${API_BASE}/api/faculty/${facultyId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': authToken
            }
        });
        
        return await response.json();
    } catch (error) {
        console.error('Delete faculty error:', error);
        return { success: false, message: 'Network error' };
    }
}

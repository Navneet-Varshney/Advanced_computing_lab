

// Dynamic API Base URL - loaded from server config
let API_BASE = 'http://localhost:9001';

/**
 * Fetch server configuration to get dynamic API base URL
 */
async function loadServerConfig() {
    try {
        // Fetch from port 9001 where the Java server is running
        const response = await fetch('http://localhost:9001/config');
        if (response.ok) {
            const config = await response.json();
            API_BASE = config.api_base;
            console.log('✅ Server config loaded:', config);
            return config;
        }
    } catch (error) {
        console.warn('⚠️ Could not load server config, using default:', error);
    }
    return null;
}

// Load config when page loads
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadServerConfig);
} else {
    loadServerConfig();
}

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

async function forgotPassword(facultyId, name) {
    try {
        const response = await fetch(`${API_BASE}/api/forgot-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ faculty_id: facultyId, name: name })
        });
        
        return await response.json();
    } catch (error) {
        console.error('Forgot password error:', error);
        return { success: false, message: 'Network error' };
    }
}

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

async function searchFaculty(query) {
    try {
        const response = await fetch(`${API_BASE}/api/search?q=${encodeURIComponent(query)}`);
        return await response.json();
    } catch (error) {
        console.error('Search error:', error);
        return { success: false, results: [] };
    }
}

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

async function updateFaculty(facultyId, updates, authToken) {
    try {
        // Verify authToken exists
        if (!authToken) {
            console.error('❌ No auth token provided');
            return { success: false, message: 'Authentication required. Please log in again.' };
        }
        
        const response = await fetch(`${API_BASE}/api/faculty/${facultyId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': authToken
            },
            body: JSON.stringify(updates)
        });
        
        if (response.status === 403) {
            return { success: false, message: 'Permission denied. Please log in again.' };
        }
        
        return await response.json();
    } catch (error) {
        console.error('❌ Update faculty error:', error);
        return { success: false, message: 'Network error: ' + error.message };
    }
}

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

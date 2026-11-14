// =====================================================================
// common.js - helper functions used by EVERY page.
// Each HTML page loads this file first, then its own page script.
// =====================================================================

const API_BASE = '/api';

// ---------- Login session (saved in the browser's localStorage) ----------

function getToken() {
  return localStorage.getItem('token');
}

function getUser() {
  return JSON.parse(localStorage.getItem('user') || 'null');
}

// Called after login/register: the backend returns { token, user }
function saveLogin(authResponse) {
  localStorage.setItem('token', authResponse.token);
  localStorage.setItem('user', JSON.stringify(authResponse.user));
}

function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  window.location.href = 'index.html';
}

// Every protected page calls this first. No token -> back to the login page.
function requireLogin() {
  const user = getUser();
  if (!getToken() || !user) {
    window.location.href = 'index.html';
    throw new Error('Not logged in'); // stops the rest of the page script
  }

  // Fill the top bar: user name, logout button, highlight current page
  const nameEl = document.getElementById('userName');
  if (nameEl) nameEl.textContent = user.name;

  const logoutBtn = document.getElementById('logoutBtn');
  if (logoutBtn) logoutBtn.addEventListener('click', logout);

  const currentPage = window.location.pathname.split('/').pop();
  document.querySelectorAll('.topbar nav a').forEach((link) => {
    if (link.getAttribute('href') === currentPage) link.classList.add('active');
  });

  return user;
}

// ---------- Calling the Spring Boot API ----------

// One function for all API calls. It adds the JWT token and turns errors into messages.
async function apiRequest(method, path, body) {
  const options = { method: method, headers: {} };

  const token = getToken();
  if (token) {
    options.headers['Authorization'] = 'Bearer ' + token;
  }
  if (body !== undefined) {
    options.headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(body);
  }

  let response;
  try {
    response = await fetch(API_BASE + path, options);
  } catch (networkError) {
    throw new Error('Cannot reach the server. Is the Spring Boot app running?');
  }

  // Token expired or invalid -> log out
  if (response.status === 401 && token) {
    logout();
    throw new Error('Your session has expired. Please log in again.');
  }

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  if (response.status === 204) return null; // 204 = success with no body (e.g. delete)
  return response.json();
}

// The backend sends errors as { message, fieldErrors }
async function readErrorMessage(response) {
  try {
    const error = await response.json();
    if (error.fieldErrors) return Object.values(error.fieldErrors).join('. ');
    if (error.message) return error.message;
  } catch (e) {
    // response was not JSON
  }
  return 'Something went wrong (HTTP ' + response.status + ')';
}

const api = {
  get: (path) => apiRequest('GET', path),
  post: (path, body) => apiRequest('POST', path, body),
  put: (path, body) => apiRequest('PUT', path, body),
  delete: (path) => apiRequest('DELETE', path),
};

// Downloads a CSV report. We use fetch (not a plain link) because the request needs the token.
async function downloadReport(type, year) {
  const response = await fetch(API_BASE + '/reports/download?type=' + type + '&year=' + year, {
    headers: { Authorization: 'Bearer ' + getToken() },
  });
  if (!response.ok) throw new Error(await readErrorMessage(response));

  const blob = await response.blob();
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = 'farmtrack-' + type + '-report.csv';
  link.click();
  URL.revokeObjectURL(link.href);
}

// ---------- Formatting ----------

// 40000 -> "₹40,000.00",  -1000 -> "-₹1,000.00"
function formatMoney(value) {
  const number = Number(value || 0);
  const text = Math.abs(number).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  return (number < 0 ? '-₹' : '₹') + text;
}

// "2026-06-15" -> "15 Jun 2026"
function formatDate(isoDate) {
  if (!isoDate) return '-';
  return new Date(isoDate + 'T00:00:00').toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

// Today's date as "yyyy-mm-dd" for date inputs
function today() {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return now.getFullYear() + '-' + month + '-' + day;
}

// green for profit, red for loss
function profitClass(value) {
  if (Number(value) > 0) return 'profit';
  if (Number(value) < 0) return 'loss';
  return '';
}

// Makes user-typed text safe before putting it inside HTML (prevents XSS attacks)
function escapeHtml(text) {
  if (text === null || text === undefined) return '';
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

// Reads ?id=5 from the URL
function getQueryParam(name) {
  return new URLSearchParams(window.location.search).get(name);
}

// ---------- Messages ----------

function showMessage(elementId, text, type) {
  const box = document.getElementById(elementId);
  box.textContent = text;
  box.className = 'message ' + (type || 'error');
  box.hidden = false;
}

function hideMessage(elementId) {
  document.getElementById(elementId).hidden = true;
}

// ---------- Shared data ----------

const CATEGORY_LABELS = {
  SEEDS: 'Seeds',
  FERTILIZER: 'Fertilizer',
  LABOUR: 'Labour',
  PESTICIDE: 'Pesticides',
  IRRIGATION: 'Irrigation',
  MACHINERY: 'Machinery',
  TRANSPORT: 'Transport',
  OTHER: 'Other',
};

// Fills a <select> with the farmer's crops
function fillCropDropdown(selectElement, crops, firstOptionText) {
  let html = firstOptionText ? '<option value="">' + firstOptionText + '</option>' : '';
  crops.forEach((crop) => {
    html += '<option value="' + crop.id + '">' + escapeHtml(crop.cropName) + '</option>';
  });
  selectElement.innerHTML = html;
}

// Fills a year <select> with this year and the 5 years before it
function fillYearDropdown(selectElement) {
  const thisYear = new Date().getFullYear();
  for (let year = thisYear; year >= thisYear - 5; year--) {
    selectElement.innerHTML += '<option value="' + year + '">' + year + '</option>';
  }
}

// ---------- Opened by double-clicking the .html file? ----------
// The pages need the Spring Boot app to load data, so they only work at http://localhost:8080
if (window.location.protocol === 'file:') {
  document.body.innerHTML =
    '<div class="container"><div class="message error">' +
    'This page was opened as a file, so it cannot reach the backend.<br>' +
    'Start the Spring Boot app and open <b>http://localhost:8080</b> in your browser.' +
    '</div></div>';
  throw new Error('Open FarmTrack at http://localhost:8080, not as a file');
}

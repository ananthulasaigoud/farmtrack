// login.js - login page

// The shared guest (demo) account. Must match app.guest.phone / app.guest.password in application.properties.
const GUEST_PHONE = '9999999999';
const GUEST_PASSWORD = 'guest123';

// Already logged in? Go straight to the dashboard.
if (getToken()) {
  window.location.href = 'dashboard.html';
}

// Coming back after resetting the password
if (getQueryParam('reset')) {
  showMessage('message', 'Password updated. Please log in with your new password.', 'success');
}

// Normal login
document.getElementById('loginForm').addEventListener('submit', (event) => {
  event.preventDefault(); // stop the browser from reloading the page
  const identifier = document.getElementById('identifier').value.trim();
  const password = document.getElementById('password').value;
  login(identifier, password);
});

// Guest login: fill in the guest credentials and log in
document.getElementById('guestLoginBtn').addEventListener('click', () => {
  document.getElementById('identifier').value = GUEST_PHONE;
  document.getElementById('password').value = GUEST_PASSWORD;
  login(GUEST_PHONE, GUEST_PASSWORD);
});

async function login(identifier, password) {
  hideMessage('message');
  const buttons = [document.getElementById('loginBtn'), document.getElementById('guestLoginBtn')];
  buttons.forEach((b) => (b.disabled = true));

  try {
    const result = await api.post('/auth/login', { identifier: identifier, password: password }); // { token, user }

    if (result.user.role === 'ADMIN') {
      showMessage('message', 'Admin accounts use the API directly (Postman). This website is for farmers.');
      buttons.forEach((b) => (b.disabled = false));
      return;
    }

    saveLogin(result);
    window.location.href = 'dashboard.html';
  } catch (error) {
    showMessage('message', error.message);
    buttons.forEach((b) => (b.disabled = false));
  }
}

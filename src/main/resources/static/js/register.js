// register.js - registration page

document.getElementById('registerForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  hideMessage('message');

  const password = document.getElementById('password').value;
  const confirmPassword = document.getElementById('confirmPassword').value;
  if (password !== confirmPassword) {
    showMessage('message', 'Passwords do not match');
    return;
  }

  const body = {
    name: document.getElementById('name').value.trim(),
    phone: document.getElementById('phone').value.trim(),
    village: document.getElementById('village').value.trim(),
    email: document.getElementById('email').value.trim() || null,
    password: password,
  };

  const button = document.getElementById('registerBtn');
  button.disabled = true;

  try {
    const result = await api.post('/auth/register', body); // returns { token, user }
    saveLogin(result);
    window.location.href = 'dashboard.html';
  } catch (error) {
    showMessage('message', error.message); // e.g. "Phone number ... is already registered"
    button.disabled = false;
  }
});

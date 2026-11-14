// forgot-password.js
// Step 1: backend creates a one-time reset token (valid 15 minutes).
// Step 2: user sends the token + new password.
// No SMS/email service is connected yet, so in development the backend returns the token
// in the response and we fill it in automatically.

document.getElementById('requestForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  hideMessage('message');

  try {
    const identifier = document.getElementById('identifier').value.trim();
    const result = await api.post('/auth/forgot-password', { identifier: identifier });

    showMessage('message', result.message, 'success');
    if (result.resetToken) {
      document.getElementById('token').value = result.resetToken;
    }

    document.getElementById('requestForm').hidden = true;
    document.getElementById('resetForm').hidden = false;
  } catch (error) {
    showMessage('message', error.message);
  }
});

document.getElementById('resetForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  hideMessage('message');

  try {
    await api.post('/auth/reset-password', {
      token: document.getElementById('token').value.trim(),
      newPassword: document.getElementById('newPassword').value,
    });
    window.location.href = 'index.html?reset=1';
  } catch (error) {
    showMessage('message', error.message);
  }
});

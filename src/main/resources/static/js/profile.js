// profile.js
// API used:
//   GET    /api/farmers/{id}            my details
//   PUT    /api/farmers/{id}            update my details
//   POST   /api/auth/change-password    change password
//   DELETE /api/farmers/{id}            delete my account

const user = requireLogin();

document.getElementById('profileForm').addEventListener('submit', saveProfile);
document.getElementById('passwordForm').addEventListener('submit', changePassword);
document.getElementById('deleteAccountBtn').addEventListener('click', deleteAccount);

loadProfile();

// The shared guest account can't change its details or password, or be deleted (the backend blocks it too)
if (user.guest) {
  document.getElementById('guestNotice').hidden = false;
  document.querySelectorAll('#profileForm input, #profileForm button, #passwordForm input, #passwordForm button, #deleteAccountBtn')
    .forEach((element) => (element.disabled = true));
}

async function loadProfile() {
  try {
    const farmer = await api.get('/farmers/' + user.farmerId);
    document.getElementById('name').value = farmer.name;
    document.getElementById('phone').value = farmer.phone;
    document.getElementById('village').value = farmer.village;
  } catch (error) {
    showMessage('profileMessage', error.message);
  }
}

async function saveProfile(event) {
  event.preventDefault();
  const body = {
    name: document.getElementById('name').value.trim(),
    phone: document.getElementById('phone').value.trim(),
    village: document.getElementById('village').value.trim(),
  };
  try {
    const updated = await api.put('/farmers/' + user.farmerId, body);

    // keep the saved user in localStorage up to date (name is shown in the top bar)
    user.name = updated.name;
    user.phone = updated.phone;
    localStorage.setItem('user', JSON.stringify(user));
    document.getElementById('userName').textContent = updated.name;

    showMessage('profileMessage', 'Details saved', 'success');
  } catch (error) {
    showMessage('profileMessage', error.message);
  }
}

async function changePassword(event) {
  event.preventDefault();
  try {
    const result = await api.post('/auth/change-password', {
      currentPassword: document.getElementById('currentPassword').value,
      newPassword: document.getElementById('newPassword').value,
    });
    document.getElementById('passwordForm').reset();
    showMessage('passwordMessage', result.message, 'success');
  } catch (error) {
    showMessage('passwordMessage', error.message);
  }
}

async function deleteAccount() {
  if (!confirm('Delete your account and ALL your farm records? This cannot be undone.')) return;
  try {
    await api.delete('/farmers/' + user.farmerId);
    logout();
  } catch (error) {
    showMessage('profileMessage', error.message);
  }
}

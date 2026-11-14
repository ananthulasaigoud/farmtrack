// crops.js - list, add, edit and delete crops
// API used:
//   GET    /api/farmers/{farmerId}/crops   list my crops
//   POST   /api/farmers/{farmerId}/crops   add a crop
//   PUT    /api/crops/{id}                 update a crop
//   DELETE /api/crops/{id}                 delete a crop

const user = requireLogin();

let crops = [];          // crops loaded from the backend
let editingCropId = null; // null = adding a new crop, a number = editing that crop

document.getElementById('addCropBtn').addEventListener('click', () => openForm(null));
document.getElementById('cancelBtn').addEventListener('click', closeForm);
document.getElementById('cropForm').addEventListener('submit', saveCrop);

loadCrops();

async function loadCrops() {
  try {
    crops = await api.get('/farmers/' + user.farmerId + '/crops');
    showCrops();

    // Opened from the crop details page with crops.html?edit=5
    const editId = getQueryParam('edit');
    if (editId) editCrop(Number(editId));
  } catch (error) {
    showMessage('message', error.message);
  }
}

function showCrops() {
  const table = document.getElementById('cropTable');
  if (crops.length === 0) {
    table.innerHTML = '<tr><td colspan="8" class="empty">No crops yet. Click "+ Add crop" to add one.</td></tr>';
    return;
  }

  let rows = '';
  crops.forEach((crop) => {
    rows += `
      <tr>
        <td>
          <a href="crop-details.html?id=${crop.id}"><b>${escapeHtml(crop.cropName)}</b></a>
          <div class="small">${escapeHtml(crop.landDetails)}</div>
        </td>
        <td>${escapeHtml(crop.season) || '-'}</td>
        <td>${formatDate(crop.sowingDate)}</td>
        <td>${formatDate(crop.expectedHarvestDate)}</td>
        <td class="right">${formatMoney(crop.totalExpenses)}</td>
        <td class="right">${formatMoney(crop.totalIncome)}</td>
        <td class="right ${profitClass(crop.profit)}"><b>${formatMoney(crop.profit)}</b></td>
        <td class="actions">
          <button class="btn btn-light btn-small" onclick="editCrop(${crop.id})">Edit</button>
          <button class="btn btn-danger btn-small" onclick="deleteCrop(${crop.id})">Delete</button>
        </td>
      </tr>`;
  });
  table.innerHTML = rows;
}

// crop = null -> empty form for a new crop; crop = object -> form filled for editing
function openForm(crop) {
  editingCropId = crop ? crop.id : null;
  document.getElementById('formTitle').textContent = crop ? 'Edit crop' : 'Add crop';

  document.getElementById('cropName').value = crop ? crop.cropName : '';
  document.getElementById('season').value = crop ? crop.season || '' : 'Kharif';
  document.getElementById('landDetails').value = crop ? crop.landDetails || '' : '';
  document.getElementById('sowingDate').value = crop ? crop.sowingDate || '' : '';
  document.getElementById('expectedHarvestDate').value = crop ? crop.expectedHarvestDate || '' : '';
  document.getElementById('additionalNotes').value = crop ? crop.additionalNotes || '' : '';

  hideMessage('formMessage');
  document.getElementById('formCard').hidden = false;
  document.getElementById('cropName').focus();
}

function closeForm() {
  document.getElementById('formCard').hidden = true;
  editingCropId = null;
}

function editCrop(id) {
  const crop = crops.find((c) => c.id === id);
  if (crop) openForm(crop);
}

async function saveCrop(event) {
  event.preventDefault();

  const body = {
    cropName: document.getElementById('cropName').value.trim(),
    season: document.getElementById('season').value || null,
    landDetails: document.getElementById('landDetails').value.trim() || null,
    sowingDate: document.getElementById('sowingDate').value || null,
    expectedHarvestDate: document.getElementById('expectedHarvestDate').value || null,
    additionalNotes: document.getElementById('additionalNotes').value.trim() || null,
  };

  try {
    if (editingCropId) {
      await api.put('/crops/' + editingCropId, body);
      showMessage('message', 'Crop updated', 'success');
    } else {
      await api.post('/farmers/' + user.farmerId + '/crops', body);
      showMessage('message', 'Crop added', 'success');
    }
    closeForm();
    loadCrops();
  } catch (error) {
    // e.g. "Expected harvest date cannot be before the sowing date" from the backend
    showMessage('formMessage', error.message);
  }
}

async function deleteCrop(id) {
  const crop = crops.find((c) => c.id === id);
  if (!confirm('Delete "' + crop.cropName + '"? Its expenses and sales will also be deleted.')) return;

  try {
    await api.delete('/crops/' + id);
    showMessage('message', 'Crop deleted', 'success');
    loadCrops();
  } catch (error) {
    showMessage('message', error.message);
  }
}
